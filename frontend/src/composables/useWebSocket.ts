import { onUnmounted } from 'vue'
import { connect, subscribe, sendChat, sendRead, sendPing } from '@/api/ws'
import type { WsIncoming } from '@/types/ws'

/**
 * 页面级 WebSocket 接入：进入页面时建立连接并订阅下行消息，
 * 页面卸载时自动取消订阅（连接保持全局复用）。
 */
export function useWebSocket(onMessage?: (payload: WsIncoming) => void) {
	connect()
	let unsubscribe: (() => void) | undefined
	if (onMessage) {
		unsubscribe = subscribe(onMessage)
	}
	onUnmounted(() => {
		unsubscribe?.()
	})
	return { sendChat, sendRead, sendPing }
}
