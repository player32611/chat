import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as authApi from '@/api/auth'
import { isPhone } from '@/utils/validator'

export function useAuth() {
	const authStore = useAuthStore()
	const countdown = ref(0)
	let timer: number | null = null

	async function sendCode(phone: string): Promise<void> {
		if (!isPhone(phone)) {
			uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
			return
		}
		await authApi.sendSms(phone)
		uni.showToast({ title: '验证码已发送', icon: 'none' })
		startCountdown()
	}

	async function login(phone: string, code: string): Promise<void> {
		await authStore.login(phone, code)
	}

	function startCountdown(): void {
		countdown.value = 60
		timer = setInterval(() => {
			countdown.value -= 1
			if (countdown.value <= 0) {
				stopCountdown()
			}
		}, 1000)
	}

	function stopCountdown(): void {
		if (timer !== null) {
			clearInterval(timer)
			timer = null
		}
	}

	return { authStore, countdown, sendCode, login, stopCountdown }
}
