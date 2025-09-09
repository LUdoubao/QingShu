package org.doubao.dialog.service.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.doubao.dialog.service.enums.MessagePushType;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.doubao.dialog.service.vo.MessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 对话业务WebSocket处理器
 * 核心职责：
 * 1. 维护用户WebSocket连接会话（在线用户映射）
 * 2. 处理客户端发送的实时消息（如心跳、已读确认）
 * 3. 推送消息给指定用户（私信、系统通知）
 * 4. 管理用户在线状态（连接建立=上线，连接关闭=下线）
 */
@Component
public class DialogWebSocketHandler extends TextWebSocketHandler {

	// ========================= 常量常量与状态管理 =========================
	/** WebSocket会话属性：用户ID（从Token解析后存入） */
	private static final String SESSION_ATTR_USER_ID = "userId";
	/** 在线用户WebSocket会话映射（线程安全，key=用户ID，value=WebSocketSession） */
	private final Map<Long, WebSocketSession> onlineUserSessionMap = new ConcurrentHashMap<>(128);
	/** Redis缓存工具类（用于在线状态、会话信息） */
	@Resource
	private RedisCacheUtil redisCacheUtil;

	private static final Logger log = LoggerFactory.getLogger(DialogWebSocketHandler.class);

	// ========================= 连接生命周期管理 =========================
	/**
	 * 连接建立成功回调（用户上线）
	 * 流程：1. 解析用户ID 2. 存储会话映射 3. 更新Redis在线状态 4. 日志记录
	 * @param session WebSocket会话对象（包含连接信息、属性等）
	 * @throws Exception 连接处理异常
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// 1. 从会话属性中获取用户ID（Token解析逻辑在WebSocketInterceptor中完成）
		Object object = session.getAttributes().get(SESSION_ATTR_USER_ID);
		log.info("afterConnectionEstablished-WebSocket receive message | userId: {}", object);
		Long userId = Long.valueOf(String.valueOf(object)) ;
		if (ObjectUtil.isNull(userId)) {
			log.error("WebSocket connection failed | userId is null (sessionId: {})", session.getId());
			session.close(CloseStatus.POLICY_VIOLATION.withReason("用户身份验证失败"));
			return;
		}

		// 2. 存储在线用户会话映射（覆盖旧连接，解决多端登录问题）
		WebSocketSession oldSession = onlineUserSessionMap.put(userId, session);
		if (ObjectUtil.isNotNull(oldSession) && oldSession.isOpen()) {
			// 关闭旧连接（确保同一用户仅保持一个有效连接）
			oldSession.close(CloseStatus.NORMAL.withReason("账号在其他设备登录，当前连接已断开"));
			log.error("WebSocket old session closed | userId: {}, oldSessionId: {}, newSessionId: {}",
					userId, oldSession.getId(), session.getId());
		}

		// 3. 更新Redis在线状态（key=用户在线前缀+用户ID，value=会话ID，过期时间=30分钟）
		String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
		redisCacheUtil.set(onlineKey, session.getId(), 30, TimeUnit.MINUTES);

		// 4. 日志记录连接成功
		log.info("WebSocket connection established | userId: {}, sessionId: {}, onlineUserCount: {}",
				userId, session.getId(), onlineUserSessionMap.size());
	}

	/**
	 * 连接关闭回调（用户下线）
	 * 流程：1. 清理会话映射 2. 清除Redis在线状态 3. 日志记录
	 * @param session 关闭的会话对象
	 * @param status 关闭状态（正常/异常）
	 * @throws Exception 关闭处理异常
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		// 1. 从会话属性中获取用户ID
		Object object = session.getAttributes().get(SESSION_ATTR_USER_ID);
		log.info("afterConnectionClosed-WebSocket receive message | userId: {}", object);
		Long userId = Long.valueOf(String.valueOf(object)) ;
		if (ObjectUtil.isNull(userId)) {
			log.error("WebSocket connection closed | userId is null (sessionId: {})", session.getId());
			return;
		}

		// 2. 清理在线用户会话映射（仅删除当前会话，避免误删新连接）
		WebSocketSession storedSession = onlineUserSessionMap.get(userId);
		if (ObjectUtil.isNotNull(storedSession) && storedSession.getId().equals(session.getId())) {
			onlineUserSessionMap.remove(userId);
			// 3. 清除Redis在线状态（延迟10秒，防止网络波动导致的误下线）
			String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
			redisCacheUtil.expire(onlineKey, 10, TimeUnit.SECONDS);
		}

		// 4. 日志记录连接关闭
		log.info("WebSocket connection closed | userId: {}, sessionId: {}, status: {}, onlineUserCount: {}",
				userId, session.getId(), status, onlineUserSessionMap.size());
	}


	// ========================= 客户端消息处理 =========================
	/**
	 * 接收客户端文本消息（如心跳、已读确认）
	 * 消息格式：JSON字符串，必须包含"msgType"字段（消息类型）
	 * @param session 发送消息的客户端会话
	 * @param message 客户端发送的文本消息
	 * @throws Exception 消息处理异常
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		Object object = session.getAttributes().get(SESSION_ATTR_USER_ID);
		log.info("handleTextMessage-WebSocket receive message | userId: {}", object);
		Long userId = Long.valueOf(String.valueOf(object)) ;
		String msgContent = message.getPayload();
		log.info("WebSocket receive message | userId: {}, sessionId: {}, content: {}",
				userId, session.getId(), msgContent);

		// 1. 校验消息格式（非空+JSON格式）
		if (StrUtil.isBlank(msgContent)) {
			sendErrorMessage(session, "消息内容不能为空");
			return;
		}
		Map<String, Object> msgMap;
		try {
			msgMap = JSON.parseObject(msgContent, new TypeReference<Map<String, Object>>() {});
		} catch (Exception e) {
			sendErrorMessage(session, "消息格式错误，必须为JSON字符串");
			log.error("WebSocket parse message failed | userId: {}, content: {}, error: {}",
					userId, msgContent, e.getMessage(), e);
			return;
		}

		// 2. 解析消息类型，分发处理
		String msgType = (String) msgMap.get("msgType");
		if (StrUtil.isBlank(msgType)) {
			sendErrorMessage(session, "消息缺少必要字段：msgType");
			return;
		}

		// 按消息类型处理（心跳、已读确认等）
		switch (msgType) {
			case "HEARTBEAT":
				handleHeartbeat(userId, session); // 处理心跳（刷新在线状态）
				break;
			case "MSG_READ_CONFIRM":
				handleMsgReadConfirm(userId, msgMap); // 处理消息已读确认
				break;
			default:
				sendErrorMessage(session, "不支持的消息类型：" + msgType);
				log.error("WebSocket unsupported msgType | userId: {}, msgType: {}", userId, msgType);
		}
	}

	/**
	 * 处理客户端异常（如连接中断、消息发送失败）
	 * @param session 出现异常的会话
	 * @param exception 异常对象
	 * @throws Exception 异常处理过程中的二次异常
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		Long userId = (Long) session.getAttributes().get(SESSION_ATTR_USER_ID);
		log.error("WebSocket transport error | userId: {}, sessionId: {}, error: {}",
				userId, session.getId(), exception.getMessage(), exception);

		// 异常时关闭会话（触发afterConnectionClosed回调，清理在线状态）
		if (session.isOpen()) {
			session.close(CloseStatus.SERVER_ERROR.withReason("连接传输异常"));
		}
	}


	// ========================= 消息发送工具方法 =========================
	/**
	 * 向指定用户推送私信消息（实时对话核心方法）
	 *
	 * @param userId    目标用户ID
	 * @param messageVO 消息VO（包含消息ID、内容、发送者等信息）
	 */
	public void pushPrivateMessage(Long userId, MessageVO messageVO, MessagePushType messagePushType) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(messageVO)) {
			log.error("WebSocket push message failed | userId or messageVO is null");
			return;
		}

		// 1. 获取用户在线会话
		log.info("WebSocket push private message onlineUserSessionMap {}",
				JSON.toJSONString(onlineUserSessionMap));
		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (ObjectUtil.isNull(session) || !session.isOpen()) {
			log.error("WebSocket push message failed | user offline (userId: {})", userId);
			return;
		}

		// 2. 构建推送消息（JSON格式，包含消息类型和数据）
		Map<String, Object> pushMsg = new ConcurrentHashMap<>(2);
		pushMsg.put("msgType", messagePushType.getName()); // 消息类型：私信
		pushMsg.put("data", messageVO);
		String jsonMsg = JSON.toJSONString(pushMsg);

		// 3. 发送消息（带异常处理，避免单用户推送失败影响整体）
		try {
			session.sendMessage(new TextMessage(jsonMsg));
			log.info("WebSocket push private message success | userId: {}, msgId: {}",
					userId, messageVO.getId());
		} catch (IOException e) {
			log.error("WebSocket push private message failed | userId: {}, msgId: {}, error: {}",
					userId, messageVO.getId(), e.getMessage(), e);
			// 推送失败时关闭会话（后续消息走离线推送）
			if (session.isOpen()) {
				try {
					session.close(CloseStatus.SERVER_ERROR.withReason("消息推送失败，连接已断开"));
				} catch (IOException ex) {
					log.error("WebSocket close session failed | userId: {}, sessionId: {}",
							userId, session.getId(), ex);
				}
			}
		}
	}

	/**
	 * 向指定用户推送系统通知（如会话被删除、用户被拉黑）
	 * @param userId 目标用户ID
	 * @param notifyContent 通知内容
	 * @param notifyType 通知类型（如"SESSION_DELETED"、"USER_BLOCKED"）
	 */
	public void pushSystemNotify(Long userId, String notifyContent, String notifyType) {
		if (ObjectUtil.isNull(userId) || StrUtil.isBlank(notifyContent) || StrUtil.isBlank(notifyType)) {
			log.error("WebSocket push notify failed | param is null (userId: {}, notifyType: {})",
					userId, notifyType);
			return;
		}

		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (ObjectUtil.isNull(session) || !session.isOpen()) {
			log.info("WebSocket push notify failed | user offline (userId: {})", userId);
			return;
		}

		// 构建系统通知消息格式
		Map<String, Object> notifyMsg = new ConcurrentHashMap<>(3);
		notifyMsg.put("msgType", "SYSTEM_NOTIFY"); // 消息类型：系统通知
		notifyMsg.put("notifyType", notifyType);   // 通知子类型
		notifyMsg.put("content", notifyContent);   // 通知内容
		String jsonMsg = JSON.toJSONString(notifyMsg);

		try {
			session.sendMessage(new TextMessage(jsonMsg));
			log.info("WebSocket push system notify success | userId: {}, notifyType: {}",
					userId, notifyType);
		} catch (IOException e) {
			log.error("WebSocket push system notify failed | userId: {}, notifyType: {}, error: {}",
					userId, notifyType, e.getMessage(), e);
		}
	}

	/**
	 * 向客户端发送错误消息（如参数错误、权限不足）
	 * @param session 目标客户端会话
	 * @param errorMsg 错误描述
	 */
	private void sendErrorMessage(WebSocketSession session, String errorMsg) {
		if (ObjectUtil.isNull(session) || !session.isOpen()) {
			log.error("WebSocket send error message failed | session closed");
			return;
		}

		Map<String, Object> errorMsgMap = new ConcurrentHashMap<>(2);
		errorMsgMap.put("msgType", "ERROR");
		errorMsgMap.put("content", errorMsg);
		String jsonMsg = JSON.toJSONString(errorMsgMap);

		try {
			session.sendMessage(new TextMessage(jsonMsg));
			log.info("WebSocket send error message | sessionId: {}, errorMsg: {}",
					session.getId(), errorMsg);
		} catch (IOException e) {
			log.error("WebSocket send error message failed | sessionId: {}, errorMsg: {}, ex: {}",
					session.getId(), errorMsg, e.getMessage(), e);
		}
	}


	// ========================= 具体消息类型处理 =========================
	/**
	 * 处理客户端心跳消息（刷新在线状态，防止超时下线）
	 * 逻辑：1. 刷新Redis在线状态过期时间 2. 回复心跳确认
	 * @param userId 用户ID
	 * @param session 客户端会话
	 */
	private void handleHeartbeat(Long userId, WebSocketSession session) {
		// 1. 刷新Redis在线状态（重置为30分钟过期）
		String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
		redisCacheUtil.expire(onlineKey, 30, TimeUnit.MINUTES);

		// 2. 回复心跳确认（告知客户端连接正常）
		Map<String, Object> heartbeatResp = new ConcurrentHashMap<>(2);
		heartbeatResp.put("msgType", "HEARTBEAT_RESP");
		heartbeatResp.put("timestamp", System.currentTimeMillis());
		String jsonResp = JSON.toJSONString(heartbeatResp);

		try {
			session.sendMessage(new TextMessage(jsonResp));
			log.info("WebSocket handle heartbeat success | userId: {}, sessionId: {}",
					userId, session.getId());
		} catch (IOException e) {
			log.error("WebSocket send heartbeat resp failed | userId: {}, sessionId: {}, error: {}",
					userId, session.getId(), e.getMessage(), e);
		}
	}

	/**
	 * 处理消息已读确认（清除用户对应会话的未读消息数）
	 * 客户端消息格式：{"msgType":"MSG_READ_CONFIRM", "sessionId": 123, "lastReadMsgId": "60d21b4667d0d8992e610c85"}
	 * @param userId 用户ID（已读操作的发起者）
	 * @param msgMap 客户端消息映射
	 */
	private void handleMsgReadConfirm(Long userId, Map<String, Object> msgMap) {
		// 1. 解析消息中的会话ID和最后已读消息ID
		Object sessionIdObj = msgMap.get("sessionId");
		Object lastReadMsgIdObj = msgMap.get("lastReadMsgId");
		if (ObjectUtil.isNull(sessionIdObj) || ObjectUtil.isNull(lastReadMsgIdObj)) {
			log.error("WebSocket handle msg read confirm failed | missing param (userId: {})", userId);
			return;
		}

		Long sessionId = null;
		try {
			sessionId = Long.parseLong(sessionIdObj.toString());
		} catch (NumberFormatException e) {
			log.error("WebSocket handle msg read confirm failed | sessionId is not number (userId: {}, sessionId: {})",
					userId, sessionIdObj, e);
			return;
		}
		String lastReadMsgId = lastReadMsgIdObj.toString();

		// 2. 清除Redis中该会话的未读消息数（原子操作）
		String unreadCountKey = RedisCacheUtil.DIALOG_UNREAD_COUNT_PREFIX + userId;
		redisCacheUtil.hSet(unreadCountKey, sessionId.toString(), 0);
		// 刷新未读消息数缓存过期时间（7天）
		redisCacheUtil.expire(unreadCountKey, 7, TimeUnit.DAYS);

		log.info("WebSocket handle msg read confirm success | userId: {}, sessionId: {}, lastReadMsgId: {}",
				userId, sessionId, lastReadMsgId);

		// TODO: 此处可扩展逻辑：更新数据库中消息的已读状态（批量标记该会话中小于lastReadMsgId的消息为已读）
	}


	// ========================= 在线状态查询工具方法 =========================
	/**
	 * 查询用户是否在线（内存映射+Redis双重校验）
	 * @param userId 用户ID
	 * @return true=在线，false=离线
	 */
	public boolean isUserOnline(Long userId) {
		if (ObjectUtil.isNull(userId)) {
			return false;
		}

		// 1. 先查内存映射（性能优先，避免Redis交互）
		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (ObjectUtil.isNotNull(session) && session.isOpen()) {
			return true;
		}

		// 2. 内存映射无有效会话时，查Redis（防止内存数据与Redis不一致）
		String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
		String sessionId = redisCacheUtil.getString(onlineKey, String.class);
		return ObjectUtil.isNotNull(sessionId);
	}

	/**
	 * 获取当前在线用户数量（内存映射统计）
	 * @return 在线用户数
	 */
	public int getOnlineUserCount() {
		return onlineUserSessionMap.size();
	}

	/**
	 * 推送消息状态给发送者
	 * @param senderId 消息发送者ID
	 * @param messagePushType 消息推送类型
	 * @param readData 消息已读数据
	 */
	public void pushMessage(Long senderId, MessagePushType messagePushType, JSONObject readData) {
		pushPrivateMessage(senderId, new MessageVO(Long.valueOf(readData.getString("sessionId")), senderId) , messagePushType);
		log.info("WebSocket push message status to sender success | senderId: {}, messagePushType: {}, readData: {}",
				senderId, messagePushType.getName(), readData);
	}


}
