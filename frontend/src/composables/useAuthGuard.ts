import { onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

/** 页面级登录守卫：未登录则跳转登录页 */
export function useAuthGuard(): void {
	const authStore = useAuthStore()
	onShow(() => {
		if (!authStore.isLoggedIn) {
			uni.reLaunch({ url: '/pages/login/login' })
		}
	})
}
