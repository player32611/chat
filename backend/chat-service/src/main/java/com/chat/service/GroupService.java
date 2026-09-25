package com.chat.service;

import com.chat.model.vo.GroupMemberVO;
import com.chat.model.vo.GroupVO;

import java.util.List;

/**
 * 群聊业务接口。
 */
public interface GroupService {

	/**
	 * 创建群聊（创建者即群主），返回群信息（含会话 ID）。
	 */
	GroupVO createGroup(Long ownerId, String name, String avatar, List<Long> memberIds);

	/**
	 * 群信息（仅群成员可见）。
	 */
	GroupVO getGroup(Long userId, Long groupId);

	/**
	 * 群成员列表（仅群成员可见，群主在前）。
	 */
	List<GroupMemberVO> listMembers(Long userId, Long groupId);

	/**
	 * 添加群成员（任意群成员可邀请）。
	 */
	void addMembers(Long operatorId, Long groupId, List<Long> userIds);

	/**
	 * 移除群成员（仅群主）。
	 */
	void removeMember(Long operatorId, Long groupId, Long userId);

	/**
	 * 修改群信息（仅群主）。
	 */
	GroupVO updateGroup(Long operatorId, Long groupId, String name, String avatar, String announcement);

	/**
	 * 解散群（仅群主）。
	 */
	void dissolveGroup(Long operatorId, Long groupId);
}
