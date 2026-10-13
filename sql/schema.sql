-- =============================================================================
-- smart-query-system 数据库初始化脚本
-- 执行方式：mysql -u root -p < sql/schema.sql
-- 说明：默认库名 smart_query，可自行修改；预置账号 admin / admin123
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `smart_query`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `smart_query`;

-- -----------------------------------------------------------------------------
-- 用户表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名，唯一',
    `password`    VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密后的密码',
    `nickname`    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- 预置账号（密码：admin123，BCrypt 加密值）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `status`)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin' AND `deleted` = 0);

-- -----------------------------------------------------------------------------
-- 会话表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL COMMENT '所属用户 ID',
    `title`       VARCHAR(200) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    `model`       VARCHAR(64)  DEFAULT NULL COMMENT '该会话使用的模型',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_update` (`user_id`, `update_time`),
    KEY `idx_user_title` (`user_id`, `title`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '会话表';

-- -----------------------------------------------------------------------------
-- 消息表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `session_id` BIGINT      NOT NULL COMMENT '会话 ID',
    `user_id`    BIGINT      NOT NULL COMMENT '所属用户 ID（冗余，便于统计与隔离）',
    `role`       VARCHAR(16) NOT NULL COMMENT '角色：user / assistant / system',
    `content`    TEXT        NOT NULL COMMENT '消息内容',
    `model`      VARCHAR(64) DEFAULT NULL COMMENT '生成该消息的模型',
    `tokens`     INT         DEFAULT NULL COMMENT '消耗 token 数',
    `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`    TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_session_create` (`session_id`, `create_time`),
    KEY `idx_user_session` (`user_id`, `session_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '消息表';
