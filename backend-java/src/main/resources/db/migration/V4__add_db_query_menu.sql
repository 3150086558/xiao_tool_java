-- 数据库查询菜单（幂等插入，确保系统管理 parent_id=1 存在）
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, icon, sort_order, visible, status, created_at, updated_at)
SELECT 1, '数据库查询', 'M', 'db-query', 'db-query/index', 'DataBase', 7, 1, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '数据库查询' AND parent_id = 1)
ON CONFLICT DO NOTHING;

-- 给超级管理员(role_id=1)分配数据库查询菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_name = '数据库查询' AND parent_id = 1
ON CONFLICT DO NOTHING;
