/** 后端接口基地址（H5 开发环境；小程序/真机需改为局域网 IP 或域名） */
export const BASE_URL = 'http://localhost:8080'

/** WebSocket 地址 */
export const WS_URL = 'ws://localhost:8080/ws'

/** 成功状态码 */
export const CODE_SUCCESS = 200

/** 未登录状态码 */
export const CODE_UNAUTHORIZED = 401

/** 本地存储 token key */
export const TOKEN_KEY = 'chat_token'

/** 单文件上传大小上限（字节，10MB，与后端 spring.servlet.multipart.max-file-size 保持一致） */
export const MAX_IMAGE_SIZE = 10 * 1024 * 1024
