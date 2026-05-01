package org.doubao.dialog.service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final DialogWebSocketHandler dialogWebSocketHandler;
	private final WebSocketAuthInterceptor webSocketAuthInterceptor;

	public WebSocketConfig(DialogWebSocketHandler dialogWebSocketHandler, WebSocketAuthInterceptor webSocketAuthInterceptor) {
		this.dialogWebSocketHandler = dialogWebSocketHandler;
		this.webSocketAuthInterceptor = webSocketAuthInterceptor;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(dialogWebSocketHandler, "/dialog/ws/chat")
				.setAllowedOrigins("*")
				.addInterceptors(webSocketAuthInterceptor);
	}
}
