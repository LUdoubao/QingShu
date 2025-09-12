package org.doubao.dialog.service.service.impl;

import org.doubao.dialog.service.config.DialogWebSocketHandler;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.feign.UserFeignClient;
import org.doubao.dialog.service.mapper.DialogSessionMapper;
import org.doubao.dialog.service.req.SessionCreateReq;
import org.doubao.dialog.service.service.SessionService;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.doubao.dialog.service.vo.SessionVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话管理服务实现类
 * 核心逻辑：1. 会话唯一性校验 2. 缓存与数据库同步 3. 权限校验（会话归属）
 */
@Service
public class SessionServiceImpl implements SessionService {

    // ===================== 常量定义 =====================
    /**
     * AI助手固定目标ID
     */
    private static final Long AI_TARGET_ID = 10000L;
    /**
     * 会话缓存过期时间（秒）- 1小时
     */
    private static final Integer SESSION_CACHE_EXPIRE_SEC = 3600;

    // ===================== 依赖注入 =====================
    @Autowired
    private DialogSessionMapper sessionMapper;
    @Autowired
    private DialogWebSocketHandler dialogWebSocketHandler;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    // ===================== 接口实现 =====================

    /**
     * 创建会话核心逻辑：
     * 1. 校验会话类型（AI会话固定targetId）
     * 2. 校验目标用户合法性（用户会话需调用user-service）
     * 3. 检查会话是否已存在（唯一索引：userId+sessionType+targetId）
     * 4. 已存在则返回，不存在则创建并同步缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：确保DB与缓存操作原子性
    public Long createSession(Long userId, SessionCreateReq createReq) {
        // 1. 解析请求参数
        DialogSession.SessionTypeEnum sessionType = DialogSession.SessionTypeEnum.valueOf(createReq.getSessionType());
        Long targetId = createReq.getTargetId();

        // 2. AI会话特殊处理：固定targetId为10000，无需校验目标用户
        if (sessionType == DialogSession.SessionTypeEnum.AI) {
            targetId = AI_TARGET_ID;
            createReq.setTargetId(targetId);
        }

        // 3. 用户会话校验：目标用户必须存在（调用user-service）
        if (sessionType == DialogSession.SessionTypeEnum.USER) {
            // 校验目标用户是否存在
            Result<List<UserInfo>> userResult = userFeignClient.getUsersByIds(Collections.singleton(targetId));
            if (!userResult.isSuccess() || userResult.getData() == null || userResult.getData().isEmpty()) {
                throw new BusinessException(ErrorCode.USER_DISABLED_OR_NOT_EXISTS);
            }
            // 校验：不能与自己创建会话
            if (Objects.equals(userId, targetId)) {
                throw new BusinessException(ErrorCode.DIALOG_SESSION_SELF_CREATE);
            }
        }

        // 4. 检查会话是否已存在（唯一索引：userId+sessionType+targetId）
        LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DialogSession::getUserId, userId)
                .eq(DialogSession::getSessionType, sessionType)
                .eq(DialogSession::getTargetId, targetId)
                .eq(DialogSession::getDeleted, 0); // 排除已删除的会话
        DialogSession existSession = sessionMapper.selectOne(queryWrapper);

        // 5. 处理已存在的会话（若已删除则恢复）
        if (existSession != null) {
            // 若会话被逻辑删除，恢复为正常状态
            if (existSession.getDeleted() == 1) {
                existSession.setDeleted(0);
                existSession.setUpdatedTime(LocalDateTime.now());
                sessionMapper.updateById(existSession);
                // 同步缓存（恢复后的会话信息）
                syncSessionToCache(existSession);
            }
            return existSession.getId();
        }

        // 6. 创建新会话（DB插入）
        DialogSession newSession = buildNewSession(userId, targetId, sessionType);
        sessionMapper.insert(newSession);

        // 7. 同步会话到Redis缓存
        syncSessionToCache(newSession);

        // 8. 返回新会话ID
        return newSession.getId();
    }

    /**
     * 优化后核心逻辑：
     * 1. 仅查询当前用户作为「所有者（userId）」创建的未删除用户会话（满足“只查自己的会话”需求）
     * 2. 额外查询当前用户作为「目标（targetId）」的对方会话（用于同步最新消息）
     * 3. 按「聊天对象」匹配双方会话，合并最新消息到自己的会话中
     * 4. 按“置顶>合并后的最新消息时间倒序”排序
     * 5. 分页+补充目标用户信息/未读计数，转换为VO返回
     */
    @Override
    public Page<SessionVO> getSessionList(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 校验参数
        if (userId == null || pageNum == null || pageSize == null || pageNum < 1 || pageSize < 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 2. 核心查询：仅获取当前用户创建的会话（userId=当前用户，未删除，用户间会话）
        List<DialogSession> ownSessions = queryOwnUserSessions(userId);
        if (CollectionUtils.isEmpty(ownSessions)) {
            return new Page<>(pageNum, pageSize, 0); // 无自己创建的会话，返回空分页
        }

        // 3. 辅助查询：获取对方创建的、以当前用户为目标的会话（用于同步最新消息）
        Map<Long, DialogSession> targetSessionMap = getTargetSessionMap(userId);

        // 4. 关键操作：将对方会话的最新消息合并到自己的会话中
        List<DialogSession> sessionsWithLatestMsg = mergeLatestMessageToOwnSessions(ownSessions, targetSessionMap);

        // 5. 按“置顶降序→最新消息时间降序”排序
        List<DialogSession> sortedSessions = sortSessions(sessionsWithLatestMsg);

        // 6. 分页（基于自己的会话列表，合并消息后分页）
        List<DialogSession> pagedSessions = doManualPagination(sortedSessions, pageNum, pageSize);

        // 7. 转换为VO并补充信息（目标用户信息、实时未读计数）
        List<SessionVO> sessionVOList = pagedSessions.stream()
                .map(sessionPO -> convertToSessionVO(sessionPO, userId))
                .collect(Collectors.toList());

        // 获取目标用户在线状态
        Map<Long, Boolean> onlineStatusMap = getTargetUserOnlineStatus(pagedSessions);
        sessionVOList.forEach(sessionVO -> {
            sessionVO.setOnline(onlineStatusMap.getOrDefault(sessionVO.getTargetId(),  false));
        });
        // 8. 构建分页结果（总数为自己创建的会话数）
        Page<SessionVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(ownSessions.size());
        resultPage.setRecords(sessionVOList);

        return resultPage;
    }

    private Map<Long, Boolean> getTargetUserOnlineStatus(List<DialogSession> pagedSessions) {
        Map<Long, Boolean> onlineStatusMap = new HashMap<>();
        Set<Long> collect = pagedSessions.stream().map(DialogSession::getTargetId).collect(Collectors.toSet());
        for (Long targetId : collect) {
            if (dialogWebSocketHandler.isUserOnline(targetId)) {
                onlineStatusMap.put(targetId, true);
            } else {
                onlineStatusMap.put(targetId, false);
            }
        }
        return onlineStatusMap;
    }
    // ===================== 新增辅助方法 =====================
    /**
     * 仅查询当前用户创建的会话（userId=当前用户）
     * @param userId 当前用户ID
     * @return 自己创建的会话列表
     */
    @Override
    public List<DialogSession> queryOwnUserSessions(Long userId) {
        LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DialogSession::getUserId, userId) // 仅自己创建的会话
                .eq(DialogSession::getDeleted, 0)
                .eq(DialogSession::getSessionType, "USER");
        return sessionMapper.selectList(queryWrapper);
    }
    /**
     * 查询对方创建的、以当前用户为目标的会话，构建“聊天对象ID→对方会话”的映射
     * @param userId 当前用户ID（对方会话的targetId）
     * @return 聊天对象ID与对方会话的映射
     */
    private Map<Long, DialogSession> getTargetSessionMap(Long userId) {
        LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DialogSession::getTargetId, userId) // 对方创建的、目标是当前用户
                .eq(DialogSession::getDeleted, 0)
                .eq(DialogSession::getSessionType, "USER");

        List<DialogSession> targetSessions = sessionMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(targetSessions)) {
            return Collections.emptyMap();
        }

        // 以“对方会话的userId（即聊天对象ID）”为key，存储对方会话
        return targetSessions.stream()
                .collect(Collectors.toMap(
                        DialogSession::getUserId, // 对方会话的所有者=当前用户的聊天对象
                        session -> session,
                        (oldVal, newVal) -> isSessionNewer(newVal, oldVal) ? newVal : oldVal // 同一聊天对象多个会话，保留最新的
                ));
    }

    /**
     * 将对方会话的最新消息合并到自己的会话中
     * 逻辑：对每个自己的会话，找对应的对方会话，取两者中最新的消息更新到自己的会话
     * @param ownSessions 自己创建的会话列表
     * @param targetSessionMap 对方会话映射（聊天对象ID→对方会话）
     * @return 合并最新消息后的自己的会话列表
     */
    private List<DialogSession> mergeLatestMessageToOwnSessions(List<DialogSession> ownSessions,
                                                                Map<Long, DialogSession> targetSessionMap) {
        if (CollectionUtils.isEmpty(targetSessionMap)) {
            return ownSessions; // 无对方会话，直接返回自己的会话
        }

        return ownSessions.stream().map(ownSession -> {
            // 1. 自己的会话对应的聊天对象ID（即ownSession的targetId）
            Long chatTargetId = ownSession.getTargetId();
            // 2. 从映射中获取该聊天对象创建的、以自己为目标的会话
            DialogSession targetSession = targetSessionMap.get(chatTargetId);
            if (targetSession == null) {
                return ownSession; // 无对方会话，保留自己会话的原有消息
            }

            // 3. 比较自己的会话和对方会话的消息新旧，取最新的消息更新到自己的会话
            if (isSessionNewer(targetSession, ownSession)) {
                // 对方会话消息更新：覆盖自己会话的最后消息字段
                ownSession.setLastMsgContent(targetSession.getLastMsgContent());
                ownSession.setLastMsgTime(targetSession.getLastMsgTime());
                ownSession.setLastMsgId(targetSession.getLastMsgId());
                ownSession.setIsLastMsgOwner(0);
                // 可选：同步对方会话的更新时间（确保排序准确性）
                ownSession.setUpdatedTime(targetSession.getUpdatedTime());
            }

            return ownSession;
        }).collect(Collectors.toList());
    }


    /**
     * 判断会话A是否比会话B更新（按最后消息时间，时间相同按更新时间）
     * @param sessionA 待比较会话A
     * @param sessionB 待比较会话B
     * @return true=A更新，false=B更新
     */
    private boolean isSessionNewer(DialogSession sessionA, DialogSession sessionB) {
        LocalDateTime aLastMsgTime = Optional.ofNullable(sessionA.getLastMsgTime()).orElse(LocalDateTime.MIN);
        LocalDateTime bLastMsgTime = Optional.ofNullable(sessionB.getLastMsgTime()).orElse(LocalDateTime.MIN);

        // 1. 先比较最后消息时间
        if (aLastMsgTime.isAfter(bLastMsgTime)) {
            return true;
        }
        if (aLastMsgTime.isBefore(bLastMsgTime)) {
            return false;
        }

        // 2. 最后消息时间相同 → 比较更新时间
        LocalDateTime aUpdateTime = Optional.ofNullable(sessionA.getUpdatedTime()).orElse(LocalDateTime.MIN);
        LocalDateTime bUpdateTime = Optional.ofNullable(sessionB.getUpdatedTime()).orElse(LocalDateTime.MIN);
        return aUpdateTime.isAfter(bUpdateTime);
    }

    /**
     * 按“置顶降序→最后消息时间降序”排序会话
     * @param sessions 待排序会话列表
     * @return 排序后的会话列表
     */
    private List<DialogSession> sortSessions(List<DialogSession> sessions) {
        return sessions.stream()
                .sorted((s1, s2) -> {
                    // 1. 先按置顶状态降序（1=置顶，0=未置顶）
                    int topCompare = Integer.compare(s2.getIsTop(), s1.getIsTop());
                    if (topCompare != 0) {
                        return topCompare;
                    }

                    // 2. 再按合并后的最新消息时间降序（时间null视为最早）
                    LocalDateTime s1Time = Optional.ofNullable(s1.getLastMsgTime()).orElse(LocalDateTime.MIN);
                    LocalDateTime s2Time = Optional.ofNullable(s2.getLastMsgTime()).orElse(LocalDateTime.MIN);
                    return s2Time.compareTo(s1Time);
                })
                .collect(Collectors.toList());
    }

    /**
     * 手动分页（基于自己的会话列表，合并消息后分页）
     * @param sortedSessions 已排序的会话列表
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页条数
     * @return 分页后的会话列表
     */
    private List<DialogSession> doManualPagination(List<DialogSession> sortedSessions, int pageNum, int pageSize) {
        int total = sortedSessions.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        if (startIndex >= total) {
            return Collections.emptyList();
        }

        return sortedSessions.subList(startIndex, endIndex);
    }

    /**
     * 会话置顶核心逻辑：
     * 1. 校验会话归属（当前用户是否为会话所有者）
     * 2. 校验置顶状态（避免重复操作）
     * 3. 更新DB置顶状态，同步缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSessionTop(Long userId, Long sessionId, Integer isTop) {
        // 1. 校验置顶状态参数（只能是0或1）
        if (isTop != 0 && isTop != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        // 2. 查询会话并校验归属
        DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
        if (sessionPO == null) {
            throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
        }

        // 3. 避免重复操作（当前状态与目标状态一致）
        if (Objects.equals(sessionPO.getIsTop(), isTop)) {
            throw new BusinessException(ErrorCode.DIALOG_SESSION_ALREADY_TOP);
        }

        // 4. 更新DB置顶状态
        sessionPO.setIsTop(isTop);
        sessionPO.setUpdatedTime(LocalDateTime.now());
        sessionMapper.updateById(sessionPO);

        // 5. 同步缓存（更新后的置顶状态）
        syncSessionToCache(sessionPO);
    }

    /**
     * 删除会话核心逻辑（逻辑删除）：
     * 1. 校验会话归属
     * 2. 校验AI会话（AI会话不允许删除）
     * 3. 标记DB逻辑删除，删除缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long userId, Long sessionId) {
        // 1. 查询会话并校验归属
        DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
        if (sessionPO == null) {
            throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
        }

        // 2. AI会话特殊限制：不允许删除
        if (sessionPO.getSessionType() == DialogSession.SessionTypeEnum.AI) {
            throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_ALLOW_DELETE);
        }

        // 3. 逻辑删除（标记isDeleted=1）
        sessionPO.setDeleted(1);
        sessionPO.setUpdatedTime(LocalDateTime.now());
        sessionMapper.updateById(sessionPO);

        // 4. 删除Redis缓存（避免缓存脏数据）
        redisCacheUtil.deleteSessionCache(userId, sessionId);
        redisCacheUtil.deleteSessionListCache(userId);
    }

    /**
     * 未读清零核心逻辑：
     * 1. 校验会话归属（当前用户必须是会话接收方）
     * 3. 更新DB未读计数为0，同步缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearSessionUnread(Long userId, Long sessionId) {
        // 1. 查询会话并校验归属（当前用户必须是会话接收方）
        DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
        if (sessionPO == null) {
            throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
        } else {
            dialogWebSocketHandler.handleUnreadCountChange(userId, sessionId, 0);
        }
    }

    @Override
    public void clearAllSessionUnread(Long userId) {
        // 获取所有当前用户会话列表
        List<DialogSession> sessions = queryOwnUserSessions(userId);
        if (sessions.isEmpty()) {
            return;
        }
        sessions.forEach(session -> {
            dialogWebSocketHandler.handleUnreadCountChange(userId, session.getId(), 0);
        });
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        // 获取所有当前用户会话列表
        List<DialogSession> sessions = queryOwnUserSessions(userId);
        if (sessions.isEmpty()) {
            return 0;
        }
        // 求和会话未读数
        return sessions.stream().mapToInt(DialogSession::getUnreadCount).sum();
    }

    /**
     * 根据会话ID和用户ID查询会话（用于校验归属）
     * 优先从缓存查询，缓存不存在则从DB查询并同步到缓存
     */
    @Override
    public DialogSession getSessionByIdAndUserId(Long sessionId, Long userId) {
        // 1. 优先从缓存查询
        DialogSession sessionPO = redisCacheUtil.getSessionCache(userId, sessionId);
        if (sessionPO != null) {
            return sessionPO;
        }

        // 2. 缓存不存在，从DB查询
        LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DialogSession::getId, sessionId)
                .eq(DialogSession::getUserId, userId)
                .eq(DialogSession::getDeleted, 0);
        sessionPO = sessionMapper.selectOne(queryWrapper);

        // 3. DB查询结果同步到缓存（存在则同步，不存在则不缓存）
        if (sessionPO != null) {
            syncSessionToCache(sessionPO);
        }

        // 4. 返回结果
        return sessionPO;
    }

    @Override
    public DialogSession getSessionByIdAndReceiverId(Long sessionId, Long receiverId) {

        // 2. 缓存不存在，从DB查询
        LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DialogSession::getId, sessionId)
                .eq(DialogSession::getTargetId, receiverId)
                .eq(DialogSession::getDeleted, 0);

		return sessionMapper.selectOne(queryWrapper);
    }

    // ===================== 辅助方法 =====================

    /**
     * 构建新会话PO对象
     *
     * @param userId      会话所有者ID
     * @param targetId    目标ID
     * @param sessionType 会话类型
     * @return 新会话PO
     */
    private DialogSession buildNewSession(Long userId, Long targetId, DialogSession.SessionTypeEnum sessionType) {
        DialogSession sessionPO = new DialogSession();
        sessionPO.setUserId(userId);
        sessionPO.setTargetId(targetId);
        sessionPO.setSessionType(sessionType);
        sessionPO.setUnreadCount(0); // 初始未读计数为0
        sessionPO.setIsTop(0); // 初始不置顶
        sessionPO.setDeleted(0); // 初始未删除
        sessionPO.setCreatedTime(LocalDateTime.now());
        sessionPO.setUpdatedTime(LocalDateTime.now());
        // 初始无消息，lastMsg相关字段为null
        sessionPO.setLastMsgId(null);
        sessionPO.setLastMsgContent(null);
        sessionPO.setLastMsgTime(LocalDateTime.now());
        return sessionPO;
    }

    /**
     * 将会话PO转换为VO（补充目标用户信息、未读计数）
     *
     * @param sessionPO 会话PO
     * @param userId    当前用户ID（用于获取未读计数）
     * @return 会话VO
     */
    private SessionVO convertToSessionVO(DialogSession sessionPO, Long userId) {
        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(sessionPO, sessionVO);

        // 1. 聊天对象ID=自己会话的targetId（固定，因为是自己创建的会话）
        Long chatTargetId = sessionPO.getTargetId();

        // 2. 补充目标用户信息（头像、昵称）
        fillTargetUserInfo(sessionVO, chatTargetId);

        // 3. 从缓存获取实时未读计数（优先缓存，避免DB延迟）
        Integer unreadCount = redisCacheUtil.getSessionUnreadCount(userId, sessionPO.getId());
        sessionVO.setUnreadCount(Optional.ofNullable(unreadCount).orElse(0));


        // 3. 格式化最后消息时间（如：10分钟前、15:30、06-12）
        sessionVO.setLastMsgTimeStr(formatLastMsgTime(sessionPO.getLastMsgTime()));

        return sessionVO;
    }
    private void fillTargetUserInfo(SessionVO sessionVO, Long targetUserId) {
        try {
            if (Objects.equals(sessionVO.getSessionType(), DialogSession.SessionTypeEnum.AI.getValue())) {
                sessionVO.setTargetNickname("AI助手");
                sessionVO.setTargetAvatarUrl(""); // AI默认头像
                return;
            }
            // 调用用户服务查询目标用户信息（假设Feign返回Result<List<UserInfo>>）
            Result<List<UserInfo>> userResult = userFeignClient.getUsersByIds(Collections.singleton(targetUserId));
            if (userResult.isSuccess() && !CollectionUtils.isEmpty(userResult.getData())) {
                UserInfo targetUser = userResult.getData().get(0);
                sessionVO.setTargetNickname(targetUser.getNickname());
                sessionVO.setTargetAvatarUrl(targetUser.getAvatarUrl());
            } else {
                // 用户信息查询失败 → 显示默认值
                sessionVO.setTargetNickname("未知用户");
                sessionVO.setTargetAvatarUrl("");
            }
        } catch (Exception e) {
            // 异常降级 → 显示默认值
            sessionVO.setTargetNickname("未知用户");
            sessionVO.setTargetAvatarUrl("");
        }
    }
    /**
     * 格式化最后消息时间（抖音风格）
     *
     * @param lastMsgTime 最后消息时间
     * @return 格式化后的时间字符串（如：10分钟前、15:30、06-12）
     */
    private String formatLastMsgTime(LocalDateTime lastMsgTime) {
        if (lastMsgTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutesDiff = java.time.Duration.between(lastMsgTime, now).toMinutes();

        // 1小时内：XX分钟前
        if (minutesDiff < 60) {
            return minutesDiff + "分钟前";
        }

        // 1-24小时内：HH:MM
        long hoursDiff = java.time.Duration.between(lastMsgTime, now).toHours();
        if (hoursDiff < 24) {
            return java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(lastMsgTime);
        }

        // 超过24小时：MM-DD
        return java.time.format.DateTimeFormatter.ofPattern("MM-dd").format(lastMsgTime);
    }

    /**
     * 同步会话PO到Redis缓存
     *
     * @param sessionPO 会话PO
     */
    private void syncSessionToCache(DialogSession sessionPO) {
        // 1. 缓存单个会话信息（Hash类型）
        redisCacheUtil.setSessionCache(sessionPO.getUserId(), sessionPO.getId(), sessionPO, SESSION_CACHE_EXPIRE_SEC);

        // 2. 缓存会话列表排序（ZSet类型，score=最后消息时间戳）
        long score = sessionPO.getLastMsgTime() != null
                ? sessionPO.getLastMsgTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis();
        redisCacheUtil.addSessionToListCache(sessionPO.getUserId(), sessionPO.getId(), score, SESSION_CACHE_EXPIRE_SEC);
    }
}
  