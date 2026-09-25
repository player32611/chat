import { request } from './request'
import type { Friend, FriendRequest } from '@/types/friend'

export function listFriends(): Promise<Friend[]> {
	return request<Friend[]>({ url: '/api/friend/list' })
}

export function sendRequest(toUserId: number, message: string): Promise<null> {
	return request<null>({ url: '/api/friend/request', method: 'POST', data: { toUserId, message } })
}

export function listReceived(): Promise<FriendRequest[]> {
	return request<FriendRequest[]>({ url: '/api/friend/requests/received' })
}

export function listSent(): Promise<FriendRequest[]> {
	return request<FriendRequest[]>({ url: '/api/friend/requests/sent' })
}

export function acceptRequest(id: number): Promise<null> {
	return request<null>({ url: `/api/friend/request/${id}/accept`, method: 'POST' })
}

export function rejectRequest(id: number): Promise<null> {
	return request<null>({ url: `/api/friend/request/${id}/reject`, method: 'POST' })
}

export function deleteFriend(friendId: number): Promise<null> {
	return request<null>({ url: `/api/friend/${friendId}`, method: 'DELETE' })
}
