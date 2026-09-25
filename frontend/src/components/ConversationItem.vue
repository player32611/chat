<template>
	<view class="conversation-item" @click="emit('click', conversation)">
		<view class="avatar-wrap">
			<UserAvatar :src="conversation.avatar" :name="conversation.name" :size="96" />
			<view v-if="conversation.unreadCount > 0" class="badge">
				<text class="badge-text">{{
					conversation.unreadCount > 99 ? '99+' : conversation.unreadCount
				}}</text>
			</view>
		</view>
		<view class="info">
			<view class="info-top">
				<text class="name ellipsis">{{ conversation.name }}</text>
				<text class="time">{{ formatConversationTime(conversation.lastMessageAt) }}</text>
			</view>
			<text class="preview ellipsis">{{ preview }}</text>
		</view>
	</view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import UserAvatar from './UserAvatar.vue'
import { MESSAGE_TYPE_IMAGE } from '@/types/message'
import { formatConversationTime } from '@/utils/time'
import type { Conversation } from '@/types/conversation'

const props = defineProps<{ conversation: Conversation }>()

const emit = defineEmits<{ (e: 'click', conversation: Conversation): void }>()

const preview = computed(() => {
	const message = props.conversation.lastMessage
	if (!message) return '暂无消息'
	return message.type === MESSAGE_TYPE_IMAGE ? '[图片]' : message.content
})
</script>

<style lang="scss" scoped>
.conversation-item {
	display: flex;
	align-items: center;
	padding: $space-md;
	background: $color-bg-white;
}

.conversation-item + .conversation-item {
	margin-top: $space-xs;
}

.avatar-wrap {
	position: relative;
}

.badge {
	position: absolute;
	top: -4rpx;
	right: -4rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	min-width: 32rpx;
	height: 32rpx;
	padding: 0 8rpx;
	background: $color-danger;
	border-radius: $radius-round;
}

.badge-text {
	font-size: 20rpx;
	color: #ffffff;
}

.info {
	flex: 1;
	min-width: 0;
	margin-left: $space-md;
	overflow: hidden;
}

.info-top {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.name {
	flex: 1;
	min-width: 0;
	font-size: 30rpx;
	font-weight: 500;
}

.time {
	flex-shrink: 0;
	margin-left: $space-sm;
	font-size: 22rpx;
	color: $color-text-secondary;
}

.preview {
	display: block;
	margin-top: $space-xs;
	font-size: 26rpx;
	color: $color-text-secondary;
}
</style>
