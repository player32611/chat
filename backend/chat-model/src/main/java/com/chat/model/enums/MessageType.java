package com.chat.model.enums;

import lombok.Getter;

/**
 * 消息类型。
 */
@Getter
public enum MessageType {

	TEXT(1, "文本"),
	IMAGE(2, "图片");

	private final int code;
	private final String desc;

	MessageType(int code, String desc) {
		this.code = code;
		this.desc = desc;
	}
}
