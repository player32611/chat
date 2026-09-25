/** 校验手机号格式 */
export function isPhone(phone: string): boolean {
	return /^1\d{10}$/.test(phone)
}
