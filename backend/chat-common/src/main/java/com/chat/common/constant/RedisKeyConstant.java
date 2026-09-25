package com.chat.common.constant;

/**
 * Redis Key 常量。
 */
public final class RedisKeyConstant {

	private RedisKeyConstant() {
	}

	/** 短信验证码：sms:code:{phone} */
	public static final String SMS_CODE = "sms:code:";

	/** 在线状态：presence:{userId} */
	public static final String PRESENCE = "presence:";

	/** 登出黑名单：jwt:blacklist:{jti} */
	public static final String JWT_BLACKLIST = "jwt:blacklist:";
}
