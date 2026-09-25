import { getToken, clearToken } from '@/utils/token'
import { BASE_URL, CODE_SUCCESS, CODE_UNAUTHORIZED } from '@/utils/constants'
import type { Result } from '@/types/api'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface RequestOptions {
	url: string
	method?: HttpMethod
	data?: Record<string, unknown>
	header?: Record<string, string>
}

/** 统一请求封装：注入 token、统一响应、401 跳转登录 */
export function request<T>(options: RequestOptions): Promise<T> {
	return new Promise<T>((resolve, reject) => {
		const token = getToken()
		uni.request({
			url: BASE_URL + options.url,
			method: options.method ?? 'GET',
			data: options.data,
			header: {
				'Content-Type': 'application/json',
				...(token ? { Authorization: `Bearer ${token}` } : {}),
				...(options.header ?? {}),
			},
			success: (res) => {
				const result = res.data as unknown as Result<T>
				if (res.statusCode === CODE_UNAUTHORIZED || result.code === CODE_UNAUTHORIZED) {
					clearToken()
					uni.reLaunch({ url: '/pages/login/login' })
					reject(result)
					return
				}
				if (result.code === CODE_SUCCESS) {
					resolve(result.data)
				} else {
					uni.showToast({ title: result.msg || '请求失败', icon: 'none' })
					reject(result)
				}
			},
			fail: (err) => {
				uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
				reject(err)
			},
		})
	})
}
