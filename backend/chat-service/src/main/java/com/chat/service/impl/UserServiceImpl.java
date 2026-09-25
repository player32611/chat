package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.common.constant.CommonConstant;
import com.chat.common.exception.BusinessException;
import com.chat.mapper.UserMapper;
import com.chat.model.entity.User;
import com.chat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;

	@Override
	public User getById(Long id) {
		return userMapper.selectById(id);
	}

	@Override
	public User getByPhone(String phone) {
		return userMapper.selectOne(
				new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
	}

	@Override
	public User findOrCreateByPhone(String phone) {
		User user = getByPhone(phone);
		if (user != null) {
			return user;
		}
		User created = new User();
		created.setPhone(phone);
		created.setNickname("用户" + phone.substring(phone.length() - 4));
		created.setAvatar(CommonConstant.DEFAULT_AVATAR);
		created.setStatus(1);
		created.setLastLoginAt(LocalDateTime.now());
		try {
			userMapper.insert(created);
			return created;
		} catch (DuplicateKeyException e) {
			log.warn("用户已存在，改为查询：{}", phone);
			return getByPhone(phone);
		}
	}

	@Override
	public User updateMe(Long userId, String nickname, String avatar) {
		User user = userMapper.selectById(userId);
		if (user == null) {
			throw new BusinessException("用户不存在");
		}
		if (nickname != null) {
			String trimmed = nickname.trim();
			if (trimmed.isEmpty()) {
				throw new BusinessException("昵称不能为空");
			}
			user.setNickname(trimmed);
		}
		if (avatar != null) {
			user.setAvatar(avatar);
		}
		userMapper.updateById(user);
		return user;
	}
}
