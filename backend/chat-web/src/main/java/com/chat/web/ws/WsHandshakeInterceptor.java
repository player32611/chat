package com.chat.web.ws;

import com.chat.common.constant.RedisKeyConstant;
import com.chat.web.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器：从 query 参数 token 鉴权，解析 userId 写入 attributes。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsHandshakeInterceptor implements HandshakeInterceptor {

	private static final String ATTR_USER_ID = "userId";

	private final JwtTokenProvider tokenProvider;
	private final StringRedisTemplate redisTemplate;

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Map<String, Object> attributes) {
		if (!(request instanceof ServletServerHttpRequest servletRequest)) {
			return false;
		}
		String token = servletRequest.getServletRequest().getParameter("token");
		if (token == null || token.isBlank()) {
			return false;
		}
		try {
			Claims claims = tokenProvider.parse(token);
			String jti = claims.getId();
			if (jti != null && Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyConstant.JWT_BLACKLIST + jti))) {
				return false;
			}
			attributes.put(ATTR_USER_ID, Long.valueOf(claims.getSubject()));
			return true;
		} catch (Exception e) {
			log.warn("WS 握手鉴权失败");
			return false;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler wsHandler, Exception exception) {
		// 无需处理
	}
}
