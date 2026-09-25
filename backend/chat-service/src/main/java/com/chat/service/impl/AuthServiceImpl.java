package com.chat.service.impl;

import com.chat.model.entity.User;
import com.chat.service.AuthService;
import com.chat.service.SmsService;
import com.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证业务实现。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final SmsService smsService;
	private final UserService userService;

	@Override
	public User login(String phone, String code) {
		smsService.verifyCode(phone, code);
		return userService.findOrCreateByPhone(phone);
	}
}
