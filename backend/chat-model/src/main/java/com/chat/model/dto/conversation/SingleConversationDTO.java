package com.chat.model.dto.conversation;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建单聊会话请求。
 */
@Data
public class SingleConversationDTO {

	@NotNull(message = "请指定对方用户")
	private Long targetUserId;
}
