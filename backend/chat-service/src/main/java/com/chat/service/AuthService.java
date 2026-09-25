package com.chat.service;

import com.chat.model.entity.User;

/**
 * 认证业务接口。
 */
public interface AuthService {

	/**
	 * 手机号验证码登录（首次登录隐式注册），返回用户。
	 */
	User login(String phone, String code);
}
