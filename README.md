# 聊天室系统

跨端即时通讯应用，支持**单聊、群聊与好友体系**。前后端分离：前端 uni-app（Vue3 + TypeScript）一套代码多端运行（H5 / 微信小程序 / App），后端 Spring Boot 多模块提供 REST + WebSocket 实时消息服务。

## 核心功能

- **认证**：手机号 + 验证码登录（首次登录隐式注册）、JWT 鉴权、登出黑名单（jti + Redis TTL）
- **好友**：搜索用户、申请-同意制、好友列表、在线状态实时展示
- **单聊**：实时文本/图片消息、历史消息向前分页、未读计数、已读回执、消息时间分组
- **群聊**：创建群、成员管理（加人/移人/解散）、群消息、群头像/名称
- **个人中心**：头像上传（本地大小预检 + OSS）、昵称修改、登出
- **通用**：统一响应 `{ code, msg, data }`、文件上传大小限制（前后端双重校验）、多端适配

## 技术栈

**前端**

- uni-app (Vue3) + TypeScript + Vite
- Pinia（状态管理）、uni-ui（组件库）、SCSS（样式，rpx 单位）
- iconfont（Unicode 图标）、ESLint + Prettier（tab 缩进、单引号、无分号）

**后端**

- Spring Boot 3.2.5（Java 21）+ Maven 多模块
- MyBatis-Plus 3.5.5、MySQL 8、Redis
- JWT（jjwt 0.12.5）+ Spring WebSocket（实时消息）
- 阿里云 OSS（文件/头像）、阿里云 SMS（短信，dev 环境 mock）

## 目录结构

```
chat/
├── frontend/                     # uni-app 前端
│   └── src/
│       ├── api/                  # 请求与 WebSocket 封装（按模块拆分）
│       ├── components/           # 业务组件（头像、气泡、卡片等）
│       ├── composables/          # 组合式函数（useWebSocket 等）
│       ├── stores/               # Pinia 状态（auth 等）
│       ├── pages/                # 页面（登录/会话/聊天/好友/群聊/我的）
│       ├── types/                # 类型定义
│       └── utils/                # token / time / constants 等
├── backend/                      # Spring Boot 多模块后端
│   ├── chat-common/              # 通用：Result、异常、常量
│   ├── chat-model/               # Entity / DTO / VO
│   ├── chat-mapper/              # MyBatis-Plus Mapper
│   ├── chat-service/             # Service 接口与实现
│   ├── chat-web/                 # 启动类 + Controller + WebSocket + 配置
│   └── sql/schema.sql            # 建表脚本
└── README.md
```

模块依赖：`chat-web → chat-service → chat-mapper → chat-model → chat-common`。

## 快速开始

### 1. 数据库

```bash
mysql -u root -p < backend/sql/schema.sql
```

### 2. 后端

配置 `backend/chat-web/src/main/resources/application-dev.yml`（数据库、Redis、JWT 密钥、阿里云 OSS/SMS）。该文件含密钥，已加入 `.gitignore`，请按需自建。

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3. 前端

```bash
cd frontend
yarn install
yarn dev:h5          # H5 预览
yarn dev:mp-weixin   # 微信小程序
```

## 分阶段实施进度

- [x] 阶段 1 — 脚手架 + 认证（手机号验证码登录/登出、JWT、mock 短信）
- [x] 阶段 2 — 好友体系（搜索、申请-同意、好友列表、用户卡片）
- [x] 阶段 3 — 单聊 + 实时消息（WebSocket、文本/图片、历史、未读、已读回执）
- [x] 阶段 4 — 群聊（创建群、成员管理、群消息）
- [x] 阶段 5 — 完善（在线状态、头像上传、个人资料、多端适配）

## 开发说明

- dev 环境短信验证码打印在后端日志（mock 短信），查看后端控制台获取验证码。
- 文件上传上限 10MB（后端 `max-file-size` 与前端 `MAX_IMAGE_SIZE` 双端校验）。
- 前端 iconfont 图标字体现放 `src/static/iconfont/`，如需增补见 `frontend/README.md`。
