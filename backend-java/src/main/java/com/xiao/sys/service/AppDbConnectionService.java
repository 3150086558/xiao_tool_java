package com.xiao.sys.service;

import com.xiao.sys.entity.AppDbConnection;
import java.util.List;

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
}
