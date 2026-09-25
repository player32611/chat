package com.chat.service.sms;

/**
 * 短信发送器，按环境切换实现。
 */
public interface SmsSender {

	/**
	 * 发送短信验证码。
	 */
	void send(String phone, String code);
}
