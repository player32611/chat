package com.chat.web.controller;

import com.chat.common.constant.RedisKeyConstant;
import com.chat.common.result.Result;
import com.chat.model.dto.auth.LoginDTO;
import com.chat.model.dto.auth.SmsSendDTO;
import com.chat.model.entity.User;
import com.chat.model.vo.LoginVO;
import com.chat.model.vo.UserVO;
import com.chat.service.AuthService;
import com.chat.service.SmsService;
import com.chat.service.UserService;
import com.chat.web.security.AuthUser;
import com.chat.web.security.JwtTokenProvider;
import com.chat.web.security.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final SmsService smsService;
	private final AuthService authService;
	private final UserService userService;
	private final JwtTokenProvider tokenProvider;
	private final StringRedisTemplate redisTemplate;

	@PostMapping("/sms/send")
	public Result<Void> sendSms(@Valid @RequestBody SmsSendDTO dto) {
		smsService.sendCode(dto.getPhone());
		return Result.success();
	}

	@PostMapping("/login")
	public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
		User user = authService.login(dto.getPhone(), dto.getCode());
		LoginVO vo = new LoginVO();
		vo.setToken(tokenProvider.generateToken(user.getId()));
		vo.setUser(UserVO.from(user));
		return Result.success(vo);
	}

	@PostMapping("/logout")
	public Result<Void> logout() {
		AuthUser authUser = UserContext.get();
		if (authUser != null) {
			long ttl = Math.max(0, authUser.expiresAt().getTime() - System.currentTimeMillis());
			redisTemplate.opsForValue().set(
					RedisKeyConstant.JWT_BLACKLIST + authUser.jti(), "1", Duration.ofMillis(ttl));
		}
		return Result.success();
	}

	@GetMapping("/me")
	public Result<UserVO> me() {
		return Result.success(UserVO.from(userService.getById(UserContext.getUserId())));
	}
}
