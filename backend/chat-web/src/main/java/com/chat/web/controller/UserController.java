package com.chat.web.controller;

import com.chat.common.exception.BusinessException;
import com.chat.common.result.Result;
import com.chat.model.dto.user.UserUpdateDTO;
import com.chat.model.entity.User;
import com.chat.model.vo.SearchUserVO;
import com.chat.model.vo.UserVO;
import com.chat.service.FriendService;
import com.chat.service.UserService;
import com.chat.web.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户接口。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final FriendService friendService;

	@GetMapping("/search")
	public Result<List<SearchUserVO>> search(@RequestParam String keyword) {
		if (!StringUtils.hasText(keyword)) {
			throw new BusinessException("请输入搜索关键词");
		}
		return Result.success(friendService.searchUsers(UserContext.getUserId(), keyword.trim()));
	}

	@GetMapping("/{id}")
	public Result<UserVO> get(@PathVariable Long id) {
		User user = userService.getById(id);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		return Result.success(UserVO.from(user));
	}

	@PutMapping("/me")
	public Result<UserVO> updateMe(@Valid @RequestBody UserUpdateDTO dto) {
		User user = userService.updateMe(UserContext.getUserId(), dto.getNickname(), dto.getAvatar());
		return Result.success(UserVO.from(user));
	}
}
