package com.chat.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Mock 短信发送器（dev 环境），验证码仅打印日志。
 */
@Slf4j
@Component
@Profile("dev")
public class MockSmsSender implements SmsSender {

	@Override
	public void send(String phone, String code) {
		log.info("[MOCK SMS] phone={}, code={}", phone, code);
	}
}
