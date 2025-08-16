package org.doubao.dialog.service.service.impl;


import com.alibaba.fastjson.JSON;
import org.doubao.dialog.service.mapper.AssistantDialogMapper;
import org.doubao.dialog.service.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.websocket.Session;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务实现类
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {
	private static final Logger log = LoggerFactory.getLogger(WebSocketServiceImpl.class);

	// 存储用户ID与WebSocket会话的映射
	private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	// 存储管理员会话
	private static final Map<Long, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

	@Autowired
	private AssistantDialogMapper dialogMapper;

	/**
	 * 推送消息给用户
	 */
	@Override
	public void pushToUser(Long userId, Object data) {
		if (userId == null) {
			log.warn("用户ID为空，无法推送消息");
			return;
		}

		WebSocketSession session = userSessions.get(userId);
		if (session != null && session.isOpen()) {
			try {
				String jsonMessage = convertToJson(data);
				session.sendMessage(new TextMessage(jsonMessage));
			} catch (IOException e) {
				log.error("推送消息给用户失败: {}", e.getMessage());
			}
		}
	}

	/**
	 * 通知管理员有新消息
	 */
	@Override
	public void notifyAdmin(Long dialogId, String content) {
		// 构建通知内容
		Map<String, Object> notify = new HashMap<>();
		notify.put("type", "new_message");
		notify.put("dialogId", dialogId);
		notify.put("content", content);
		notify.put("time", System.currentTimeMillis());

		String jsonNotify = JSON.toJSONString(notify);

		// 推送给所有在线管理员
		for (WebSocketSession session : adminSessions.values()) {
			if (session.isOpen()) {
				try {
					session.sendMessage(new TextMessage(jsonNotify));
				} catch (IOException e) {
					log.error("推送通知给管理员失败: {}", e.getMessage());
				}
			}
		}
	}

	// 用户连接建立
	public void addUserSession(Long userId, WebSocketSession session) {
		userSessions.put(userId, session);
	}

	// 管理员连接建立
	public void addAdminSession(Long adminId, WebSocketSession session) {
		adminSessions.put(adminId,session);
	}

	// 用户断开连接
	public void removeUserSession(Long userId) {
		userSessions.remove(userId);
	}

	// 管理员断开连接
	public void removeAdminSession(Long adminId) {
		adminSessions.remove(adminId);
	}

	@Override
	public int adminSessionCount() {
		return adminSessions.size();
	}

	@Override
	public int userSessionCount() {
		return userSessions.size();
	}

	private String convertToJson(Object object) {
		return JSON.toJSONString(object);
	}
}