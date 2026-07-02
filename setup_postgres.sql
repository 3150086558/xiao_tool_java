-- =============================================
-- PostgreSQL 初始化脚本
-- =============================================
-- 前置要求：
--   请先创建数据库并连接到此数据库后再运行本脚本。
--
-- 创建数据库命令（在 psql 或 Navicat 中执行一次即可）：
--   CREATE DATABASE "0701_my_project";
--
-- 运行方式：
--   1. psql 命令行（已连接到此数据库）：
--      psql -U postgres -d "0701_my_project" -f setup_postgres.sql
--   2. Navicat / DBeaver 等图形工具：
--      先创建数据库并连接，然后在查询窗口中运行本脚本
-- =============================================

-- =============================================
-- 4. 创建表结构
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    created_at VARCHAR(30) NOT NULL
);

-- 记账记录表
CREATE TABLE IF NOT EXISTS records (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    record_date VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK(type IN ('income','expense')),
    category VARCHAR(50) NOT NULL,
    sub_category VARCHAR(50) DEFAULT '',
    amount REAL NOT NULL CHECK(amount >= 0),
    account VARCHAR(50) DEFAULT '',
    note TEXT DEFAULT '',
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_records_user ON records(user_id);
CREATE INDEX IF NOT EXISTS idx_records_date ON records(record_date);

-- 菜单表
CREATE TABLE IF NOT EXISTS menus (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    parent_id INTEGER DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(10) DEFAULT '',
    sort_order INTEGER DEFAULT 0,
    created_at VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_menus_user ON menus(user_id);

-- 待办事项表
CREATE TABLE IF NOT EXISTS todos (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    completed INTEGER DEFAULT 0,
    priority INTEGER DEFAULT 0,
    due_date VARCHAR(20),
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_todos_user ON todos(user_id);

-- 备忘录表
CREATE TABLE IF NOT EXISTS notes (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT DEFAULT '',
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_notes_user ON notes(user_id);

-- 数据库连接配置表
CREATE TABLE IF NOT EXISTS db_connections (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    db_type VARCHAR(20) NOT NULL,
    host VARCHAR(100) DEFAULT '',
    port INTEGER DEFAULT 0,
    username VARCHAR(100) DEFAULT '',
    password VARCHAR(200) DEFAULT '',
    "database" VARCHAR(100) DEFAULT '',
    sqlite_path VARCHAR(500) DEFAULT '',
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_db_conn_user ON db_connections(user_id);

-- =============================================
-- 5. 插入默认管理员账号
--    用户名: admin
--    密码: Mkld@2026 （SHA256 哈希存储）
-- =============================================

INSERT INTO users (username, password, created_at)
VALUES ('admin', '4c5b73ae988dd72ce2ee2038c90b8d98e05c46f461037b26301fd1ab5a844d7f', '2026-07-01 00:00:00')
ON CONFLICT (username) DO NOTHING;

-- =============================================
-- 6. 初始化管理员菜单
-- =============================================

-- 获取 admin 用户 ID
DO $$
DECLARE
    v_uid INTEGER;
    v_fid1 INTEGER;
    v_fid2 INTEGER;
    v_fid3 INTEGER;
BEGIN
    SELECT id INTO v_uid FROM users WHERE username = 'admin';
    IF v_uid IS NULL THEN RETURN; END IF;

    -- 一级菜单
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, 0, '财务管理', '💰', 1, '2026-07-01 00:00:00') RETURNING id INTO v_fid1;
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, 0, '日常工具', '🔧', 2, '2026-07-01 00:00:00') RETURNING id INTO v_fid2;
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, 0, '系统管理', '⚙️', 3, '2026-07-01 00:00:00') RETURNING id INTO v_fid3;

    -- 二级菜单
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, v_fid1, '记账', '📊', 1, '2026-07-01 00:00:00');
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, v_fid1, '统计报表', '📈', 2, '2026-07-01 00:00:00');
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, v_fid2, '待办事项', '✅', 1, '2026-07-01 00:00:00');
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, v_fid2, '备忘录', '📝', 2, '2026-07-01 00:00:00');
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, v_fid2, '数据库查询', '🗄️', 3, '2026-07-01 00:00:00');
    INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (v_uid, v_fid3, '用户管理', '👥', 1, '2026-07-01 00:00:00');

    -- 示例数据库连接配置
    INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, "database", sqlite_path, created_at, updated_at)
    VALUES (v_uid, '本地MySQL-连接', 'mysql', '127.0.0.1', 3306, 'root', '123456', '0701_my_project', '', '2026-07-01 23:49:29', '2026-07-01 23:54:38');
    INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, "database", sqlite_path, created_at, updated_at)
    VALUES (v_uid, '本地SQLite-连接', 'sqlite', '', 0, '', '', '', 'D:\MyPersonalize\InstallPosition\AI_data\Trae_data\20260617_project_generate\my_project\data\0701_my_project.db', '2026-07-01 23:49:29', '2026-07-01 23:49:29');
    INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, "database", sqlite_path, created_at, updated_at)
    VALUES (v_uid, '本地pgsql-连接', 'postgres', '127.0.0.1', 5432, 'postgres', '123456', '0701_my_project', '', '2026-07-01 23:55:14', '2026-07-01 23:57:25');
END $$;

-- =============================================
-- 完成！
-- 启动应用时 .env 配置：
--   DB_TYPE=postgres
--   DB_HOST=127.0.0.1
--   DB_PORT=5432
--   DB_NAME=0701_my_project
--   DB_USER=postgres
--   DB_PASSWORD=你的postgres密码
-- =============================================
