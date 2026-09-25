package com.chat.web.security;

import java.util.Date;

/**
 * 已认证用户上下文信息。
 */
public record AuthUser(Long userId, String jti, Date expiresAt) {
}
