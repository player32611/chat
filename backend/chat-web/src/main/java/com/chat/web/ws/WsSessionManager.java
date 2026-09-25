package com.chat.web.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理：维护 用户 ID ↔ 会话 映射，并提供推送能力。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsSessionManager {

	private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper;

	public void register(Long userId, WebSocketSession session) {
		sessions.put(userId, session);
	}

	public void unregister(Long userId) {
		sessions.remove(userId);
	}

	public boolean isOnline(Long userId) {
		return sessions.containsKey(userId);
	}

	public void sendToUser(Long userId, Object payload) {
		WebSocketSession session = sessions.get(userId);
		if (session == null || !session.isOpen()) {
			return;
		}
		try {
			session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
		} catch (Exception e) {
			log.warn("WS 推送失败 userId={}: {}", userId, e.getMessage());
		}
	}
}
