import { request } from './request'
import type { SearchUser } from '@/types/friend'
import type { User } from '@/types/user'

export function searchUsers(keyword: string): Promise<SearchUser[]> {
	return request<SearchUser[]>({ url: '/api/user/search', data: { keyword } })
}

export function updateMe(data: { nickname?: string; avatar?: string }): Promise<User> {
	return request<User>({ url: '/api/user/me', method: 'PUT', data })
}
