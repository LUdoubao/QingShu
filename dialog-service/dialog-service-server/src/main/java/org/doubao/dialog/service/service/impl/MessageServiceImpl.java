package org.doubao.dialog.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.dialog.service.config.DialogWebSocketHandler;
import org.doubao.dialog.service.entity.DialogMessage;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.enums.MessagePushType;
import org.doubao.dialog.service.feign.UserFeignClient;
import org.doubao.dialog.service.mapper.DialogSessionMapper;
import org.doubao.dialog.service.messaging.DialogEventPublisher;
import org.doubao.dialog.service.req.MessageSendReq;
import org.doubao.dialog.service.service.MessageService;
import org.doubao.dialog.service.service.SessionService;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.doubao.dialog.service.vo.MessageVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

	private static final Integer MSG_PREVIEW_MAX_LEN = 20;
	private static final Integer MSG_CACHE_EXPIRE_SEC = 1800;
	private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Resource
	private DialogEventPublisher dialogEventPublisher;

	@Autowired
	private DialogSessionMapper sessionMapper;

	@Autowired
	private SessionService sessionService;

	@Autowired
	private UserFeignClient userFeignClient;

	@Autowired
	private DialogWebSocketHandler webSocketHandler;

	@Autowired
	private RedisCacheUtil redisCacheUtil;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String sendMessage(MessageSendReq sendReq, Long senderId) {
		Long sessionId = sendReq.getSessionId();
		String content = sendReq.getContent();
		String extInfo = sendReq.getExtInfo();
		String tmpId = "";
		if (extInfo != null && !extInfo.isEmpty()) {
			JSONObject parse = JSONObject.parseObject(extInfo);
			tmpId = parse.getString("tempId");
		}
		DialogMessage.ContentTypeEnum contentType = DialogMessage.ContentTypeEnum.valueOf(sendReq.getContentType());

		DialogSession sessionPO = validateSessionAndSender(sessionId, senderId);
		Long receiverId = Objects.equals(sessionPO.getUserId(), senderId) ? sessionPO.getTargetId() : sessionPO.getUserId();
		Boolean check = userFeignClient.checkChatPermission(receiverId, senderId).getData();
		if (!Boolean.TRUE.equals(check)) {
			throw new BusinessException(ErrorCode.USER_CHAT_PRIVACY_NOT_OPEN);
		}

		validateMessageContent(content, contentType);

		DialogMessage messagePO = buildMessagePO(sessionId, senderId, receiverId, content, contentType);
		DialogMessage savedMsg = mongoTemplate.insert(messagePO);

		updateSessionLastMsg(sessionPO, savedMsg, content, contentType);
		incrementReceiverUnreadCount(receiverId, sessionId);

		if (tmpId != null && !tmpId.isEmpty()) {
			webSocketHandler.sendMsgSuccess(messagePO.getSenderId(), messagePO.getId(), tmpId);
		}

		pushMessageToReceiver(savedMsg, receiverId, sessionPO);
		return String.valueOf(savedMsg.getId());
	}

	@Override
	public Page<MessageVO> getMessageHistory(Long sessionId, Long userId, Integer pageNum, Integer pageSize) {
		DialogSession currentUserSession = sessionService.getSessionByIdAndUserId(sessionId, userId);
		if (currentUserSession == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}

		if (!Objects.equals(currentUserSession.getUserId(), userId) && !Objects.equals(currentUserSession.getTargetId(), userId)) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_ALLOW_SEE);
		}

		Long targetId = Objects.equals(currentUserSession.getUserId(), userId) ? currentUserSession.getTargetId() : currentUserSession.getUserId();
		DialogSession targetUserSession = getTargetUserSession(targetId, userId);

		List<Long> sessionIds = new ArrayList<>();
		sessionIds.add(currentUserSession.getId());
		sessionIds.add(targetUserSession.getId());

		long skip = (long) (pageNum - 1) * pageSize;
		Criteria criteria = Criteria.where("sessionId").in(sessionIds).and("deleted").is(0);
		Query query = Query.query(criteria)
				.with(Sort.by(Sort.Direction.DESC, "sendTime"))
				.with(Sort.by(Sort.Direction.DESC, "_id"))
				.skip(skip)
				.limit(pageSize);

		long total = mongoTemplate.count(Query.query(criteria), DialogMessage.class);
		List<DialogMessage> messagePOList = mongoTemplate.find(query, DialogMessage.class);
		List<MessageVO> messageVOList = messagePOList.stream()
				.map(messagePO -> convertToMessageVO(messagePO, userId, sessionId))
				.collect(Collectors.toList());

		Page<MessageVO> resultPage = new Page<>(pageNum, pageSize);
		resultPage.setTotal(total);
		resultPage.setRecords(messageVOList);

		markReceivedMessagesAsRead(sessionIds, userId, messagePOList);
		return resultPage;
	}

	private void markReceivedMessagesAsRead(List<Long> sessionIds, Long currentUserId, List<DialogMessage> messagePOList) {
		if (messagePOList.isEmpty()) {
			return;
		}
		List<String> unreadMsgIds = messagePOList.stream()
				.filter(msg -> Objects.equals(msg.getReceiverId(), currentUserId))
				.filter(msg -> msg.getStatus() == DialogMessage.MessageStatusEnum.SENT)
				.map(DialogMessage::getId)
				.collect(Collectors.toList());
		if (unreadMsgIds.isEmpty()) {
			return;
		}
		for (Long sessionId : sessionIds) {
			try {
				markMessagesAsRead(sessionId, currentUserId, unreadMsgIds);
			} catch (Exception e) {
				log.warn("mark messages as read failed for session {}", sessionId, e);
			}
		}
	}

	private DialogSession getTargetUserSession(Long targetUserId, Long currentUserId) {
		LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DialogSession::getUserId, targetUserId)
				.eq(DialogSession::getTargetId, currentUserId)
				.eq(DialogSession::getSessionType, "USER")
				.eq(DialogSession::getDeleted, 0)
				.last("limit 1");
		DialogSession dialogSession = sessionMapper.selectOne(queryWrapper);
		if (dialogSession == null) {
			dialogSession = new DialogSession();
			dialogSession.setUserId(targetUserId);
			dialogSession.setTargetId(currentUserId);
			dialogSession.setSessionType(DialogSession.SessionTypeEnum.USER);
			dialogSession.setCreatedId(currentUserId);
			dialogSession.setUpdatedId(currentUserId);
			dialogSession.setCreatedTime(LocalDateTime.now());
			dialogSession.setUpdatedTime(LocalDateTime.now());
			dialogSession.setUnreadCount(0);
			dialogSession.setIsTop(0);
			dialogSession.setHidden(0);
			dialogSession.setIsLastMsgOwner(0);
			sessionMapper.insert(dialogSession);
			redisCacheUtil.deleteSessionCache(targetUserId, dialogSession.getId());
			redisCacheUtil.deleteSessionListCache(targetUserId);
		}
		return dialogSession;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resendMessage(String msgId, Long senderId) {
		DialogMessage messagePO = getMessageByIdAndSenderId(msgId, senderId);
		if (messagePO == null) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_EXIST);
		}
		if (messagePO.getStatus() != DialogMessage.MessageStatusEnum.FAILED) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW_RESEND);
		}

		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.SENT)
				.set("sendTime", LocalDateTime.now())
				.set("updatedAt", LocalDateTime.now());
		mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(msgId)), update, DialogMessage.class);

		messagePO = mongoTemplate.findById(msgId, DialogMessage.class);
		if (messagePO == null) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW_RESEND_QUERY);
		}

		DialogSession sessionPO = sessionService.getSessionByIdAndUserId(messagePO.getSessionId(), senderId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_EXIST_IN_SESSION);
		}

		pushMessageToReceiver(messagePO, messagePO.getReceiverId(), sessionPO);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void clearSessionMessages(Long sessionId, Long userId) {
		DialogSession sessionPO = sessionService.getSessionByIdAndUserId(sessionId, userId);
		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}

		Query query = Query.query(Criteria.where("sessionId").is(sessionId));
		mongoTemplate.remove(query, DialogMessage.class);

		sessionPO.setLastMsgId(null);
		sessionPO.setLastMsgContent(null);
		sessionPO.setLastMsgTime(LocalDateTime.now());
		sessionPO.setUnreadCount(0);
		sessionPO.setUpdatedTime(LocalDateTime.now());
		sessionMapper.updateById(sessionPO);

		redisCacheUtil.setSessionCache(userId, sessionId, sessionPO, MSG_CACHE_EXPIRE_SEC);
		redisCacheUtil.setSessionUnreadCount(userId, sessionId, 0);
		redisCacheUtil.deleteMessageCache(sessionId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void markMessagesAsRead(Long sessionId, Long receiverId, List<String> msgIds) {
		DialogSession sessionPO = sessionService.getSessionByIdAndReceiverId(sessionId, receiverId);
		if (sessionPO == null) {
			return;
		}
		DialogSession mySession = getTargetUserSession(receiverId, sessionPO.getUserId());

		Criteria criteria = Criteria.where("sessionId").is(sessionId)
				.and("receiverId").is(receiverId)
				.and("status").is(DialogMessage.MessageStatusEnum.SENT);
		if (!msgIds.isEmpty()) {
			criteria.and("_id").in(msgIds);
		}

		Query query = Query.query(criteria);
		long unreadCount = mongoTemplate.count(query, DialogMessage.class);
		if (unreadCount == 0) {
			return;
		}

		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.READ)
				.set("readTime", LocalDateTime.now())
				.set("updatedAt", LocalDateTime.now());
		mongoTemplate.updateMulti(query, update, DialogMessage.class);

		sessionService.clearSessionUnread(receiverId, mySession.getId());

		Long senderId = Objects.equals(sessionPO.getUserId(), receiverId) ? sessionPO.getTargetId() : sessionPO.getUserId();
		pushReadStatusToSender(senderId, sessionId, msgIds);
	}

	@Override
	public DialogMessage getMessageByIdAndSenderId(String msgId, Long senderId) {
		DialogMessage messagePO = redisCacheUtil.getMessageCache(msgId);
		if (messagePO != null) {
			if (Objects.equals(messagePO.getSenderId(), senderId)) {
				return messagePO;
			}
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW);
		}

		Query query = Query.query(Criteria.where("_id").is(msgId).and("senderId").is(senderId));
		messagePO = mongoTemplate.findOne(query, DialogMessage.class);
		if (messagePO != null) {
			redisCacheUtil.setMessageCache(messagePO, MSG_CACHE_EXPIRE_SEC);
		}
		return messagePO;
	}

	private DialogSession validateSessionAndSender(Long sessionId, Long senderId) {
		DialogSession sessionPO = sessionService.getSessionByIdAndUserId(sessionId, senderId);
		if (sessionPO == null) {
			LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(DialogSession::getId, sessionId)
					.eq(DialogSession::getTargetId, senderId)
					.eq(DialogSession::getDeleted, 0);
			sessionPO = sessionMapper.selectOne(queryWrapper);
		}

		if (sessionPO == null) {
			throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
		}
		if (!Objects.equals(sessionPO.getUserId(), senderId) && !Objects.equals(sessionPO.getTargetId(), senderId)) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_NOT_ALLOW_SEND);
		}
		return sessionPO;
	}

	private void validateMessageContent(String content, DialogMessage.ContentTypeEnum contentType) {
		if (content == null || content.trim().isEmpty()) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CONTENT_EMPTY);
		}
		if (contentType == DialogMessage.ContentTypeEnum.TEXT && content.length() > 500) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CONTENT_TOO_LONG);
		}
		if (contentType == DialogMessage.ContentTypeEnum.EMOJI && !content.matches("^\\[\\w+\\]$")) {
			throw new BusinessException(ErrorCode.DIALOG_MESSAGE_CONTENT_INVALID);
		}
	}

	private DialogMessage buildMessagePO(Long sessionId, Long senderId, Long receiverId, String content, DialogMessage.ContentTypeEnum contentType) {
		DialogMessage messagePO = new DialogMessage();
		messagePO.setSessionId(sessionId);
		messagePO.setSenderId(senderId);
		messagePO.setReceiverId(receiverId);
		messagePO.setContent(content);
		messagePO.setContentType(contentType);
		messagePO.setStatus(DialogMessage.MessageStatusEnum.SENT);
		messagePO.setSendTime(LocalDateTime.now());
		messagePO.setReadTime(null);
		messagePO.setIsRevoked(0);
		messagePO.setUpdatedAt(LocalDateTime.now());
		return messagePO;
	}

	private void updateSessionLastMsg(DialogSession sessionPO, DialogMessage messagePO, String content, DialogMessage.ContentTypeEnum contentType) {
		String lastMsgContent = contentType == DialogMessage.ContentTypeEnum.EMOJI
				? "[表情]"
				: content.length() > MSG_PREVIEW_MAX_LEN ? content.substring(0, MSG_PREVIEW_MAX_LEN) + "..." : content;

		sessionPO.setLastMsgId(messagePO.getId());
		sessionPO.setLastMsgContent(lastMsgContent);
		sessionPO.setLastMsgTime(messagePO.getSendTime());
		sessionPO.setIsLastMsgOwner(1);
		sessionPO.setUpdatedTime(LocalDateTime.now());
		sessionMapper.updateById(sessionPO);

		redisCacheUtil.setSessionCache(sessionPO.getUserId(), sessionPO.getId(), sessionPO, MSG_CACHE_EXPIRE_SEC);
		if (Objects.equals(sessionPO.getTargetId(), messagePO.getSenderId())) {
			redisCacheUtil.setSessionCache(sessionPO.getTargetId(), sessionPO.getId(), sessionPO, MSG_CACHE_EXPIRE_SEC);
		}
	}

	private void incrementReceiverUnreadCount(Long receiverId, Long sessionId) {
		redisCacheUtil.incrementSessionUnreadCount(receiverId, sessionId);
		DialogSession receiverSessionPO = sessionService.getSessionByIdAndUserId(sessionId, receiverId);
		if (receiverSessionPO != null) {
			receiverSessionPO.setUnreadCount(receiverSessionPO.getUnreadCount() + 1);
			receiverSessionPO.setIsLastMsgOwner(0);
			receiverSessionPO.setUpdatedTime(LocalDateTime.now());
			sessionMapper.updateById(receiverSessionPO);
		}
	}

	@Async
	protected void pushMessageToReceiver(DialogMessage messagePO, Long receiverId, DialogSession sessionPO) {
		try {
			DialogSession targetUserSession = getTargetUserSession(receiverId, sessionPO.getUserId());
			if (targetUserSession.getHidden() == 1) {
				targetUserSession.setHidden(0);
				sessionMapper.updateById(targetUserSession);
			}

			MessageVO messageVO = convertToMessageVO(messagePO, receiverId, targetUserSession.getId());
			if (webSocketHandler.isUserOnline(receiverId)) {
				webSocketHandler.pushPrivateMessage(receiverId, messageVO, MessagePushType.PRIVATE_MSG);
			} else {
				String senderName = webSocketHandler.getSenderName(messagePO.getSenderId());
				dialogEventPublisher.sendOfflineNotification(messageVO, receiverId, senderName);
			}

			targetUserSession.setUnreadCount(targetUserSession.getUnreadCount() + 1);
			targetUserSession.setIsLastMsgOwner(0);
			sessionMapper.updateById(targetUserSession);
			redisCacheUtil.setSessionUnreadCount(receiverId, targetUserSession.getId(), targetUserSession.getUnreadCount());
		} catch (Exception e) {
			updateMessageStatusToFailed(messagePO.getId());
			log.error("push message to receiver failed", e);
		}
	}

	private void pushReadStatusToSender(Long senderId, Long sessionId, List<String> msgIds) {
		if (!webSocketHandler.isUserOnline(senderId)) {
			return;
		}
		JSONObject readData = new JSONObject();
		readData.put("sessionId", sessionId);
		readData.put("msgIds", msgIds.isEmpty() ? "all" : msgIds);
		readData.put("readTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		webSocketHandler.pushMessage(senderId, MessagePushType.MSG_READ, readData);
	}

	private void updateMessageStatusToFailed(String msgId) {
		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.FAILED)
				.set("updatedAt", LocalDateTime.now());
		mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(msgId)), update, DialogMessage.class);

		DialogMessage messagePO = mongoTemplate.findById(msgId, DialogMessage.class);
		if (messagePO != null) {
			redisCacheUtil.setMessageCache(messagePO, MSG_CACHE_EXPIRE_SEC);
		}
	}

	private MessageVO convertToMessageVO(DialogMessage messagePO, Long currentUserId, Long sessionId) {
		MessageVO messageVO = new MessageVO();
		BeanUtils.copyProperties(messagePO, messageVO);
		messageVO.setStatus(messagePO.getStatus().getValue());
		messageVO.setContentType(messagePO.getContentType().getValue());
		messageVO.setShowSessionId(sessionId);
		messageVO.setSelfSend(Objects.equals(messagePO.getSenderId(), currentUserId));

		Result<List<UserInfoDes>> userResult = userFeignClient.getUsersByIds(Collections.singleton(messagePO.getSenderId()));
		if (userResult.isSuccess() && userResult.getData() != null && !userResult.getData().isEmpty()) {
			UserInfoDes senderUser = userResult.getData().get(0);
			messageVO.setSenderNickname(senderUser.getNickname());
			messageVO.setSenderAvatarUrl(senderUser.getAvatarUrl());
		} else {
			messageVO.setSenderNickname("未知用户");
			messageVO.setSenderAvatarUrl("");
		}

		String contentPreview = messagePO.getContentType() == DialogMessage.ContentTypeEnum.EMOJI
				? "[表情]"
				: messagePO.getContent().length() > MSG_PREVIEW_MAX_LEN
				? messagePO.getContent().substring(0, MSG_PREVIEW_MAX_LEN) + "..."
				: messagePO.getContent();
		messageVO.setContentPreview(contentPreview);
		messageVO.setSendTimeStr(formatSendTime(messagePO.getSendTime()));
		return messageVO;
	}

	private String formatSendTime(LocalDateTime sendTime) {
		if (sendTime == null) {
			return "";
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minusDays(1);
		LocalDateTime oneMonthAgo = now.minusMonths(1);

		if (sendTime.toLocalDate().isEqual(now.toLocalDate())) {
			return sendTime.format(DateTimeFormatter.ofPattern("HH:mm"));
		}
		if (sendTime.toLocalDate().isEqual(yesterday.toLocalDate())) {
			return "昨天 " + sendTime.format(DateTimeFormatter.ofPattern("HH:mm"));
		}
		if (sendTime.isAfter(oneMonthAgo)) {
			return sendTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
		}
		return sendTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
}
