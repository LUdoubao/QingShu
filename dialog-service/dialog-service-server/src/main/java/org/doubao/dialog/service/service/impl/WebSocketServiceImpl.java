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
 * 业务说明：处理WebSocket连接和消息推送功能，包括用户和管理员的实时通信
 * 核心功能：1. 管理用户和管理员的WebSocket会话 2. 实时消息推送 3. 会话连接管理
 * 适用场景：
 * 1. AI助手消息实时推送
 * 2. 用户间私信实时通信
 * 3. 管理员通知系统
 */
@Service
public class WebSocketServiceImpl implements WebSocketService {
	private static final Logger log = LoggerFactory.getLogger(WebSocketServiceImpl.class);

	// 存储用户ID与WebSocket会话的映射
	private static final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

	// 存储管理员会话
	private static final Map<Long, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

	/** 对话Mapper，用于获取对话相关信息 */
	@Autowired
	private AssistantDialogMapper dialogMapper;

	/**
	 * 推送消息给用户
	 * 业务说明：向指定用户推送消息，用于实时通知用户新消息
	 * 业务流程：
	 * 1. 检查用户ID是否为空
	 * 2. 从会话映射中获取用户WebSocket会话
	 * 3. 检查会话是否处于开启状态
	 * 4. 将数据转换为JSON格式并发送消息
	 * 异常处理：推送失败时记录错误日志
	 * 参数校验：用户ID不能为空
	 * @param userId 用户ID，标识消息接收方
	 * @param data 消息数据，需要推送的数据内容
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
	 * 业务说明：向所有在线管理员推送新消息通知，用于管理员监控用户咨询
	 * 业务流程：
	 * 1. 构建通知内容（类型、对话ID、内容、时间）
	 * 2. 遍历所有在线管理员会话
	 * 3. 检查会话是否处于开启状态
	 * 4. 发送通知消息给每个在线管理员
	 * 异常处理：推送失败时记录错误日志
	 * 数据处理：通知内容包含消息类型、对话ID、内容和时间戳
	 * @param dialogId 对话ID，标识消息所属对话
	 * @param content 消息内容，需要通知的内容
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

	/**
	 * 添加用户会话
	 * 业务说明：将用户ID与WebSocket会话建立关联，用于后续消息推送
	 * 业务流程：将用户ID和对应的WebSocket会话存入映射表
	 * 参数校验：用户ID和会话不能为空
	 * 数据处理：使用ConcurrentHashMap保证线程安全
	 * @param userId 用户ID，标识会话所属用户
	 * @param session WebSocket会话，与用户建立的连接
	 */
	public void addUserSession(Long userId, WebSocketSession session) {
		userSessions.put(userId, session);
	}

	/**
	 * 添加管理员会话
	 * 业务说明：将管理员ID与WebSocket会话建立关联，用于管理员消息推送
	 * 业务流程：将管理员ID和对应的WebSocket会话存入映射表
	 * 参数校验：管理员ID和会话不能为空
	 * 数据处理：使用ConcurrentHashMap保证线程安全
	 * @param adminId 管理员ID，标识会话所属管理员
	 * @param session WebSocket会话，与管理员建立的连接
	 */
	public void addAdminSession(Long adminId, WebSocketSession session) {
		adminSessions.put(adminId,session);
	}

	/**
	 * 移除用户会话
	 * 业务说明：从映射表中移除用户会话，通常在用户断开连接时调用
	 * 业务流程：根据用户ID从映射表中移除对应的WebSocket会话
	 * 参数校验：用户ID不能为空
	 * 数据处理：移除后该用户将无法接收实时消息推送
	 * @param userId 用户ID，标识需要移除会话的用户
	 */
	public void removeUserSession(Long userId) {
		userSessions.remove(userId);
	}

	/**
	 * 移除管理员会话
	 * 业务说明：从映射表中移除管理员会话，通常在管理员断开连接时调用
	 * 业务流程：根据管理员ID从映射表中移除对应的WebSocket会话
	 * 参数校验：管理员ID不能为空
	 * 数据处理：移除后该管理员将无法接收实时通知
	 * @param adminId 管理员ID，标识需要移除会话的管理员
	 */
	public void removeAdminSession(Long adminId) {
		adminSessions.remove(adminId);
	}

	/**
	 * 获取管理员会话数量
	 * 业务说明：获取当前在线管理员的数量
	 * 业务流程：返回管理员会话映射表的大小
	 * 数据处理：返回当前在线管理员的数量
	 * @return 在线管理员数量
	 */
	@Override
	public int adminSessionCount() {
		return adminSessions.size();
	}

	/**
	 * 获取用户会话数量
	 * 业务说明：获取当前在线用户的数量
	 * 业务流程：返回用户会话映射表的大小
	 * 数据处理：返回当前在线用户的数量
	 * @return 在线用户数量
	 */
	@Override
	public int userSessionCount() {
		return userSessions.size();
	}

	/**
	 * 将对象转换为JSON字符串
	 * 业务说明：将Java对象转换为JSON格式的字符串，用于WebSocket消息传输
	 * 业务流程：使用FastJSON将对象序列化为JSON字符串
	 * 参数校验：对象不能为空
	 * 数据处理：将Java对象转换为标准JSON格式
	 * @param object 需要转换的对象
	 * @return JSON格式的字符串
	 */
	private String convertToJson(Object object) {
		return JSON.toJSONString(object);
	}
}