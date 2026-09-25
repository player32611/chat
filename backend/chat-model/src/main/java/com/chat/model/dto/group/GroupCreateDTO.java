package com.chat.model.dto.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建群聊请求。
 */
@Data
public class GroupCreateDTO {

	@NotBlank(message = "群名称不能为空")
	@Size(max = 50, message = "群名称过长")
	private String name;

	private String avatar;

	@NotEmpty(message = "请选择群成员")
	private List<Long> memberIds;
}
