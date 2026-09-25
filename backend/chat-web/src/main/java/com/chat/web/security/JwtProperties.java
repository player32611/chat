package com.chat.web.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置项。
 */
@Data
@Component
@ConfigurationProperties(prefix = "chat.jwt")
public class JwtProperties {

	/** 签名密钥（至少 32 字符） */
	private String secret;

	/** 过期时间（分钟） */
	private long expireMinutes = 10080;
}
