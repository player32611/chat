package com.chat.model.vo;

import lombok.Data;

/**
 * 登录响应。
 */
@Data
public class LoginVO {

	private String token;

	private UserVO user;
}
