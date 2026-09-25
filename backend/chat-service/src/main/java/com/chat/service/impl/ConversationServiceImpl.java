package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chat.common.exception.BusinessException;
import com.chat.mapper.ConversationMapper;
import com.chat.mapper.ConversationMemberMapper;
import com.chat.mapper.GroupMapper;
import com.chat.mapper.MessageMapper;
import com.chat.mapper.UserMapper;
import com.chat.model.entity.Conversation;
import com.chat.model.entity.ConversationMember;
import com.chat.model.entity.GroupInfo;
import com.chat.model.entity.Message;
import com.chat.model.entity.User;
import com.chat.model.enums.ConversationType;
import com.chat.model.enums.MemberRole;
import com.chat.model.vo.ConversationVO;
import com.chat.model.vo.MessageVO;
import com.chat.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 会话业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

	private final ConversationMapper conversationMapper;
	private final ConversationMemberMapper memberMapper;
	private final GroupMapper groupMapper;
	private final MessageMapper messageMapper;
	private final UserMapper userMapper;

	@Transactional
	@Override
	public ConversationVO getOrCreateSingle(Long currentUserId, Long targetUserId) {
		if (currentUserId.equals(targetUserId)) {
			throw new BusinessException("不能和自己聊天");
		}
		if (userMapper.selectById(targetUserId) == null) {
			throw new BusinessException("用户不存在");
		}
		Long conversationId = conversationMapper.selectSingleConversationId(currentUserId, targetUserId);
		Conversation conversation;
		if (conversationId == null) {
			conversation = new Conversation();
			conversation.setType(ConversationType.SINGLE.getCode());
			conversationMapper.insert(conversation);
			insertMember(conversation.getId(), currentUserId);
			insertMember(conversation.getId(), targetUserId);
		} else {
			conversation = conversationMapper.selectById(conversationId);
		}

		User target = userMapper.selectById(targetUserId);
		ConversationVO vo = new ConversationVO();
		vo.setId(conversation.getId());
		vo.setType(conversation.getType());
		vo.setGroupId(conversation.getGroupId());
		vo.setUnreadCount(0);
		vo.setLastMessageAt(conversation.getLastMessageAt());
		vo.setTargetUserId(targetUserId);
		vo.setName(target.getNickname());
		vo.setAvatar(target.getAvatar());
		return vo;
	}

	@Override
	public List<ConversationVO> listConversations(Long userId) {
		List<ConversationMember> myMembers = memberMapper.selectList(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getUserId, userId));
		if (myMembers.isEmpty()) {
			return new ArrayList<>();
		}
		Map<Long, ConversationMember> memberByConv = myMembers.stream()
				.collect(Collectors.toMap(ConversationMember::getConversationId, Function.identity()));
		List<Long> conversationIds = myMembers.stream().map(ConversationMember::getConversationId).toList();

		List<Conversation> conversations = conversationMapper.selectList(new LambdaQueryWrapper<Conversation>()
				.in(Conversation::getId, conversationIds));

		List<Long> lastMessageIds = conversations.stream()
				.map(Conversation::getLastMessageId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		Map<Long, Message> lastMessageMap = lastMessageIds.isEmpty() ? Map.of()
				: messageMapper.selectBatchIds(lastMessageIds).stream()
						.collect(Collectors.toMap(Message::getId, Function.identity()));

		Map<Long, Long> peerByConversation = new HashMap<>();
		List<Long> singleConversationIds = conversations.stream()
				.filter(c -> c.getType() != null && c.getType() == ConversationType.SINGLE.getCode())
				.map(Conversation::getId)
				.toList();
		List<ConversationMember> singleMembers = singleConversationIds.isEmpty() ? List.of()
				: memberMapper.selectList(new LambdaQueryWrapper<ConversationMember>()
						.in(ConversationMember::getConversationId, singleConversationIds)
						.ne(ConversationMember::getUserId, userId));
		singleMembers.forEach(m -> peerByConversation.put(m.getConversationId(), m.getUserId()));
		List<Long> peerIds = singleMembers.stream().map(ConversationMember::getUserId).distinct().toList();
		Map<Long, User> userMap = peerIds.isEmpty() ? Map.of()
				: userMapper.selectBatchIds(peerIds).stream()
						.collect(Collectors.toMap(User::getId, Function.identity()));

		Map<Long, GroupInfo> groupMap = buildGroupMap(conversations);

		return conversations.stream()
				.sorted(Comparator.comparing(Conversation::getLastMessageAt,
						Comparator.nullsLast(Comparator.reverseOrder())))
				.map(c -> toVO(c, memberByConv.get(c.getId()), peerByConversation.get(c.getId()),
						lastMessageMap, userMap, groupMap))
				.toList();
	}

	@Override
	public List<MessageVO> listMessages(Long userId, Long conversationId, Long beforeId, int limit) {
		if (!isMember(userId, conversationId)) {
			throw new BusinessException("不是会话成员");
		}
		int size = Math.min(Math.max(limit, 1), 50);
		List<Message> messages = messageMapper.selectList(new LambdaQueryWrapper<Message>()
				.eq(Message::getConversationId, conversationId)
				.lt(beforeId != null, Message::getId, beforeId)
				.orderByDesc(Message::getId)
				.last("LIMIT " + size));
		List<Message> ascending = new ArrayList<>(messages);
		Collections.reverse(ascending);
		List<MessageVO> vos = ascending.stream().map(MessageVO::from).toList();
		enrichSenders(vos);
		return vos;
	}

	@Override
	public Long markRead(Long userId, Long conversationId) {
		if (!isMember(userId, conversationId)) {
			throw new BusinessException("不是会话成员");
		}
		Conversation conversation = conversationMapper.selectById(conversationId);
		Long lastMessageId = conversation == null ? null : conversation.getLastMessageId();
		long readId = lastMessageId == null ? 0L : lastMessageId;
		memberMapper.update(null, new LambdaUpdateWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversationId)
				.eq(ConversationMember::getUserId, userId)
				.set(ConversationMember::getUnreadCount, 0)
				.set(ConversationMember::getLastReadMessageId, readId));
		return readId;
	}

	@Override
	public List<Long> listMemberIds(Long conversationId) {
		return memberMapper.selectList(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversationId))
				.stream()
				.map(ConversationMember::getUserId)
				.toList();
	}

	@Override
	public boolean isMember(Long userId, Long conversationId) {
		return memberMapper.selectCount(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversationId)
				.eq(ConversationMember::getUserId, userId)) > 0;
	}

	private void insertMember(Long conversationId, Long userId) {
		ConversationMember member = new ConversationMember();
		member.setConversationId(conversationId);
		member.setUserId(userId);
		member.setRole(MemberRole.MEMBER.getCode());
		member.setUnreadCount(0);
		member.setLastReadMessageId(0L);
		memberMapper.insert(member);
	}

	private void enrichSenders(List<MessageVO> messages) {
		if (messages.isEmpty()) {
			return;
		}
		Set<Long> senderIds = messages.stream().map(MessageVO::getSenderId).collect(Collectors.toSet());
		Map<Long, User> userMap = userMapper.selectBatchIds(senderIds).stream()
				.collect(Collectors.toMap(User::getId, Function.identity()));
		messages.forEach(vo -> {
			User sender = userMap.get(vo.getSenderId());
			if (sender != null) {
				vo.setSenderName(sender.getNickname());
				vo.setSenderAvatar(sender.getAvatar());
			}
		});
	}

	private Map<Long, GroupInfo> buildGroupMap(List<Conversation> conversations) {
		List<Long> groupIds = conversations.stream()
				.filter(c -> c.getType() != null && c.getType() == ConversationType.GROUP.getCode())
				.map(Conversation::getGroupId)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		if (groupIds.isEmpty()) {
			return Map.of();
		}
		return groupMapper.selectBatchIds(groupIds).stream()
				.collect(Collectors.toMap(GroupInfo::getId, Function.identity()));
	}

	private ConversationVO toVO(Conversation conversation, ConversationMember myMember, Long peerId,
			Map<Long, Message> lastMessageMap, Map<Long, User> userMap, Map<Long, GroupInfo> groupMap) {
		ConversationVO vo = new ConversationVO();
		vo.setId(conversation.getId());
		vo.setType(conversation.getType());
		vo.setGroupId(conversation.getGroupId());
		vo.setUnreadCount(myMember == null ? 0 : myMember.getUnreadCount());
		vo.setLastMessageAt(conversation.getLastMessageAt());
		Message lastMessage = conversation.getLastMessageId() == null ? null
				: lastMessageMap.get(conversation.getLastMessageId());
		vo.setLastMessage(MessageVO.from(lastMessage));
		if (conversation.getType() != null && conversation.getType() == ConversationType.SINGLE.getCode()
				&& peerId != null) {
			User peer = userMap.get(peerId);
			vo.setTargetUserId(peerId);
			vo.setName(peer == null ? "" : peer.getNickname());
			vo.setAvatar(peer == null ? "" : peer.getAvatar());
		} else {
			GroupInfo group = groupMap.get(conversation.getGroupId());
			vo.setName(group == null ? "群聊" : group.getName());
			vo.setAvatar(group == null ? "" : group.getAvatar());
		}
		return vo;
	}
}
