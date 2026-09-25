package com.chat.web.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 生成与解析。
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	/**
	 * 生成 token。
	 */
	public String generateToken(Long userId) {
		long now = System.currentTimeMillis();
		Date expiresAt = new Date(now + jwtProperties.getExpireMinutes() * 60_000L);
		return Jwts.builder()
				.subject(String.valueOf(userId))
				.id(UUID.randomUUID().toString().replace("-", ""))
				.issuedAt(new Date(now))
				.expiration(expiresAt)
				.signWith(secretKey())
				.compact();
	}

	/**
	 * 解析 token，非法则抛异常。
	 */
	public Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(secretKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey secretKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}
}
