package org.doubao.dialog.service.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.req.SessionCreateReq;
import org.doubao.dialog.service.vo.SessionVO;

import java.util.List;

/**
 * 会话管理服务接口
 * 定义功能：创建会话、查询会话列表、置顶/取消置顶、删除会话、未读清零
 */
public interface SessionService {

    /**
     * 创建会话（用户-用户/用户-AI）
     * @param userId 当前用户ID（会话所有者）
     * @param createReq 会话创建请求参数（targetId：目标ID，sessionType：会话类型）
     * @return 新建/已存在的会话ID
     */
    Long createSession(Long userId, SessionCreateReq createReq);

    /**
     * 分页查询用户会话列表
     * @param userId 当前用户ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 页大小
     * @return 分页会话VO列表（含未读计数、最后一条消息预览、目标用户信息）
     */
    Page<SessionVO> getSessionList(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 会话置顶/取消置顶
     * @param userId 当前用户ID（校验会话归属）
     * @param sessionId 会话ID
     * @param isTop 是否置顶（1=置顶，0=取消）
     */
    void updateSessionTop(Long userId, Long sessionId, Integer isTop);

    /**
     * 删除会话（逻辑删除）
     * @param userId 当前用户ID（校验会话归属）
     * @param sessionId 会话ID
     */
    void deleteSession(Long userId, Long sessionId);
    /**
     * 隐藏会话
     * @param userId 当前用户ID（校验会话归属）
     * @param sessionId 会话ID
     */
    void hiddenSession(Long userId, Long sessionId);

    /**
     * 会话未读清零
     * @param userId 当前用户ID（校验会话归属，接收方身份）
     * @param sessionId 会话ID
     */
    void clearSessionUnread(Long userId, Long sessionId);

    void clearAllSessionUnread(Long userId);

    Integer getUnreadCount(Long userId);

    /**
     * 根据会话ID和用户ID查询会话（内部调用，用于校验会话归属）
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 会话PO（不存在则返回null）
     */
    DialogSession getSessionByIdAndUserId(Long sessionId, Long userId);

    DialogSession getSessionByIdAndReceiverId(Long sessionId, Long receiverId);

    List<DialogSession> queryOwnUserSessions(Long userId);

    List<DialogSession> queryAllOwnUserSessions(Long userId);
}
  