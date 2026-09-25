package com.chat.model.dto.group;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改群信息请求（仅群主）。
 */
@Data
public class GroupUpdateDTO {

	@Size(max = 50, message = "群名称过长")
	private String name;

	private String avatar;

	@Size(max = 255, message = "群公告过长")
	private String announcement;
}
