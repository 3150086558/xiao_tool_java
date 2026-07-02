# -*- coding: utf-8 -*-
"""数据库查询工具路由"""
import os
import socket
from datetime import datetime
from typing import Any, Dict

from fastapi import APIRouter, Depends, HTTPException

from app.database import exec_sql, fetchall, fetchone, get_db
from app.deps import CurrentUser, get_current_user
from app.models.schemas import DbConnectionCreate, DbConnectionUpdate, DbQueryRequest

router = APIRouter(prefix="/api/app", tags=["数据库查询"])


# ==================== 数据库连接配置管理 ====================
@router.get("/db-connections")
def list_db_connections(user: CurrentUser = Depends(get_current_user)):
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """SELECT id, name, db_type, host, port, username, "database",
                      sqlite_path, created_at, updated_at
               FROM db_connections WHERE user_id = %s ORDER BY id""",
            (user.user_id,),
        )
        conns = fetchall(cur)
    for c in conns:
        c["password"] = "******"
    return {"connections": conns}


@router.post("/db-connections", status_code=201)
def create_db_connection(payload: DbConnectionCreate, user: CurrentUser = Depends(get_current_user)):
    name = payload.name.strip()
    db_type = payload.db_type.strip()
    if not name:
        raise HTTPException(status_code=400, detail="连接名称不能为空")
    if not db_type:
        raise HTTPException(status_code=400, detail="请选择数据库类型")
    if db_type == "sqlite":
        if not payload.sqlite_path.strip():
            raise HTTPException(status_code=400, detail="SQLite 文件路径不能为空")
    else:
        if not payload.host.strip() or not payload.username.strip() or not payload.database.strip():
            raise HTTPException(status_code=400, detail="主机、用户名、数据库名为必填项")

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "SELECT id FROM db_connections WHERE user_id = %s AND name = %s",
            (user.user_id, name),
        )
        if fetchone(cur):
            raise HTTPException(status_code=400, detail=f"连接名称 \"{name}\" 已存在")
        exec_sql(
            conn,
            """INSERT INTO db_connections
               (user_id, name, db_type, host, port, username, password, "database", sqlite_path, created_at, updated_at)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
            (
                user.user_id, name, db_type, payload.host.strip(), payload.port,
                payload.username.strip(), payload.password, payload.database.strip(),
                payload.sqlite_path.strip(), now, now,
            ),
        )
    return {"ok": True, "message": "连接配置已保存"}


@router.put("/db-connections/{conn_id}")
def update_db_connection(
    conn_id: int, payload: DbConnectionUpdate, user: CurrentUser = Depends(get_current_user)
):
    name = payload.name.strip()
    db_type = payload.db_type.strip()
    if not name:
        raise HTTPException(status_code=400, detail="连接名称不能为空")
    if not db_type:
        raise HTTPException(status_code=400, detail="请选择数据库类型")
    if db_type == "sqlite":
        if not payload.sqlite_path.strip():
            raise HTTPException(status_code=400, detail="SQLite 文件路径不能为空")
    else:
        if not payload.host.strip() or not payload.username.strip() or not payload.database.strip():
            raise HTTPException(status_code=400, detail="主机、用户名、数据库名为必填项")

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "SELECT id FROM db_connections WHERE id = %s AND user_id = %s",
            (conn_id, user.user_id),
        )
        if not fetchone(cur):
            raise HTTPException(status_code=404, detail="连接不存在")
        cur = exec_sql(
            conn,
            "SELECT id FROM db_connections WHERE user_id = %s AND name = %s AND id != %s",
            (user.user_id, name, conn_id),
        )
        if fetchone(cur):
            raise HTTPException(status_code=400, detail=f"连接名称 \"{name}\" 已存在")

        if payload.password:
            exec_sql(
                conn,
                """UPDATE db_connections SET name=%s, db_type=%s, host=%s, port=%s,
                       username=%s, password=%s, "database"=%s, sqlite_path=%s, updated_at=%s
                   WHERE id=%s AND user_id=%s""",
                (
                    name, db_type, payload.host.strip(), payload.port, payload.username.strip(),
                    payload.password, payload.database.strip(), payload.sqlite_path.strip(),
                    now, conn_id, user.user_id,
                ),
            )
        else:
            exec_sql(
                conn,
                """UPDATE db_connections SET name=%s, db_type=%s, host=%s, port=%s,
                       username=%s, "database"=%s, sqlite_path=%s, updated_at=%s
                   WHERE id=%s AND user_id=%s""",
                (
                    name, db_type, payload.host.strip(), payload.port, payload.username.strip(),
                    payload.database.strip(), payload.sqlite_path.strip(),
                    now, conn_id, user.user_id,
                ),
            )
    return {"ok": True, "message": "连接已更新"}


@router.delete("/db-connections/{conn_id}")
def delete_db_connection(conn_id: int, user: CurrentUser = Depends(get_current_user)):
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "SELECT id FROM db_connections WHERE id = %s AND user_id = %s",
            (conn_id, user.user_id),
        )
        if not fetchone(cur):
            raise HTTPException(status_code=404, detail="连接不存在")
        exec_sql(conn, "DELETE FROM db_connections WHERE id = %s", (conn_id,))
    return {"ok": True, "message": "连接已删除"}


# ==================== 通用数据库查询 ====================
def _connect_external_db(config: Dict[str, Any]):
    """连接外部数据库（支持 sqlite/postgres/mysql）。"""
    db_type = (config.get("db_type") or "postgres").lower()

    if db_type == "sqlite":
        import sqlite3

        sqlite_path = config.get("sqlite_path", "")
        if not sqlite_path:
            raise ValueError("SQLite 数据库文件路径不能为空")
        if not os.path.exists(sqlite_path):
            raise FileNotFoundError(f"SQLite 文件不存在：{sqlite_path}")
        conn = sqlite3.connect(sqlite_path)
        conn.row_factory = sqlite3.Row
        return conn

    if db_type == "postgres":
        import psycopg2

        host = config.get("host", "127.0.0.1")
        port = int(config.get("port", 5432))
        _probe_port(host, port, "PostgreSQL")
        try:
            return psycopg2.connect(
                host=host, port=port,
                dbname=config.get("database", ""),
                user=config.get("username", ""),
                password=config.get("password", ""),
            )
        except psycopg2.OperationalError:
            raise Exception(
                f"PostgreSQL 连接失败：无法连接到 {host}:{port} 上的数据库 "
                f"\"{config.get('database', '')}\"，请检查用户名、密码是否正确，以及数据库是否存在"
            )

    if db_type == "mysql":
        import pymysql

        host = config.get("host", "127.0.0.1")
        port = int(config.get("port", 3306))
        _probe_port(host, port, "MySQL")
        try:
            return pymysql.connect(
                host=host, port=port, user=config.get("username", ""),
                password=config.get("password", ""),
                database=config.get("database", ""), autocommit=True,
            )
        except pymysql.OperationalError as e:
            err_msg = str(e) or ""
            if hasattr(e, "args") and e.args:
                for arg in e.args:
                    if isinstance(arg, str) and arg.strip():
                        err_msg = arg.strip()
                        break
            if not err_msg:
                err_msg = f"无法连接到 {host}:{port} 上的数据库 \"{config.get('database', '')}\"，请检查用户名、密码是否正确"
            raise Exception(f"MySQL 连接失败：{err_msg}")

    raise ValueError(f"不支持的数据库类型：{db_type}")


def _probe_port(host: str, port: int, label: str):
    """探测端口是否可达。"""
    try:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(3)
        sock.connect((host, port))
        sock.close()
    except Exception:
        raise Exception(f"{label} 连接失败：无法访问 {host}:{port}，请确认服务已启动且端口正确")


@router.post("/db-query")
def db_query(payload: DbQueryRequest, user: CurrentUser = Depends(get_current_user)):
    try:
        return _db_query_internal(payload, user)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/db/query")
def db_query_v2(payload: DbQueryRequest, user: CurrentUser = Depends(get_current_user)):
    try:
        return _db_query_internal(payload, user)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


def _db_query_internal(payload: DbQueryRequest, user: CurrentUser):
    try:
        action = payload.action or "connect"
        config = dict(payload.config or {})
        db_type = (config.get("db_type") or "postgres").lower()

        # 密码为空或隐藏标记时，从已保存的连接中读取真实密码
        pwd = config.get("password", "")
        if not pwd or pwd == "******":
            with get_db() as conn:
                cur = exec_sql(
                    conn,
                    """SELECT password FROM db_connections
                       WHERE user_id = %s AND db_type = %s AND host = %s AND port = %s
                         AND username = %s AND "database" = %s
                       LIMIT 1""",
                    (
                        user.user_id, db_type, config.get("host", ""),
                        int(config.get("port", 0)),
                        config.get("username", ""), config.get("database", ""),
                    ),
                )
                row = fetchone(cur)
                if row and row.get("password"):
                    config["password"] = row["password"]

        if action == "connect":
            ext_conn = _connect_external_db(config)
            ext_conn.close()
            return {"ok": True, "message": "连接成功"}

        ext_conn = _connect_external_db(config)
        cur = ext_conn.cursor()

        if action == "tables":
            if db_type == "sqlite":
                cur.execute(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name"
                )
            elif db_type == "postgres":
                cur.execute(
                    """SELECT table_name FROM information_schema.tables
                       WHERE table_schema = 'public' ORDER BY table_name"""
                )
            elif db_type == "mysql":
                cur.execute("SHOW TABLES")
            tables = [row[0] for row in cur.fetchall()]
            cur.close()
            ext_conn.close()
            return {"tables": tables}

        if action == "schema":
            table = payload.table or ""
            if not table:
                raise HTTPException(status_code=400, detail="缺少表名")
            if db_type == "sqlite":
                cur.execute(f"PRAGMA table_info([{table}])")
                columns = [
                    {"name": r[1], "type": r[2], "nullable": "NO" if r[3] else "YES", "default": r[4]}
                    for r in cur.fetchall()
                ]
            elif db_type == "postgres":
                cur.execute(
                    """SELECT column_name, data_type, is_nullable, column_default
                       FROM information_schema.columns
                       WHERE table_name = %s AND table_schema = 'public'
                       ORDER BY ordinal_position""",
                    (table,),
                )
                columns = [
                    {"name": r[0], "type": r[1], "nullable": r[2], "default": r[3]}
                    for r in cur.fetchall()
                ]
            elif db_type == "mysql":
                cur.execute(
                    """SELECT column_name, data_type, is_nullable, column_default
                       FROM information_schema.columns
                       WHERE table_name = %s AND table_schema = DATABASE()
                       ORDER BY ordinal_position""",
                    (table,),
                )
                columns = [
                    {"name": r[0], "type": r[1], "nullable": r[2], "default": r[3]}
                    for r in cur.fetchall()
                ]
            cur.close()
            ext_conn.close()
            return {"table": table, "columns": columns}

        if action == "query":
            sql = (payload.sql or "").strip()
            if not sql:
                raise HTTPException(status_code=400, detail="SQL 不能为空")
            first_word = sql.split()[0].upper()
            if first_word not in ("SELECT", "SHOW", "DESCRIBE", "DESC", "EXPLAIN"):
                raise HTTPException(
                    status_code=400,
                    detail="仅支持查询类 SQL（SELECT / SHOW / DESCRIBE / EXPLAIN）",
                )
            cur.execute(sql)
            col_names = [desc[0] for desc in cur.description] if cur.description else []
            rows = cur.fetchall()
            cur.close()
            ext_conn.close()
            return {"columns": col_names, "rows": [list(r) for r in rows], "count": len(rows)}

        raise HTTPException(status_code=400, detail="未知操作")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
