import { WS_URL } from '@/utils/constants'
import { getToken } from '@/utils/token'
import type { WsIncoming } from '@/types/ws'

type Handler = (payload: WsIncoming) => void

const handlers = new Set<Handler>()
let socketTask: UniApp.SocketTask | null = null
let connected = false
let manualClosed = false
let reconnectTimer: number | null = null

/** 建立连接（幂等，未登录或已连接时直接返回） */
export function connect(): void {
	if (connected) return
	const token = getToken()
	if (!token) return
	manualClosed = false
	const task = uni.connectSocket({
		url: `${WS_URL}?token=${encodeURIComponent(token)}`,
		fail: () => scheduleReconnect(),
	})
	socketTask = task
	task.onOpen(() => {
		connected = true
	})
	task.onMessage((res) => {
		const text = typeof res.data === 'string' ? res.data : ''
		if (!text) return
		try {
			const payload = JSON.parse(text) as WsIncoming
			handlers.forEach((h) => h(payload))
		} catch {
			// 忽略无法解析的消息
		}
	})
	task.onClose(() => {
		connected = false
		if (socketTask === task) socketTask = null
		scheduleReconnect()
	})
	task.onError(() => {
		task.close({})
	})
}

/** 主动断开（登出时调用，不再重连） */
export function disconnect(): void {
	manualClosed = true
	if (reconnectTimer !== null) {
		clearTimeout(reconnectTimer)
		reconnectTimer = null
	}
	socketTask?.close({})
	socketTask = null
	connected = false
}

function scheduleReconnect(): void {
	if (manualClosed) return
	if (reconnectTimer !== null) return
	reconnectTimer = setTimeout(() => {
		reconnectTimer = null
		connect()
	}, 3000)
}

function send(data: Record<string, unknown>): void {
	if (!connected || !socketTask) return
	socketTask.send({ data: JSON.stringify(data) })
}

export function sendChat(conversationId: number, messageType: number, content: string): void {
	send({ type: 'chat', data: { conversationId, messageType, content } })
}

export function sendRead(conversationId: number): void {
	send({ type: 'read', data: { conversationId } })
}

export function sendPing(): void {
	send({ type: 'ping' })
}

/** 订阅下行消息，返回取消订阅函数 */
export function subscribe(handler: Handler): () => void {
	if (handlers.has(handler)) {
		return () => handlers.delete(handler)
	}
	handlers.add(handler)
	return () => {
		handlers.delete(handler)
	}
}
