package com.xiao.sys;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 约束初始化脚本与当前 Java 实体字段保持一致，避免新环境初始化后功能直接报错。
 */
class X2026070322xhjSqlInitContractTest {

    @Test
    void postgresInitShouldContainCurrentTodoAndNoteColumns() throws Exception {
        String sql = Files.readString(Path.of("..", "sql", "init_postgres.sql"));

        assertAll(
                () -> assertTrue(sql.contains("assignee"), "todos 表缺少 assignee 字段"),
                () -> assertTrue(sql.contains("completed_at"), "todos 表缺少 completed_at 字段"),
                () -> assertTrue(sql.contains("note_type"), "notes 表缺少 note_type 字段")
        );
    }

    @Test
    void postgresMigrationShouldKeepCompletedAtCompatibleWithCurrentEntityShape() throws Exception {
        String migrationSql = Files.readString(Path.of("src", "main", "resources", "db", "migration", "V2__add_dict_and_fields.sql"));
        String appCode = Files.readString(Path.of("src", "main", "java", "com", "xiao", "sys", "XiaoSysApplication.java"));

        assertAll(
                () -> assertTrue(migrationSql.contains("ALTER TABLE todos ADD COLUMN IF NOT EXISTS completed_at VARCHAR(30);"), "PostgreSQL 迁移脚本未统一 completed_at 字段类型"),
                () -> assertTrue(appCode.contains("ALTER TABLE todos ALTER COLUMN completed_at TYPE VARCHAR(30)"), "PostgreSQL 旧库 completed_at 类型纠偏逻辑缺失")
        );
    }

    @Test
    void mysqlAndSqliteInitShouldContainCurrentTodoAndNoteColumns() throws Exception {
        String mysqlSql = Files.readString(Path.of("..", "sql", "init_mysql.sql"));
        String sqliteSql = Files.readString(Path.of("..", "sql", "init_sqlite.sql"));

        assertAll(
                () -> assertTrue(mysqlSql.contains("assignee"), "MySQL 初始化脚本缺少 todos.assignee"),
                () -> assertTrue(mysqlSql.contains("completed_at"), "MySQL 初始化脚本缺少 todos.completed_at"),
                () -> assertTrue(mysqlSql.contains("note_type"), "MySQL 初始化脚本缺少 notes.note_type"),
                () -> assertTrue(sqliteSql.contains("assignee"), "SQLite 初始化脚本缺少 todos.assignee"),
                () -> assertTrue(sqliteSql.contains("completed_at"), "SQLite 初始化脚本缺少 todos.completed_at"),
                () -> assertTrue(sqliteSql.contains("note_type"), "SQLite 初始化脚本缺少 notes.note_type")
        );
    }
}