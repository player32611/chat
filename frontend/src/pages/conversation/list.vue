<template>
	<view class="conversation-page">
		<view v-if="loaded && conversationStore.conversations.length === 0" class="empty">
			暂无会话，去好友列表发起聊天吧
		</view>
		<ConversationItem
			v-for="conversation in conversationStore.conversations"
			:key="conversation.id"
			:conversation="conversation"
			@click="openChat"
		/>
	</view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useConversationStore } from '@/stores/conversation'
import { useAuthGuard } from '@/composables/useAuthGuard'
import { useWebSocket } from '@/composables/useWebSocket'
import ConversationItem from '@/components/ConversationItem.vue'
import type { Conversation } from '@/types/conversation'
import type { WsIncoming } from '@/types/ws'

const authStore = useAuthStore()
const conversationStore = useConversationStore()

useAuthGuard()

useWebSocket((payload: WsIncoming) => {
	if (payload.type === 'message') {
		conversationStore.applyIncoming(payload.data, false)
	}
})

const loaded = computed(() => conversationStore.loaded)

onShow(async () => {
	if (!authStore.isLoggedIn) return
	await conversationStore.load()
})

function openChat(conversation: Conversation): void {
	conversationStore.clearUnread(conversation.id)
	const params = [
		`id=${conversation.id}`,
		`type=${conversation.type}`,
		`name=${encodeURIComponent(conversation.name)}`,
		`avatar=${encodeURIComponent(conversation.avatar)}`,
	]
	if (conversation.groupId !== null && conversation.groupId !== undefined) {
		params.push(`groupId=${conversation.groupId}`)
	}
	uni.navigateTo({ url: `/pages/chat/chat?${params.join('&')}` })
}
</script>

<style lang="scss" scoped>
.conversation-page {
	padding: $space-md;
}

.empty {
	padding: $space-xl 0;
	font-size: 28rpx;
	color: $color-text-secondary;
	text-align: center;
}
</style>
