-- 聊天室系统数据库结构（MySQL 8，InnoDB，utf8mb4）
CREATE DATABASE IF NOT EXISTS chat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE chat;

-- 用户表
CREATE TABLE `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`         VARCHAR(20)  NOT NULL COMMENT '手机号',
    `nickname`      VARCHAR(50)  NOT NULL COMMENT '昵称',
    `avatar`        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像 URL',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1 正常 0 禁用',
    `last_login_at` DATETIME     NULL COMMENT '最后登录时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删 1 已删',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 好友关系表（规范化：user_id < friend_id 唯一一行，避免双向冗余）
CREATE TABLE `friend` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT      NOT NULL COMMENT '用户 ID（较小者）',
    `friend_id`  BIGINT      NOT NULL COMMENT '好友 ID（较大者）',
    `remark`     VARCHAR(50) NULL COMMENT '备注名',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`    TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_friend_id` (`friend_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '好友关系表';

-- 好友申请表
CREATE TABLE `friend_request` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `from_user_id` BIGINT       NOT NULL COMMENT '申请人 ID',
    `to_user_id`   BIGINT       NOT NULL COMMENT '接收人 ID',
    `message`      VARCHAR(255) NOT NULL DEFAULT '' COMMENT '验证消息',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 待处理 1 同意 2 拒绝',
    `handled_at`   DATETIME     NULL COMMENT '处理时间',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_to_user_status` (`to_user_id`, `status`),
    KEY `idx_from_user` (`from_user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '好友申请表';

-- 会话表（单聊/群聊统一）
CREATE TABLE `conversation` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `type`             TINYINT  NOT NULL COMMENT '类型：1 单聊 2 群聊',
    `group_id`         BIGINT   NULL COMMENT '群 ID（群聊时指向 group.id）',
    `last_message_id`  BIGINT   NULL COMMENT '最后一条消息 ID',
    `last_message_at`  DATETIME NULL COMMENT '最后消息时间',
    `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_group_id` (`group_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话表';

-- 群聊信息表（群专属信息独立成表）
CREATE TABLE `group_info` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`         VARCHAR(50)  NOT NULL COMMENT '群名称',
    `avatar`       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '群头像 URL',
    `owner_id`     BIGINT       NOT NULL COMMENT '群主 ID',
    `announcement` VARCHAR(255) NULL COMMENT '群公告',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '群聊信息表';

-- 会话成员表（含未读/已读游标）
CREATE TABLE `conversation_member` (
    `id`                   BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `conversation_id`      BIGINT   NOT NULL COMMENT '会话 ID',
    `user_id`              BIGINT   NOT NULL COMMENT '用户 ID',
    `role`                 TINYINT  NOT NULL DEFAULT 2 COMMENT '角色：1 群主 2 普通成员',
    `unread_count`         INT      NOT NULL DEFAULT 0 COMMENT '未读消息数',
    `last_read_message_id` BIGINT   NOT NULL DEFAULT 0 COMMENT '最后已读消息 ID',
    `joined_at`            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `deleted`              TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conv_user` (`conversation_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话成员表';

-- 消息表
CREATE TABLE `message` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `conversation_id` BIGINT   NOT NULL COMMENT '会话 ID',
    `sender_id`       BIGINT   NOT NULL COMMENT '发送人 ID',
    `type`            TINYINT  NOT NULL COMMENT '类型：1 文本 2 图片',
    `content`         TEXT     NOT NULL COMMENT '内容（文本或图片 URL）',
    `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`         TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_conv_id` (`conversation_id`, `id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '消息表';
