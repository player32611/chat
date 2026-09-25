package com.chat.service;

/**
 * 短信验证码业务接口。
 */
public interface SmsService {

	/**
	 * 发送验证码。
	 */
	void sendCode(String phone);

	/**
	 * 校验验证码，校验失败抛业务异常。
	 */
	void verifyCode(String phone, String code);
}
