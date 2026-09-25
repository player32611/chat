package com.chat.model.enums;

import lombok.Getter;

/**
 * 会话成员角色。
 */
@Getter
public enum MemberRole {

	OWNER(1, "群主"),
	MEMBER(2, "成员");

	private final int code;
	private final String desc;

	MemberRole(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
