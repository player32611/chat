<template>
	<image v-if="src" class="avatar" :src="src" :style="sizeStyle" mode="aspectFill" />
	<view v-else class="avatar avatar-fallback" :style="sizeStyle">
		<text class="avatar-text">{{ initial }}</text>
	</view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
	defineProps<{
		src?: string
		name?: string
		size?: number
	}>(),
	{
		src: '',
		name: '',
		size: 88,
	}
)

const initial = computed(() => (props.name || '用').charAt(0).toUpperCase())

const sizeStyle = computed(() => ({
	width: `${props.size}rpx`,
	height: `${props.size}rpx`,
	fontSize: `${Math.round(props.size * 0.42)}rpx`,
}))
</script>

<style lang="scss" scoped>
.avatar {
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;
	background: $color-primary;
	border-radius: $radius-round;
}

.avatar-text {
	font-weight: 600;
	color: #ffffff;
}
</style>
