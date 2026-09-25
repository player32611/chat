package com.chat.model.enums;

import lombok.Getter;

/**
 * 会话类型。
 */
@Getter
public enum ConversationType {

	SINGLE(1, "单聊"),
	GROUP(2, "群聊");

	private final int code;
	private final String desc;

	ConversationType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
