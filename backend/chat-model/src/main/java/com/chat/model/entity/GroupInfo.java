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
 * 群聊信息实体。
 */
@Data
@TableName("group_info")
public class GroupInfo {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 群名称 */
	private String name;

	/** 群头像 URL */
	private String avatar;

	/** 群主 ID */
	private Long ownerId;

	/** 群公告 */
	private String announcement;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updatedAt;

	@TableLogic
	private Integer deleted;
}
