package com.chat.service;

import com.chat.model.vo.ConversationVO;
import com.chat.model.vo.MessageVO;

import java.util.List;

/**
 * 会话业务接口。
 */
public interface ConversationService {

	/**
	 * 获取或创建单聊会话。
	 */
	ConversationVO getOrCreateSingle(Long currentUserId, Long targetUserId);

	/**
	 * 会话列表（含未读数、最后消息、对方/群信息）。
	 */
	List<ConversationVO> listConversations(Long userId);

	/**
	 * 历史消息（向前分页，返回升序）。
	 */
	List<MessageVO> listMessages(Long userId, Long conversationId, Long beforeId, int limit);

	/**
	 * 标记已读，返回最后已读消息 ID。
	 */
	Long markRead(Long userId, Long conversationId);

	/**
	 * 会话成员用户 ID 列表。
	 */
	List<Long> listMemberIds(Long conversationId);

	/**
	 * 判断用户是否为会话成员。
	 */
	boolean isMember(Long userId, Long conversationId);
}
