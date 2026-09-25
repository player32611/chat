import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as conversationApi from '@/api/conversation'
import type { Conversation } from '@/types/conversation'
import type { Message } from '@/types/message'

export const useConversationStore = defineStore('conversation', () => {
	const conversations = ref<Conversation[]>([])
	const loaded = ref(false)

	async function load(): Promise<void> {
		conversations.value = await conversationApi.listConversations()
		loaded.value = true
	}

	function sortByTime(): void {
		conversations.value.sort((a, b) => (b.lastMessageAt ?? '').localeCompare(a.lastMessageAt ?? ''))
	}

	/** 插入或更新某个会话（创建单聊后、收到消息后），并置顶 */
	function upsert(conversation: Conversation): void {
		const index = conversations.value.findIndex((c) => c.id === conversation.id)
		if (index === -1) conversations.value.push(conversation)
		else conversations.value[index] = conversation
		sortByTime()
	}

	function clearUnread(conversationId: number): void {
		const conversation = conversations.value.find((c) => c.id === conversationId)
		if (conversation) conversation.unreadCount = 0
	}

	/**
	 * 处理一条新消息：更新最后消息，非当前打开会话则未读 +1。
	 * 若本地无该会话（对方首次主动发来），触发一次重新拉取。
	 */
	function applyIncoming(message: Message, currentOpen: boolean): void {
		const conversation = conversations.value.find((c) => c.id === message.conversationId)
		if (!conversation) {
			void load()
			return
		}
		conversation.lastMessage = message
		conversation.lastMessageAt = message.createdAt
		if (!currentOpen) conversation.unreadCount += 1
		sortByTime()
	}

	return { conversations, loaded, load, upsert, clearUnread, applyIncoming }
})
