/** 用户信息 */
export interface User {
	id: number
	phone: string
	nickname: string
	avatar: string
	lastLoginAt?: string
}

/** 登录返回 */
export interface LoginResult {
	token: string
	user: User
}
