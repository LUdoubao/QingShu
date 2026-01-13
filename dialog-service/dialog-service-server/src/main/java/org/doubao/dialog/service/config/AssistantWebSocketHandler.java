package org.doubao.dialog.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.doubao.dialog.service.dto.MessageRequest;
import org.doubao.dialog.service.service.MessageService;
import org.doubao.dialog.service.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class AssistantWebSocketHandler extends TextWebSocketHandler {
	private static final Logger log = LoggerFactory.getLogger(AssistantWebSocketHandler.class);

	private final WebSocketService webSocketService;
	private final MessageService messageService;
	private final ObjectMapper objectMapper;

	public AssistantWebSocketHandler(WebSocketService webSocketService,
									 MessageService messageService,
									 ObjectMapper objectMapper) {
		this.webSocketService = webSocketService;
		this.messageService = messageService;
		this.objectMapper = objectMapper;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		String path = Objects.requireNonNull(session.getUri()).getPath();
		String query = Objects.requireNonNull(session.getUri()).getQuery();
		if (path.contains("/user")) {
			log.info("用户端 WebSocket 连接已建立：{}",path + query);
			String userId = extractUserId(query);
			webSocketService.addUserSession((!Objects.equals(userId, "") && userId != null) ? Long.parseLong(userId) : 10000L, session);
			log.info("用户 {} 的 WebSocket 连接已建立，当前用户连接数: {}", userId, webSocketService.userSessionCount());

			// 发送连接确认消息
			// sendConnectionAck(session, "user");
		} else if (path.contains("/admin")) {
			log.info("管理员端 WebSocket 连接已建立：{}",path  + query);
			String userId = extractUserId(query);
			webSocketService.addAdminSession((!Objects.equals(userId, "") && userId != null) ? Long.parseLong(userId) : 10000L, session);
			log.info("管理员 {} 的 WebSocket 连接已建立，当前管理员连接数: {}", userId, webSocketService.adminSessionCount());

			// 发送连接确认消息
			// sendConnectionAck(session, "admin");
		}
	}
	public static String extractUserId(String query) {
		try {
			if (query == null || !query.contains("userId=")) {
				throw new IllegalArgumentException("URL中缺少userId参数");
			}

			// 分割参数并解码（处理特殊字符如%20）
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				if (pair.startsWith("userId=")) {
					return URLDecoder.decode(
							pair.substring("userId=".length()), StandardCharsets.UTF_8.name()
					);
				}
			}
			throw new IllegalArgumentException("未找到userId参数");
		} catch (Exception e) {
			log.info("解析用户ID失败", e);
			return null;
		}

	}
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		String payload = message.getPayload();
		log.info("收到 WebSocket 消息: {}", payload);

		try {
			MessageRequest messageObj = objectMapper.readValue(payload, MessageRequest.class);
			String path = Objects.requireNonNull(session.getUri()).getPath();

			log.info("处理消息的路径: {}", path);
			messageService.handleUserMessage(messageObj);
		} catch (Exception e) {
			log.error("处理 WebSocket 消息失败", e);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		String path = Objects.requireNonNull(session.getUri()).getPath();
		String query = Objects.requireNonNull(session.getUri()).getQuery();
		log.info("WebSocket 连接正在关闭：{}", path + query);
		if (path.contains("/user")) {
			// 用户端断开
			String userId = extractUserId(query);
			webSocketService.removeUserSession((!Objects.equals(userId, "") && userId != null) ? Long.parseLong(userId) : 10000L);
			log.info("用户 {} 的 WebSocket 连接已关闭，当前用户连接数: {}", userId, webSocketService.userSessionCount());
		} else if (path.contains("/admin")) {
			// 管理员端断开
			String userId = extractUserId(path);
			if (userId == null || userId.isEmpty()) {
				log.error("管理员ID为空:{}", path);
				return;
			}
			webSocketService.removeUserSession(Long.parseLong(userId));
			log.info("管理员 {} 的 WebSocket 连接已关闭，当前管理员连接数: {}", userId, webSocketService.adminSessionCount());
		}
	}
}