import type { User } from './user'

/** 好友：对方用户信息 + 备注 */
export interface Friend {
	friendId: number
	remark: string
	user: User
	online?: boolean
}

/** 好友申请 */
export interface FriendRequest {
	id: number
	fromUserId: number
	toUserId: number
	message: string
	status: number
	createdAt: string
	fromUser?: User
	toUser?: User
}

/** 搜索到的用户（含与我的关系） */
export interface SearchUser extends User {
	relation: number
}

/** 关系：无关系 */
export const RELATION_NONE = 0
/** 关系：已是好友 */
export const RELATION_FRIEND = 1
/** 关系：我已申请（待对方处理） */
export const RELATION_OUTGOING = 2
/** 关系：对方已申请（待我处理） */
export const RELATION_INCOMING = 3
