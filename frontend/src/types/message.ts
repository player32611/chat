/** 消息类型：文本 */
export const MESSAGE_TYPE_TEXT = 1
/** 消息类型：图片 */
export const MESSAGE_TYPE_IMAGE = 2

/** 消息 */
export interface Message {
	id: number
	conversationId: number
	senderId: number
	/** 发送人昵称（群聊展示用，单聊对方昵称） */
	senderName?: string
	/** 发送人头像 */
	senderAvatar?: string
	type: number
	content: string
	createdAt: string
}
