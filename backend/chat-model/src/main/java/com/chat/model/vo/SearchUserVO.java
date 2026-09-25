package com.chat.model.vo;

import com.chat.model.entity.User;
import lombok.Data;

/**
 * 搜索到的用户视图对象（含与当前用户的关系）。
 */
@Data
public class SearchUserVO {

	private Long id;

	private String phone;

	private String nickname;

	private String avatar;

	/** 关系：0 无关系 1 已是好友 2 我已申请（待对方） 3 对方已申请（待我） */
	private Integer relation;

	public static SearchUserVO from(User user) {
		SearchUserVO vo = new SearchUserVO();
		vo.setId(user.getId());
		vo.setPhone(user.getPhone());
		vo.setNickname(user.getNickname());
		vo.setAvatar(user.getAvatar());
		return vo;
	}
}
