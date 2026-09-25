<template>
	<view class="login-page">
		<view class="login-header">
			<text class="login-title">聊天室</text>
			<text class="login-subtitle">手机号验证码登录</text>
		</view>

		<view class="login-form">
			<view class="form-item">
				<IconFont name="phone" :size="36" color="#999999" />
				<input
					v-model="phone"
					class="form-input"
					type="number"
					maxlength="11"
					placeholder="请输入手机号"
				/>
			</view>

			<view class="form-item">
				<IconFont name="verify" :size="36" color="#999999" />
				<input
					v-model="code"
					class="form-input"
					type="number"
					maxlength="6"
					placeholder="请输入验证码"
				/>
				<view
					class="code-btn"
					:class="{ 'code-btn-disabled': countdown > 0 }"
					@click="handleSendCode"
				>
					<text>{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}</text>
				</view>
			</view>

			<button class="login-btn" :loading="loading" :disabled="loading" @click="handleLogin">
				登录
			</button>
		</view>
	</view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onShow, onUnload } from '@dcloudio/uni-app'
import IconFont from '@/components/IconFont.vue'
import { useAuth } from '@/composables/useAuth'
import { isPhone } from '@/utils/validator'

const { authStore, countdown, sendCode, login, stopCountdown } = useAuth()

const phone = ref('')
const code = ref('')
const loading = ref(false)

onShow(() => {
	if (authStore.isLoggedIn) {
		uni.reLaunch({ url: '/pages/conversation/list' })
	}
})

onUnload(() => {
	stopCountdown()
})

async function handleSendCode(): Promise<void> {
	if (countdown.value > 0) return
	await sendCode(phone.value)
}

async function handleLogin(): Promise<void> {
	if (!isPhone(phone.value)) {
		uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
		return
	}
	if (code.value.length !== 6) {
		uni.showToast({ title: '请输入6位验证码', icon: 'none' })
		return
	}
	loading.value = true
	try {
		await login(phone.value, code.value)
		uni.showToast({ title: '登录成功', icon: 'success' })
		uni.reLaunch({ url: '/pages/conversation/list' })
	} catch {
		// 错误提示已在请求层统一处理
	} finally {
		loading.value = false
	}
}
</script>

<style lang="scss" scoped>
.login-page {
	min-height: 100vh;
	padding: 0 $space-lg;
	background: linear-gradient(180deg, $color-primary 0%, $color-bg 40%);
}

.login-header {
	padding: 160rpx 0 80rpx;

	.login-title {
		display: block;
		font-size: 64rpx;
		font-weight: 700;
		color: #ffffff;
	}

	.login-subtitle {
		display: block;
		margin-top: $space-sm;
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.85);
	}
}

.login-form {
	padding: $space-lg;
	background: $color-bg-white;
	border-radius: $radius-lg;
}

.form-item {
	display: flex;
	align-items: center;
	height: 100rpx;
	border-bottom: 1rpx solid $color-border;

	.form-input {
		flex: 1;
		height: 100%;
		margin-left: $space-sm;
		font-size: 30rpx;
	}

	.code-btn {
		padding: 12rpx 0 12rpx 24rpx;
		font-size: 26rpx;
		color: $color-primary;
	}

	.code-btn-disabled {
		color: $color-text-secondary;
	}
}

.login-btn {
	margin-top: $space-xl;
	font-size: 32rpx;
	color: #ffffff;
	background: $color-primary;
	border-radius: $radius-md;

	&::after {
		border: none;
	}
}
</style>
