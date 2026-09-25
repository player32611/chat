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
 * 好友关系实体（规范化：user_id < friend_id 存唯一一行）。
 */
@Data
@TableName("friend")
public class Friend {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 用户 ID（较小者） */
	private Long userId;

	/** 好友 ID（较大者） */
	private Long friendId;

	/** 备注名 */
	private String remark;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	@TableLogic
	private Integer deleted;
}
