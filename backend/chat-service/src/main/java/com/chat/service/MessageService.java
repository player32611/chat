package com.chat.service;

import com.chat.model.vo.MessageVO;

/**
 * 消息业务接口。
 */
public interface MessageService {

	/**
	 * 发送消息（落库 + 更新会话 + 其他成员未读自增），返回消息 VO。
	 */
	MessageVO sendMessage(Long senderId, Long conversationId, int type, String content);
}
