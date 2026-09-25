package com.chat.web.config;

import com.chat.web.ws.ChatWebSocketHandler;
import com.chat.web.ws.WsHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	private final ChatWebSocketHandler chatWebSocketHandler;
	private final WsHandshakeInterceptor wsHandshakeInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(chatWebSocketHandler, "/ws")
				.addInterceptors(wsHandshakeInterceptor)
				.setAllowedOrigins("*");
	}
}
