package org.doubao.dialog.service.service.impl;

import io.vavr.collection.Set;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
     * 查询会话列表核心逻辑：
     * 1. 分页查询DB（按“置顶>最后消息时间倒序”排序）
     * 2. 补充目标用户信息（头像、昵称）
     * 3. 从缓存获取最新未读计数（避免DB查询延迟）
     * 4. 转换为VO返回
     */
    @Override
    @SuppressWarnings("unchecked")
    public Page<SessionVO> getSessionList(Long userId, Integer pageNum, Integer pageSize) {
        // 1. 构建分页查询条件（按“置顶降序→最后消息时间降序”排序）
        Page<DialogSession> sessionPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DialogSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DialogSession::getUserId, userId)
                .eq(DialogSession::getDeleted, 0)
                .orderByDesc(DialogSession::getIsTop)
                .orderByDesc(DialogSession::getLastMsgTime);

        // 2. 分页查询DB
        sessionPage = sessionMapper.selectPage(sessionPage, queryWrapper);

        // 3. 转换为VO并补充信息（目标用户信息、未读计数）
        Page<SessionVO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(sessionPage.getTotal());
        resultPage.setRecords(sessionPage.getRecords().stream()
                .map(sessionPO -> convertToSessionVO(sessionPO, userId))
                .collect(Collectors.toList()));

        // 4. 返回结果
        return resultPage;
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
     * 2. 获取当前未读计数（DB+缓存）
     * 3. 更新DB未读计数为0，同步缓存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer clearSessionUnread(Long userId, Long sessionId) {
        // 1. 查询会话并校验归属（当前用户必须是会话接收方）
        DialogSession sessionPO = getSessionByIdAndUserId(sessionId, userId);
        if (sessionPO == null) {
            throw new BusinessException(ErrorCode.DIALOG_SESSION_NOT_EXIST);
        } else {
            // 2. 获取清零前的未读计数（以缓存为准，缓存不存在则取DB）
            Integer unreadCountBeforeClear = redisCacheUtil.getSessionUnreadCount(userId, sessionId);
            if (unreadCountBeforeClear == null) {
                unreadCountBeforeClear = sessionPO.getUnreadCount();
            }

            // 3. 未读计数已为0，无需操作
            if (unreadCountBeforeClear == 0) {
                return 0;
            }

            // 4. 更新DB未读计数为0
            sessionPO.setUnreadCount(0);
            sessionPO.setUpdatedTime(LocalDateTime.now());
            sessionMapper.updateById(sessionPO);

            // 5. 同步缓存未读计数为0
            redisCacheUtil.setSessionUnreadCount(userId, sessionId, 0);

            // 6. 返回清零前的未读计数
            return unreadCountBeforeClear;
        }
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

        // 1. 补充目标用户信息（AI会话固定信息，用户会话调用user-service）
        if (sessionPO.getSessionType() == DialogSession.SessionTypeEnum.AI) {
            sessionVO.setTargetNickname("AI助手");
            sessionVO.setTargetAvatarUrl(""); // AI默认头像
        } else {
            Result<List<UserInfo>> userResult = userFeignClient.getUsersByIds(Collections.singleton(sessionPO.getTargetId()));
            if (userResult.isSuccess() && userResult.getData() != null && !userResult.getData().isEmpty()) {
                UserInfo targetUser = userResult.getData().get(0);
                sessionVO.setTargetNickname(targetUser.getNickname());
                sessionVO.setTargetAvatarUrl(targetUser.getAvatarUrl());
            } else {
                // 目标用户查询失败（如已注销），显示默认信息
                sessionVO.setTargetNickname("未知用户");
                sessionVO.setTargetAvatarUrl(""); // 默认头像
            }
        }

        // 2. 补充最新未读计数（从缓存获取，缓存不存在则取DB）
        Integer unreadCount = redisCacheUtil.getSessionUnreadCount(userId, sessionPO.getId());
        sessionVO.setUnreadCount(unreadCount != null ? unreadCount : sessionPO.getUnreadCount());

        // 3. 格式化最后消息时间（如：10分钟前、15:30、06-12）
        sessionVO.setLastMsgTimeStr(formatLastMsgTime(sessionPO.getLastMsgTime()));

        return sessionVO;
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
  