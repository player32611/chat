import { request } from './request'
import type { LoginResult, User } from '@/types/user'

export function sendSms(phone: string): Promise<null> {
	return request<null>({ url: '/api/auth/sms/send', method: 'POST', data: { phone } })
}

export function login(phone: string, code: string): Promise<LoginResult> {
	return request<LoginResult>({ url: '/api/auth/login', method: 'POST', data: { phone, code } })
}

export function logout(): Promise<null> {
	return request<null>({ url: '/api/auth/logout', method: 'POST' })
}

export function getMe(): Promise<User> {
	return request<User>({ url: '/api/auth/me' })
}
