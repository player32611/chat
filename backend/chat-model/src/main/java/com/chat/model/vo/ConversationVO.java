package com.chat.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话视图对象。
 */
@Data
public class ConversationVO {

	private Long id;

	private Integer type;

	private Long groupId;

	private Integer unreadCount;

	/** 单聊对方昵称或群名称 */
	private String name;

	/** 单聊对方头像或群头像 */
	private String avatar;

	/** 单聊对方用户 ID */
	private Long targetUserId;

	private MessageVO lastMessage;

	private LocalDateTime lastMessageAt;
}
