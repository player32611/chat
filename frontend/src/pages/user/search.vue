<template>
	<view class="search-page">
		<view class="search-bar">
			<input
				v-model="keyword"
				class="search-input"
				placeholder="输入手机号或昵称"
				confirm-type="search"
				@confirm="doSearch"
			/>
			<button class="search-btn" size="mini" @click="doSearch">搜索</button>
		</view>

		<view v-if="searched && results.length === 0" class="empty">未找到用户</view>

		<view class="result-list">
			<view v-for="user in results" :key="user.id" class="result-item" @click="openCard(user)">
				<UserAvatar :src="user.avatar" :name="user.nickname" :size="88" />
				<view class="result-info">
					<text class="result-name">{{ user.nickname }}</text>
					<text class="result-phone">{{ user.phone }}</text>
				</view>
				<text class="result-relation">{{ relationText(user.relation) }}</text>
			</view>
		</view>

		<UserCardPopup :user="selectedUser" :visible="cardVisible" @close="cardVisible = false">
			<template v-if="selectedUser && selectedUser.relation === RELATION_NONE">
				<input v-model="message" class="msg-input" maxlength="100" placeholder="验证消息（可选）" />
				<button class="add-btn" @click="handleAdd">添加好友</button>
			</template>
			<template v-else-if="selectedUser">
				<text class="relation-tip">{{ relationText(selectedUser.relation) }}</text>
			</template>
		</UserCardPopup>
	</view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthGuard } from '@/composables/useAuthGuard'
import UserAvatar from '@/components/UserAvatar.vue'
import UserCardPopup from '@/components/UserCardPopup.vue'
import * as userApi from '@/api/user'
import * as friendApi from '@/api/friend'
import {
	RELATION_NONE,
	RELATION_FRIEND,
	RELATION_OUTGOING,
	RELATION_INCOMING,
} from '@/types/friend'
import type { SearchUser } from '@/types/friend'

useAuthGuard()

const keyword = ref('')
const results = ref<SearchUser[]>([])
const searched = ref(false)
const cardVisible = ref(false)
const selectedUser = ref<SearchUser | null>(null)
const message = ref('')

async function doSearch(): Promise<void> {
	const kw = keyword.value.trim()
	if (!kw) {
		uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
		return
	}
	try {
		results.value = await userApi.searchUsers(kw)
		searched.value = true
	} catch {
		// 错误提示已在请求层统一处理
	}
}

function openCard(user: SearchUser): void {
	selectedUser.value = user
	message.value = ''
	cardVisible.value = true
}

function relationText(relation: number): string {
	if (relation === RELATION_FRIEND) return '已是好友'
	if (relation === RELATION_OUTGOING) return '已申请'
	if (relation === RELATION_INCOMING) return '待处理'
	return '添加'
}

async function handleAdd(): Promise<void> {
	const user = selectedUser.value
	if (!user) return
	try {
		await friendApi.sendRequest(user.id, message.value.trim())
		uni.showToast({ title: '申请已发送', icon: 'success' })
		cardVisible.value = false
		await doSearch()
	} catch {
		// 错误提示已在请求层统一处理
	}
}
</script>

<style lang="scss" scoped>
.search-page {
	padding: $space-md;
}

.search-bar {
	display: flex;
	align-items: center;
	margin-bottom: $space-md;
}

.search-input {
	flex: 1;
	height: 68rpx;
	padding: 0 $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
	font-size: 28rpx;
}

.search-btn {
	margin-left: $space-md;
	color: #ffffff;
	background: $color-primary;
}

.empty {
	margin-top: $space-xl;
	font-size: 28rpx;
	color: $color-text-secondary;
	text-align: center;
}

.result-item {
	display: flex;
	align-items: center;
	padding: $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
}

.result-item + .result-item {
	margin-top: $space-xs;
}

.result-info {
	flex: 1;
	margin-left: $space-md;
}

.result-name {
	font-size: 30rpx;
}

.result-phone {
	display: block;
	margin-top: $space-xs;
	font-size: 24rpx;
	color: $color-text-secondary;
}

.result-relation {
	font-size: 26rpx;
	color: $color-primary;
}

.msg-input {
	height: 68rpx;
	padding: 0 $space-md;
	margin-bottom: $space-md;
	background: $color-bg;
	border-radius: $radius-md;
	font-size: 28rpx;
}

.add-btn {
	color: #ffffff;
	background: $color-primary;
}

.relation-tip {
	font-size: 28rpx;
	color: $color-text-secondary;
}
</style>
