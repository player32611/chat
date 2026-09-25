<template>
	<view class="group-detail-page">
		<view class="group-card">
			<UserAvatar :src="group?.avatar" :name="group?.name" :size="128" />
			<text class="group-name">{{ group?.name }}</text>
			<text class="group-meta">{{ group?.memberCount ?? 0 }} 人</text>
		</view>

		<view class="section-title">群成员</view>

		<view v-for="member in members" :key="member.userId" class="member-item">
			<UserAvatar :src="member.avatar" :name="member.nickname" :size="88" />
			<view class="member-info">
				<view class="member-name-row">
					<text class="member-name">{{ member.nickname }}</text>
					<text v-if="member.role === GROUP_ROLE_OWNER" class="owner-badge">群主</text>
				</view>
			</view>
			<text
				v-if="isOwner && member.userId !== myId"
				class="remove-btn"
				@click="confirmRemove(member)"
			>
				移除
			</text>
		</view>

		<view class="actions">
			<button class="action-btn" @click="openAdd">添加成员</button>
			<button v-if="isOwner" class="action-btn danger" @click="confirmDissolve">解散群聊</button>
		</view>

		<view v-if="addVisible" class="mask" @click="addVisible = false">
			<view class="add-panel" @click.stop>
				<view class="add-title">选择要添加的好友</view>
				<scroll-view scroll-y class="add-list">
					<view
						v-for="friend in addableFriends"
						:key="friend.friendId"
						class="add-item"
						@click="toggleAdd(friend.friendId)"
					>
						<view class="check" :class="{ checked: addSelected.includes(friend.friendId) }">
							<IconFont
								v-if="addSelected.includes(friend.friendId)"
								name="check"
								:size="28"
								color="#ffffff"
							/>
						</view>
						<UserAvatar :src="friend.user.avatar" :name="friend.user.nickname" :size="72" />
						<text class="add-name">{{ friend.remark || friend.user.nickname }}</text>
					</view>
					<view v-if="addableFriends.length === 0" class="add-empty">没有可添加的好友</view>
				</scroll-view>
				<button class="add-confirm" :disabled="addSelected.length === 0" @click="confirmAdd">
					确定（{{ addSelected.length }}）
				</button>
			</view>
		</view>
	</view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useAuthGuard } from '@/composables/useAuthGuard'
import IconFont from '@/components/IconFont.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import * as friendApi from '@/api/friend'
import * as groupApi from '@/api/group'
import { GROUP_ROLE_OWNER } from '@/types/group'
import type { Group, GroupMember } from '@/types/group'
import type { Friend } from '@/types/friend'

const authStore = useAuthStore()
useAuthGuard()

const groupId = ref(0)
const group = ref<Group | null>(null)
const members = ref<GroupMember[]>([])
const friends = ref<Friend[]>([])
const addVisible = ref(false)
const addSelected = ref<number[]>([])

const myId = computed(() => authStore.user?.id ?? 0)
const isOwner = computed(() => group.value !== null && group.value.ownerId === myId.value)
const addableFriends = computed(() =>
	friends.value.filter((f) => !members.value.some((m) => m.userId === f.friendId))
)

onLoad((options) => {
	groupId.value = Number(options?.id ?? 0)
})

onShow(async () => {
	if (groupId.value === 0) return
	try {
		await authStore.ensureUser()
	} catch {
		// 拉取用户信息失败不阻塞页面
	}
	await loadAll()
})

async function loadAll(): Promise<void> {
	try {
		const [g, m] = await Promise.all([
			groupApi.getGroup(groupId.value),
			groupApi.listMembers(groupId.value),
		])
		group.value = g
		members.value = m
	} catch {
		// 错误已在请求层统一处理
	}
}

async function openAdd(): Promise<void> {
	addSelected.value = []
	addVisible.value = true
	if (friends.value.length === 0) {
		friends.value = await friendApi.listFriends()
	}
}

function toggleAdd(id: number): void {
	const index = addSelected.value.indexOf(id)
	if (index === -1) addSelected.value.push(id)
	else addSelected.value.splice(index, 1)
}

async function confirmAdd(): Promise<void> {
	if (addSelected.value.length === 0) return
	try {
		await groupApi.addMembers(groupId.value, addSelected.value)
		addVisible.value = false
		await loadAll()
	} catch {
		// 错误已在请求层统一处理
	}
}

function confirmRemove(member: GroupMember): void {
	uni.showModal({
		title: '提示',
		content: `确定将「${member.nickname}」移出群聊吗？`,
		success: (res) => {
			if (res.confirm) void doRemove(member.userId)
		},
	})
}

async function doRemove(userId: number): Promise<void> {
	try {
		await groupApi.removeMember(groupId.value, userId)
		await loadAll()
	} catch {
		// 错误已在请求层统一处理
	}
}

function confirmDissolve(): void {
	uni.showModal({
		title: '提示',
		content: '确定解散该群聊吗？此操作不可恢复。',
		success: (res) => {
			if (res.confirm) void doDissolve()
		},
	})
}

async function doDissolve(): Promise<void> {
	try {
		await groupApi.dissolveGroup(groupId.value)
		uni.reLaunch({ url: '/pages/conversation/list' })
	} catch {
		// 错误已在请求层统一处理
	}
}
</script>

<style lang="scss" scoped>
.group-detail-page {
	padding: $space-md;
}

.group-card {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: $space-xl 0;
	background: $color-bg-white;
	border-radius: $radius-lg;
}

.group-name {
	margin-top: $space-md;
	font-size: 36rpx;
	font-weight: 600;
}

.group-meta {
	margin-top: $space-xs;
	font-size: 26rpx;
	color: $color-text-secondary;
}

.section-title {
	margin: $space-lg 0 $space-sm;
	font-size: 26rpx;
	color: $color-text-secondary;
}

.member-item {
	display: flex;
	align-items: center;
	padding: $space-md;
	background: $color-bg-white;
	border-radius: $radius-md;
}

.member-item + .member-item {
	margin-top: $space-xs;
}

.member-info {
	flex: 1;
	min-width: 0;
	margin-left: $space-md;
}

.member-name-row {
	display: flex;
	align-items: center;
}

.member-name {
	font-size: 30rpx;
}

.owner-badge {
	margin-left: $space-sm;
	padding: 2rpx 12rpx;
	font-size: 22rpx;
	color: $color-primary;
	border: 1rpx solid $color-primary;
	border-radius: $radius-sm;
}

.remove-btn {
	padding: 8rpx 24rpx;
	font-size: 26rpx;
	color: $color-danger;
}

.actions {
	display: flex;
	margin-top: $space-xl;
}

.action-btn {
	flex: 1;
	color: $color-primary;
	background: $color-bg-white;
}

.action-btn.danger {
	margin-left: $space-md;
	color: $color-danger;
}

.mask {
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

.add-panel {
	display: flex;
	flex-direction: column;
	width: 600rpx;
	max-height: 70vh;
	padding: $space-lg;
	background: $color-bg-white;
	border-radius: $radius-lg;
}

.add-title {
	font-size: 30rpx;
	font-weight: 600;
}

.add-list {
	flex: 1;
	max-height: 50vh;
	margin-top: $space-md;
}

.add-item {
	display: flex;
	align-items: center;
	padding: $space-sm 0;
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

.add-name {
	margin-left: $space-md;
	font-size: 28rpx;
}

.add-empty {
	padding: $space-lg 0;
	font-size: 26rpx;
	color: $color-text-secondary;
	text-align: center;
}

.add-confirm {
	margin-top: $space-md;
	color: #ffffff;
	background: $color-primary;
}

.add-confirm[disabled] {
	color: rgba(255, 255, 255, 0.7);
	background: #b2dfc4;
}
</style>
