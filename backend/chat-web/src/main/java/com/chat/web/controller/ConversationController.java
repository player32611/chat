package com.chat.web.controller;

import com.chat.common.result.Result;
import com.chat.model.dto.conversation.SingleConversationDTO;
import com.chat.model.vo.ConversationVO;
import com.chat.model.vo.MessageVO;
import com.chat.service.ConversationService;
import com.chat.web.security.UserContext;
import com.chat.web.ws.WsSessionManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 会话接口。
 */
@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {

	private final ConversationService conversationService;
	private final WsSessionManager wsSessionManager;

	@GetMapping("/list")
	public Result<List<ConversationVO>> list() {
		return Result.success(conversationService.listConversations(UserContext.getUserId()));
	}

	@PostMapping("/single")
	public Result<ConversationVO> single(@Valid @RequestBody SingleConversationDTO dto) {
		return Result.success(conversationService.getOrCreateSingle(UserContext.getUserId(), dto.getTargetUserId()));
	}

	@GetMapping("/{id}/messages")
	public Result<List<MessageVO>> messages(@PathVariable Long id,
			@RequestParam(required = false) Long before,
			@RequestParam(defaultValue = "20") int limit) {
		return Result.success(conversationService.listMessages(UserContext.getUserId(), id, before, limit));
	}

	@PostMapping("/{id}/read")
	public Result<Void> read(@PathVariable Long id) {
		Long userId = UserContext.getUserId();
		Long lastReadId = conversationService.markRead(userId, id);
		pushReadReceipt(id, userId, lastReadId);
		return Result.success();
	}

	private void pushReadReceipt(Long conversationId, Long readerId, Long lastReadId) {
		Map<String, Object> readData = Map.of(
				"conversationId", conversationId,
				"userId", readerId,
				"lastReadMessageId", lastReadId == null ? 0L : lastReadId);
		Map<String, Object> payload = Map.of("type", "read", "data", readData);
		for (Long memberId : conversationService.listMemberIds(conversationId)) {
			if (!memberId.equals(readerId)) {
				wsSessionManager.sendToUser(memberId, payload);
			}
		}
	}
}
