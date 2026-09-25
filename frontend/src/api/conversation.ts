import { request } from './request'
import type { Conversation } from '@/types/conversation'
import type { Message } from '@/types/message'

export function listConversations(): Promise<Conversation[]> {
	return request<Conversation[]>({ url: '/api/conversation/list' })
}

export function getOrCreateSingle(targetUserId: number): Promise<Conversation> {
	return request<Conversation>({
		url: '/api/conversation/single',
		method: 'POST',
		data: { targetUserId },
	})
}

export function listMessages(
	conversationId: number,
	before?: number,
	limit = 20
): Promise<Message[]> {
	const data: Record<string, unknown> = { limit }
	if (before !== undefined) data.before = before
	return request<Message[]>({ url: `/api/conversation/${conversationId}/messages`, data })
}

export function markRead(conversationId: number): Promise<null> {
	return request<null>({ url: `/api/conversation/${conversationId}/read`, method: 'POST' })
}
