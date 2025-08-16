package org.doubao.dialog.service.service;

import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;

/**
 * WebSocket服务接口
 */
public interface WebSocketService {


	/**
	 * 推送消息给用户
	 * @param userId 用户ID
	 * @param data 消息数据
	 */
	void pushToUser(Long userId, Object data);

	/**
	 * 通知管理员有新消息
	 * @param dialogId 对话ID
	 * @param content 消息内容
	 */
	void notifyAdmin(Long dialogId, String content);

	void addUserSession(Long userId, WebSocketSession session);

	void addAdminSession(Long adminId, WebSocketSession session);

	void removeUserSession(Long userId);

	void removeAdminSession(Long adminId);

	int adminSessionCount();
	int userSessionCount();
}