package com.chat.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话成员实体（含未读/已读游标）。
 */
@Data
@TableName("conversation_member")
public class ConversationMember {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 会话 ID */
	private Long conversationId;

	/** 用户 ID */
	private Long userId;

	/** 角色：1 群主 2 普通成员 */
	private Integer role;

	/** 未读消息数 */
	private Integer unreadCount;

	/** 最后已读消息 ID */
	private Long lastReadMessageId;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime joinedAt;

	@TableLogic
	private Integer deleted;
}
