package com.chat.service.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信发送器（非 dev 环境），占位实现，接入真实 SDK 时替换。
 */
@Slf4j
@Component
@Profile("!dev")
public class AliyunSmsSender implements SmsSender {

	@Override
	public void send(String phone, String code) {
		// TODO: 接入阿里云短信 SDK，按 application.yml 中的配置调用
		log.warn("[ALIYUN SMS] 尚未接入真实短信，phone={}, code={}", phone, code);
	}
}
