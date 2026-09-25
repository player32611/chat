package com.chat.model.vo;

import com.chat.model.entity.FriendRequest;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 好友申请视图对象。
 */
@Data
public class FriendRequestVO {

	private Long id;

	private Long fromUserId;

	private Long toUserId;

	private String message;

	/** 状态：0 待处理 1 同意 2 拒绝 */
	private Integer status;

	private LocalDateTime createdAt;

	/** 申请人信息（收到的申请时填充） */
	private UserVO fromUser;

	/** 接收人信息（发出的申请时填充） */
	private UserVO toUser;

	public static FriendRequestVO from(FriendRequest request) {
		FriendRequestVO vo = new FriendRequestVO();
		vo.setId(request.getId());
		vo.setFromUserId(request.getFromUserId());
		vo.setToUserId(request.getToUserId());
		vo.setMessage(request.getMessage());
		vo.setStatus(request.getStatus());
		vo.setCreatedAt(request.getCreatedAt());
		return vo;
	}
}
