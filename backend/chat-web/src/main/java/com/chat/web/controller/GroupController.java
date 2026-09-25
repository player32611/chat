package com.chat.web.controller;

import com.chat.common.result.Result;
import com.chat.model.dto.group.GroupCreateDTO;
import com.chat.model.dto.group.GroupMemberAddDTO;
import com.chat.model.dto.group.GroupUpdateDTO;
import com.chat.model.vo.GroupMemberVO;
import com.chat.model.vo.GroupVO;
import com.chat.service.GroupService;
import com.chat.web.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 群聊接口。
 */
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

	private final GroupService groupService;

	@PostMapping
	public Result<GroupVO> create(@Valid @RequestBody GroupCreateDTO dto) {
		return Result.success(groupService.createGroup(
				UserContext.getUserId(), dto.getName(), dto.getAvatar(), dto.getMemberIds()));
	}

	@GetMapping("/{id}")
	public Result<GroupVO> detail(@PathVariable Long id) {
		return Result.success(groupService.getGroup(UserContext.getUserId(), id));
	}

	@GetMapping("/{id}/members")
	public Result<List<GroupMemberVO>> members(@PathVariable Long id) {
		return Result.success(groupService.listMembers(UserContext.getUserId(), id));
	}

	@PostMapping("/{id}/member")
	public Result<Void> addMember(@PathVariable Long id, @Valid @RequestBody GroupMemberAddDTO dto) {
		groupService.addMembers(UserContext.getUserId(), id, dto.getUserIds());
		return Result.success();
	}

	@DeleteMapping("/{id}/member/{userId}")
	public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
		groupService.removeMember(UserContext.getUserId(), id, userId);
		return Result.success();
	}

	@PutMapping("/{id}")
	public Result<GroupVO> update(@PathVariable Long id, @Valid @RequestBody GroupUpdateDTO dto) {
		return Result.success(groupService.updateGroup(
				UserContext.getUserId(), id, dto.getName(), dto.getAvatar(), dto.getAnnouncement()));
	}

	@DeleteMapping("/{id}")
	public Result<Void> dissolve(@PathVariable Long id) {
		groupService.dissolveGroup(UserContext.getUserId(), id);
		return Result.success();
	}
}
