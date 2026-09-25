package com.chat.model.dto.group;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 添加群成员请求。
 */
@Data
public class GroupMemberAddDTO {

	@NotEmpty(message = "请选择要添加的成员")
	private List<Long> userIds;
}
