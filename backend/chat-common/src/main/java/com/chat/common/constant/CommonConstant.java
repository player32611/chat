package com.chat.common.constant;

/**
 * 通用常量。
 */
public final class CommonConstant {

	private CommonConstant() {
	}

	/** 成功状态码 */
	public static final int CODE_SUCCESS = 200;

	/** 未登录 */
	public static final int CODE_UNAUTHORIZED = 401;

	/** 参数错误 */
	public static final int CODE_BAD_REQUEST = 400;

	/** 无权限 */
	public static final int CODE_FORBIDDEN = 403;

	/** 服务异常 */
	public static final int CODE_ERROR = 500;

	/** 请求头 Token 名称 */
	public static final String HEADER_TOKEN = "Authorization";

	/** Token 前缀 */
	public static final String TOKEN_PREFIX = "Bearer ";

	/** 默认头像（空串，前端展示昵称首字符占位） */
	public static final String DEFAULT_AVATAR = "";
}
