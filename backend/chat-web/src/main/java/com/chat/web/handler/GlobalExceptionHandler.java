package com.chat.web.handler;

import com.chat.common.constant.CommonConstant;
import com.chat.common.exception.BusinessException;
import com.chat.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public Result<Void> handleBusiness(BusinessException e) {
		return Result.fail(e.getCode(), e.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Result<Void> handleValidation(MethodArgumentNotValidException e) {
		String msg = e.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("参数错误");
		return Result.fail(CommonConstant.CODE_BAD_REQUEST, msg);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
		return Result.fail(CommonConstant.CODE_BAD_REQUEST, "请求体格式错误");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public Result<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
		return Result.fail(CommonConstant.CODE_BAD_REQUEST, "文件过大，单个文件不能超过 10MB");
	}

	@ExceptionHandler(Exception.class)
	public Result<Void> handleException(Exception e) {
		log.error("系统异常", e);
		return Result.fail(CommonConstant.CODE_ERROR, "系统繁忙，请稍后重试");
	}
}
