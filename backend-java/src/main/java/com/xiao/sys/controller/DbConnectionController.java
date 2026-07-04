package com.xiao.sys.controller;

import com.xiao.sys.common.Result;
import com.xiao.sys.entity.AppDbConnection;
import com.xiao.sys.security.LoginUser;
import com.xiao.sys.service.AppDbConnectionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据库连接管理 Controller
 */
@RestController
@RequestMapping("/api/app/db-connections")
public class DbConnectionController {

    private final AppDbConnectionService dbConnectionService;

    public DbConnectionController(AppDbConnectionService dbConnectionService) {
        this.dbConnectionService = dbConnectionService;
    }

    /**
     * 获取连接列表
     */
    @GetMapping
    public Result<Map<String, Object>> listConnections() {
        Integer userId = getCurrentUserId();
        List<AppDbConnection> list = dbConnectionService.listConnections(userId);
        return Result.success(Map.of("connections", list));
    }

    /**
     * 新增连接
     */
    @PostMapping
    public Result<AppDbConnection> createConnection(@RequestBody Map<String, Object> body) {
        Integer userId = getCurrentUserId();
        AppDbConnection conn = mapToEntity(body);
        AppDbConnection result = dbConnectionService.createConnection(conn, userId);
        return Result.success(result);
    }

    /**
     * 更新连接
     */
    @PutMapping("/{id}")
    public Result<AppDbConnection> updateConnection(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer userId = getCurrentUserId();
        AppDbConnection conn = mapToEntity(body);
        AppDbConnection result = dbConnectionService.updateConnection(id, conn, userId);
        return Result.success(result);
    }

    /**
     * 删除连接
     */
    @DeleteMapping("/{id}")
    public Result<Map<String, Boolean>> deleteConnection(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        boolean deleted = dbConnectionService.deleteConnection(id, userId);
        return Result.success(Map.of("ok", deleted));
    }

    @PostMapping("/test")
    public Result<Map<String, Object>> testConnection(@RequestBody Map<String, Object> body) {
        AppDbConnection conn = mapToEntity(body);
        boolean success = dbConnectionService.testConnection(conn);
        return Result.success(Map.of("success", success));
    }

    /**
     * 通过已保存的连接ID测试连接
     */
    @PostMapping("/{id}/test")
    public Result<Map<String, Object>> testConnectionById(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        boolean success = dbConnectionService.testConnectionById(id, userId);
        return Result.success(Map.of("success", success));
    }

    /**
     * 获取数据库表列表
     */
    @GetMapping("/{id}/tables")
    public Result<List<Map<String, Object>>> getTableList(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        List<Map<String, Object>> tables = dbConnectionService.getTableList(id, userId);
        return Result.success(tables);
    }

    /**
     * 获取表结构
     */
    @GetMapping("/{id}/tables/{tableName}/structure")
    public Result<List<Map<String, Object>>> getTableStructure(
            @PathVariable Integer id,
            @PathVariable String tableName) {
        Integer userId = getCurrentUserId();
        List<Map<String, Object>> structure = dbConnectionService.getTableStructure(id, userId, tableName);
        return Result.success(structure);
    }

    /**
     * 预览表数据
     */
    @GetMapping("/{id}/tables/{tableName}/preview")
    public Result<Map<String, Object>> previewTableData(
            @PathVariable Integer id,
            @PathVariable String tableName,
            @RequestParam(defaultValue = "100") Integer limit) {
        Integer userId = getCurrentUserId();
        Map<String, Object> data = dbConnectionService.previewTableData(id, userId, tableName, limit);
        return Result.success(data);
    }

    /**
     * 执行 SQL 查询
     */
    @PostMapping("/{id}/execute")
    public Result<Map<String, Object>> executeQuery(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        Integer userId = getCurrentUserId();
        String sql = getStr(body, "sql");
        Map<String, Object> result = dbConnectionService.executeQuery(id, userId, sql);
        return Result.success(result);
    }

    private AppDbConnection mapToEntity(Map<String, Object> body) {
        AppDbConnection conn = new AppDbConnection();
        conn.setName(getStr(body, "name"));
        conn.setDbType(getStr(body, "db_type"));
        conn.setHost(getStr(body, "host"));
        Object port = body.get("port");
        if (port != null) {
            conn.setPort(Integer.parseInt(port.toString()));
        }
        conn.setDatabase(getStr(body, "database"));
        conn.setUsername(getStr(body, "username"));
        conn.setPassword(getStr(body, "password"));
        conn.setSqlitePath(getStr(body, "sqlite_path"));
        return conn;
    }

    private String getStr(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? String.valueOf(v) : null;
    }

    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
