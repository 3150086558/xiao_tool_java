package com.xiao.sys.service;

import java.util.List;
import java.util.Map;

/**
 * 数据库查询服务接口
 */
public interface DbQueryService {

    /**
     * 测试连接并获取表列表
     */
    List<String> listTables(Map<String, Object> config) throws Exception;

    /**
     * 执行SQL查询
     */
    Map<String, Object> executeQuery(Map<String, Object> config, String sql) throws Exception;

    /**
     * 获取表结构
     */
    List<Map<String, Object>> getTableSchema(Map<String, Object> config, String table) throws Exception;
}
