function pad(n: number): string {
	return n < 10 ? `0${n}` : `${n}`
}

function isSameDay(a: Date, b: Date): boolean {
	return (
		a.getFullYear() === b.getFullYear() &&
		a.getMonth() === b.getMonth() &&
		a.getDate() === b.getDate()
	)
}

/** 消息分组时间：今天 HH:mm，昨天「昨天 HH:mm」，同年 M-D HH:mm，跨年 YYYY-M-D HH:mm */
export function formatTime(time: string): string {
	if (!time) return ''
	const d = new Date(time)
	const now = new Date()
	const hm = `${pad(d.getHours())}:${pad(d.getMinutes())}`
	if (isSameDay(d, now)) return hm
	const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000)
	if (isSameDay(d, yesterday)) return `昨天 ${hm}`
	if (d.getFullYear() === now.getFullYear()) return `${d.getMonth() + 1}-${d.getDate()} ${hm}`
	return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()} ${hm}`
}

/** 是否开始新的时间分组：与上一条消息间隔 >= 2 分钟 */
export function isNewTimeGroup(prevTime: string, currTime: string): boolean {
	if (!prevTime) return true
	return new Date(currTime).getTime() - new Date(prevTime).getTime() >= 2 * 60 * 1000
}

/** 会话列表时间：刚刚 / N分钟前 / 今天 HH:mm / 昨天 / 日期 */
export function formatConversationTime(time: string | null): string {
	if (!time) return ''
	const d = new Date(time)
	const diff = Date.now() - d.getTime()
	if (diff < 60 * 1000) return '刚刚'
	if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)}分钟前`
	if (isSameDay(d, new Date())) return `${pad(d.getHours())}:${pad(d.getMinutes())}`
	return formatTime(time)
}
