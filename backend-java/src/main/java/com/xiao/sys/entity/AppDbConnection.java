package com.xiao.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 数据库连接实体类
 */
@TableName("db_connections")
public class AppDbConnection {

    @TableId(type = IdType.AUTO)
    private Integer id;

    // 用户ID
    private Integer userId;

    // 连接名称
    private String name;

    // 数据库类型 mysql/postgresql/sqlite
    private String dbType;

    // 主机地址
    private String host;

    // 端口
    private Integer port;

    // 数据库名
    private String database;

    // 用户名
    private String username;

    // 密码
    private String password;

    // SQLite文件路径
    private String sqlitePath;

    // 创建时间
    private String createdAt;

    // 更新时间
    private String updatedAt;

    // 连接状态（非数据库字段）
    @TableField(exist = false)
    private Boolean connected;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSqlitePath() { return sqlitePath; }
    public void setSqlitePath(String sqlitePath) { this.sqlitePath = sqlitePath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Boolean getConnected() { return connected; }
    public void setConnected(Boolean connected) { this.connected = connected; }
}
