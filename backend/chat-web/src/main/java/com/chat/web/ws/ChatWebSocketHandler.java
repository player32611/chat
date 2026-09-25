package com.chat.web.ws;

import com.chat.common.exception.BusinessException;
import com.chat.model.vo.FriendVO;
import com.chat.model.vo.MessageVO;
import com.chat.service.ConversationService;
import com.chat.service.FriendService;
import com.chat.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 聊天 WebSocket 处理器：处理 chat / read / ping 消息。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private static final String ATTR_USER_ID = "userId";

	private final WsSessionManager sessionManager;
	private final ObjectMapper objectMapper;
	private final MessageService messageService;
	private final ConversationService conversationService;
	private final FriendService friendService;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		Long userId = userId(session);
		if (userId != null) {
			sessionManager.register(userId, session);
			broadcastPresence(userId, true);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		Long userId = userId(session);
		if (userId == null) {
			return;
		}
		try {
			JsonNode node = objectMapper.readTree(message.getPayload());
			String type = node.path("type").asText();
			if ("chat".equals(type)) {
				handleChat(userId, node.path("data"));
			} else if ("read".equals(type)) {
				handleRead(userId, node.path("data"));
			} else if ("ping".equals(type)) {
				sessionManager.sendToUser(userId, Map.of("type", "pong"));
			}
		} catch (Exception e) {
			log.warn("WS 消息处理失败 userId={}: {}", userId, e.getMessage());
		}
	}

	private void handleChat(Long senderId, JsonNode data) {
		long conversationId = data.path("conversationId").asLong();
		if (conversationId <= 0) {
			return;
		}
		int messageType = data.path("messageType").asInt();
		String content = data.path("content").asText();
		try {
			MessageVO vo = messageService.sendMessage(senderId, conversationId, messageType, content);
			Map<String, Object> payload = Map.of("type", "message", "data", vo);
			for (Long memberId : conversationService.listMemberIds(conversationId)) {
				sessionManager.sendToUser(memberId, payload);
			}
		} catch (BusinessException e) {
			sessionManager.sendToUser(senderId, Map.of("type", "error", "data", Map.of("msg", e.getMessage())));
		}
	}

	private void handleRead(Long userId, JsonNode data) {
		long conversationId = data.path("conversationId").asLong();
		if (conversationId <= 0) {
			return;
		}
		Long lastReadId = conversationService.markRead(userId, conversationId);
		Map<String, Object> readData = Map.of(
				"conversationId", conversationId,
				"userId", userId,
				"lastReadMessageId", lastReadId == null ? 0L : lastReadId);
		Map<String, Object> payload = Map.of("type", "read", "data", readData);
		for (Long memberId : conversationService.listMemberIds(conversationId)) {
			if (!memberId.equals(userId)) {
				sessionManager.sendToUser(memberId, payload);
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		Long userId = userId(session);
		if (userId != null) {
			sessionManager.unregister(userId);
			broadcastPresence(userId, false);
		}
	}

	private Long userId(WebSocketSession session) {
		return (Long) session.getAttributes().get(ATTR_USER_ID);
	}

	/**
	 * 向该用户的所有好友广播在线/离线状态。
	 */
	private void broadcastPresence(Long userId, boolean online) {
		Map<String, Object> payload = Map.of(
				"type", "presence",
				"data", Map.of("userId", userId, "online", online));
		try {
			for (FriendVO friend : friendService.listFriends(userId)) {
				sessionManager.sendToUser(friend.getFriendId(), payload);
			}
		} catch (Exception e) {
			log.warn("广播在线状态失败 userId={}: {}", userId, e.getMessage());
		}
	}
}
