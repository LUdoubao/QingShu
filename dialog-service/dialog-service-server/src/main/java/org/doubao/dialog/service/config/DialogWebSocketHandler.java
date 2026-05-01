package org.doubao.dialog.service.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.doubao.dialog.service.entity.DialogMessage;
import org.doubao.dialog.service.entity.DialogSession;
import org.doubao.dialog.service.enums.MessagePushType;
import org.doubao.dialog.service.feign.UserFeignClient;
import org.doubao.dialog.service.mapper.DialogSessionMapper;
import org.doubao.dialog.service.messaging.DialogEventPublisher;
import org.doubao.dialog.service.util.RedisCacheUtil;
import org.doubao.dialog.service.vo.MessageVO;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
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

@Component
public class DialogWebSocketHandler extends TextWebSocketHandler {

	private static final String SESSION_ATTR_USER_ID = "userId";
	private final Map<Long, WebSocketSession> onlineUserSessionMap = new ConcurrentHashMap<>(128);

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

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Object object = session.getAttributes().get(SESSION_ATTR_USER_ID);
		Long userId = Long.valueOf(String.valueOf(object));

		WebSocketSession oldSession = onlineUserSessionMap.put(userId, session);
		if (oldSession != null && oldSession.isOpen()) {
			oldSession.close(CloseStatus.NORMAL.withReason("账号在其他设备登录，当前连接已断开"));
		}

		String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
		redisCacheUtil.set(onlineKey, session.getId(), 30, TimeUnit.MINUTES);

		List<DialogSession> sessions = sessionMapper.selectList(
				new LambdaQueryWrapper<DialogSession>()
						.eq(DialogSession::getTargetId, userId)
						.eq(DialogSession::getSessionType, "USER")
						.eq(DialogSession::getDeleted, 0)
		);
		for (DialogSession dialogSession : sessions) {
			JSONObject data = new JSONObject();
			data.put("sessionId", dialogSession.getId());
			data.put("sessionType", dialogSession.getSessionType().getValue());
			pushMessage(dialogSession.getUserId(), MessagePushType.USER_ONLINE, data);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Object object = session.getAttributes().get(SESSION_ATTR_USER_ID);
		if (object == null) {
			return;
		}
		Long userId = Long.valueOf(String.valueOf(object));

		WebSocketSession storedSession = onlineUserSessionMap.get(userId);
		if (storedSession != null && storedSession.getId().equals(session.getId())) {
			onlineUserSessionMap.remove(userId);
			String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
			redisCacheUtil.expire(onlineKey, 10, TimeUnit.SECONDS);
		}

		List<DialogSession> sessions = sessionMapper.selectList(
				new LambdaQueryWrapper<DialogSession>()
						.eq(DialogSession::getTargetId, userId)
						.eq(DialogSession::getSessionType, "USER")
						.eq(DialogSession::getDeleted, 0)
		);
		for (DialogSession dialogSession : sessions) {
			JSONObject data = new JSONObject();
			data.put("sessionId", dialogSession.getId());
			data.put("sessionType", dialogSession.getSessionType().getValue());
			pushMessage(dialogSession.getUserId(), MessagePushType.USER_OFFLINE, data);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		Object object = session.getAttributes().get(SESSION_ATTR_USER_ID);
		Long userId = Long.valueOf(String.valueOf(object));
		String msgContent = message.getPayload();
		if (msgContent.isEmpty()) {
			sendErrorMessage(session, "消息内容不能为空");
			return;
		}

		Map<String, Object> msgMap;
		try {
			msgMap = JSON.parseObject(msgContent, new TypeReference<Map<String, Object>>() {});
		} catch (Exception e) {
			sendErrorMessage(session, "消息格式错误，必须为JSON字符串");
			return;
		}

		String msgType = (String) msgMap.get("msgType");
		if (msgType == null || msgType.isEmpty()) {
			sendErrorMessage(session, "消息缺少必要字段：msgType");
			return;
		}

		switch (msgType) {
			case "HEARTBEAT":
				handleHeartbeat(userId, session);
				break;
			case "MSG_READ_CONFIRM":
				handleMsgReadConfirm(userId, msgMap);
				break;
			case "CONVERSATION_ENTER":
				handleConversationEnter(userId, msgMap);
				break;
			case "CONVERSATION_LEAVE":
				handleConversationLeave(userId, msgMap);
				break;
			case "TYPING_STATUS":
				handleTypingStatus(userId, msgMap);
				break;
			case "UNREAD_COUNT_CHANGE":
				Object data = msgMap.getOrDefault("data", new HashMap<>());
				JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
				handleUnreadCountChange(userId, jsonData.get("sessionId"), jsonData.get("unreadCount"));
				break;
			default:
				sendErrorMessage(session, "不支持的消息类型：" + msgType);
		}
	}

	public void handleUnreadCountChange(Long userId, Object sessionIdObj, Object unreadCountObj) {
		if (sessionIdObj == null || unreadCountObj == null) {
			return;
		}
		Long sessionId = Long.parseLong(sessionIdObj.toString());
		int unreadCount = Integer.parseInt(unreadCountObj.toString());
		redisCacheUtil.setSessionUnreadCount(userId, sessionId, unreadCount);
		DialogSession dialogSession = new DialogSession();
		dialogSession.setId(sessionId);
		dialogSession.setUnreadCount(unreadCount);
		sessionMapper.updateById(dialogSession);
	}

	private void handleTypingStatus(Long userId, Map<String, Object> msgMap) {
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		Object sessionIdObj = jsonData.get("sessionId");
		Object isTypingObj = jsonData.get("isTyping");
		if (sessionIdObj == null || isTypingObj == null) {
			return;
		}
		long sessionId = Long.parseLong(sessionIdObj.toString());
		redisCacheUtil.set(RedisCacheUtil.DIALOG_TYPING_STATUS_PREFIX + sessionId + ":" + userId,
				isTypingObj.toString(), 10, TimeUnit.SECONDS);
	}

	private void handleConversationLeave(Long userId, Map<String, Object> msgMap) {
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		Object sessionIdObj = jsonData.get("sessionId");
		if (sessionIdObj == null) {
			return;
		}
		Long sessionId = Long.valueOf(sessionIdObj.toString());
		redisCacheUtil.deleteSessionCache(userId, sessionId);
	}

	private void handleConversationEnter(Long userId, Map<String, Object> msgMap) {
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		Object sessionIdObj = jsonData.get("sessionId");
		if (sessionIdObj == null) {
			return;
		}
		Long sessionId = Long.valueOf(sessionIdObj.toString());
		redisCacheUtil.addSessionToListCache(userId, sessionId, 0, 30 * 60);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (session.isOpen()) {
			session.close(CloseStatus.SERVER_ERROR.withReason("连接传输异常"));
		}
	}

	public void pushPrivateMessage(Long userId, MessageVO messageVO, MessagePushType messagePushType) {
		if (userId == null || messageVO == null) {
			return;
		}
		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (!isUserOnline(userId)) {
			String senderName = getSenderName(messageVO.getSenderId());
			dialogEventPublisher.sendOfflineNotification(messageVO, userId, senderName);
			return;
		}

		Map<String, Object> pushMsg = new ConcurrentHashMap<>(2);
		pushMsg.put("msgType", messagePushType.getName());
		pushMsg.put("data", messageVO);
		String jsonMsg = JSON.toJSONString(pushMsg);
		try {
			session.sendMessage(new TextMessage(jsonMsg));
		} catch (IOException e) {
			if (session.isOpen()) {
				try {
					session.close(CloseStatus.SERVER_ERROR.withReason("消息推送失败，连接已断开"));
				} catch (IOException ignored) {
				}
			}
		}
	}

	public void sendMsgSuccess(Long userId, String msgId, String tmpId) {
		if (userId == null || msgId == null) {
			return;
		}
		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (!isUserOnline(userId)) {
			return;
		}

		Map<String, Object> pushMsg = new ConcurrentHashMap<>(2);
		pushMsg.put("msgType", MessagePushType.SENT.getName());
		pushMsg.put("msgId", msgId);
		pushMsg.put("tmpId", tmpId);
		try {
			session.sendMessage(new TextMessage(JSON.toJSONString(pushMsg)));
		} catch (IOException e) {
			log.error("send msg success event failed", e);
		}
	}

	public String getSenderName(Long senderId) {
		Result<List<UserInfoDes>> userResult = userFeignClient.getUsersByIds(Collections.singleton(senderId));
		if (userResult.isSuccess() && userResult.getData() != null && !userResult.getData().isEmpty()) {
			return userResult.getData().get(0).getNickname();
		}
		return "未知用户";
	}

	public void pushSystemNotify(Long userId, String notifyContent, String notifyType) {
		if (userId == null || StringUtils.isBlank(notifyContent) || StringUtils.isBlank(notifyType)) {
			return;
		}
		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (session == null || !session.isOpen()) {
			return;
		}
		Map<String, Object> notifyMsg = new ConcurrentHashMap<>(3);
		notifyMsg.put("msgType", "SYSTEM_NOTIFY");
		notifyMsg.put("notifyType", notifyType);
		notifyMsg.put("content", notifyContent);
		try {
			session.sendMessage(new TextMessage(JSON.toJSONString(notifyMsg)));
		} catch (IOException e) {
			log.error("push system notify failed", e);
		}
	}

	private void sendErrorMessage(WebSocketSession session, String errorMsg) {
		if (session == null || !session.isOpen()) {
			return;
		}
		Map<String, Object> errorMsgMap = new ConcurrentHashMap<>(2);
		errorMsgMap.put("msgType", "ERROR");
		errorMsgMap.put("content", errorMsg);
		try {
			session.sendMessage(new TextMessage(JSON.toJSONString(errorMsgMap)));
		} catch (IOException e) {
			log.error("send error message failed", e);
		}
	}

	private void handleHeartbeat(Long userId, WebSocketSession session) {
		String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
		redisCacheUtil.expire(onlineKey, 30, TimeUnit.MINUTES);

		Map<String, Object> heartbeatResp = new ConcurrentHashMap<>(2);
		heartbeatResp.put("msgType", "HEARTBEAT_RESP");
		heartbeatResp.put("timestamp", System.currentTimeMillis());

		List<DialogSession> sessions = sessionMapper.selectList(
				new LambdaQueryWrapper<DialogSession>()
						.eq(DialogSession::getTargetId, userId)
						.eq(DialogSession::getSessionType, "USER")
						.eq(DialogSession::getDeleted, 0)
		);
		try {
			session.sendMessage(new TextMessage(JSON.toJSONString(heartbeatResp)));
			for (DialogSession dialogSession : sessions) {
				JSONObject data = new JSONObject();
				data.put("sessionId", dialogSession.getId());
				data.put("sessionType", dialogSession.getSessionType().getValue());
				pushMessage(dialogSession.getUserId(), MessagePushType.USER_ONLINE, data);
			}
		} catch (IOException e) {
			for (DialogSession dialogSession : sessions) {
				JSONObject data = new JSONObject();
				data.put("sessionId", dialogSession.getId());
				data.put("sessionType", dialogSession.getSessionType().getValue());
				pushMessage(dialogSession.getUserId(), MessagePushType.USER_OFFLINE, data);
			}
		}
	}

	private void handleMsgReadConfirm(Long userId, Map<String, Object> msgMap) {
		Object data = msgMap.getOrDefault("data", new HashMap<>());
		JSONObject jsonData = JSON.parseObject(JSON.toJSONString(data));
		Object sessionIdObj = jsonData.get("sessionId");
		Object sendIdObj = jsonData.get("sendId");
		Object msgId = jsonData.get("msgId");
		if (sessionIdObj == null || sendIdObj == null || msgId == null) {
			return;
		}

		Long sessionId = Long.parseLong(sessionIdObj.toString());
		Long sendId = Long.parseLong(sendIdObj.toString());
		String unreadCountKey = RedisCacheUtil.DIALOG_UNREAD_COUNT_PREFIX + userId;
		redisCacheUtil.hSet(unreadCountKey, sessionId.toString(), 0);
		redisCacheUtil.expire(unreadCountKey, 7, TimeUnit.DAYS);

		JSONObject readData = new JSONObject();
		List<String> msgIds = new ArrayList<>();
		msgIds.add(msgId.toString());
		readData.put("sessionId", sessionId);
		readData.put("msgIds", msgIds);
		readData.put("readTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		pushMessage(sendId, MessagePushType.MSG_READ, readData);

		readConfirmMarkMessages(sessionId, userId, msgIds);
	}

	public void readConfirmMarkMessages(Long sessionId, Long receiverId, List<String> msgIds) {
		Criteria criteria = Criteria.where("sessionId").is(sessionId)
				.and("receiverId").is(receiverId)
				.and("status").is(DialogMessage.MessageStatusEnum.SENT.getValue());
		if (!msgIds.isEmpty()) {
			criteria.and("_id").in(msgIds);
		}

		Query query = Query.query(criteria);
		Update update = new Update();
		update.set("status", DialogMessage.MessageStatusEnum.READ.getValue())
				.set("readTime", LocalDateTime.now())
				.set("updatedAt", LocalDateTime.now());
		mongoTemplate.updateMulti(query, update, DialogMessage.class);
	}

	public boolean isUserOnline(Long userId) {
		if (userId == null) {
			return false;
		}
		WebSocketSession session = onlineUserSessionMap.get(userId);
		if (session != null && session.isOpen()) {
			return true;
		}
		String onlineKey = RedisCacheUtil.DIALOG_USER_ONLINE_PREFIX + userId;
		String sessionId = redisCacheUtil.getString(onlineKey, String.class);
		return sessionId != null;
	}

	public int getOnlineUserCount() {
		return onlineUserSessionMap.size();
	}

	public void pushMessage(Long senderId, MessagePushType messagePushType, JSONObject readData) {
		if (senderId == null || readData == null) {
			return;
		}
		WebSocketSession session = onlineUserSessionMap.get(senderId);
		if (session == null || !session.isOpen()) {
			return;
		}
		Map<String, Object> pushMsg = new ConcurrentHashMap<>(2);
		pushMsg.put("msgType", messagePushType.getName());
		pushMsg.put("data", readData);
		try {
			session.sendMessage(new TextMessage(JSON.toJSONString(pushMsg)));
		} catch (IOException e) {
			if (session.isOpen()) {
				try {
					session.close(CloseStatus.SERVER_ERROR.withReason("消息推送失败，连接已断开"));
				} catch (IOException ignored) {
				}
			}
		}
	}
}
