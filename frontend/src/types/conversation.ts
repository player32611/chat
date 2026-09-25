import type { Message } from './message'

/** 会话类型：单聊 */
export const CONVERSATION_TYPE_SINGLE = 1
/** 会话类型：群聊 */
export const CONVERSATION_TYPE_GROUP = 2

/** 会话 */
export interface Conversation {
	id: number
	type: number
	groupId: number | null
	unreadCount: number
	/** 单聊对方昵称或群名称 */
	name: string
	/** 单聊对方头像或群头像 */
	avatar: string
	/** 单聊对方用户 ID */
	targetUserId: number | null
	lastMessage: Message | null
	lastMessageAt: string | null
}
