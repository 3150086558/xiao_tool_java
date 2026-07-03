package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.service.DbQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据库查询 Controller
 */
@RestController
@RequestMapping("/api/app/db/query")
public class DbQueryController {

    private final DbQueryService dbQueryService;

    public DbQueryController(DbQueryService dbQueryService) {
        this.dbQueryService = dbQueryService;
    }

    /**
     * 统一查询入口
     * action: tables / query / schema
     */
    @PostMapping
    public Result<?> executeQuery(@RequestBody Map<String, Object> body) {
        String action = getStr(body, "action");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) body.get("config");

        try {
            switch (action) {
                case "tables": {
                    List<String> tables = dbQueryService.listTables(config);
                    return Result.success(Map.of("tables", tables));
                }
                case "query": {
                    String sql = getStr(body, "sql");
                    Map<String, Object> result = dbQueryService.executeQuery(config, sql);
                    return Result.success(result);
                }
                case "schema": {
                    String table = getStr(body, "table");
                    List<Map<String, Object>> columns = dbQueryService.getTableSchema(config, table);
                    return Result.success(Map.of("columns", columns));
                }
                default:
                    return Result.fail("不支持的操作: " + action);
            }
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    private String getStr(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? String.valueOf(v) : null;
    }
}
