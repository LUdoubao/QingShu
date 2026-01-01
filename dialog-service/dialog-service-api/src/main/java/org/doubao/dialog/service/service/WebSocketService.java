package org.doubao.dialog.service.service;

import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;

/**
 * WebSocket服务接口
 * 适用场景：
 * 1. 实时消息推送（用户间消息、AI回复、系统通知）
 * 2. 在线状态管理（用户/管理员在线会话维护）
 * 3. 消息状态同步（已读、撤回等状态实时更新）
 * 业务说明：定义WebSocket实时通信服务接口，支持用户与管理员间的实时消息推送
 */
public interface WebSocketService {


	/**
	 * 推送消息给指定用户
	 * 业务说明：向指定用户推送实时消息（如对方回复、系统通知等）
	 * @param userId 目标用户ID，标识消息接收方
	 * @param data 推送的消息数据，包含消息内容、发送者等信息
	 */
	void pushToUser(Long userId, Object data);

	/**
	 * 通知管理员有新消息
	 * 业务说明：向管理员推送新消息通知，便于及时处理用户咨询
	 * @param dialogId 对话ID，标识消息所属的对话
	 * @param content 消息内容，用于管理员快速了解消息要点
	 */
	void notifyAdmin(Long dialogId, String content);

	/**
	 * 添加用户WebSocket会话
	 * 业务说明：将用户的WebSocket会话添加到在线用户映射中
	 * @param userId 用户ID，标识WebSocket会话归属
	 * @param session WebSocket会话对象，用于后续消息推送
	 */
	void addUserSession(Long userId, WebSocketSession session);

	/**
	 * 添加管理员WebSocket会话
	 * 业务说明：将管理员的WebSocket会话添加到在线管理员映射中
	 * @param adminId 管理员ID，标识WebSocket会话归属
	 * @param session WebSocket会话对象，用于后续消息推送
	 */
	void addAdminSession(Long adminId, WebSocketSession session);

	/**
	 * 移除用户WebSocket会话
	 * 业务说明：从在线用户映射中移除指定用户的WebSocket会话
	 * @param userId 用户ID，标识需要移除会话的用户
	 */
	void removeUserSession(Long userId);

	/**
	 * 移除管理员WebSocket会话
	 * 业务说明：从在线管理员映射中移除指定管理员的WebSocket会话
	 * @param adminId 管理员ID，标识需要移除会话的管理员
	 */
	void removeAdminSession(Long adminId);

	/**
	 * 获取在线管理员数量
	 * 业务说明：统计当前在线的管理员数量
	 * @return 在线管理员数量，用于管理员负载均衡
	 */
	int adminSessionCount();
	
	/**
	 * 获取在线用户数量
	 * 业务说明：统计当前在线的用户数量
	 * @return 在线用户数量，用于系统监控和统计
	 */
	int userSessionCount();
}