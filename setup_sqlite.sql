-- =============================================
-- SQLite 初始化脚本
-- 用 sqlite3 执行：
--   sqlite3 data/accounting.db < setup_sqlite.sql
-- 或者 Python 执行：
--   python -c "import sqlite3; conn = sqlite3.connect('data/accounting.db'); conn.executescript(open('setup_sqlite.sql','r').read()); conn.close()"
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    created_at TEXT NOT NULL
);

-- 记账记录表
CREATE TABLE IF NOT EXISTS records (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    record_date TEXT NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('income','expense')),
    category TEXT NOT NULL,
    sub_category TEXT DEFAULT '',
    amount REAL NOT NULL CHECK(amount >= 0),
    account TEXT DEFAULT '',
    note TEXT DEFAULT '',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_records_user ON records(user_id);
CREATE INDEX IF NOT EXISTS idx_records_date ON records(record_date);

-- 菜单表
CREATE TABLE IF NOT EXISTS menus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    parent_id INTEGER DEFAULT 0,
    name TEXT NOT NULL,
    icon TEXT DEFAULT '',
    sort_order INTEGER DEFAULT 0,
    created_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_menus_user ON menus(user_id);

-- 待办事项表
CREATE TABLE IF NOT EXISTS todos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    completed INTEGER DEFAULT 0,
    priority INTEGER DEFAULT 0,
    due_date TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_todos_user ON todos(user_id);

-- 备忘录表
CREATE TABLE IF NOT EXISTS notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    content TEXT DEFAULT '',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_notes_user ON notes(user_id);

-- 数据库连接配置表
CREATE TABLE IF NOT EXISTS db_connections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    db_type TEXT NOT NULL,
    host TEXT DEFAULT '',
    port INTEGER DEFAULT 0,
    username TEXT DEFAULT '',
    password TEXT DEFAULT '',
    "database" TEXT DEFAULT '',
    sqlite_path TEXT DEFAULT '',
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
CREATE INDEX IF NOT EXISTS idx_db_conn_user ON db_connections(user_id);

-- =============================================
-- 插入默认管理员账号
--   用户名: admin
--   密码: Mkld@2026 （SHA256 哈希存储）
-- =============================================

-- 重置 users 表自增计数器，确保 admin 用户 ID 始终为 1
DELETE FROM sqlite_sequence WHERE name='users';
INSERT OR IGNORE INTO sqlite_sequence (name, seq) VALUES ('users', 0);

INSERT OR IGNORE INTO users (username, password, created_at)
VALUES ('admin', '4c5b73ae988dd72ce2ee2038c90b8d98e05c46f461037b26301fd1ab5a844d7f', '2026-07-01 00:00:00');

-- =============================================
-- 初始化管理员菜单
-- =============================================

-- 一级菜单
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT id, 0, '财务管理', '💰', 1, '2026-07-01 00:00:00' FROM users WHERE username = 'admin';
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT id, 0, '日常工具', '🔧', 2, '2026-07-01 00:00:00' FROM users WHERE username = 'admin';
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT id, 0, '系统管理', '⚙️', 3, '2026-07-01 00:00:00' FROM users WHERE username = 'admin';

-- 二级菜单（财务管理）
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT u.id, m.id, '记账', '📊', 1, '2026-07-01 00:00:00' FROM users u JOIN menus m ON u.username = 'admin' AND m.user_id = u.id AND m.parent_id = 0 AND m.name = '财务管理';
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT u.id, m.id, '统计报表', '📈', 2, '2026-07-01 00:00:00' FROM users u JOIN menus m ON u.username = 'admin' AND m.user_id = u.id AND m.parent_id = 0 AND m.name = '财务管理';

-- 二级菜单（日常工具）
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT u.id, m.id, '待办事项', '✅', 1, '2026-07-01 00:00:00' FROM users u JOIN menus m ON u.username = 'admin' AND m.user_id = u.id AND m.parent_id = 0 AND m.name = '日常工具';
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT u.id, m.id, '备忘录', '📝', 2, '2026-07-01 00:00:00' FROM users u JOIN menus m ON u.username = 'admin' AND m.user_id = u.id AND m.parent_id = 0 AND m.name = '日常工具';
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT u.id, m.id, '数据库查询', '🗄️', 3, '2026-07-01 00:00:00' FROM users u JOIN menus m ON u.username = 'admin' AND m.user_id = u.id AND m.parent_id = 0 AND m.name = '日常工具';

-- 二级菜单（系统管理）
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) SELECT u.id, m.id, '用户管理', '👥', 1, '2026-07-01 00:00:00' FROM users u JOIN menus m ON u.username = 'admin' AND m.user_id = u.id AND m.parent_id = 0 AND m.name = '系统管理';

-- 示例数据库连接配置
INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, "database", sqlite_path, created_at, updated_at)
SELECT u.id, '本地MySQL-连接', 'mysql', '127.0.0.1', 3306, 'root', '123456', '0701_my_project', '', '2026-07-01 23:49:29', '2026-07-01 23:54:38' FROM users u WHERE u.username = 'admin';
INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, "database", sqlite_path, created_at, updated_at)
SELECT u.id, '本地SQLite-连接', 'sqlite', '', 0, '', '', '', 'D:\MyPersonalize\InstallPosition\AI_data\Trae_data\20260617_project_generate\my_project\data\0701_my_project.db', '2026-07-01 23:49:29', '2026-07-01 23:49:29' FROM users u WHERE u.username = 'admin';
INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, "database", sqlite_path, created_at, updated_at)
SELECT u.id, '本地pgsql-连接', 'postgres', '127.0.0.1', 5432, 'postgres', '123456', '0701_my_project', '', '2026-07-01 23:55:14', '2026-07-01 23:57:25' FROM users u WHERE u.username = 'admin';

-- =============================================
-- 完成！
-- 启动应用时 .env 配置：
--   DB_TYPE=sqlite
--   DB_SQLITE_PATH=data/accounting.db
-- =============================================
