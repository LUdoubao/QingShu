package org.doubao.dialog.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final AssistantWebSocketHandler webSocketHandler;
	private final DialogWebSocketHandler dialogWebSocketHandler;
	private final WebSocketAuthInterceptor webSocketAuthInterceptor;

	public WebSocketConfig(AssistantWebSocketHandler webSocketHandler, DialogWebSocketHandler dialogWebSocketHandler, WebSocketAuthInterceptor webSocketAuthInterceptor) {
		this.webSocketHandler = webSocketHandler;
		this.dialogWebSocketHandler = dialogWebSocketHandler;
		this.webSocketAuthInterceptor = webSocketAuthInterceptor;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// 用户端WebSocket
		registry.addHandler(webSocketHandler, "/dialog/ws/user")
				.setAllowedOrigins("*");

		// 管理员端WebSocket
		registry.addHandler(webSocketHandler, "/dialog/ws/admin")
				.setAllowedOrigins("*");

		registry.addHandler(dialogWebSocketHandler, "/dialog/ws/chat")
				.setAllowedOrigins("*")
				.addInterceptors(webSocketAuthInterceptor);
	}
}