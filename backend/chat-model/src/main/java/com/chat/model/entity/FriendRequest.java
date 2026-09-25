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
 * 好友申请实体。
 */
@Data
@TableName("friend_request")
public class FriendRequest {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 申请人 ID */
	private Long fromUserId;

	/** 接收人 ID */
	private Long toUserId;

	/** 验证消息 */
	private String message;

	/** 状态：0 待处理 1 同意 2 拒绝 */
	private Integer status;

	/** 处理时间 */
	private LocalDateTime handledAt;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	@TableLogic
	private Integer deleted;
}
