package com.chat.model.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料请求。
 */
@Data
public class UserUpdateDTO {

	@Size(max = 50, message = "昵称过长")
	private String nickname;

	private String avatar;
}
