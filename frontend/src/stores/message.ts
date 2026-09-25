import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as conversationApi from '@/api/conversation'
import type { Message } from '@/types/message'

const PAGE_SIZE = 20

export const useMessageStore = defineStore('message', () => {
	const conversationId = ref<number | null>(null)
	const messages = ref<Message[]>([])
	const hasMore = ref(false)
	const lastReadId = ref(0)

	function open(id: number): void {
		if (conversationId.value === id) return
		conversationId.value = id
		messages.value = []
		hasMore.value = false
		lastReadId.value = 0
	}

	function reset(): void {
		conversationId.value = null
		messages.value = []
		hasMore.value = false
		lastReadId.value = 0
	}

	/** 拉取历史消息；before 为空拉第一页，否则向前翻页，返回是否还有更早消息 */
	async function loadHistory(before?: number): Promise<boolean> {
		if (conversationId.value === null) return false
		const list = await conversationApi.listMessages(conversationId.value, before, PAGE_SIZE)
		if (before === undefined) messages.value = list
		else messages.value = [...list, ...messages.value]
		hasMore.value = list.length >= PAGE_SIZE
		return hasMore.value
	}

	function append(message: Message): void {
		if (messages.value.some((m) => m.id === message.id)) return
		messages.value.push(message)
	}

	function markRead(upToId: number): void {
		if (upToId > lastReadId.value) lastReadId.value = upToId
	}

	return {
		conversationId,
		messages,
		hasMore,
		lastReadId,
		open,
		reset,
		loadHistory,
		append,
		markRead,
	}
})
