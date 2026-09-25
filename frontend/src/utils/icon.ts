/**
 * iconfont 图标 Unicode 码点表（与 src/static/iconfont/iconfont.css 中的实际码点对应）。
 * 值为十六进制码点，由 IconFont.vue 用 String.fromCodePoint 渲染为图标字符。
 */
export const ICON_MAP: Record<string, number> = {
	chat: 0xe792, // 信息（消息 Tab）
	contact: 0xe7c1, // 用户（好友/通讯录 Tab，暂代）
	user: 0xe7c1, // 用户（我的 Tab）
	group: 0xe7c5, // 群
	add: 0xe7f1, // 增加（添加入口）
	addUser: 0xe7f1, // 增加（添加好友）
	send: 0xe7d7, // 发送
	image: 0xe7f3, // 图片
	camera: 0xe7dd, // 相机（上传头像）
	back: 0xe7a5, // 向左（返回）
	arrowRight: 0xe7a6, // 向右
	search: 0xe782, // 搜索
	more: 0xe79f, // 更多
	setting: 0xe7f2, // 设置
	logout: 0xe7a0, // 退出
	phone: 0xe7c0, // 电话（手机号输入）
	verify: 0xe797, // 验证码输入
	check: 0xe780, // 确认（同意好友 / 已读）
	close: 0xe781, // 取消（拒绝好友 / 关闭）
	delete: 0xe77b, // 删除
}
