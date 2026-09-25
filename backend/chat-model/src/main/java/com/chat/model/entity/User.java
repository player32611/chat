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
 * 用户实体。
 */
@Data
@TableName("`user`")
public class User {

	@TableId(type = IdType.AUTO)
	private Long id;

	/** 手机号 */
	private String phone;

	/** 昵称 */
	private String nickname;

	/** 头像 URL */
	private String avatar;

	/** 状态：1 正常 0 禁用 */
	private Integer status;

	/** 最后登录时间 */
	private LocalDateTime lastLoginAt;

	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createdAt;

	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updatedAt;

	@TableLogic
	private Integer deleted;
}
