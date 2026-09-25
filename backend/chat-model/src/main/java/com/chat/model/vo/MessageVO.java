package com.chat.model.vo;

import com.chat.model.entity.Message;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息视图对象。
 */
@Data
public class MessageVO {

	private Long id;

	private Long conversationId;

	private Long senderId;

	/** 发送人昵称（群聊展示用） */
	private String senderName;

	/** 发送人头像（群聊展示用） */
	private String senderAvatar;

	private Integer type;

	private String content;

	private LocalDateTime createdAt;

	public static MessageVO from(Message message) {
		if (message == null) {
			return null;
		}
		MessageVO vo = new MessageVO();
		vo.setId(message.getId());
		vo.setConversationId(message.getConversationId());
		vo.setSenderId(message.getSenderId());
		vo.setType(message.getType());
		vo.setContent(message.getContent());
		vo.setCreatedAt(message.getCreatedAt());
		return vo;
	}
}
