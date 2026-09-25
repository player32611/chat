<template>
	<view v-if="visible" class="popup-mask" @click="emit('close')">
		<view class="popup-card" @click.stop>
			<UserAvatar :src="user?.avatar" :name="user?.nickname" :size="128" />
			<text class="popup-nickname">{{ user?.nickname }}</text>
			<text class="popup-phone">{{ user?.phone }}</text>
			<view class="popup-actions">
				<slot />
			</view>
		</view>
	</view>
</template>

<script setup lang="ts">
import UserAvatar from './UserAvatar.vue'
import type { User } from '@/types/user'

defineProps<{
	user: User | null
	visible: boolean
}>()

const emit = defineEmits<{ (e: 'close'): void }>()
</script>

<style lang="scss" scoped>
.popup-mask {
	position: fixed;
	top: 0;
	right: 0;
	bottom: 0;
	left: 0;
	z-index: 999;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(0, 0, 0, 0.5);
}

.popup-card {
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 520rpx;
	padding: $space-xl $space-lg;
	background: $color-bg-white;
	border-radius: $radius-lg;
}

.popup-nickname {
	margin-top: $space-md;
	font-size: 34rpx;
	font-weight: 600;
}

.popup-phone {
	margin-top: $space-xs;
	font-size: 26rpx;
	color: $color-text-secondary;
}

.popup-actions {
	width: 100%;
	margin-top: $space-lg;
}
</style>
