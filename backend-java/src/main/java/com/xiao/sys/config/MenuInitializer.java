package com.xiao.sys.config;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单初始化器 - 启动时检查并添加缺失的系统菜单，清理重复菜单
 */
@Component
public class MenuInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuInitializer.class);

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void run(String... args) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            // 先清理重复菜单
            cleanupDuplicateMenus(sqlSession);
            // 初始化菜单
            initMenuIfNotExists(sqlSession, "数据字典", "dict", "system/dict/index", "Collection", 1, 6);
            initMenuIfNotExists(sqlSession, "数据库查询", "db-query", "db-query/index", "DataBase", 2, 3);
            sqlSession.commit();
        } catch (Exception e) {
            log.warn("菜单初始化失败，可能是数据库表尚未创建: {}", e.getMessage());
        }
    }

    private void cleanupDuplicateMenus(SqlSession sqlSession) {
        try {
            // 1. 将数据库查询菜单移动到日常工具(parent_id=2)下，并设置排序
            String updateDbQuerySql = "UPDATE sys_menu SET parent_id = 2, sort_order = 3 WHERE path = 'db-query' AND parent_id != 2";
            PreparedStatement updateDbQueryStmt = sqlSession.getConnection().prepareStatement(updateDbQuerySql);
            int updatedCount = updateDbQueryStmt.executeUpdate();
            if (updatedCount > 0) {
                log.info("已将数据库查询菜单移动到日常工具目录下");
            }
            
            // 2. 确保数据字典菜单在系统管理(parent_id=1)下
            String updateDictSql = "UPDATE sys_menu SET parent_id = 1, sort_order = 6 WHERE path = 'dict' AND parent_id != 1";
            PreparedStatement updateDictStmt = sqlSession.getConnection().prepareStatement(updateDictSql);
            int dictUpdatedCount = updateDictStmt.executeUpdate();
            if (dictUpdatedCount > 0) {
                log.info("已将数据字典菜单移动到系统管理目录下");
            }
            
            // 3. 清理其他重复菜单
            String[] menuPaths = {"dict", "db-query"};
            for (String path : menuPaths) {
                String findSql = "SELECT id FROM sys_menu WHERE path = ? ORDER BY id ASC";
                PreparedStatement findStmt = sqlSession.getConnection().prepareStatement(findSql);
                findStmt.setString(1, path);
                ResultSet rs = findStmt.executeQuery();
                
                List<Integer> menuIds = new ArrayList<>();
                while (rs.next()) {
                    menuIds.add(rs.getInt(1));
                }
                
                if (menuIds.size() > 1) {
                    // 保留第一个，删除其他的
                    for (int i = 1; i < menuIds.size(); i++) {
                        Integer menuId = menuIds.get(i);
                        // 先删除角色菜单关联
                        String deleteRoleSql = "DELETE FROM sys_role_menu WHERE menu_id = ?";
                        PreparedStatement deleteRoleStmt = sqlSession.getConnection().prepareStatement(deleteRoleSql);
                        deleteRoleStmt.setInt(1, menuId);
                        deleteRoleStmt.execute();
                        
                        // 再删除菜单
                        String deleteMenuSql = "DELETE FROM sys_menu WHERE id = ?";
                        PreparedStatement deleteMenuStmt = sqlSession.getConnection().prepareStatement(deleteMenuSql);
                        deleteMenuStmt.setInt(1, menuId);
                        deleteMenuStmt.execute();
                        
                        log.info("清理重复菜单，删除ID: {}", menuId);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("清理重复菜单失败: {}", e.getMessage());
        }
    }

    private void initMenuIfNotExists(SqlSession sqlSession, String menuName, String path, String component, String icon, int parentId, int sortOrder) {
        try {
            // 检查是否已存在相同路径的菜单
            String checkSql = "SELECT id FROM sys_menu WHERE path = ? AND parent_id = ?";
            PreparedStatement checkStmt = sqlSession.getConnection().prepareStatement(checkSql);
            checkStmt.setString(1, path);
            checkStmt.setInt(2, parentId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                // 不存在则创建新菜单
                String insertSql = "INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, icon, sort_order, visible, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
                PreparedStatement insertStmt = sqlSession.getConnection().prepareStatement(insertSql);
                insertStmt.setInt(1, parentId);
                insertStmt.setString(2, menuName);
                insertStmt.setString(3, "M");
                insertStmt.setString(4, path);
                insertStmt.setString(5, component);
                insertStmt.setString(6, icon);
                insertStmt.setInt(7, sortOrder);
                insertStmt.setInt(8, 1);
                insertStmt.setInt(9, 1);
                insertStmt.setObject(10, LocalDateTime.now());
                insertStmt.setObject(11, LocalDateTime.now());
                
                ResultSet insertRs = insertStmt.executeQuery();
                if (insertRs.next()) {
                    Integer menuId = insertRs.getInt(1);
                    log.info("已添加{}菜单，菜单ID: {}", menuName, menuId);
                    
                    // 给超级管理员分配权限
                    try {
                        String roleSql = "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
                        PreparedStatement roleStmt = sqlSession.getConnection().prepareStatement(roleSql);
                        roleStmt.setInt(1, 1);
                        roleStmt.setInt(2, menuId);
                        roleStmt.execute();
                        log.info("已给超级管理员分配{}菜单权限", menuName);
                    } catch (Exception e) {
                        log.warn("分配{}菜单权限失败: {}", menuName, e.getMessage());
                    }
                }
            } else {
                log.info("{}菜单已存在，跳过创建", menuName);
            }
        } catch (Exception e) {
            log.warn("添加{}菜单失败: {}", menuName, e.getMessage());
        }
    }
}
