package com.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chat.model.entity.Conversation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 会话 Mapper。
 */
public interface ConversationMapper extends BaseMapper<Conversation> {

	/**
	 * 查找两个用户之间的单聊会话 ID，不存在返回 null。
	 */
	@Select("SELECT c.id FROM conversation c "
			+ "JOIN conversation_member a ON a.conversation_id = c.id AND a.deleted = 0 AND a.user_id = #{userIdA} "
			+ "JOIN conversation_member b ON b.conversation_id = c.id AND b.deleted = 0 AND b.user_id = #{userIdB} "
			+ "WHERE c.type = 1 AND c.deleted = 0 LIMIT 1")
	Long selectSingleConversationId(@Param("userIdA") Long userIdA, @Param("userIdB") Long userIdB);
}
