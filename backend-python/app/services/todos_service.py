# -*- coding: utf-8 -*-
"""待办事项业务逻辑"""
from datetime import datetime

from app.database import exec_sql, fetchone, get_db


def _convert_todo(row) -> dict:
    d = dict(row)
    d["done"] = bool(d.get("completed", 0))
    d["dueDate"] = d.get("due_date")
    d["createTime"] = d.get("created_at")
    d["updateTime"] = d.get("updated_at")
    return d


def list_todos(user_id: int) -> list:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """SELECT * FROM todos WHERE user_id = %s
               ORDER BY completed ASC,
               CASE priority WHEN 'high' THEN 0 WHEN 'normal' THEN 1 ELSE 2 END,
               created_at DESC""",
            (user_id,),
        )
        return [_convert_todo(r) for r in cur.fetchall()]


def get_todo(user_id: int, todo_id: int) -> dict:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "SELECT * FROM todos WHERE id = %s AND user_id = %s",
            (todo_id, user_id),
        )
        row = fetchone(cur)
        return _convert_todo(row) if row else None


def create_todo(user_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    priority = str(data.get("priority", "normal") or "normal").strip() or "normal"
    due_date = str(data.get("dueDate") or data.get("due_date") or "").strip() or None
    remark = str(data.get("remark", "")).strip() or ""

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """INSERT INTO todos (user_id, title, priority, due_date, remark, created_at, updated_at)
               VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING *""",
            (user_id, title, priority, due_date, remark, now, now),
        )
        row = fetchone(cur)
        return _convert_todo(row) if row else None


def update_todo(user_id: int, todo_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    completed = int(data.get("done", data.get("completed", 0)))
    priority = str(data.get("priority", "normal") or "normal").strip() or "normal"
    due_date = str(data.get("dueDate") or data.get("due_date") or "").strip() or None
    remark = str(data.get("remark", "")).strip() or ""

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """UPDATE todos SET title=%s, completed=%s, priority=%s, due_date=%s, remark=%s, updated_at=%s
               WHERE id=%s AND user_id=%s RETURNING *""",
            (title, completed, priority, due_date, remark, now, todo_id, user_id),
        )
        row = fetchone(cur)
        return _convert_todo(row) if row else None


def delete_todo(user_id: int, todo_id: int) -> int:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "DELETE FROM todos WHERE id=%s AND user_id=%s",
            (todo_id, user_id),
        )
        return cur.rowcount
