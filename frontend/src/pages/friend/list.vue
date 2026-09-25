<template>
	<view class="friend-page">
		<view class="top-bar">
			<view class="entry" @click="goSearch">
				<IconFont name="search" :size="34" color="#999999" />
				<text class="entry-text">搜索用户</text>
			</view>
			<view class="entry" @click="goRequests">
				<IconFont name="addUser" :size="34" color="#999999" />
				<text class="entry-text">新的好友</text>
				<view v-if="pendingCount > 0" class="badge">
					<text class="badge-text">{{ pendingCount }}</text>
				</view>
			</view>
			<view class="entry" @click="goGroupCreate">
				<IconFont name="group" :size="34" color="#999999" />
				<text class="entry-text">发起群聊</text>
			</view>
		</view>

		<view v-if="friends.length === 0" class="empty">还没有好友，去搜索添加吧</view>

		<view
			v-for="friend in friends"
			:key="friend.friendId"
			class="friend-item"
			@click="openCard(friend)"
		>
			<view class="avatar-box">
				<UserAvatar :src="friend.user.avatar" :name="friend.user.nickname" :size="88" />
				<view v-if="friend.online" class="online-dot" />
			</view>
			<view class="friend-info">
				<text class="friend-name">{{ friend.remark || friend.user.nickname }}</text>
			</view>
		</view>

		<UserCardPopup :user="selectedUser" :visible="cardVisible" @close="cardVisible = false">
			<button class="chat-btn" @click="handleChat">发消息</button>
			<button class="delete-btn" @click="handleDelete">删除好友</button>
		</UserCardPopup>
	</view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useAuthGuard } from '@/composables/useAuthGuard'
import IconFont from '@/components/IconFont.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import UserCardPopup from '@/components/UserCardPopup.vue'
import * as friendApi from '@/api/friend'
import * as conversationApi from '@/api/conversation'
import { useConversationStore } from '@/stores/conversation'
import { useWebSocket } from '@/composables/useWebSocket'
import type { Friend } from '@/types/friend'
import type { User } from '@/types/user'

const authStore = useAuthStore()
const conversationStore = useConversationStore()
useAuthGuard()

useWebSocket((payload) => {
	if (payload.type === 'presence') {
		const friend = friends.value.find((f) => f.friendId === payload.data.userId)
		if (friend) {
			friend.online = payload.data.online
		}
	}
})

const friends = ref<Friend[]>([])
const pendingCount = ref(0)
const cardVisible = ref(false)
const selectedUser = ref<User | null>(null)
const selectedFriendId = ref<number | null>(null)

onShow(async () => {
	if (!authStore.isLoggedIn) return
	await Promise.all([loadFriends(), loadPendingCount()])
})

async function loadFriends(): Promise<void> {
	friends.value = await friendApi.listFriends()
}

async function loadPendingCount(): Promise<void> {
	const received = await friendApi.listReceived()
	pendingCount.value = received.filter((r) => r.status === 0).length
}

function goSearch(): void {
	uni.navigateTo({ url: '/pages/user/search' })
}

function goRequests(): void {
	uni.navigateTo({ url: '/pages/friend/requests' })
}

function goGroupCreate(): void {
	uni.navigateTo({ url: '/pages/group/create' })
}

function openCard(friend: Friend): void {
	selectedUser.value = friend.user
	selectedFriendId.value = friend.friendId
	cardVisible.value = true
}

async function handleChat(): Promise<void> {
	const user = selectedUser.value
	if (!user) return
	try {
		const conversation = await conversationApi.getOrCreateSingle(user.id)
		cardVisible.value = false
		conversationStore.upsert(conversation)
		uni.navigateTo({
			url: `/pages/chat/chat?id=${conversation.id}&name=${encodeURIComponent(conversation.name)}&avatar=${encodeURIComponent(conversation.avatar)}`,
		})
	} catch {
		// 错误提示已在请求层统一处理
	}
}

function handleDelete(): void {
	const friendId = selectedFriendId.value
	if (friendId === null) return
	uni.showModal({
		title: '提示',
		content: '确定删除该好友吗？',
		success: (res) => {
			if (res.confirm) {
				void doDelete(friendId)
			}
		},
	})
}

async function doDelete(friendId: number): Promise<void> {
	try {
		await friendApi.deleteFriend(friendId)
		cardVisible.value = false
		await loadFriends()
	} catch {
		// 错误提示已在请求层统一处理
	}
}
</script>

<style lang="scss" scoped>
.friend-page {
	padding: $space-md;
}

.top-bar {
	display: flex;
	margin-bottom: $space-md;
}

.entry {
	display: flex;
	align-items: center;
	flex: 1;
	padding: $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
}

.entry + .entry {
	margin-left: $space-md;
}

.entry-text {
	margin-left: $space-sm;
	font-size: 28rpx;
}

.badge {
	display: flex;
	align-items: center;
	justify-content: center;
	min-width: 32rpx;
	height: 32rpx;
	margin-left: $space-sm;
	padding: 0 8rpx;
	background: $color-danger;
	border-radius: $radius-round;
}

.badge-text {
	font-size: 22rpx;
	color: #ffffff;
}

.empty {
	margin-top: $space-xl;
	font-size: 28rpx;
	color: $color-text-secondary;
	text-align: center;
}

.friend-item {
	display: flex;
	align-items: center;
	padding: $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
}

.avatar-box {
	position: relative;
}

.online-dot {
	position: absolute;
	right: 0;
	bottom: 0;
	width: 20rpx;
	height: 20rpx;
	background: $color-primary;
	border: 3rpx solid #ffffff;
	border-radius: $radius-round;
}

.friend-item + .friend-item {
	margin-top: $space-xs;
}

.friend-info {
	flex: 1;
	margin-left: $space-md;
}

.friend-name {
	font-size: 30rpx;
}

.chat-btn {
	color: $color-primary;
	background: $color-bg-white;
}

.delete-btn {
	color: $color-danger;
	background: $color-bg-white;
}
</style>
