package com.chat.service;

import com.chat.model.entity.User;

/**
 * 用户业务接口。
 */
public interface UserService {

	/**
	 * 按 ID 查询用户。
	 */
	User getById(Long id);

	/**
	 * 按手机号查询用户，不存在返回 null。
	 */
	User getByPhone(String phone);

	/**
	 * 按手机号查找，不存在则自动注册。
	 */
	User findOrCreateByPhone(String phone);

	/**
	 * 修改当前用户资料（昵称/头像），返回更新后的用户。
	 */
	User updateMe(Long userId, String nickname, String avatar);
}
