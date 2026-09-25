package com.chat.web.security;

import com.chat.common.constant.CommonConstant;
import com.chat.common.constant.RedisKeyConstant;
import com.chat.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证过滤器：校验 token，写入用户上下文。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final List<String> EXCLUDE_PATHS = List.of(
			"/api/auth/sms/send", "/api/auth/login", "/ws", "/error");
	private static final AntPathMatcher MATCHER = new AntPathMatcher();

	private final JwtTokenProvider tokenProvider;
	private final StringRedisTemplate redisTemplate;
	private final ObjectMapper objectMapper;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// CORS 预检请求（OPTIONS）不携带 Authorization 头，需放行交由 Spring 的 CORS 处理，
		// 否则预检被 401 拒绝，浏览器会拦截后续跨域请求
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			return true;
		}
		String path = request.getServletPath();
		return EXCLUDE_PATHS.stream().anyMatch(p -> MATCHER.match(p, path));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		AuthUser authUser = resolveAuthUser(request);
		if (authUser == null) {
			writeUnauthorized(response);
			return;
		}
		UserContext.set(authUser);
		try {
			chain.doFilter(request, response);
		} finally {
			UserContext.clear();
		}
	}

	private AuthUser resolveAuthUser(HttpServletRequest request) {
		String header = request.getHeader(CommonConstant.HEADER_TOKEN);
		if (header == null || !header.startsWith(CommonConstant.TOKEN_PREFIX)) {
			return null;
		}
		String token = header.substring(CommonConstant.TOKEN_PREFIX.length());
		try {
			Claims claims = tokenProvider.parse(token);
			String jti = claims.getId();
			if (Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyConstant.JWT_BLACKLIST + jti))) {
				return null;
			}
			return new AuthUser(Long.valueOf(claims.getSubject()), jti, claims.getExpiration());
		} catch (Exception e) {
			return null;
		}
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.getWriter().write(objectMapper.writeValueAsString(
				Result.fail(CommonConstant.CODE_UNAUTHORIZED, "未登录或登录已失效")));
	}
}
