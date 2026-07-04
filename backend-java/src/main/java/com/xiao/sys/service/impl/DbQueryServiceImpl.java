package com.xiao.sys.service.impl;

import com.xiao.sys.service.DbQueryService;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 数据库查询服务实现。
 */
@Service
public class DbQueryServiceImpl implements DbQueryService {

    @Override
    public List<String> listTables(Map<String, Object> config) throws Exception {
        String dbType = normalizeDbType(getString(config, "db_type", "mysql"));
        List<String> tables = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection(config, dbType);
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schemaPattern = "postgresql".equals(dbType) ? "public" : null;
            ResultSet rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } finally {
            closeConnection(conn);
        }
        Collections.sort(tables);
        return tables;
    }

    @Override
    public Map<String, Object> executeQuery(Map<String, Object> config, String sql) throws Exception {
        String dbType = normalizeDbType(getString(config, "db_type", "mysql"));
        Map<String, Object> result = new HashMap<>();
        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection(config, dbType);
            stmt = conn.createStatement();
            boolean hasResultSet = stmt.execute(sql);
            if (hasResultSet) {
                rs = stmt.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int colCount = metaData.getColumnCount();
                for (int i = 1; i <= colCount; i++) {
                    columns.add(metaData.getColumnLabel(i));
                }
                int limit = 1000;
                while (rs.next() && rows.size() < limit) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                result.put("columns", columns);
                result.put("rows", rows);
            } else {
                int updateCount = stmt.getUpdateCount();
                result.put("columns", Arrays.asList("影响行数"));
                result.put("rows", Arrays.asList(Collections.singletonMap("影响行数", updateCount)));
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception ignored) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (Exception ignored) {
                }
            }
            closeConnection(conn);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTableSchema(Map<String, Object> config, String table) throws Exception {
        String dbType = normalizeDbType(getString(config, "db_type", "mysql"));
        List<Map<String, Object>> columns = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection(config, dbType);
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            String schemaPattern = "postgresql".equals(dbType) ? "public" : null;
            ResultSet rs = metaData.getColumns(catalog, schemaPattern, table, "%");
            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("name", rs.getString("COLUMN_NAME"));
                col.put("type", rs.getString("TYPE_NAME"));
                col.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                col.put("default", rs.getString("COLUMN_DEF"));
                columns.add(col);
            }
            rs.close();
        } finally {
            closeConnection(conn);
        }
        return columns;
    }

    private Connection getConnection(Map<String, Object> config, String dbType) throws Exception {
        dbType = normalizeDbType(dbType);
        String url;
        String driver;
        String username = getString(config, "username", "");
        String password = getString(config, "password", "");
        String host = getString(config, "host", "127.0.0.1");
        int port = getInt(config, "port", 3306);
        String database = getString(config, "database", "");
        String sqlitePath = getString(config, "sqlite_path", "");

        switch (dbType) {
            case "postgresql":
                driver = "org.postgresql.Driver";
                url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
                break;
            case "sqlite":
                driver = "org.sqlite.JDBC";
                url = "jdbc:sqlite:" + sqlitePath;
                break;
            case "mysql":
            default:
                driver = "com.mysql.cj.jdbc.Driver";
                url = "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
                break;
        }
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception ignored) {
            }
        }
    }

    private String getString(Map<String, Object> map, String key, String def) {
        Object v = map.get(key);
        return v != null ? String.valueOf(v) : def;
    }

    private int getInt(Map<String, Object> map, String key, int def) {
        Object v = map.get(key);
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private String normalizeDbType(String dbType) {
        if (dbType == null || dbType.isBlank()) {
            return "mysql";
        }
        if ("postgres".equalsIgnoreCase(dbType)) {
            return "postgresql";
        }
        return dbType.toLowerCase(Locale.ROOT);
    }
}