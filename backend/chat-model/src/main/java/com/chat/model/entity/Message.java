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
 * 消息实体。
 */
@Data
@TableName("message")
public class Message {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 会话 ID */
	private Long conversationId;

	/** 发送人 ID */
	private Long senderId;

	/** 类型：1 文本 2 图片 */
	private Integer type;

	/** 内容（文本或图片 URL） */
	private String content;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	@TableLogic
	private Integer deleted;
}
