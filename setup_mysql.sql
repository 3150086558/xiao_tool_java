-- =============================================
-- MySQL 初始化脚本
-- 用 root 执行：
--   mysql -u root -p < setup_mysql.sql
-- =============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS 0701_my_project DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 使用数据库
USE 0701_my_project;

-- =============================================
-- 3. 创建表结构
-- =============================================

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    created_at VARCHAR(30) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 记账记录表
CREATE TABLE IF NOT EXISTS records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    record_date VARCHAR(20) NOT NULL,
    type VARCHAR(10) NOT NULL,
    category VARCHAR(50) NOT NULL,
    sub_category VARCHAR(50) DEFAULT '',
    amount DOUBLE NOT NULL,
    account VARCHAR(50) DEFAULT '',
    note TEXT,
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_records_user (user_id),
    INDEX idx_records_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 菜单表
CREATE TABLE IF NOT EXISTS menus (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    parent_id INT DEFAULT 0,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(10) DEFAULT '',
    sort_order INT DEFAULT 0,
    created_at VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_menus_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 待办事项表
CREATE TABLE IF NOT EXISTS todos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    completed TINYINT DEFAULT 0,
    priority INT DEFAULT 0,
    due_date VARCHAR(20) DEFAULT NULL,
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_todos_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 备忘录表
CREATE TABLE IF NOT EXISTS notes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_notes_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 数据库连接配置表
CREATE TABLE IF NOT EXISTS db_connections (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    db_type VARCHAR(20) NOT NULL,
    host VARCHAR(100) DEFAULT '',
    port INT DEFAULT 0,
    username VARCHAR(100) DEFAULT '',
    password VARCHAR(200) DEFAULT '',
    `database` VARCHAR(100) DEFAULT '',
    sqlite_path VARCHAR(500) DEFAULT '',
    created_at VARCHAR(30) NOT NULL,
    updated_at VARCHAR(30) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_db_conn_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 4. 插入默认管理员账号
--    用户名: admin
--    密码: Mkld@2026 （SHA256 哈希存储）
-- =============================================

INSERT IGNORE INTO users (username, password, created_at)
VALUES ('admin', '4c5b73ae988dd72ce2ee2038c90b8d98e05c46f461037b26301fd1ab5a844d7f', '2026-07-01 00:00:00');

-- =============================================
-- 5. 初始化管理员菜单
-- =============================================

-- 使用变量存储菜单ID
SET @uid = (SELECT id FROM users WHERE username = 'admin' LIMIT 1);

-- 一级菜单
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, 0, '财务管理', '💰', 1, '2026-07-01 00:00:00');
SET @fid1 = LAST_INSERT_ID();

INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, 0, '日常工具', '🔧', 2, '2026-07-01 00:00:00');
SET @fid2 = LAST_INSERT_ID();

INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, 0, '系统管理', '⚙️', 3, '2026-07-01 00:00:00');
SET @fid3 = LAST_INSERT_ID();

-- 二级菜单
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, @fid1, '记账', '📊', 1, '2026-07-01 00:00:00');
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, @fid1, '统计报表', '📈', 2, '2026-07-01 00:00:00');
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, @fid2, '待办事项', '✅', 1, '2026-07-01 00:00:00');
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, @fid2, '备忘录', '📝', 2, '2026-07-01 00:00:00');
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, @fid2, '数据库查询', '🗄️', 3, '2026-07-01 00:00:00');
INSERT INTO menus (user_id, parent_id, name, icon, sort_order, created_at) VALUES (@uid, @fid3, '用户管理', '👥', 1, '2026-07-01 00:00:00');

-- 示例数据库连接配置
INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, `database`, sqlite_path, created_at, updated_at)
VALUES (@uid, '本地MySQL-连接', 'mysql', '127.0.0.1', 3306, 'root', '123456', '0701_my_project', '', '2026-07-01 23:49:29', '2026-07-01 23:54:38');
INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, `database`, sqlite_path, created_at, updated_at)
VALUES (@uid, '本地SQLite-连接', 'sqlite', '', 0, '', '', '', 'D:\\MyPersonalize\\InstallPosition\\AI_data\\Trae_data\\20260617_project_generate\\my_project\\data\\0701_my_project.db', '2026-07-01 23:49:29', '2026-07-01 23:49:29');
INSERT INTO db_connections (user_id, name, db_type, host, port, username, password, `database`, sqlite_path, created_at, updated_at)
VALUES (@uid, '本地pgsql-连接', 'postgres', '127.0.0.1', 5432, 'postgres', '123456', '0701_my_project', '', '2026-07-01 23:55:14', '2026-07-01 23:57:25');

-- =============================================
-- 完成！
-- 启动应用时 .env 配置：
--   DB_TYPE=mysql
--   DB_HOST=127.0.0.1
--   DB_PORT=3306
--   DB_NAME=0701_my_project
--   DB_USER=root
--   DB_PASSWORD=你的mysql密码
-- =============================================
