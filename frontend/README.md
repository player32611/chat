# chat-frontend

聊天室系统前端，基于 uni-app（Vue3 + TypeScript + Vite）+ Pinia + uni-ui + SCSS。

## 目录结构

```
src/
├── api/          网络请求封装（request 统一封装 + 各模块接口）
├── components/   业务组件
├── composables/  复杂业务逻辑 hook
├── stores/       Pinia 全局状态
├── pages/        页面
├── types/        统一类型定义
├── utils/        工具函数
├── assets/styles/ 全局样式（variables/reset/common）
└── static/       静态资源
```

## 运行

```bash
yarn install
yarn dev:h5          # H5 预览
yarn dev:mp-weixin   # 微信小程序
yarn type-check      # TS 类型检查
yarn lint            # 代码检查
```

## iconfont 使用（Unicode）

1. 在 iconfont.cn 生成项目，加入 `src/utils/icon.ts` 中列出的全部图标。
2. 下载 `iconfont.css` / `iconfont.ttf` / `.woff` / `.woff2`，放入 `src/static/iconfont/` 目录。
3. `src/utils/icon.ts` 以十六进制码点（`0xe7xx`）记录每个图标，按实际生成结果对齐即可。
4. `App.vue` 已引入 `iconfont.css`；`IconFont.vue` 用 `String.fromCodePoint` 渲染。

> 注意：小程序端本地字体存在限制，H5 正常；如需小程序支持可考虑 base64 内嵌字体。

> 当前 iconfont 项目缺少「发送」「验证码/盾牌」「通讯录」三个图标，`icon.ts` 中对应项暂用相近图标代替（见文件内注释），建议后续在 iconfont.cn 补充。

## 开发说明

- dev 环境短信验证码打印在后端日志（`MockSmsSender`），查看后端控制台获取。
- `BASE_URL` / `WS_URL` 见 `src/utils/constants.ts`，小程序或真机调试需改为局域网 IP 或域名。
- 代码规范：ESLint + Prettier，tab 缩进，单引号，不使用 `any`。
