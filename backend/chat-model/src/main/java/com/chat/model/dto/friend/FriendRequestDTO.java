package com.chat.model.dto.friend;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 好友申请请求。
 */
@Data
public class FriendRequestDTO {

	@NotNull(message = "对方用户不能为空")
	private Long toUserId;

	@Size(max = 100, message = "验证消息不能超过100字")
	private String message;
}
