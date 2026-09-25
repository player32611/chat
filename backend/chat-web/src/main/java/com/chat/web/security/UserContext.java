package com.chat.web.security;

/**
 * 当前登录用户上下文（ThreadLocal）。
 */
public final class UserContext {

	private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

	private UserContext() {
	}

	public static void set(AuthUser authUser) {
		HOLDER.set(authUser);
	}

	public static AuthUser get() {
		return HOLDER.get();
	}

	public static Long getUserId() {
		AuthUser authUser = HOLDER.get();
		return authUser == null ? null : authUser.userId();
	}

	public static void clear() {
		HOLDER.remove();
	}
}
