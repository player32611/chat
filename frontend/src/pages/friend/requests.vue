<template>
	<view class="requests-page">
		<view class="tabs">
			<view
				v-for="tab in tabs"
				:key="tab.key"
				class="tab"
				:class="{ active: activeTab === tab.key }"
				@click="switchTab(tab.key)"
			>
				{{ tab.label }}
			</view>
		</view>

		<view v-if="activeTab === 'received'">
			<view v-if="received.length === 0" class="empty">暂无收到的申请</view>
			<view v-for="request in received" :key="request.id" class="request-item">
				<UserAvatar :src="request.fromUser?.avatar" :name="request.fromUser?.nickname" :size="88" />
				<view class="request-info">
					<text class="request-name">{{ request.fromUser?.nickname }}</text>
					<text class="request-msg">{{ request.message || '请求添加你为好友' }}</text>
				</view>
				<view v-if="request.status === 0" class="request-actions">
					<button class="action-btn accept" size="mini" @click="handleAccept(request)">同意</button>
					<button class="action-btn reject" size="mini" @click="handleReject(request)">拒绝</button>
				</view>
				<text v-else class="request-status">{{ statusText(request.status) }}</text>
			</view>
		</view>

		<view v-else>
			<view v-if="sent.length === 0" class="empty">暂无发出的申请</view>
			<view v-for="request in sent" :key="request.id" class="request-item">
				<UserAvatar :src="request.toUser?.avatar" :name="request.toUser?.nickname" :size="88" />
				<view class="request-info">
					<text class="request-name">{{ request.toUser?.nickname }}</text>
					<text class="request-msg">{{ request.message || '请求添加对方为好友' }}</text>
				</view>
				<text class="request-status">{{ statusText(request.status) }}</text>
			</view>
		</view>
	</view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useAuthGuard } from '@/composables/useAuthGuard'
import UserAvatar from '@/components/UserAvatar.vue'
import * as friendApi from '@/api/friend'
import type { FriendRequest } from '@/types/friend'

const authStore = useAuthStore()
useAuthGuard()

const tabs = [
	{ key: 'received', label: '收到的申请' },
	{ key: 'sent', label: '发出的申请' },
] as const

const activeTab = ref<'received' | 'sent'>('received')
const received = ref<FriendRequest[]>([])
const sent = ref<FriendRequest[]>([])

onShow(async () => {
	if (!authStore.isLoggedIn) return
	await loadRequests()
})

async function loadRequests(): Promise<void> {
	const [r, s] = await Promise.all([friendApi.listReceived(), friendApi.listSent()])
	received.value = r
	sent.value = s
}

function switchTab(tab: 'received' | 'sent'): void {
	activeTab.value = tab
}

function statusText(status: number): string {
	if (status === 0) return '待处理'
	if (status === 1) return '已同意'
	return '已拒绝'
}

async function handleAccept(request: FriendRequest): Promise<void> {
	try {
		await friendApi.acceptRequest(request.id)
		uni.showToast({ title: '已添加为好友', icon: 'success' })
		await loadRequests()
	} catch {
		// 错误提示已在请求层统一处理
	}
}

async function handleReject(request: FriendRequest): Promise<void> {
	try {
		await friendApi.rejectRequest(request.id)
		await loadRequests()
	} catch {
		// 错误提示已在请求层统一处理
	}
}
</script>

<style lang="scss" scoped>
.requests-page {
	padding: $space-md;
}

.tabs {
	display: flex;
	margin-bottom: $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
	overflow: hidden;
}

.tab {
	flex: 1;
	padding: $space-md 0;
	font-size: 28rpx;
	color: $color-text-secondary;
	text-align: center;
}

.tab.active {
	font-weight: 600;
	color: $color-primary;
	border-bottom: 4rpx solid $color-primary;
}

.empty {
	margin-top: $space-xl;
	font-size: 28rpx;
	color: $color-text-secondary;
	text-align: center;
}

.request-item {
	display: flex;
	align-items: center;
	padding: $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
}

.request-item + .request-item {
	margin-top: $space-xs;
}

.request-info {
	flex: 1;
	margin-left: $space-md;
}

.request-name {
	font-size: 30rpx;
}

.request-msg {
	display: block;
	margin-top: $space-xs;
	font-size: 24rpx;
	color: $color-text-secondary;
}

.request-actions {
	display: flex;
}

.action-btn {
	margin-left: $space-sm;
}

.action-btn.accept {
	color: #ffffff;
	background: $color-primary;
}

.action-btn.reject {
	color: $color-text-secondary;
	background: $color-bg;
}

.request-status {
	font-size: 26rpx;
	color: $color-text-secondary;
}
</style>
