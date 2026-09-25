<template>
	<view class="bubble-wrap" :class="self ? 'is-self' : 'is-other'">
		<view v-if="timeText" class="time-group">
			<text class="time-group-text">{{ timeText }}</text>
		</view>
		<view class="bubble-row">
			<UserAvatar v-if="!self" class="avatar" :src="avatar" :name="name" :size="76" />
			<view class="content">
				<text v-if="!self" class="sender-name">{{ name }}</text>
				<view class="line">
					<text v-if="self && read" class="read-text">已读</text>
					<image
						v-if="message.type === MESSAGE_TYPE_IMAGE"
						class="msg-image"
						:src="message.content"
						mode="widthFix"
						@click="preview"
					/>
					<view v-else class="bubble">
						<text class="msg-text">{{ message.content }}</text>
					</view>
				</view>
			</view>
			<UserAvatar v-if="self" class="avatar" :src="avatar" :name="name" :size="76" />
		</view>
	</view>
</template>

<script setup lang="ts">
import UserAvatar from './UserAvatar.vue'
import { MESSAGE_TYPE_IMAGE } from '@/types/message'
import type { Message } from '@/types/message'

const props = defineProps<{
	message: Message
	self: boolean
	avatar: string
	name: string
	timeText: string
	read: boolean
}>()

function preview(): void {
	if (props.message.type === MESSAGE_TYPE_IMAGE) {
		uni.previewImage({ urls: [props.message.content] })
	}
}
</script>

<style lang="scss" scoped>
.bubble-wrap {
	display: flex;
	flex-direction: column;
	padding: 0 $space-md;
}

.time-group {
	display: flex;
	justify-content: center;
	margin: $space-md 0;
}

.time-group-text {
	padding: 4rpx 16rpx;
	font-size: 22rpx;
	color: $color-text-secondary;
	background: rgba(0, 0, 0, 0.06);
	border-radius: $radius-sm;
}

.bubble-row {
	display: flex;
	align-items: flex-start;
}

.is-self .bubble-row {
	justify-content: flex-end;
}

.is-other .bubble-row {
	justify-content: flex-start;
}

.avatar {
	flex-shrink: 0;
}

.is-self .avatar {
	margin-left: $space-sm;
}

.is-other .avatar {
	margin-right: $space-sm;
}

.content {
	display: flex;
	flex-direction: column;
	min-width: 0;
	max-width: 70%;
}

.is-self .content {
	align-items: flex-end;
}

.is-other .content {
	align-items: flex-start;
}

.sender-name {
	margin-bottom: 6rpx;
	font-size: 22rpx;
	color: $color-text-secondary;
}

.line {
	display: flex;
	align-items: flex-end;
	min-width: 0;
}

.read-text {
	margin-right: $space-xs;
	font-size: 20rpx;
	color: $color-text-secondary;
}

.bubble {
	padding: $space-sm $space-md;
	border-radius: $radius-md;
}

.is-self .bubble {
	background: $color-primary;
	border-top-right-radius: $radius-sm;
}

.is-other .bubble {
	background: $color-bg-white;
	border-top-left-radius: $radius-sm;
}

.msg-text {
	font-size: 30rpx;
	line-height: 1.5;
	word-break: break-all;
}

.is-self .msg-text {
	color: #ffffff;
}

.is-other .msg-text {
	color: $color-text;
}

.msg-image {
	max-width: 360rpx;
	background: $color-bg;
	border-radius: $radius-md;
}
</style>
