<template>
	<view class="profile-page">
		<view class="user-card">
			<view class="avatar-wrap" @click="changeAvatar">
				<UserAvatar :src="user?.avatar" :name="user?.nickname" :size="128" />
				<view class="avatar-mask">
					<IconFont name="camera" :size="36" color="#ffffff" />
				</view>
			</view>
			<view class="nickname-row" @click="editNickname">
				<text class="nickname">{{ user?.nickname }}</text>
				<IconFont name="arrowRight" :size="30" color="#999999" />
			</view>
			<text class="phone">{{ user?.phone }}</text>
		</view>

		<button class="logout-btn" @click="handleLogout">退出登录</button>
	</view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useAuthGuard } from '@/composables/useAuthGuard'
import UserAvatar from '@/components/UserAvatar.vue'
import IconFont from '@/components/IconFont.vue'
import { uploadImage } from '@/api/file'
import * as userApi from '@/api/user'
import type { User } from '@/types/user'

const authStore = useAuthStore()
useAuthGuard()

const user = computed<User | null>(() => authStore.user)

onShow(async () => {
	if (authStore.isLoggedIn && !authStore.user) {
		await authStore.fetchMe()
	}
})

function changeAvatar(): void {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		success: async (res) => {
			const filePath = res.tempFilePaths[0]
			if (!filePath) return
			try {
				const url = await uploadImage(filePath)
				const updated = await userApi.updateMe({ avatar: url })
				authStore.setUser(updated)
			} catch {
				// 错误提示已在请求层统一处理
			}
		},
	})
}

function editNickname(): void {
	uni.showModal({
		title: '修改昵称',
		editable: true,
		placeholderText: user.value?.nickname || '请输入昵称',
		success: async (res) => {
			if (!res.confirm) return
			const nickname = (res.content || '').trim()
			if (!nickname) {
				uni.showToast({ title: '昵称不能为空', icon: 'none' })
				return
			}
			try {
				const updated = await userApi.updateMe({ nickname })
				authStore.setUser(updated)
			} catch {
				// 错误提示已在请求层统一处理
			}
		},
	})
}

async function handleLogout(): Promise<void> {
	await authStore.logout()
	uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<style lang="scss" scoped>
.profile-page {
	padding: $space-lg;
}

.user-card {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: $space-xl 0;
	background: $color-bg-white;
	border-radius: $radius-lg;
}

.avatar-wrap {
	position: relative;
}

.avatar-mask {
	position: absolute;
	right: 0;
	bottom: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 48rpx;
	height: 48rpx;
	background: rgba(0, 0, 0, 0.45);
	border-radius: $radius-round;
}

.nickname-row {
	display: flex;
	align-items: center;
	margin-top: $space-md;
}

.nickname {
	font-size: 36rpx;
	font-weight: 600;
}

.phone {
	margin-top: $space-xs;
	font-size: 26rpx;
	color: $color-text-secondary;
}

.logout-btn {
	margin-top: $space-xl;
	color: $color-danger;
	background: $color-bg-white;
}
</style>
