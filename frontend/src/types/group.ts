/** 角色：群主 */
export const GROUP_ROLE_OWNER = 1
/** 角色：普通成员 */
export const GROUP_ROLE_MEMBER = 2

/** 群聊信息 */
export interface Group {
	id: number
	name: string
	avatar: string
	ownerId: number
	announcement: string | null
	memberCount: number
	conversationId: number
	createdAt: string
}

/** 群成员 */
export interface GroupMember {
	userId: number
	nickname: string
	avatar: string
	role: number
	joinedAt: string
}
