# -*- coding: utf-8 -*-
"""PostgreSQL 数据库连接与查询工具"""
import os
from contextlib import contextmanager

import psycopg2
from psycopg2.extras import RealDictCursor

# 数据库连接配置（默认匹配 org_sys）
DB_CONFIG = {
    "host": os.getenv("DB_HOST", "127.0.0.1"),
    "port": int(os.getenv("DB_PORT", "5432")),
    "dbname": os.getenv("DB_NAME", "org_sys"),
    "user": os.getenv("DB_USER", "postgres"),
    "password": os.getenv("DB_PASSWORD", "123456"),
}


@contextmanager
def get_db():
    """获取数据库连接的上下文管理器。

    成功时提交事务，异常时回滚，最终关闭连接。
    """
    conn = psycopg2.connect(**DB_CONFIG)
    try:
        yield conn
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


def exec_sql(conn, sql, params=None):
    """执行 SQL，返回使用 RealDictCursor 的游标。"""
    cur = conn.cursor(cursor_factory=RealDictCursor)
    cur.execute(sql, params or ())
    return cur


def fetchone(cur):
    """取一条记录并转为 dict。"""
    row = cur.fetchone()
    return dict(row) if row else None


def fetchall(cur):
    """取所有记录并转为 dict 列表。"""
    rows = cur.fetchall()
    return [dict(row) for row in rows] if rows else []
