package com.chat.model.enums;

import lombok.Getter;

/**
 * 好友申请状态。
 */
@Getter
public enum FriendRequestStatus {

	PENDING(0, "待处理"),
	ACCEPTED(1, "已同意"),
	REJECTED(2, "已拒绝");

	private final int code;
	private final String desc;

	FriendRequestStatus(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
