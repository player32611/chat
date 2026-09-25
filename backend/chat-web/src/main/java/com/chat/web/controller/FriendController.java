package com.chat.web.controller;

import com.chat.common.result.Result;
import com.chat.model.dto.friend.FriendRequestDTO;
import com.chat.model.vo.FriendRequestVO;
import com.chat.model.vo.FriendVO;
import com.chat.service.FriendService;
import com.chat.web.security.UserContext;
import com.chat.web.ws.WsSessionManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 好友接口。
 */
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

	private final FriendService friendService;
	private final WsSessionManager wsSessionManager;

	@PostMapping("/request")
	public Result<Void> sendRequest(@Valid @RequestBody FriendRequestDTO dto) {
		friendService.sendRequest(UserContext.getUserId(), dto.getToUserId(), dto.getMessage());
		return Result.success();
	}

	@GetMapping("/requests/received")
	public Result<List<FriendRequestVO>> listReceived() {
		return Result.success(friendService.listReceived(UserContext.getUserId()));
	}

	@GetMapping("/requests/sent")
	public Result<List<FriendRequestVO>> listSent() {
		return Result.success(friendService.listSent(UserContext.getUserId()));
	}

	@PostMapping("/request/{id}/accept")
	public Result<Void> accept(@PathVariable Long id) {
		friendService.acceptRequest(UserContext.getUserId(), id);
		return Result.success();
	}

	@PostMapping("/request/{id}/reject")
	public Result<Void> reject(@PathVariable Long id) {
		friendService.rejectRequest(UserContext.getUserId(), id);
		return Result.success();
	}

	@GetMapping("/list")
	public Result<List<FriendVO>> list() {
		List<FriendVO> friends = friendService.listFriends(UserContext.getUserId());
		for (FriendVO friend : friends) {
			friend.setOnline(wsSessionManager.isOnline(friend.getFriendId()));
		}
		return Result.success(friends);
	}

	@DeleteMapping("/{friendId}")
	public Result<Void> delete(@PathVariable Long friendId) {
		friendService.deleteFriend(UserContext.getUserId(), friendId);
		return Result.success();
	}
}
