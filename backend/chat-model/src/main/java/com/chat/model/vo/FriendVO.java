package com.chat.model.vo;

import lombok.Data;

/**
 * 好友视图对象：对方用户信息 + 备注。
 */
@Data
public class FriendVO {

	/** 好友的用户 ID */
	private Long friendId;

	/** 备注名（可空） */
	private String remark;

	/** 好友用户信息 */
	private UserVO user;

	/** 是否在线 */
	private Boolean online;
}
