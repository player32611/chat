package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chat.common.exception.BusinessException;
import com.chat.mapper.ConversationMapper;
import com.chat.mapper.ConversationMemberMapper;
import com.chat.mapper.MessageMapper;
import com.chat.mapper.UserMapper;
import com.chat.model.entity.Conversation;
import com.chat.model.entity.ConversationMember;
import com.chat.model.entity.Message;
import com.chat.model.entity.User;
import com.chat.model.enums.MessageType;
import com.chat.model.vo.MessageVO;
import com.chat.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 消息业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

	private final MessageMapper messageMapper;
	private final ConversationMapper conversationMapper;
	private final ConversationMemberMapper memberMapper;
	private final UserMapper userMapper;

	@Transactional
	@Override
	public MessageVO sendMessage(Long senderId, Long conversationId, int type, String content) {
		if (!isMember(senderId, conversationId)) {
			throw new BusinessException("不是会话成员");
		}
		if (type != MessageType.TEXT.getCode() && type != MessageType.IMAGE.getCode()) {
			throw new BusinessException("不支持的消息类型");
		}
		if (content == null || content.isBlank()) {
			throw new BusinessException("消息内容不能为空");
		}

		Message message = new Message();
		message.setConversationId(conversationId);
		message.setSenderId(senderId);
		message.setType(type);
		message.setContent(content);
		messageMapper.insert(message);

		conversationMapper.update(null, new LambdaUpdateWrapper<Conversation>()
				.eq(Conversation::getId, conversationId)
				.set(Conversation::getLastMessageId, message.getId())
				.set(Conversation::getLastMessageAt, message.getCreatedAt()));

		memberMapper.update(null, new LambdaUpdateWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversationId)
				.ne(ConversationMember::getUserId, senderId)
				.setSql("unread_count = unread_count + 1"));

		MessageVO vo = MessageVO.from(message);
		User sender = userMapper.selectById(senderId);
		if (sender != null) {
			vo.setSenderName(sender.getNickname());
			vo.setSenderAvatar(sender.getAvatar());
		}
		return vo;
	}

	private boolean isMember(Long userId, Long conversationId) {
		return memberMapper.selectCount(new LambdaQueryWrapper<ConversationMember>()
				.eq(ConversationMember::getConversationId, conversationId)
				.eq(ConversationMember::getUserId, userId)) > 0;
	}
}
