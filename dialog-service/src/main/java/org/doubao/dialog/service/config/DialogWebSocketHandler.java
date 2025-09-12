package org.doubao.dialog.service.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.doubao.dialog.service.entity.DialogMessage;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.enums.MessagePushType;
import org.doubao.dialog.service.feign.UserFeignClient;
import org.doubao.dialog.service.mapper.DialogSessionMapper;
import org.doubao.dialog.service.messaging.DialogEventPublisher;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.doubao.dialog.service.vo.MessageVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

	// ========================= 常量与状态管理 =========================
	/** AI助手固定ID */
	private static final Long AI_SENDER_ID = 10000L;
	/** WebSocket会话属性：用户ID（从Token解析后存入） */
	private static final String SESSION_ATTR_USER_ID = "userId";
	/** 在线用户WebSocket会话映射（线程安全，key=用户ID，value=WebSocketSession） */
	private final Map<Long, WebSocketSession> onlineUserSessionMap = new ConcurrentHashMap<>(128);
	/** Redis缓存工具类（用于在线状态、会话信息） */
	@Resource
	private RedisCacheUtil redisCacheUtil;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private DialogSessionMapper sessionMapper;
	@Autowired
	private UserFeignClient userFeignClient;
	@Resource
	private DialogEventPublisher dialogEventPublisher;
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
		log.info("WebSocket连接建立-接收消息 | 用户ID: {}", object);
		Long userId = Long.valueOf(String.valueOf(object));
		if (ObjectUtil.isNull(userId)) {
			log.error("WebSocket连接失败 | 用户ID为空 (会话ID: {})", session.getId());
			session.close(CloseStatus.POLICY_VIOLATION.withReason("用户身份验证失败"));
			return;
		}

		// 2. 存储在线用户会话映射（覆盖旧连接，解决多端登录问题）
		WebSocketSession oldSession = onlineUserSessionMap.put(userId, session);
		if (ObjectUtil.isNotNull(oldSession) && oldSession.isOpen()) {
			// 关闭旧连接（确保同一用户仅保持一个有效连接）
			oldSession.close(CloseStatus.NORMAL.withReason("账号在其他设备登录，当前连接已断开"));
			log.warn("WebSocket旧会话已关闭 | 用户ID: {}, 旧会话ID: {}, 新会话ID: {}",
					userId, oldSession.getId(), session.getId());
		}

		// 3. 更新Redis在线状态（key=用户在线前缀+用户ID，value=会话ID，过期时间=30分钟）
		String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
		redisCacheUtil.set(onlineKey, session.getId(), 30, TimeUnit.MINUTES);

		// 4. 日志记录连接成功
		log.info("WebSocket连接建立成功 | 用户ID: {}, 会话ID: {}, 当前在线用户数: {}",
				userId, session.getId(), onlineUserSessionMap.size());

		// 5. 推送用户上线消息
		// 获取该用户的所有为targetId对话
		List<DialogSession> sessions = sessionMapper.selectList(
				new LambdaQueryWrapper<>(DialogSession.class).eq(DialogSession::getTargetId, userId)
		);
		for (DialogSession dialogSession : sessions) {
			JSONObject data = new JSONObject();
			data.put("sessionId", dialogSession.getId());
			data.put("sessionType", dialogSession.getSessionType().getValue());
			pushMessage(dialogSession.getUserId(), MessagePushType.USER_ONLINE, data);
		}
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
		log.info("WebSocket连接关闭-接收消息 | 用户ID: {}", object);
		Long userId = Long.valueOf(String.valueOf(object));
		if (ObjectUtil.isNull(userId)) {
			log.error("WebSocket连接关闭 | 用户ID为空 (会话ID: {})", session.getId());
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
		log.info("WebSocket连接已关闭 | 用户ID: {}, 会话ID: {}, 关闭状态: {}, 当前在线用户数: {}",
				userId, session.getId(), status, onlineUserSessionMap.size());
		// 推送用户下线消息
		// 获取该用户的所有为targetId对话
		List<DialogSession> sessions = sessionMapper.selectList(
				new LambdaQueryWrapper<>(DialogSession.class).eq(DialogSession::getTargetId, userId)
		);
		for (DialogSession dialogSession : sessions) {
			JSONObject data = new JSONObject();
			data.put("sessionId", dialogSession.getId());
			data.put("sessionType", dialogSession.getSessionType().getValue());
			pushMessage(dialogSession.getUserId(), MessagePushType.USER_OFFLINE, data);
		}
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
		log.info("处理文本消息-WebSocket接收消息 | 用户ID: {}", object);
		Long userId = Long.valueOf(String.valueOf(object));
		String msgContent = message.getPayload();
		log.info("WebSocket接收消息 | 用户ID: {}, 会话ID: {}, 消息内容: {}",
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
			log.error("WebSocket解析消息失败 | 用户ID: {}, 消息内容: {}, 错误信息: {}",
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
			case "CONVERSATION_ENTER":
				handleConversationEnter(userId, msgMap); // 处理进入会话
				break;
			case "CONVERSATION_LEAVE":
				handleConversationLeave(userId, msgMap); // 处理离开会话
				break;
			case "TYPING_STATUS":
				handleTypingStatus(userId, msgMap); // 处理输入状态更新
				break;
			case "UNREAD_COUNT_CHANGE":
				Object data = msgMap.getOrDefault("data", new HashMap<>());
				JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
				Object sessionIdObj = jsonData.get("sessionId");
				Object unreadCountObj = jsonData.get("unreadCount");
				handleUnreadCountChange(userId, sessionIdObj, unreadCountObj);
				break;
			default:
				sendErrorMessage(session, "不支持的消息类型：" + msgType);
				log.error("WebSocket不支持的消息类型 | 用户ID: {}, 消息类型: {}", userId, msgType);
		}
	}

	public void handleUnreadCountChange(Long userId, Object sessionIdObj, Object unreadCountObj) {
		if (ObjectUtil.isNull(sessionIdObj) || ObjectUtil.isNull(unreadCountObj)) {
			log.error("处理未读数变更失败 | 缺少必要参数 (用户ID: {})", userId);
			return;
		}
		redisCacheUtil.setSessionUnreadCount(userId, Long.parseLong(sessionIdObj.toString()), Integer.parseInt(unreadCountObj.toString()));
		// 同步更新数据库会话未读数
		DialogSession dialogSession = new DialogSession();
		dialogSession.setId(Long.parseLong(sessionIdObj.toString()));
		dialogSession.setUnreadCount(Integer.parseInt(unreadCountObj.toString()));
		sessionMapper.updateById(dialogSession);
	}

	/**
	 * 处理用户输入状态更新
	 * @param userId 用户ID
	 * @param msgMap 消息数据
	 */
	private void handleTypingStatus(Long userId, Map<String, Object> msgMap) {
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		Object sessionIdObj = jsonData.get("sessionId");
		Object isTypingObj = jsonData.get("isTyping");
		if (ObjectUtil.isNull(sessionIdObj)) {
			log.error("处理输入状态失败 | 缺少必要参数 (用户ID: {})", userId);
			return;
		}
		if (ObjectUtil.isNull(isTypingObj)) {
			log.error("处理输入状态失败 | 缺少必要参数 (用户ID: {})", userId);
			return;
		}
		long sessionId = Long.parseLong(sessionIdObj.toString());
		redisCacheUtil.set(RedisCacheUtil.DIALOG_TYPING_STATUS_PREFIX + sessionId + ":" + userId,
				isTypingObj.toString(), 10, TimeUnit.SECONDS);
		// TODO 推送给对方用户
	}

	/**
	 * 处理离开会话事件
	 * @param userId 用户ID
	 * @param msgMap 消息数据
	 */
	private void handleConversationLeave(Long userId, Map<String, Object> msgMap) {
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		Object sessionIdObj = jsonData.get("sessionId");
		if (ObjectUtil.isNull(sessionIdObj)) {
			log.error("处理离开会话失败 | 缺少必要参数 (用户ID: {})", userId);
			return;
		}
		Long sessionId = Long.valueOf(sessionIdObj.toString());
		redisCacheUtil.deleteSessionCache(userId, sessionId);
	}

	/**
	 * 处理进入会话事件
	 * @param userId 用户ID
	 * @param msgMap 消息数据
	 */
	private void handleConversationEnter(Long userId, Map<String, Object> msgMap) {
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		Object sessionIdObj = jsonData.get("sessionId");
		if (ObjectUtil.isNull(sessionIdObj)) {
			log.warn("处理进入会话失败 | 缺少必要参数 (用户ID: {})", userId);
			return;
		}
		Long sessionId = Long.valueOf(sessionIdObj.toString());
		redisCacheUtil.addSessionToListCache(userId, sessionId, 0, 30 * 60);
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
		log.error("WebSocket传输错误 | 用户ID: {}, 会话ID: {}, 错误信息: {}",
				userId, session.getId(), exception.getMessage(), exception);

		// 异常时关闭会话（触发afterConnectionClosed回调，清理在线状态）
		if (session.isOpen()) {
			session.close(CloseStatus.SERVER_ERROR.withReason("连接传输异常"));
		}
	}

	// ========================= 消息发送工具方法 =========================
	/**
	 * 向指定用户推送私信消息（实时对话核心方法）
	 * @param userId    目标用户ID
	 * @param messageVO 消息VO（包含消息ID、内容、发送者等信息）
	 * @param messagePushType 消息推送类型
	 */
	public void pushPrivateMessage(Long userId, MessageVO messageVO, MessagePushType messagePushType) {
		if (ObjectUtil.isNull(userId) || ObjectUtil.isNull(messageVO)) {
			log.error("推送私信失败 | 用户ID或消息内容为空");
			return;
		}

		// 1. 获取用户在线会话
		log.info("推送私信-在线用户会话映射: {}", JSON.toJSONString(onlineUserSessionMap.keySet()));
		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (!isUserOnline(userId)) {
			log.info("推送私信失败 | 用户已离线 (用户ID: {})", userId);
			// 离线：通过MQ发送系统通知（调用notification-service）
			String senderName = getSenderName(messageVO.getSenderId());
			dialogEventPublisher.sendOfflineNotification(messageVO, userId, senderName);
			return;
		}

		// 2. 构建推送消息（JSON格式，包含消息类型和数据）
		Map<String, Object> pushMsg = new ConcurrentHashMap<>(2);
		pushMsg.put("msgType", messagePushType.getName());
		pushMsg.put("data", messageVO);
		String jsonMsg = JSON.toJSONString(pushMsg);

		// 3. 发送消息（带异常处理）
		try {
			session.sendMessage(new TextMessage(jsonMsg));
			log.info("私信推送成功 | 用户ID: {}, 消息ID: {}", userId, messageVO.getId());
		} catch (IOException e) {
			log.error("私信推送失败 | 用户ID: {}, 消息ID: {}, 错误信息: {}",
					userId, messageVO.getId(), e.getMessage(), e);
			// 推送失败时关闭会话（后续消息走离线推送）
			if (session.isOpen()) {
				try {
					session.close(CloseStatus.SERVER_ERROR.withReason("消息推送失败，连接已断开"));
				} catch (IOException ex) {
					log.error("关闭会话失败 | 用户ID: {}, 会话ID: {}", userId, session.getId(), ex);
				}
			}
		}
	}
	/**
	 * 获取发送者名称（AI/用户）
	 * @param senderId 发送者ID
	 * @return 发送者名称（AI助手/用户昵称）
	 */
	public String getSenderName(Long senderId) {
		// AI助手固定名称
		if (Objects.equals(senderId, AI_SENDER_ID)) {
			return "AI助手";
		}

		// 用户名称：调用user-service查询
		Result<List<UserInfo>> userResult = userFeignClient.getUsersByIds(Collections.singleton(senderId));
		if (userResult.isSuccess() && userResult.getData() != null && !userResult.getData().isEmpty()) {
			UserInfo userInfo = userResult.getData().get(0);
			return userInfo.getNickname();
		} else {
			return "未知用户";
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
			log.error("推送系统通知失败 | 参数缺失 (用户ID: {}, 通知类型: {})", userId, notifyType);
			return;
		}

		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (ObjectUtil.isNull(session) || !session.isOpen()) {
			log.info("推送系统通知失败 | 用户已离线 (用户ID: {})", userId);
			return;
		}

		// 构建系统通知消息格式
		Map<String, Object> notifyMsg = new ConcurrentHashMap<>(3);
		notifyMsg.put("msgType", "SYSTEM_NOTIFY");
		notifyMsg.put("notifyType", notifyType);
		notifyMsg.put("content", notifyContent);
		String jsonMsg = JSON.toJSONString(notifyMsg);

		try {
			session.sendMessage(new TextMessage(jsonMsg));
			log.info("系统通知推送成功 | 用户ID: {}, 通知类型: {}", userId, notifyType);
		} catch (IOException e) {
			log.error("系统通知推送失败 | 用户ID: {}, 通知类型: {}, 错误信息: {}",
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
			log.error("发送错误消息失败 | 会话已关闭");
			return;
		}

		Map<String, Object> errorMsgMap = new ConcurrentHashMap<>(2);
		errorMsgMap.put("msgType", "ERROR");
		errorMsgMap.put("content", errorMsg);
		String jsonMsg = JSON.toJSONString(errorMsgMap);

		try {
			session.sendMessage(new TextMessage(jsonMsg));
			log.info("错误消息已发送 | 会话ID: {}, 错误内容: {}", session.getId(), errorMsg);
		} catch (IOException e) {
			log.error("发送错误消息失败 | 会话ID: {}, 错误内容: {}, 异常信息: {}",
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
		// 获取该用户的所有为targetId对话
		List<DialogSession> sessions = sessionMapper.selectList(
				new LambdaQueryWrapper<>(DialogSession.class).eq(DialogSession::getTargetId, userId)
		);
		try {
			session.sendMessage(new TextMessage(jsonResp));
			for (DialogSession dialogSession : sessions) {
				JSONObject data = new JSONObject();
				data.put("sessionId", dialogSession.getId());
				data.put("sessionType", dialogSession.getSessionType().getValue());
				pushMessage(dialogSession.getUserId(), MessagePushType.USER_ONLINE, data);
			}
			log.info("用户上线消息已推送 | 用户ID: {}", userId);
			log.info("心跳处理成功 | 用户ID: {}, 会话ID: {}", userId, session.getId());
		} catch (IOException e) {
			for (DialogSession dialogSession : sessions) {
				JSONObject data = new JSONObject();
				data.put("sessionId", dialogSession.getId());
				data.put("sessionType", dialogSession.getSessionType().getValue());
				pushMessage(dialogSession.getUserId(), MessagePushType.USER_OFFLINE, data);
			}
			log.info("用户下线消息已推送 | 用户ID: {}", userId);
			log.error("心跳响应发送失败 | 用户ID: {}, 会话ID: {}, 错误信息: {}",
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
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		// 1. 解析消息中的会话ID和最后已读消息ID
		Object sessionIdObj = jsonData.get("sessionId");
		Object sendIdObj = jsonData.get("sendId");
		Object msgId = jsonData.get("msgId");
		log.info("处理消息已读确认 | 发送方ID: {}, 会话ID: {}, 消息ID: {}", sendIdObj, sessionIdObj, msgId);

		Long sessionId = null;
		Long sendId = null;
		try {
			sessionId = Long.parseLong(sessionIdObj.toString());
			sendId = Long.parseLong(sendIdObj.toString());
		} catch (NumberFormatException e) {
			log.error("处理消息已读确认失败 | 会话ID格式错误 (用户ID: {}, 会话ID: {})", userId, sessionIdObj, e);
			return;
		}

		// 2. 清除Redis中该会话的未读消息数（原子操作）
		String unreadCountKey = RedisCacheUtil.DIALOG_UNREAD_COUNT_PREFIX + userId;
		redisCacheUtil.hSet(unreadCountKey, sessionId.toString(), 0);
		// 刷新未读消息数缓存过期时间（7天）
		redisCacheUtil.expire(unreadCountKey, 7, TimeUnit.DAYS);

		log.info("消息已读确认处理成功 | 用户ID: {}, 会话ID: {}, 最后已读消息ID: {}", userId, sessionId, msgId);
		JSONObject readData = new JSONObject();
		List<String> msgIds = new ArrayList<>();
		msgIds.add(msgId.toString());
		readData.put("sessionId", sessionId);
		readData.put("msgIds", msgIds);
		readData.put("readTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

		// 3. WebSocket推送已读状态
		pushMessage(sendId, MessagePushType.MSG_READ, readData);

		// 4. 更新数据库中消息的已读状态
		readConfirmMarkMessages(sessionId, userId, msgIds);
	}

	/**
	 * 标记消息为已读状态（MongoDB操作）
	 * @param sessionId 会话ID
	 * @param receiverId 接收者ID
	 * @param msgIds 消息ID列表
	 */
	public void readConfirmMarkMessages(Long sessionId, Long receiverId, List<String> msgIds) {
		log.info("标记消息为已读 | 会话ID: {}, 接收者ID: {}, 消息ID列表: {}", sessionId, receiverId, msgIds);
		Criteria criteria = Criteria.where("sessionId").is(sessionId)
				.and("receiverId").is(receiverId)
				.and("status").is(DialogMessage.MessageStatusEnum.SENT.getValue());

		// 若msgIds不为空，添加消息ID条件
		if (!msgIds.isEmpty()) {
			criteria.and("_id").in(msgIds);
		}

		Query query = Query.query(criteria);
		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.READ.getValue())
				.set("readTime", LocalDateTime.now())
				.set("updatedAt", LocalDateTime.now());
		mongoTemplate.updateMulti(query, update, DialogMessage.class);
		log.info("消息标记为已读成功 | 会话ID: {}, 接收者ID: {}, 消息ID列表: {}", sessionId, receiverId, msgIds);
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

		// 1. 先查内存映射（性能优先）
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
		if (ObjectUtil.isNull(senderId) || ObjectUtil.isNull(readData)) {
			log.error("推送消息失败 | 参数缺失 (发送者ID: {})", senderId);
			return;
		}

		WebSocketSession session = onlineUserSessionMap.get(senderId);
		if (ObjectUtil.isNull(session) || !session.isOpen()) {
			log.debug("推送消息失败 | 用户已离线 (用户ID: {})", senderId);
			return;
		}

		// 构建推送消息（JSON格式）
		Map<String, Object> pushMsg = new ConcurrentHashMap<>(2);
		pushMsg.put("msgType", messagePushType.getName());
		pushMsg.put("data", readData);
		String jsonMsg = JSON.toJSONString(pushMsg);

		try {
			session.sendMessage(new TextMessage(jsonMsg));
			log.info("消息状态推送成功 | 发送者ID: {}, 消息类型: {}, 已读数据: {}",
					senderId, messagePushType.getName(), readData);
		} catch (IOException e) {
			log.error("消息状态推送失败 | 发送者ID: {}, 消息类型: {}, 已读数据: {}, 错误信息: {}",
					senderId, messagePushType.getName(), readData, e.getMessage(), e);
			if (session.isOpen()) {
				try {
					session.close(CloseStatus.SERVER_ERROR.withReason("消息推送失败，连接已断开"));
				} catch (IOException ex) {
					log.error("关闭会话失败 | 用户ID: {}, 会话ID: {}", senderId, session.getId(), ex);
				}
			}
		}
	}
}