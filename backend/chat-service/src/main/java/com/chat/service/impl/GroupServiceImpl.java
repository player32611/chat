package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.common.exception.BusinessException;
import com.chat.mapper.ConversationMapper;
import com.chat.mapper.ConversationMemberMapper;
import com.chat.mapper.GroupMapper;
import com.chat.mapper.UserMapper;
import com.chat.model.entity.Conversation;
import com.chat.model.entity.ConversationMember;
import com.chat.model.entity.GroupInfo;
import com.chat.model.entity.User;
import com.chat.model.enums.ConversationType;
import com.chat.model.enums.MemberRole;
import com.chat.model.vo.GroupMemberVO;
import com.chat.model.vo.GroupVO;
import com.chat.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 群聊业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

	private final GroupMapper groupMapper;
	private final ConversationMapper conversationMapper;
	private final ConversationMemberMapper memberMapper;
	private final UserMapper userMapper;

	@Transactional
	@Override
	public GroupVO createGroup(Long ownerId, String name, String avatar, List<Long> memberIds) {
		List<Long> members = memberIds.stream()
				.filter(id -> !id.equals(ownerId))
				.distinct()
				.toList();

		GroupInfo group = new GroupInfo();
		group.setName(name);
		group.setAvatar(avatar == null ? "" : avatar);
		group.setOwnerId(ownerId);
		groupMapper.insert(group);

		Conversation conversation = new Conversation();
		conversation.setType(ConversationType.GROUP.getCode());
		conversation.setGroupId(group.getId());
		conversationMapper.insert(conversation);

		insertMember(conversation.getId(), ownerId, MemberRole.OWNER.getCode());
		for (Long memberId : members) {
			insertMember(conversation.getId(), memberId, MemberRole.MEMBER.getCode());
		}

		return toVO(group, conversation.getId(), members.size() + 1);
	}

	@Override
	public GroupVO getGroup(Long userId, Long groupId) {
		GroupInfo group = requireGroup(groupId);
		Conversation conversation = findConversation(groupId);
		requireMember(conversation.getId(), userId);
		int memberCount = Math.toIntExact(memberMapper.selectCount(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversation.getId())));
		return toVO(group, conversation.getId(), memberCount);
	}

	@Override
	public List<GroupMemberVO> listMembers(Long userId, Long groupId) {
		GroupInfo group = requireGroup(groupId);
		Conversation conversation = findConversation(groupId);
		requireMember(conversation.getId(), userId);
		List<ConversationMember> members = memberMapper.selectList(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversation.getId())
				.orderByAsc(ConversationMember::getRole)
				.orderByAsc(ConversationMember::getJoinedAt));
		List<Long> userIds = members.stream().map(ConversationMember::getUserId).toList();
		Map<Long, User> userMap = userIds.isEmpty() ? Map.of()
				: userMapper.selectBatchIds(userIds).stream()
						.collect(Collectors.toMap(User::getId, Function.identity()));
		return members.stream().map(m -> {
			GroupMemberVO vo = new GroupMemberVO();
			vo.setUserId(m.getUserId());
			vo.setRole(m.getRole());
			vo.setJoinedAt(m.getJoinedAt());
			User user = userMap.get(m.getUserId());
			if (user != null) {
				vo.setNickname(user.getNickname());
				vo.setAvatar(user.getAvatar());
			}
			return vo;
		}).toList();
	}

	@Transactional
	@Override
	public void addMembers(Long operatorId, Long groupId, List<Long> userIds) {
		requireGroup(groupId);
		Conversation conversation = findConversation(groupId);
		requireMember(conversation.getId(), operatorId);
		for (Long userId : userIds.stream().distinct().toList()) {
			if (isMember(conversation.getId(), userId)) {
				continue;
			}
			insertMember(conversation.getId(), userId, MemberRole.MEMBER.getCode());
		}
	}

	@Transactional
	@Override
	public void removeMember(Long operatorId, Long groupId, Long userId) {
		GroupInfo group = requireGroup(groupId);
		requireOwner(group, operatorId);
		if (userId.equals(operatorId)) {
			throw new BusinessException("群主不能移除自己");
		}
		Conversation conversation = findConversation(groupId);
		memberMapper.delete(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversation.getId())
				.eq(ConversationMember::getUserId, userId));
	}

	@Transactional
	@Override
	public GroupVO updateGroup(Long operatorId, Long groupId, String name, String avatar, String announcement) {
		GroupInfo group = requireGroup(groupId);
		requireOwner(group, operatorId);
		if (name != null && !name.isBlank()) {
			group.setName(name);
		}
		if (avatar != null) {
			group.setAvatar(avatar);
		}
		if (announcement != null) {
			group.setAnnouncement(announcement);
		}
		groupMapper.updateById(group);
		return getGroup(operatorId, groupId);
	}

	@Transactional
	@Override
	public void dissolveGroup(Long operatorId, Long groupId) {
		GroupInfo group = requireGroup(groupId);
		requireOwner(group, operatorId);
		Conversation conversation = findConversation(groupId);
		memberMapper.delete(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversation.getId()));
		conversationMapper.deleteById(conversation.getId());
		groupMapper.deleteById(group.getId());
	}

	private GroupInfo requireGroup(Long groupId) {
		GroupInfo group = groupMapper.selectById(groupId);
		if (group == null) {
			throw new BusinessException("群不存在");
		}
		return group;
	}

	private Conversation findConversation(Long groupId) {
		Conversation conversation = conversationMapper.selectOne(new LambdaQueryWrapper<Conversation>()
				.eq(Conversation::getGroupId, groupId)
				.eq(Conversation::getType, ConversationType.GROUP.getCode()));
		if (conversation == null) {
			throw new BusinessException("群会话不存在");
		}
		return conversation;
	}

	private void requireOwner(GroupInfo group, Long userId) {
		if (!group.getOwnerId().equals(userId)) {
			throw new BusinessException("只有群主才能执行此操作");
		}
	}

	private void requireMember(Long conversationId, Long userId) {
		if (!isMember(conversationId, userId)) {
			throw new BusinessException("不是群成员");
		}
	}

	private boolean isMember(Long conversationId, Long userId) {
		return memberMapper.selectCount(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversationId)
				.eq(ConversationMember::getUserId, userId)) > 0;
	}

	private void insertMember(Long conversationId, Long userId, int role) {
		ConversationMember member = new ConversationMember();
		member.setConversationId(conversationId);
		member.setUserId(userId);
		member.setRole(role);
		member.setUnreadCount(0);
		member.setLastReadMessageId(0L);
		memberMapper.insert(member);
	}

	private GroupVO toVO(GroupInfo group, Long conversationId, int memberCount) {
		GroupVO vo = new GroupVO();
		vo.setId(group.getId());
		vo.setName(group.getName());
		vo.setAvatar(group.getAvatar());
		vo.setOwnerId(group.getOwnerId());
		vo.setAnnouncement(group.getAnnouncement());
		vo.setMemberCount(memberCount);
		vo.setConversationId(conversationId);
		vo.setCreatedAt(group.getCreatedAt());
		return vo;
	}
}
