<script setup lang="ts">
import { onLaunch } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onLaunch(() => {
	if (authStore.isLoggedIn) {
		authStore.ensureUser().catch(() => {
			// 拉取用户信息失败不阻塞启动，401 由请求层统一处理
		})
	}
})
</script>

<style>
@import './static/iconfont/iconfont.css';
</style>

<style lang="scss">
@import './assets/styles/reset.scss';
@import './assets/styles/common.scss';
</style>
