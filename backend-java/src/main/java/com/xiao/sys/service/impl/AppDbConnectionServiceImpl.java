package com.xiao.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiao.sys.entity.AppDbConnection;
import com.xiao.sys.mapper.AppDbConnectionMapper;
import com.xiao.sys.service.AppDbConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据库连接服务实现。
 */
@Service
public class AppDbConnectionServiceImpl implements AppDbConnectionService {

    @Autowired
    private AppDbConnectionMapper dbConnectionMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<AppDbConnection> listConnections(Integer userId) {
        QueryWrapper<AppDbConnection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("id");
        List<AppDbConnection> list = dbConnectionMapper.selectList(wrapper);
        list.forEach(c -> c.setPassword(null));
        return list;
    }

    @Override
    public AppDbConnection createConnection(AppDbConnection conn, Integer userId) {
        String now = LocalDateTime.now().format(FORMATTER);
        conn.setDbType(normalizeDbType(conn.getDbType()));
        conn.setUserId(userId);
        conn.setCreatedAt(now);
        conn.setUpdatedAt(now);
        dbConnectionMapper.insert(conn);
        conn.setPassword(null);
        return conn;
    }

    @Override
    public AppDbConnection updateConnection(Integer id, AppDbConnection conn, Integer userId) {
        AppDbConnection existing = getConnectionById(id, userId);
        if (existing == null) {
            throw new RuntimeException("连接不存在");
        }
        conn.setDbType(normalizeDbType(conn.getDbType()));
        conn.setId(id);
        conn.setUserId(userId);
        conn.setUpdatedAt(LocalDateTime.now().format(FORMATTER));
        if (conn.getPassword() == null || conn.getPassword().isEmpty()) {
            conn.setPassword(existing.getPassword());
        }
        dbConnectionMapper.updateById(conn);
        conn.setPassword(null);
        return conn;
    }

    @Override
    public boolean deleteConnection(Integer id, Integer userId) {
        AppDbConnection existing = getConnectionById(id, userId);
        if (existing == null) {
            return false;
        }
        return dbConnectionMapper.deleteById(id) > 0;
    }

    @Override
    public AppDbConnection getConnectionById(Integer id, Integer userId) {
        QueryWrapper<AppDbConnection> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("user_id", userId);
        return dbConnectionMapper.selectOne(wrapper);
    }

    @Override
    public boolean testConnection(AppDbConnection conn) {
        String dbType = normalizeDbType(conn.getDbType());
        String url;
        String driver;
        String username = conn.getUsername() != null ? conn.getUsername() : "";
        String password = conn.getPassword() != null ? conn.getPassword() : "";
        String host = conn.getHost() != null ? conn.getHost() : "127.0.0.1";
        int port = conn.getPort() != null ? conn.getPort() : 3306;
        String database = conn.getDatabase() != null ? conn.getDatabase() : "";
        String sqlitePath = conn.getSqlitePath() != null ? conn.getSqlitePath() : "";

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

        java.sql.Connection sqlConn = null;
        try {
            Class.forName(driver);
            sqlConn = java.sql.DriverManager.getConnection(url, username, password);
            return sqlConn != null && !sqlConn.isClosed();
        } catch (Exception e) {
            return false;
        } finally {
            if (sqlConn != null) {
                try {
                    sqlConn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public boolean testConnectionById(Integer id, Integer userId) {
        AppDbConnection conn = getConnectionById(id, userId);
        if (conn == null) {
            return false;
        }
        return testConnection(conn);
    }

    @Override
    public Map<String, Object> executeQuery(Integer id, Integer userId, String sql) {
        AppDbConnection conn = getConnectionById(id, userId);
        if (conn == null) {
            throw new RuntimeException("连接不存在");
        }
        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("SQL 不能为空");
        }

        String dbType = normalizeDbType(conn.getDbType());
        String url = buildJdbcUrl(conn, dbType);
        String driver = getDriverClassName(dbType);
        String username = conn.getUsername() != null ? conn.getUsername() : "";
        String password = conn.getPassword() != null ? conn.getPassword() : "";

        java.sql.Connection sqlConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName(driver);
            sqlConn = DriverManager.getConnection(url, username, password);
            stmt = sqlConn.createStatement();
            boolean hasResultSet = stmt.execute(sql);

            Map<String, Object> result = new HashMap<>();
            if (hasResultSet) {
                rs = stmt.getResultSet();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columns.add(metaData.getColumnLabel(i));
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                int rowCount = 0;
                while (rs.next() && rowCount < 1000) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(columns.get(i - 1), rs.getObject(i));
                    }
                    rows.add(row);
                    rowCount++;
                }

                result.put("columns", columns);
                result.put("rows", rows);
                result.put("rowCount", rowCount);
                result.put("isQuery", true);
            } else {
                int updateCount = stmt.getUpdateCount();
                result.put("updateCount", updateCount);
                result.put("isQuery", false);
            }
            result.put("success", true);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        } finally {
            closeResources(rs, stmt, sqlConn);
        }
    }

    @Override
    public List<Map<String, Object>> getTableList(Integer id, Integer userId) {
        AppDbConnection conn = getConnectionById(id, userId);
        if (conn == null) {
            throw new RuntimeException("连接不存在");
        }

        String dbType = normalizeDbType(conn.getDbType());
        String url = buildJdbcUrl(conn, dbType);
        String driver = getDriverClassName(dbType);
        String username = conn.getUsername() != null ? conn.getUsername() : "";
        String password = conn.getPassword() != null ? conn.getPassword() : "";

        java.sql.Connection sqlConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName(driver);
            sqlConn = DriverManager.getConnection(url, username, password);

            List<Map<String, Object>> tables = new ArrayList<>();
            DatabaseMetaData metaData = sqlConn.getMetaData();

            String catalog = conn.getDatabase();
            String schemaPattern = null;
            if ("postgresql".equals(dbType)) {
                schemaPattern = "public";
            }

            rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE", "VIEW"});
            while (rs.next()) {
                Map<String, Object> table = new LinkedHashMap<>();
                table.put("tableName", rs.getString("TABLE_NAME"));
                table.put("tableType", rs.getString("TABLE_TYPE"));
                table.put("remarks", rs.getString("REMARKS"));
                tables.add(table);
            }
            return tables;
        } catch (Exception e) {
            throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, stmt, sqlConn);
        }
    }

    @Override
    public List<Map<String, Object>> getTableStructure(Integer id, Integer userId, String tableName) {
        AppDbConnection conn = getConnectionById(id, userId);
        if (conn == null) {
            throw new RuntimeException("连接不存在");
        }

        String dbType = normalizeDbType(conn.getDbType());
        String url = buildJdbcUrl(conn, dbType);
        String driver = getDriverClassName(dbType);
        String username = conn.getUsername() != null ? conn.getUsername() : "";
        String password = conn.getPassword() != null ? conn.getPassword() : "";

        java.sql.Connection sqlConn = null;
        ResultSet rs = null;
        try {
            Class.forName(driver);
            sqlConn = DriverManager.getConnection(url, username, password);

            List<Map<String, Object>> columns = new ArrayList<>();
            DatabaseMetaData metaData = sqlConn.getMetaData();

            String catalog = conn.getDatabase();
            String schemaPattern = null;
            if ("postgresql".equals(dbType)) {
                schemaPattern = "public";
            }

            rs = metaData.getColumns(catalog, schemaPattern, tableName, "%");
            while (rs.next()) {
                Map<String, Object> col = new LinkedHashMap<>();
                col.put("columnName", rs.getString("COLUMN_NAME"));
                col.put("dataType", rs.getString("TYPE_NAME"));
                col.put("columnSize", rs.getInt("COLUMN_SIZE"));
                col.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                col.put("columnDef", rs.getString("COLUMN_DEF"));
                col.put("remarks", rs.getString("REMARKS"));
                col.put("ordinalPosition", rs.getInt("ORDINAL_POSITION"));
                columns.add(col);
            }

            if (columns.isEmpty() && "mysql".equals(dbType)) {
                Statement stmt = sqlConn.createStatement();
                rs = stmt.executeQuery("DESCRIBE `" + tableName + "`");
                while (rs.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("columnName", rs.getString("Field"));
                    col.put("dataType", rs.getString("Type"));
                    col.put("nullable", "YES".equals(rs.getString("Null")));
                    col.put("columnDef", rs.getString("Default"));
                    col.put("remarks", rs.getString("Extra"));
                    columns.add(col);
                }
                stmt.close();
            }

            return columns;
        } catch (Exception e) {
            throw new RuntimeException("获取表结构失败: " + e.getMessage(), e);
        } finally {
            closeResources(rs, null, sqlConn);
        }
    }

    @Override
    public Map<String, Object> previewTableData(Integer id, Integer userId, String tableName, Integer limit) {
        int pageSize = limit != null && limit > 0 ? limit : 100;
        String sql = "SELECT * FROM \"" + tableName + "\" LIMIT " + pageSize;
        return executeQuery(id, userId, sql);
    }

    private String buildJdbcUrl(AppDbConnection conn, String dbType) {
        String host = conn.getHost() != null ? conn.getHost() : "127.0.0.1";
        int port = conn.getPort() != null ? conn.getPort() : 3306;
        String database = conn.getDatabase() != null ? conn.getDatabase() : "";
        String sqlitePath = conn.getSqlitePath() != null ? conn.getSqlitePath() : "";

        switch (dbType) {
            case "postgresql":
                return "jdbc:postgresql://" + host + ":" + port + "/" + database;
            case "sqlite":
                return "jdbc:sqlite:" + sqlitePath;
            case "mysql":
            default:
                return "jdbc:mysql://" + host + ":" + port + "/" + database
                        + "?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
        }
    }

    private String getDriverClassName(String dbType) {
        switch (dbType) {
            case "postgresql":
                return "org.postgresql.Driver";
            case "sqlite":
                return "org.sqlite.JDBC";
            case "mysql":
            default:
                return "com.mysql.cj.jdbc.Driver";
        }
    }

    private void closeResources(ResultSet rs, Statement stmt, java.sql.Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (Exception ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (Exception ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
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