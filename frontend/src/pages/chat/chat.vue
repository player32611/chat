<template>
	<view class="chat-page">
		<view v-if="isGroup" class="group-bar" @click="goGroupDetail">
			<text class="group-bar-text">{{ peerName || '群聊' }}</text>
			<IconFont name="arrowRight" :size="28" color="#999999" />
		</view>
		<scroll-view class="msg-list" scroll-y :scroll-into-view="scrollInto" scroll-with-animation>
			<view class="msg-inner">
				<view v-if="messageStore.hasMore" class="load-more" @click="loadOlder">
					<text class="load-more-text">查看更早消息</text>
				</view>
				<view
					v-for="(message, index) in messageStore.messages"
					:id="`msg-${message.id}`"
					:key="message.id"
				>
					<MessageBubble
						:message="message"
						:self="message.senderId === myId"
						:avatar="message.senderId === myId ? myAvatar : message.senderAvatar || peerAvatar"
						:name="message.senderId === myId ? myName : message.senderName || peerName"
						:time-text="timeTextOf(index)"
						:read="isRead(message)"
					/>
				</view>
			</view>
		</scroll-view>

		<view class="input-bar">
			<view class="img-btn" @click="sendImage">
				<IconFont name="image" :size="44" color="#666666" />
			</view>
			<view class="input-box">
				<input
					v-model="draft"
					class="input"
					placeholder="请输入消息"
					confirm-type="send"
					@confirm="sendText"
				/>
			</view>
			<view class="send-btn" @click="sendText">
				<IconFont name="send" :size="40" color="#ffffff" />
			</view>
		</view>
	</view>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { useConversationStore } from '@/stores/conversation'
import { useWebSocket } from '@/composables/useWebSocket'
import MessageBubble from '@/components/MessageBubble.vue'
import IconFont from '@/components/IconFont.vue'
import * as conversationApi from '@/api/conversation'
import { uploadImage } from '@/api/file'
import { CONVERSATION_TYPE_GROUP } from '@/types/conversation'
import { MESSAGE_TYPE_IMAGE, MESSAGE_TYPE_TEXT } from '@/types/message'
import { formatTime, isNewTimeGroup } from '@/utils/time'
import type { Message } from '@/types/message'
import type { WsIncoming } from '@/types/ws'

const authStore = useAuthStore()
const messageStore = useMessageStore()
const conversationStore = useConversationStore()

const conversationId = ref(0)
const conversationType = ref(1)
const groupId = ref(0)
const peerName = ref('')
const peerAvatar = ref('')
const draft = ref('')
const scrollInto = ref('')
let inited = false

const myId = computed(() => authStore.user?.id ?? 0)
const myAvatar = computed(() => authStore.user?.avatar ?? '')
const myName = computed(() => authStore.user?.nickname ?? '我')
const isGroup = computed(() => conversationType.value === CONVERSATION_TYPE_GROUP)

const { sendChat, sendRead } = useWebSocket((payload: WsIncoming) => {
	if (payload.type === 'message') {
		handleIncoming(payload.data)
	} else if (payload.type === 'read') {
		if (payload.data.conversationId === conversationId.value) {
			messageStore.markRead(payload.data.lastReadMessageId)
		}
	}
})

onLoad((options) => {
	conversationId.value = Number(options?.id ?? 0)
	conversationType.value = Number(options?.type ?? 1)
	groupId.value = Number(options?.groupId ?? 0)
	peerName.value = safeDecode(options?.name)
	peerAvatar.value = safeDecode(options?.avatar)
	messageStore.open(conversationId.value)
	uni.setNavigationBarTitle({ title: peerName.value || '聊天' })
})

onShow(async () => {
	if (conversationId.value === 0) return
	try {
		await authStore.ensureUser()
	} catch {
		// 拉取用户信息失败不阻塞会话加载
	}
	if (inited) return
	inited = true
	await loadInitial()
})

function handleIncoming(message: Message): void {
	if (message.conversationId === conversationId.value) {
		messageStore.append(message)
		conversationStore.applyIncoming(message, true)
		conversationStore.clearUnread(conversationId.value)
		void sendRead(conversationId.value)
		scrollToBottom()
	} else {
		conversationStore.applyIncoming(message, false)
	}
}

async function loadInitial(): Promise<void> {
	try {
		await messageStore.loadHistory()
		scrollToBottom()
		await conversationApi.markRead(conversationId.value)
		conversationStore.clearUnread(conversationId.value)
	} catch {
		// 错误提示已在请求层统一处理
	}
}

async function loadOlder(): Promise<void> {
	const first = messageStore.messages[0]
	if (!first) return
	await messageStore.loadHistory(first.id)
}

function sendText(): void {
	const content = draft.value.trim()
	if (!content) return
	sendChat(conversationId.value, MESSAGE_TYPE_TEXT, content)
	draft.value = ''
}

async function sendImage(): Promise<void> {
	const path = await chooseImage()
	if (!path) return
	uni.showLoading({ title: '上传中' })
	try {
		const url = await uploadImage(path)
		sendChat(conversationId.value, MESSAGE_TYPE_IMAGE, url)
	} catch {
		// 错误提示已统一处理
	} finally {
		uni.hideLoading()
	}
}

function chooseImage(): Promise<string> {
	return new Promise((resolve) => {
		uni.chooseImage({
			count: 1,
			success: (res) => resolve(res.tempFilePaths[0] ?? ''),
			fail: () => resolve(''),
		})
	})
}

function timeTextOf(index: number): string {
	const messages = messageStore.messages
	const message = messages[index]
	if (!message) return ''
	const prev = index > 0 ? messages[index - 1] : undefined
	if (!prev || isNewTimeGroup(prev.createdAt, message.createdAt)) {
		return formatTime(message.createdAt)
	}
	return ''
}

function isRead(message: Message): boolean {
	if (isGroup.value) return false
	if (message.senderId !== myId.value) return false
	const lastSelf = [...messageStore.messages].reverse().find((m) => m.senderId === myId.value)
	if (!lastSelf || lastSelf.id !== message.id) return false
	return messageStore.lastReadId >= message.id
}

function goGroupDetail(): void {
	if (groupId.value === 0) return
	uni.navigateTo({ url: `/pages/group/detail?id=${groupId.value}` })
}

function scrollToBottom(): void {
	nextTick(() => {
		const messages = messageStore.messages
		if (messages.length === 0) return
		scrollInto.value = `msg-${messages[messages.length - 1].id}`
	})
}

function safeDecode(value: string | undefined): string {
	if (!value) return ''
	try {
		return decodeURIComponent(value)
	} catch {
		return value
	}
}
</script>

<style lang="scss" scoped>
.chat-page {
	display: flex;
	flex-direction: column;
	height: 100vh;
	height: calc(100vh - var(--window-top) - var(--window-bottom));
	background: $color-bg;
	overflow: hidden;
}

.group-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: $space-sm $space-md;
	background: $color-bg-white;
	border-bottom: 1rpx solid $color-border;
}

.group-bar-text {
	font-size: 26rpx;
	color: $color-text-secondary;
}

.msg-list {
	flex: 1;
	overflow: hidden;
}

.msg-inner {
	padding: $space-md 0;
}

.load-more {
	display: flex;
	justify-content: center;
	padding: $space-sm 0;
}

.load-more-text {
	font-size: 24rpx;
	color: $color-text-secondary;
}

.input-bar {
	display: flex;
	align-items: center;
	padding: $space-sm $space-md;
	background: $color-bg-white;
	border-top: 1rpx solid $color-border;
}

.img-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 64rpx;
	height: 64rpx;
}

.input-box {
	flex: 1;
	display: flex;
	align-items: center;
	height: 72rpx;
	margin: 0 $space-sm;
	padding: 0 $space-md;
	background: $color-bg;
	border-radius: $radius-lg;
	overflow: hidden;
}

.input {
	flex: 1;
	height: 72rpx;
	font-size: 30rpx;
	background: transparent;
}

.send-btn {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 72rpx;
	height: 72rpx;
	background: $color-primary;
	border-radius: $radius-round;
}
</style>
