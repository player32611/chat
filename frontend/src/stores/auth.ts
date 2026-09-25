import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { User } from '@/types/user'
import * as authApi from '@/api/auth'
import { getToken, setToken, clearToken } from '@/utils/token'
import { disconnect } from '@/api/ws'

export const useAuthStore = defineStore('auth', () => {
	const token = ref<string>(getToken())
	const user = ref<User | null>(null)

	const isLoggedIn = computed(() => token.value !== '')

	function setAuth(newToken: string, newUser: User): void {
		token.value = newToken
		user.value = newUser
		setToken(newToken)
	}

	function setUser(newUser: User): void {
		user.value = newUser
	}

	async function login(phone: string, code: string): Promise<void> {
		const result = await authApi.login(phone, code)
		setAuth(result.token, result.user)
	}

	async function logout(): Promise<void> {
		try {
			await authApi.logout()
		} finally {
			token.value = ''
			user.value = null
			clearToken()
			disconnect()
		}
	}

	async function fetchMe(): Promise<void> {
		user.value = await authApi.getMe()
	}

	let userPromise: Promise<void> | null = null

	/** 已登录但用户信息尚未加载（如刷新后仅剩 token）时拉取一次，并做并发去重 */
	async function ensureUser(): Promise<void> {
		if (user.value !== null || !isLoggedIn.value) return
		if (!userPromise) {
			userPromise = fetchMe().finally(() => {
				userPromise = null
			})
		}
		await userPromise
	}

	return { token, user, isLoggedIn, setAuth, setUser, login, logout, fetchMe, ensureUser }
})
