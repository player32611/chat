import { request } from './request'
import type { Group, GroupMember } from '@/types/group'

export function createGroup(name: string, avatar: string, memberIds: number[]): Promise<Group> {
	return request<Group>({ url: '/api/group', method: 'POST', data: { name, avatar, memberIds } })
}

export function getGroup(id: number): Promise<Group> {
	return request<Group>({ url: `/api/group/${id}` })
}

export function listMembers(id: number): Promise<GroupMember[]> {
	return request<GroupMember[]>({ url: `/api/group/${id}/members` })
}

export function addMembers(id: number, userIds: number[]): Promise<null> {
	return request<null>({ url: `/api/group/${id}/member`, method: 'POST', data: { userIds } })
}

export function removeMember(id: number, userId: number): Promise<null> {
	return request<null>({ url: `/api/group/${id}/member/${userId}`, method: 'DELETE' })
}

export function updateGroup(
	id: number,
	patch: { name?: string; avatar?: string; announcement?: string }
): Promise<Group> {
	const data: Record<string, unknown> = { ...patch }
	return request<Group>({ url: `/api/group/${id}`, method: 'PUT', data })
}

export function dissolveGroup(id: number): Promise<null> {
	return request<null>({ url: `/api/group/${id}`, method: 'DELETE' })
}
