package com.chat.service.impl;

import com.chat.common.constant.RedisKeyConstant;
import com.chat.common.exception.BusinessException;
import com.chat.service.SmsService;
import com.chat.service.sms.SmsSender;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 短信验证码业务实现：验证码存 Redis，发送委托给 SmsSender。
 */
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

	private static final Duration CODE_TTL = Duration.ofMinutes(5);
	private static final SecureRandom RANDOM = new SecureRandom();

	private final StringRedisTemplate redisTemplate;
	private final SmsSender smsSender;

	@Override
	public void sendCode(String phone) {
		String code = String.format("%06d", RANDOM.nextInt(1_000_000));
		redisTemplate.opsForValue().set(RedisKeyConstant.SMS_CODE + phone, code, CODE_TTL);
		smsSender.send(phone, code);
	}

	@Override
	public void verifyCode(String phone, String code) {
		String key = RedisKeyConstant.SMS_CODE + phone;
		String cached = redisTemplate.opsForValue().get(key);
		if (cached == null) {
			throw new BusinessException("验证码已过期，请重新获取");
		}
		if (!cached.equals(code)) {
			throw new BusinessException("验证码错误");
		}
		redisTemplate.delete(key);
	}
}
