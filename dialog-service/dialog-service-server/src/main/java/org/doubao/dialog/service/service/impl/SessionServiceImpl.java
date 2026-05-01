package org.doubao.dialog.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.dialog.service.config.DialogWebSocketHandler;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.feign.UserFeignClient;
import org.doubao.dialog.service.mapper.DialogSessionMapper;
import org.doubao.dialog.service.req.SessionCreateReq;
import org.doubao.dialog.service.service.SessionService;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.doubao.dialog.service.vo.SessionVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionServiceImpl implements SessionService {

	private static final Integer SESSION_CACHE_EXPIRE_SEC = 3600;

	@Autowired
	private DialogSessionMapper sessionMapper;

	@Autowired
	private DialogWebSocketHandler dialogWebSocketHandler;

	@Autowired
	private UserFeignClient userFeignClient;

	@Autowired
	private RedisCacheUtil redisCacheUtil;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long createSession(Long userId, SessionCreateReq createReq) {
		DialogSession.SessionTypeEnum sessionType = DialogSession.SessionTypeEnum.valueOf(createReq.getSessionType());
		if (sessionType != DialogSession.SessionTypeEnum.USER) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}

		Long targetId = createReq.getTargetId();
		Result<List<UserInfoDes>> userResult = userFeignClient.getUsersByIds(Collections.singleton(targetId));
		if (!userResult.isSuccess() || userResult.getData() == null || userResult.getData().isEmpty()) {
			throw new BusinessException(ErrorCode.USER_DISABLED_OR_NOT_EXISTS);
		}
		if (Objects.equals(userId, targetId)) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_SELF_CREATE);
		}
		Boolean check = userFeignClient.checkChatPermission(targetId, userId).getData();
		if (!Boolean.TRUE.equals(check)) {
			throw new BusinessException(ErrorCode.USER_CHAT_PRIVACY_NOT_OPEN);
		}

		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getUserId, userId)
				.eq(DialogSession::getSessionType, sessionType)
				.eq(DialogSession::getTargetId, targetId);
		DialogSession existSession = sessionMapper.selectOne(queryWrapper);
		if (existSession != null) {
			if (existSession.getHidden() != null && existSession.getHidden() == 1) {
				existSession.setHidden(0);
				existSession.setUpdatedTime(LocalDateTime.now());
				sessionMapper.updateById(existSession);
				syncSessionToCache(existSession);
			}
			return existSession.getId();
		}

		DialogSession newSession = buildNewSession(userId, targetId, sessionType);
		sessionMapper.insert(newSession);
		syncSessionToCache(newSession);
		return newSession.getId();
	}

	@Override
	public Page<SessionVO> getSessionList(Long userId, Integer pageNum, Integer pageSize, Long currentSessionId) {
		if (userId == null || pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1) {
			throw new BusinessException(ErrorCode.BAD_REQUEST);
		}

		List<DialogSession> ownSessions = queryOwnUserSessions(userId, currentSessionId);
		if (CollectionUtils.isEmpty(ownSessions)) {
			return new Page<>(pageNum, pageSize, 0);
		}

		Map<Long, DialogSession> targetSessionMap = getTargetSessionMap(userId);
		List<DialogSession> sessionsWithLatestMsg = mergeLatestMessageToOwnSessions(ownSessions, targetSessionMap);
		List<DialogSession> sortedSessions = sortSessions(sessionsWithLatestMsg);
		List<DialogSession> pagedSessions = doManualPagination(sortedSessions, pageNum, pageSize);
		List<Long> collect = pagedSessions.stream().map(DialogSession::getTargetId).collect(Collectors.toList());
		Map<Long, Boolean> blockMap = userFeignClient.checkBatch(userId, collect).getData();

		List<SessionVO> sessionVOList = pagedSessions.stream()
				.map(sessionPO -> convertToSessionVO(sessionPO, userId, blockMap == null ? Collections.emptyMap() : blockMap))
				.collect(Collectors.toList());

		Map<Long, Boolean> onlineStatusMap = getTargetUserOnlineStatus(pagedSessions);
		sessionVOList.forEach(sessionVO -> sessionVO.setOnline(onlineStatusMap.getOrDefault(sessionVO.getTargetId(), false)));

		Page<SessionVO> resultPage = new Page<>(pageNum, pageSize);
		resultPage.setTotal(ownSessions.size());
		resultPage.setRecords(sessionVOList);
		return resultPage;
	}

	private Map<Long, Boolean> getTargetUserOnlineStatus(List<DialogSession> pagedSessions) {
		Map<Long, Boolean> onlineStatusMap = new HashMap<>();
		Set<Long> collect = pagedSessions.stream().map(DialogSession::getTargetId).collect(Collectors.toSet());
		for (Long targetId : collect) {
			onlineStatusMap.put(targetId, dialogWebSocketHandler.isUserOnline(targetId));
		}
		return onlineStatusMap;
	}

	@Override
	public List<DialogSession> queryOwnUserSessions(Long userId, Long currentSessionId) {
		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getUserId, userId)
				.eq(DialogSession::getDeleted, 0)
				.eq(DialogSession::getSessionType, "USER")
				.eq(DialogSession::getHidden, 0);
		if (currentSessionId != null) {
			queryWrapper.eq(DialogSession::getId, currentSessionId);
		}
		return sessionMapper.selectList(queryWrapper);
	}

	@Override
	public List<DialogSession> queryAllOwnUserSessions(Long userId) {
		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getUserId, userId)
				.eq(DialogSession::getDeleted, 0)
				.eq(DialogSession::getSessionType, "USER");
		return sessionMapper.selectList(queryWrapper);
	}

	private Map<Long, DialogSession> getTargetSessionMap(Long userId) {
		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getTargetId, userId)
				.eq(DialogSession::getDeleted, 0)
				.eq(DialogSession::getSessionType, "USER");
		List<DialogSession> targetSessions = sessionMapper.selectList(queryWrapper);
		if (CollectionUtils.isEmpty(targetSessions)) {
			return Collections.emptyMap();
		}
		return targetSessions.stream().collect(Collectors.toMap(
				DialogSession::getUserId,
				session -> session,
				(oldVal, newVal) -> isSessionNewer(newVal, oldVal) ? newVal : oldVal
		));
	}

	private List<DialogSession> mergeLatestMessageToOwnSessions(List<DialogSession> ownSessions, Map<Long, DialogSession> targetSessionMap) {
		if (CollectionUtils.isEmpty(targetSessionMap)) {
			return ownSessions;
		}
		return ownSessions.stream().map(ownSession -> {
			Long chatTargetId = ownSession.getTargetId();
			DialogSession targetSession = targetSessionMap.get(chatTargetId);
			if (targetSession == null) {
				return ownSession;
			}
			if (isSessionNewer(targetSession, ownSession)) {
				ownSession.setLastMsgId(targetSession.getLastMsgId());
				ownSession.setLastMsgContent(targetSession.getLastMsgContent());
				ownSession.setLastMsgTime(targetSession.getLastMsgTime());
				ownSession.setIsLastMsgOwner(0);
			}
			return ownSession;
		}).collect(Collectors.toList());
	}

	private boolean isSessionNewer(DialogSession sessionA, DialogSession sessionB) {
		LocalDateTime timeA = sessionA.getLastMsgTime();
		LocalDateTime timeB = sessionB.getLastMsgTime();
		if (timeA == null) {
			return false;
		}
		if (timeB == null) {
			return true;
		}
		return timeA.isAfter(timeB);
	}

	private List<DialogSession> sortSessions(List<DialogSession> sessions) {
		return sessions.stream()
				.sorted((a, b) -> {
					int topCompare = Integer.compare(Optional.ofNullable(b.getIsTop()).orElse(0), Optional.ofNullable(a.getIsTop()).orElse(0));
					if (topCompare != 0) {
						return topCompare;
					}
					LocalDateTime timeA = Optional.ofNullable(a.getLastMsgTime()).orElse(LocalDateTime.MIN);
					LocalDateTime timeB = Optional.ofNullable(b.getLastMsgTime()).orElse(LocalDateTime.MIN);
					return timeB.compareTo(timeA);
				})
				.collect(Collectors.toList());
	}

	private List<DialogSession> doManualPagination(List<DialogSession> sortedSessions, int pageNum, int pageSize) {
		int fromIndex = Math.max((pageNum - 1) * pageSize, 0);
		if (fromIndex >= sortedSessions.size()) {
			return Collections.emptyList();
		}
		int toIndex = Math.min(fromIndex + pageSize, sortedSessions.size());
		return sortedSessions.subList(fromIndex, toIndex);
	}

	@Override
	public void updateSessionTop(Long userId, Long sessionId, Integer isTop) {
		DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}
		if (Objects.equals(sessionPO.getIsTop(), isTop)) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_ALREADY_TOP);
		}
		sessionPO.setIsTop(isTop);
		sessionPO.setUpdatedTime(LocalDateTime.now());
		sessionMapper.updateById(sessionPO);
		syncSessionToCache(sessionPO);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteSession(Long userId, Long sessionId) {
		DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}
		sessionMapper.deleteById(sessionPO.getId());
		redisCacheUtil.deleteSessionCache(userId, sessionId);
		redisCacheUtil.deleteSessionListCache(userId);
	}

	@Override
	public void hiddenSession(Long userId, Long sessionId) {
		DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}
		sessionPO.setHidden(1);
		sessionPO.setUpdatedTime(LocalDateTime.now());
		sessionMapper.updateById(sessionPO);
		redisCacheUtil.deleteSessionCache(userId, sessionId);
		redisCacheUtil.deleteSessionListCache(userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void clearSessionUnread(Long userId, Long sessionId) {
		DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}
		dialogWebSocketHandler.handleUnreadCountChange(userId, sessionId, 0);
	}

	@Override
	public void clearAllSessionUnread(Long userId) {
		List<DialogSession> sessions = queryAllOwnUserSessions(userId);
		if (sessions.isEmpty()) {
			return;
		}
		sessions.forEach(session -> dialogWebSocketHandler.handleUnreadCountChange(userId, session.getId(), 0));
	}

	@Override
	public Integer getUnreadCount(Long userId) {
		List<DialogSession> sessions = queryOwnUserSessions(userId, null);
		if (sessions.isEmpty()) {
			return 0;
		}
		return sessions.stream().mapToInt(session -> Optional.ofNullable(session.getUnreadCount()).orElse(0)).sum();
	}

	@Override
	public DialogSession getSessionByIdAndUserId(Long sessionId, Long userId) {
		DialogSession sessionPO = redisCacheUtil.getSessionCache(userId, sessionId);
		if (sessionPO != null) {
			return sessionPO;
		}
		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getId, sessionId)
				.eq(DialogSession::getUserId, userId)
				.eq(DialogSession::getDeleted, 0);
		sessionPO = sessionMapper.selectOne(queryWrapper);
		if (sessionPO != null) {
			syncSessionToCache(sessionPO);
		}
		return sessionPO;
	}

	@Override
	public DialogSession getSessionByIdAndReceiverId(Long sessionId, Long receiverId) {
		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getId, sessionId)
				.eq(DialogSession::getTargetId, receiverId)
				.eq(DialogSession::getDeleted, 0);
		return sessionMapper.selectOne(queryWrapper);
	}

	private DialogSession buildNewSession(Long userId, Long targetId, DialogSession.SessionTypeEnum sessionType) {
		DialogSession sessionPO = new DialogSession();
		sessionPO.setUserId(userId);
		sessionPO.setTargetId(targetId);
		sessionPO.setSessionType(sessionType);
		sessionPO.setUnreadCount(0);
		sessionPO.setIsTop(0);
		sessionPO.setDeleted(0);
		sessionPO.setHidden(0);
		sessionPO.setIsLastMsgOwner(1);
		sessionPO.setCreatedTime(LocalDateTime.now());
		sessionPO.setUpdatedTime(LocalDateTime.now());
		sessionPO.setLastMsgId(null);
		sessionPO.setLastMsgContent(null);
		sessionPO.setLastMsgTime(LocalDateTime.now());
		return sessionPO;
	}

	private SessionVO convertToSessionVO(DialogSession sessionPO, Long userId, Map<Long, Boolean> blockMap) {
		SessionVO sessionVO = new SessionVO();
		BeanUtils.copyProperties(sessionPO, sessionVO);
		Long chatTargetId = sessionPO.getTargetId();

		fillTargetUserInfo(sessionVO, chatTargetId);

		Integer unreadCount = redisCacheUtil.getSessionUnreadCount(userId, sessionPO.getId());
		if (Objects.isNull(unreadCount)) {
			DialogSession dialogSession = sessionMapper.selectById(sessionPO.getId());
			unreadCount = Optional.ofNullable(dialogSession).map(DialogSession::getUnreadCount).orElse(0);
			redisCacheUtil.setSessionUnreadCount(userId, sessionPO.getId(), unreadCount);
		}
		sessionVO.setUnreadCount(unreadCount);
		sessionVO.setLastMsgTimeStr(formatLastMsgTime(sessionPO.getLastMsgTime()));
		sessionVO.setBlocked(blockMap.getOrDefault(sessionPO.getTargetId(), false));
		sessionVO.setHasUnread(unreadCount > 0);
		return sessionVO;
	}

	private void fillTargetUserInfo(SessionVO sessionVO, Long targetUserId) {
		try {
			Result<List<UserInfoDes>> userResult = userFeignClient.getUsersByIds(Collections.singleton(targetUserId));
			if (userResult.isSuccess() && !CollectionUtils.isEmpty(userResult.getData())) {
				UserInfoDes targetUser = userResult.getData().get(0);
				sessionVO.setTargetNickname(targetUser.getNickname());
				sessionVO.setTargetAvatarUrl(targetUser.getAvatarUrl());
			} else {
				sessionVO.setTargetNickname("未知用户");
				sessionVO.setTargetAvatarUrl("");
			}
		} catch (Exception e) {
			sessionVO.setTargetNickname("未知用户");
			sessionVO.setTargetAvatarUrl("");
		}
	}

	private String formatLastMsgTime(LocalDateTime lastMsgTime) {
		if (lastMsgTime == null) {
			return "";
		}
		LocalDateTime now = LocalDateTime.now();
		long minutesDiff = java.time.Duration.between(lastMsgTime, now).toMinutes();
		if (minutesDiff < 60) {
			if (minutesDiff == 0) {
				return "刚刚";
			}
			return minutesDiff + "分钟前";
		}
		long hoursDiff = java.time.Duration.between(lastMsgTime, now).toHours();
		if (hoursDiff < 24) {
			return DateTimeFormatter.ofPattern("HH:mm").format(lastMsgTime);
		}
		return DateTimeFormatter.ofPattern("MM-dd").format(lastMsgTime);
	}

	private void syncSessionToCache(DialogSession sessionPO) {
		redisCacheUtil.setSessionCache(sessionPO.getUserId(), sessionPO.getId(), sessionPO, SESSION_CACHE_EXPIRE_SEC);
		long score = sessionPO.getLastMsgTime() != null
				? sessionPO.getLastMsgTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
				: System.currentTimeMillis();
		redisCacheUtil.addSessionToListCache(sessionPO.getUserId(), sessionPO.getId(), score, SESSION_CACHE_EXPIRE_SEC);
	}
}
