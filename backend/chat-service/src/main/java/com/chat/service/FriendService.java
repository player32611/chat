package com.chat.service;

import com.chat.model.vo.FriendRequestVO;
import com.chat.model.vo.FriendVO;
import com.chat.model.vo.SearchUserVO;

import java.util.List;

/**
 * 好友业务接口。
 */
public interface FriendService {

	/** 关系：无关系 */
	int RELATION_NONE = 0;

	/** 关系：已是好友 */
	int RELATION_FRIEND = 1;

	/** 关系：我已申请（待对方处理） */
	int RELATION_OUTGOING = 2;

	/** 关系：对方已申请（待我处理） */
	int RELATION_INCOMING = 3;

	/**
	 * 搜索用户（含与当前用户的关系）。
	 */
	List<SearchUserVO> searchUsers(Long currentUserId, String keyword);

	/**
	 * 发送好友申请。
	 */
	void sendRequest(Long fromUserId, Long toUserId, String message);

	/**
	 * 同意好友申请。
	 */
	void acceptRequest(Long userId, Long requestId);

	/**
	 * 拒绝好友申请。
	 */
	void rejectRequest(Long userId, Long requestId);

	/**
	 * 收到的申请列表。
	 */
	List<FriendRequestVO> listReceived(Long userId);

	/**
	 * 发出的申请列表。
	 */
	List<FriendRequestVO> listSent(Long userId);

	/**
	 * 好友列表。
	 */
	List<FriendVO> listFriends(Long userId);

	/**
	 * 删除好友。
	 */
	void deleteFriend(Long userId, Long friendId);

	/**
	 * 计算当前用户与目标用户的关系。
	 */
	int getRelation(Long currentUserId, Long otherUserId);
}
