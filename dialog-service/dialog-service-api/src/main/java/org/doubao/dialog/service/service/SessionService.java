package org.doubao.dialog.service.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.req.SessionCreateReq;
import org.doubao.dialog.service.vo.SessionVO;

import java.util.List;

/**
 * 会话管理服务接口
 * 定义功能：创建会话、查询会话列表、置顶/取消置顶、删除会话、未读清零
 * 业务说明：定义会话管理的核心服务接口，包括会话创建、查询、状态管理等功能
 * 适用场景：用户会话管理、会话状态控制、会话列表展示
 */
public interface SessionService {

    /**
     * 创建会话（用户-用户/用户-AI）
     * 业务说明：创建用户与用户或用户与AI助手之间的会话
     * @param userId 当前用户ID（会话所有者），标识会话的创建者
     * @param createReq 会话创建请求参数，包含目标ID、会话类型等信息
     * @return 新建或已存在的会话ID，如果会话已存在则返回现有ID
     */
    Long createSession(Long userId, SessionCreateReq createReq);

    /**
     * 分页查询用户会话列表
     * 业务说明：分页获取当前用户的所有会话记录
     * @param userId 当前用户ID，标识需要查询会话列表的用户
     * @param pageNum 页码，从1开始
     * @param pageSize 页大小，控制返回记录数量
     * @return 分页会话VO列表，包含未读计数、最后一条消息预览、目标用户信息等
     */
    Page<SessionVO> getSessionList(Long userId, Integer pageNum, Integer pageSize, Long currentSessionId);

    /**
     * 会话置顶/取消置顶
     * 业务说明：设置会话的置顶状态，置顶会话在列表中优先显示
     * @param userId 当前用户ID，用于校验会话归属权限
     * @param sessionId 会话ID，标识需要设置置顶状态的会话
     * @param isTop 是否置顶，1=置顶，0=取消置顶
     */
    void updateSessionTop(Long userId, Long sessionId, Integer isTop);

    /**
     * 删除会话（逻辑删除）
     * 业务说明：逻辑删除指定会话，保留消息记录但标记会话为删除状态
     * @param userId 当前用户ID，用于校验会话归属权限
     * @param sessionId 会话ID，标识需要删除的会话
     */
    void deleteSession(Long userId, Long sessionId);
    /**
     * 隐藏会话
     * 业务说明：隐藏指定会话，使其不在会话列表中显示
     * @param userId 当前用户ID，用于校验会话归属权限
     * @param sessionId 会话ID，标识需要隐藏的会话
     */
    void hiddenSession(Long userId, Long sessionId);

    /**
     * 会话未读清零
     * 业务说明：将指定会话的未读消息数清零
     * @param userId 当前用户ID，用于校验会话归属权限（接收方身份）
     * @param sessionId 会话ID，标识需要清零未读数的会话
     */
    void clearSessionUnread(Long userId, Long sessionId);

    /**
     * 清空所有会话未读消息
     * 业务说明：将当前用户所有会话的未读消息数清零
     * @param userId 当前用户ID，标识需要清零未读消息的用户
     */
    void clearAllSessionUnread(Long userId);

    /**
     * 获取未读消息总数
     * 业务说明：获取当前用户所有会话的未读消息总数
     * @param userId 当前用户ID，标识需要获取未读总数的用户
     * @return 未读消息总数
     */
    Integer getUnreadCount(Long userId);

    /**
     * 根据会话ID和用户ID查询会话（内部调用，用于校验会话归属）
     * 业务说明：根据会话ID和用户ID查询会话记录，用于权限校验
     * @param sessionId 会话ID，标识需要查询的会话
     * @param userId 用户ID，用于校验会话归属
     * @return 会话PO对象，不存在则返回null
     */
    DialogSession getSessionByIdAndUserId(Long sessionId, Long userId);

    /**
     * 根据会话ID和接收者ID查询会话
     * 业务说明：根据会话ID和接收者ID查询会话记录，用于消息状态管理
     * @param sessionId 会话ID，标识需要查询的会话
     * @param receiverId 接收者ID，标识消息接收方
     * @return 会话PO对象，不存在则返回null
     */
    DialogSession getSessionByIdAndReceiverId(Long sessionId, Long receiverId);

    /**
     * 查询用户创建的会话列表
     * 业务说明：查询当前用户创建的所有用户会话
     * @param userId 用户ID，标识会话创建者
     * @return 用户创建的会话列表
     */
    List<DialogSession> queryOwnUserSessions(Long userId, Long currentSessionId);

    /**
     * 查询用户所有会话列表（包括隐藏的）
     * 业务说明：查询当前用户创建的所有用户会话，包括隐藏的会话
     * @param userId 用户ID，标识会话创建者
     * @return 用户所有会话列表
     */
    List<DialogSession> queryAllOwnUserSessions(Long userId);
}
  