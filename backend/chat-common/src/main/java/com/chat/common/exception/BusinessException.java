package com.chat.common.exception;

import com.chat.common.constant.CommonConstant;
import lombok.Getter;

/**
 * 业务异常，携带状态码与提示信息。
 */
@Getter
public class BusinessException extends RuntimeException {

	@java.io.Serial
	private static final long serialVersionUID = 1L;

	private final int code;

	public BusinessException(String message) {
		this(CommonConstant.CODE_ERROR, message);
	}

	public BusinessException(int code, String message) {
		super(message);
		this.code = code;
	}
}
