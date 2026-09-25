package com.chat.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群聊视图对象。
 */
@Data
public class GroupVO {

	private Long id;

	private String name;

	private String avatar;

	/** 群主 ID */
	private Long ownerId;

	/** 群公告 */
	private String announcement;

	/** 成员数量 */
	private Integer memberCount;

	/** 群对应的会话 ID */
	private Long conversationId;

	private LocalDateTime createdAt;
}
