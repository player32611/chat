<template>
	<view class="group-create-page">
		<view class="name-field">
			<text class="name-label">群名称</text>
			<input v-model="name" class="name-input" placeholder="请输入群名称" maxlength="20" />
		</view>

		<view class="section-title">选择好友（已选 {{ selectedIds.length }}）</view>

		<view v-if="friends.length === 0" class="empty">暂无好友，先去添加好友吧</view>

		<view
			v-for="friend in friends"
			:key="friend.friendId"
			class="friend-item"
			@click="toggle(friend.friendId)"
		>
			<view class="check" :class="{ checked: isSelected(friend.friendId) }">
				<IconFont v-if="isSelected(friend.friendId)" name="check" :size="28" color="#ffffff" />
			</view>
			<UserAvatar :src="friend.user.avatar" :name="friend.user.nickname" :size="80" />
			<text class="friend-name">{{ friend.remark || friend.user.nickname }}</text>
		</view>

		<button class="create-btn" :disabled="!canSubmit" @click="submit">创建群聊</button>
	</view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useAuthGuard } from '@/composables/useAuthGuard'
import IconFont from '@/components/IconFont.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import * as friendApi from '@/api/friend'
import * as groupApi from '@/api/group'
import type { Friend } from '@/types/friend'

const authStore = useAuthStore()
useAuthGuard()

const name = ref('')
const friends = ref<Friend[]>([])
const selectedIds = ref<number[]>([])

const canSubmit = computed(() => name.value.trim() !== '' && selectedIds.value.length > 0)

onShow(async () => {
	if (!authStore.isLoggedIn) return
	friends.value = await friendApi.listFriends()
})

function isSelected(id: number): boolean {
	return selectedIds.value.includes(id)
}

function toggle(id: number): void {
	const index = selectedIds.value.indexOf(id)
	if (index === -1) selectedIds.value.push(id)
	else selectedIds.value.splice(index, 1)
}

async function submit(): Promise<void> {
	if (!canSubmit.value) return
	try {
		const group = await groupApi.createGroup(name.value.trim(), '', selectedIds.value)
		uni.redirectTo({
			url: `/pages/chat/chat?id=${group.conversationId}&type=2&groupId=${group.id}&name=${encodeURIComponent(group.name)}&avatar=${encodeURIComponent(group.avatar)}`,
		})
	} catch {
		// 错误已在请求层统一处理
	}
}
</script>

<style lang="scss" scoped>
.group-create-page {
	padding: $space-md;
}

.name-field {
	display: flex;
	align-items: center;
	padding: $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
}

.name-label {
	flex-shrink: 0;
	margin-right: $space-md;
	font-size: 30rpx;
}

.name-input {
	flex: 1;
	font-size: 30rpx;
}

.section-title {
	margin: $space-lg 0 $space-sm;
	font-size: 26rpx;
	color: $color-text-secondary;
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

.friend-item + .friend-item {
	margin-top: $space-xs;
}

.check {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 40rpx;
	height: 40rpx;
	margin-right: $space-md;
	border: 2rpx solid $color-border;
	border-radius: $radius-round;
}

.check.checked {
	background: $color-primary;
	border-color: $color-primary;
}

.friend-name {
	margin-left: $space-md;
	font-size: 30rpx;
}

.create-btn {
	margin-top: $space-xl;
	color: #ffffff;
	background: $color-primary;
}

.create-btn[disabled] {
	color: rgba(255, 255, 255, 0.7);
	background: #b2dfc4;
}
</style>
