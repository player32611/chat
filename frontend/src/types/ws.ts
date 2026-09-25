import type { Message } from './message'

/** 服务端推送：新消息 */
export interface WsMessage {
	type: 'message'
	data: Message
}

/** 服务端推送：已读回执 */
export interface WsRead {
	type: 'read'
	data: {
		conversationId: number
		userId: number
		lastReadMessageId: number
	}
}

/** 服务端推送：心跳响应 */
export interface WsPong {
	type: 'pong'
}

/** 服务端推送：错误 */
export interface WsError {
	type: 'error'
	data: {
		msg: string
	}
}

/** 服务端推送：在线状态变化 */
export interface WsPresence {
	type: 'presence'
	data: {
		userId: number
		online: boolean
	}
}

/** 服务端下行的所有消息类型 */
export type WsIncoming = WsMessage | WsRead | WsPong | WsError | WsPresence
