package com.chat.model.vo;

import com.chat.model.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户视图对象。
 */
@Data
public class UserVO {

	private Long id;

	private String phone;

	private String nickname;

	private String avatar;

	private LocalDateTime lastLoginAt;

	public static UserVO from(User user) {
		if (user == null) {
			return null;
		}
		UserVO vo = new UserVO();
		vo.setId(user.getId());
		vo.setPhone(user.getPhone());
		vo.setNickname(user.getNickname());
		vo.setAvatar(user.getAvatar());
		vo.setLastLoginAt(user.getLastLoginAt());
		return vo;
	}
}
