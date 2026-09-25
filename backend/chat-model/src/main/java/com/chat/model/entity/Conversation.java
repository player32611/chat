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
 * 会话实体（单聊/群聊统一）。
 */
@Data
@TableName("conversation")
public class Conversation {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 类型：1 单聊 2 群聊 */
	private Integer type;

	/** 群 ID（群聊时指向 group_info.id） */
	private Long groupId;

	/** 最后一条消息 ID */
	private Long lastMessageId;

	/** 最后消息时间 */
	private LocalDateTime lastMessageAt;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updatedAt;

	@TableLogic
	private Integer deleted;
}
