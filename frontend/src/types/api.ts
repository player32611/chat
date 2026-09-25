/** 后端统一响应体 */
export interface Result<T = unknown> {
	code: number
	msg: string
	data: T
}
