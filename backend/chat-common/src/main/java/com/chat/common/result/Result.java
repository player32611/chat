package com.chat.common.result;

import com.chat.common.constant.CommonConstant;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应体：{ code, msg, data }。
 */
@Data
public class Result<T> implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/** 状态码 */
	private Integer code;

	/** 提示信息 */
	private String msg;

	/** 数据 */
	private T data;

	public Result() {
	}

	public Result(Integer code, String msg, T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
	}

	public static <T> Result<T> success(T data) {
		return new Result<>(CommonConstant.CODE_SUCCESS, "success", data);
	}

	public static <T> Result<T> success() {
		return success(null);
	}

	public static <T> Result<T> fail(Integer code, String msg) {
		return new Result<>(code, msg, null);
	}

	public static <T> Result<T> fail(String msg) {
		return fail(CommonConstant.CODE_ERROR, msg);
	}
}
