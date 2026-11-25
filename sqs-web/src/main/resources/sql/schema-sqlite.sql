-- =============================================================================
-- smart-query-system SQLite 初始化脚本（幂等，应用启动时由 spring.sql.init 自动执行）
-- 说明：本地文件库默认 ./data/smart-query.db；预置账号 admin / admin123
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 用户表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT    NOT NULL,
    password    TEXT    NOT NULL,
    nickname    TEXT,
    avatar      TEXT,
    status      INTEGER NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_username ON sys_user (username);
CREATE INDEX IF NOT EXISTS idx_user_create_time ON sys_user (create_time);

-- 预置账号（密码：admin123，BCrypt 加密值）
INSERT INTO sys_user (username, password, nickname, status)
SELECT 'admin', '$2a$10$bz3vYfmHbZJSesOI1bcUf.9caZo10opO7Lz.iDgfU5TPoOdoP8W3C', '管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin' AND deleted = 0);

-- -----------------------------------------------------------------------------
-- 会话表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_session (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    title       TEXT    NOT NULL DEFAULT '新对话',
    model       TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_session_user_update ON chat_session (user_id, update_time);
CREATE INDEX IF NOT EXISTS idx_session_user_title ON chat_session (user_id, title);

-- -----------------------------------------------------------------------------
-- 消息表
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chat_message (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id  INTEGER NOT NULL,
    user_id     INTEGER NOT NULL,
    role        TEXT    NOT NULL,
    content     TEXT    NOT NULL,
    model       TEXT,
    tokens      INTEGER,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_message_session_create ON chat_message (session_id, create_time);
CREATE INDEX IF NOT EXISTS idx_message_user_session ON chat_message (user_id, session_id);

-- -----------------------------------------------------------------------------
-- 知识库文档表（RAG）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_document (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    title       TEXT    NOT NULL,
    file_name   TEXT,
    file_type   TEXT,
    file_size   INTEGER,
    chunk_count INTEGER NOT NULL DEFAULT 0,
    status      TEXT    NOT NULL DEFAULT 'READY',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_kdoc_user_create ON knowledge_document (user_id, create_time);

-- -----------------------------------------------------------------------------
-- 知识库分块表（embedding 为 JSON 浮点数组文本）
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    document_id INTEGER NOT NULL,
    user_id     INTEGER NOT NULL,
    seq         INTEGER NOT NULL,
    content     TEXT    NOT NULL,
    embedding   TEXT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_kchunk_doc ON knowledge_chunk (document_id);
CREATE INDEX IF NOT EXISTS idx_kchunk_user ON knowledge_chunk (user_id);
