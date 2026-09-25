package com.chat.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 群成员视图对象。
 */
@Data
public class GroupMemberVO {

	private Long userId;

	private String nickname;

	private String avatar;

	/** 角色：1 群主 2 普通成员 */
	private Integer role;

	private LocalDateTime joinedAt;
}
