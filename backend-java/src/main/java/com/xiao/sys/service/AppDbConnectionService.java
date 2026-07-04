package com.xiao.sys.service;

import com.xiao.sys.entity.AppDbConnection;
import java.util.List;
import java.util.Map;

/**
 * 数据库连接服务接口
 */
public interface AppDbConnectionService {

    /**
     * 获取用户的所有数据库连接
     */
    List<AppDbConnection> listConnections(Integer userId);

    /**
     * 新增数据库连接
     */
    AppDbConnection createConnection(AppDbConnection conn, Integer userId);

    /**
     * 更新数据库连接
     */
    AppDbConnection updateConnection(Integer id, AppDbConnection conn, Integer userId);

    /**
     * 删除数据库连接
     */
    boolean deleteConnection(Integer id, Integer userId);

    /**
     * 根据ID获取连接
     */
    AppDbConnection getConnectionById(Integer id, Integer userId);

    /**
     * 测试数据库连接
     */
    boolean testConnection(AppDbConnection conn);

    /**
     * 通过已保存的连接ID测试连接
     */
    boolean testConnectionById(Integer id, Integer userId);

    /**
     * 执行 SQL 查询
     */
    Map<String, Object> executeQuery(Integer id, Integer userId, String sql);

    /**
     * 获取数据库表列表
     */
    List<Map<String, Object>> getTableList(Integer id, Integer userId);

    /**
     * 获取表结构
     */
    List<Map<String, Object>> getTableStructure(Integer id, Integer userId, String tableName);

    /**
     * 预览表数据
     */
    Map<String, Object> previewTableData(Integer id, Integer userId, String tableName, Integer limit);
}
