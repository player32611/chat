# 聊天室系统 — 项目逻辑结构

> 本文档面向后续接手或阅读本项目的开发者，说明整体架构、分层职责、数据库设计、前后端关键机制与端到端数据流。功能与快速启动见 [README.md](README.md)。

## 1. 整体架构

前后端分离，前端一套代码多端运行，后端提供 REST（业务接口）与 WebSocket（实时消息）两套通道。

```
┌─────────────────────────────────────────────────────────────┐
│  前端 frontend（uni-app，Vue3 + TS + Pinia + uni-ui）          │
│  H5 / 微信小程序 / App                                          │
│  ├─ REST（uni.request，Authorization: Bearer <JWT>）           │
│  └─ WebSocket（ws://…/ws?token=<JWT>，实时消息/在线状态）        │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / WS
┌──────────────────────────▼──────────────────────────────────┐
│  后端 backend（Spring Boot 3.2 多模块）                        │
│  chat-web ─▶ chat-service ─▶ chat-mapper ─▶ chat-model ─▶ chat-common │
│  Controller/WebSocket/安全 → 业务 → 数据访问 → 模型 → 通用       │
│  ├─ MySQL 8（业务数据，MyBatis-Plus）                          │
│  ├─ Redis（验证码 / 登出黑名单）                                │
│  └─ 阿里云 OSS（图片）、SMS（短信，dev 环境 mock）              │
└─────────────────────────────────────────────────────────────┘
```

模块依赖是单向的：`chat-web → chat-service → chat-mapper → chat-model → chat-common`。上层依赖下层，下层不感知上层。

## 2. 技术栈

| 层   | 技术                                                                                                                     |
| ---- | ------------------------------------------------------------------------------------------------------------------------ |
| 前端 | uni-app (Vue3) + TypeScript + Vite + Pinia + uni-ui + SCSS + iconfont                                                    |
| 后端 | Spring Boot 3.2.5（Java 21）+ Maven 多模块 + MyBatis-Plus 3.5.5 + MySQL 8 + Redis + JWT（jjwt 0.12.5）+ Spring WebSocket |
| 三方 | 阿里云 OSS（图片/头像）、阿里云 SMS（短信，dev 用 mock）                                                                 |

## 3. 目录结构

```
chat/
├── frontend/src/
│   ├── api/            # 网络层：request 封装 + 各模块 REST + ws 单例
│   ├── components/     # UserAvatar / MessageBubble / ConversationItem / UserCardPopup / IconFont
│   ├── composables/    # useAuth / useAuthGuard / useWebSocket
│   ├── stores/         # Pinia：auth / conversation / message
│   ├── pages/          # login / conversation / friend / user / chat / group
│   ├── types/          # user / message / conversation / friend / group / api / ws
│   └── utils/          # constants / token / time / icon / validator
├── backend/
│   ├── chat-common/    # Result、BusinessException、CommonConstant、RedisKeyConstant
│   ├── chat-model/     # entity / dto / vo / enums
│   ├── chat-mapper/    # MyBatis-Plus Mapper 接口
│   ├── chat-service/   # Service 接口 + impl（含 oss / sms 抽象）
│   ├── chat-web/       # 启动类 + controller + ws + security + config + handler
│   └── sql/schema.sql  # 建表脚本
└── ARCHITECTURE.md / README.md
```

## 4. 数据库设计（MySQL 8，7 张表）

统一约定：主键 `id BIGINT` 自增；`created_at`/`updated_at`；逻辑删除 `deleted`（MyBatis-Plus `@TableLogic`，1 表示已删）。

| 表                    | 用途               | 关键字段                                                                                  |
| --------------------- | ------------------ | ----------------------------------------------------------------------------------------- |
| `user`                | 用户               | phone(唯一)、nickname、avatar、status(1 正常/0 禁用)、last_login_at                       |
| `friend`              | 好友关系           | user_id、friend_id（**规范化：user_id < friend_id 存一行**）、remark                      |
| `friend_request`      | 好友申请           | from_user_id、to_user_id、message、status(0 待/1 同意/2 拒绝)、handled_at                 |
| `conversation`        | 会话（单/群统一）  | type(1 单聊/2 群聊)、group_id(群聊时指向 group_info.id)、last_message_id、last_message_at |
| `group_info`          | 群信息             | name、avatar、owner_id、announcement                                                      |
| `conversation_member` | 会话成员（含游标） | conversation_id、user_id、role(1 群主/2 成员)、unread_count、last_read_message_id         |
| `message`             | 消息               | conversation_id、sender_id、type(1 文本/2 图片)、content                                  |

设计要点：

- **好友表 3NF**：好友关系按 `user_id < friend_id` 只存一行，消除「A-B / B-A」双向冗余；查询时按 `user_id` 或 `friend_id` 任一命中。
- **单聊/群聊统一会话**：都用 `conversation` + `conversation_member` 建模；单聊 `group_id = NULL`，群聊 `group_id` 指向 `group_info`。群专属字段（名称/头像/群主/公告）独立成 `group_info` 表。
- **验证码、在线状态、登出黑名单不入库**，走 Redis（见 §6.1）。

## 5. 后端逻辑结构

### 5.1 分层与调用链

```
Controller（chat-web）  接收参数、鉴权上下文、返回 Result
      │
      ▼
Service（chat-service）  业务规则、事务、校验、组装 VO
      │
      ▼
Mapper（chat-mapper）    继承 MyBatis-Plus BaseMapper，LambdaQueryWrapper 拼条件
      │
      ▼
Entity（chat-model）     与表一一对应，@TableLogic 软删除
```

- **统一响应** `Result<T>{code, msg, data}`，`code=200` 成功；`400` 参数错、`401` 未登录、`403` 无权限、`500` 服务错。
- **全局异常** `GlobalExceptionHandler`（`@RestControllerAdvice`）集中处理：`BusinessException`（业务语义）、`MethodArgumentNotValidException`（参数校验）、`MaxUploadSizeExceededException`（文件过大）、`Exception`（兜底）。

### 5.2 认证与鉴权

1. **发验证码**：`POST /api/auth/sms/send` → `SmsService` 生成验证码存 Redis（`sms:code:{phone}`，5 分钟）。dev 环境走 `MockSmsSender`（不真发短信，码打到后端日志）。
2. **登录**：`POST /api/auth/login` → 校验验证码 → `UserService.findOrCreateByPhone`（**首次登录隐式注册**）→ `JwtTokenProvider` 签发 JWT（sub=userId，jti=随机 UUID，含过期时间）→ 返回 `{token, user}`。
3. **请求鉴权**：`JwtAuthFilter`（OncePerRequestFilter）解析 `Authorization: Bearer` → 校验签名 → 检查 jti 是否在 Redis 黑名单 → 将 `AuthUser(userId, jti, expiresAt)` 写入 `UserContext`（ThreadLocal），业务通过 `UserContext.getUserId()` 取当前用户。
4. **登出**：`POST /api/auth/logout` 把 jti 写入 Redis 黑名单 `jwt:blacklist:{jti}`，TTL = 令牌剩余有效期，实现「真登出」（黑名单期间该 token 失效）。

### 5.3 WebSocket 实时消息机制

- **握手鉴权**：`WsHandshakeInterceptor` 从 query 参数 `token` 解析 JWT + 校验黑名单，把 userId 写入 session attributes。
- **会话管理**：`WsSessionManager` 维护 `ConcurrentHashMap<Long, WebSocketSession>`（userId → session），提供 `register / unregister / isOnline / sendToUser`。
- **消息路由**：`ChatWebSocketHandler` 处理下行消息，**按会话成员广播**——`conversationService.listMemberIds(conversationId)` 拿到所有成员，逐个 `sendToUser`。因为单聊/群聊都建模为「会话 + 成员」，所以实时消息**复用同一套 WS 流程，无需区分类型**。

WS 协议：

| 方向 | type             | data                                          | 说明            |
| ---- | ---------------- | --------------------------------------------- | --------------- |
| C→S  | `chat`           | `{conversationId, messageType, content}`      | 发消息          |
| C→S  | `read`           | `{conversationId}`                            | 已读上报        |
| C→S  | `ping`           | —                                             | 心跳            |
| S→C  | `message`        | `MessageVO`                                   | 新消息推送      |
| S→C  | `read`           | `{conversationId, userId, lastReadMessageId}` | 已读回执        |
| S→C  | `presence`       | `{userId, online}`                            | 好友上下线      |
| S→C  | `pong` / `error` | — / `{msg}`                                   | 心跳响应 / 错误 |

### 5.4 核心业务流程

**发消息（`MessageServiceImpl.sendMessage`，事务）**

1. 校验成员身份、消息类型、内容非空。
2. 插入 `message`。
3. 更新 `conversation.last_message_id / last_message_at`。
4. 对**非发送者**的成员 `unread_count + 1`。
5. 组装 `MessageVO`（补 senderName/senderAvatar）返回；WS 层广播给会话内所有成员。

**未读 / 已读（`ConversationServiceImpl.markRead`）**

- 打开会话 → `unread_count = 0`、`last_read_message_id = 最后消息 id` → WS 广播 `read` 事件给其他成员，对方气泡据此标「已读」。

**在线状态（内存实现）**

- 上线：`afterConnectionEstablished` → register + `broadcastPresence(userId, true)` 给所有好友。
- 下线：`afterConnectionClosed` → unregister + `broadcastPresence(userId, false)`。
- 好友列表 `FriendController.list` 逐好友 `setOnline(wsSessionManager.isOnline(friendId))`。
- 用内存 `WsSessionManager.isOnline` 而非 Redis TTL，与单实例 WS 路由保持一致（简化：当前为单实例部署）。

**好友申请-同意制（`FriendServiceImpl`）**

- 发申请校验：不能加自己 / 对方存在 / 非好友 / 无重复待处理申请。
- 同意 → 更新申请状态 + 写入 `friend` 表（`user_id < friend_id` 规范化）。

**群聊（`GroupServiceImpl`）**

- 创建：插 `group_info` → 插 `conversation(type=2)` → 插群主 + 成员 `conversation_member`。
- 权限：**加人任意成员可操作**；**移人 / 改群信息 / 解散仅群主**（`requireOwner`），群主不可移除自己。

**文件上传（`FileController`）**

- `POST /api/file/upload`（multipart，字段 `file`）→ 校验非空 → 读字节 → `OssService.upload` 上传 OSS → 返回可访问 URL。图片消息与头像共用。
- 大小限制 10MB：后端 `spring.servlet.multipart.max-file-size`（超限抛 `MaxUploadSizeExceededException`，统一提示），前端本地预检兜底。

## 6. 前端逻辑结构

### 6.1 网络层

- **REST**（`api/request.ts`）：统一封装 `uni.request`——注入 `BASE_URL` 与 `Authorization`；响应 `code===200` resolve，否则 toast `msg` 并 reject；`401` 清 token 并 `reLaunch` 登录页。
- **WebSocket 单例**（`api/ws.ts`）：全局唯一 `SocketTask`；`connect()` 幂等、断线自动重连（3s）；`handlers` 为 `Set`，`subscribe()` 注册、返回取消函数；对外 `sendChat / sendRead / sendPing`。
- **页面接入**（`composables/useWebSocket.ts`）：`connect()` + `subscribe(onMessage)` + `onUnmounted` 自动取消订阅（连接保持全局复用）。

### 6.2 状态管理（Pinia）

| store          | 职责                                                                                                 |
| -------------- | ---------------------------------------------------------------------------------------------------- |
| `auth`         | token / user；`login / logout / fetchMe / ensureUser`（刷新后仅剩 token 时并发去重拉取一次用户信息） |
| `conversation` | 会话列表；`load / upsert / clearUnread / applyIncoming`（收到新消息更新末条、非当前会话未读 +1）     |
| `message`      | 当前会话消息；`open / loadHistory`（向前分页）/ `append`（去重）/ `markRead`                         |

### 6.3 页面与路由

- **tabBar 三项**：消息（`conversation/list`）· 好友（`friend/list`）· 我的（`user/profile`）。
- 其余页面 `navigateTo`：登录、好友申请、搜索用户、聊天、发起群聊、群聊信息。
- 页面登录守卫：`useAuthGuard` 在 `onShow` 检查未登录则 `reLaunch` 登录页。
- 启动恢复：`App.vue` onLaunch 调 `ensureUser()`，修复「刷新后 user 为 null 导致消息来源误判」的问题。

### 6.4 关键页面职责

| 页面                | 职责                                                                          |
| ------------------- | ----------------------------------------------------------------------------- |
| `login/login`       | 手机号 + 验证码登录（倒计时、校验）                                           |
| `conversation/list` | 会话列表（未读角标、末条消息、时间）、点击进入聊天                            |
| `friend/list`       | 好友列表（在线绿点 + presence 实时更新）、搜索/新好友/发起群聊入口、好友卡片  |
| `friend/requests`   | 收到/发出申请，同意/拒绝                                                      |
| `user/search`       | 搜索用户，按关系显示「添加/已申请/已是好友」                                  |
| `chat/chat`         | 聊天主界面：历史分页、发送文本/图片、时间分组、已读回执、群聊标题与群信息入口 |
| `group/create`      | 选好友建群                                                                    |
| `group/detail`      | 群信息、成员列表、加人/移人/解散                                              |
| `user/profile`      | 头像上传、昵称修改、登出                                                      |

## 7. 端到端数据流

### 7.1 登录

```
前端输入手机号/验证码 → POST /auth/sms/send（Redis 存码，dev 打日志）
→ POST /auth/login → 校验码 + 隐式注册 → 签发 JWT
→ 前端存 token + user → App.onLaunch ensureUser 恢复
→ 前端建立 WS 连接（ws?token=）→ 后端广播 presence(online=true) 给好友
```

### 7.2 发送单聊消息

```
A 在 chat 页发送 → WS sendChat → ChatWebSocketHandler.handleChat
→ MessageService.sendMessage（落库 + 会话末条 + B 未读+1）
→ 广播 message 给 A、B
→ B 的 conversation.applyIncoming（末条更新/未读+1）、message.append（若正打开该会话）
→ A 的 message.append + 气泡显示
```

### 7.3 已读回执

```
B 打开会话 → message.markRead + WS sendRead
→ 后端 markRead（清未读 + 更新 last_read_message_id）→ 广播 read 给 A
→ A 端气泡标「已读」
```

### 7.4 在线状态

```
B 上线：register → 广播 presence(online=true) 给 B 的好友（含 A）
B 下线：unregister → 广播 presence(online=false)
A 的 friend/list 订阅 presence，实时更新好友绿点
```

## 8. 关键设计决策

- **好友表规范化**：`user_id < friend_id` 单行存储，消除对称冗余，满足 3NF。
- **单聊/群聊统一会话模型**：消息路由、未读、已读、会话列表全部复用一套逻辑。
- **在线状态内存实现**：与单实例 WS 路由一致，避免 Redis 与内存状态不一致。
- **MessageVO 冗余发送人**：`senderName/senderAvatar` 在发送与拉历史时批量补齐，满足群聊「显示发送人」诉求（单聊对方固定，群聊需按消息区分）。
- **软删除**：全表 `deleted` 逻辑删除，删除操作不物理清数据。
- **登出真失效**：jti 黑名单 + 剩余有效期 TTL，登出后 token 立即不可用。
- **文件大小双端校验**：前端本地预检（`MAX_IMAGE_SIZE`）省流量，后端 `max-file-size` 兜底保证安全。
