package org.doubao.dialog.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final AssistantWebSocketHandler webSocketHandler;

	public WebSocketConfig(AssistantWebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// 用户端WebSocket
		registry.addHandler(webSocketHandler, "/dialog/ws/user")
				.setAllowedOrigins("*");

		// 管理员端WebSocket
		registry.addHandler(webSocketHandler, "/dialog/ws/admin")
				.setAllowedOrigins("*");
	}
}