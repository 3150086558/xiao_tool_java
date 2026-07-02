# -*- coding: utf-8 -*-
"""待办事项业务逻辑"""
from datetime import datetime

from app.database import exec_sql, fetchone, get_db


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
        return [dict(r) for r in cur.fetchall()]


def create_todo(user_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    priority = str(data.get("priority", "normal") or "normal").strip() or "normal"
    due_date = str(data.get("due_date", "")).strip() or None

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """INSERT INTO todos (user_id, title, priority, due_date, created_at, updated_at)
               VALUES (%s, %s, %s, %s, %s, %s) RETURNING *""",
            (user_id, title, priority, due_date, now, now),
        )
        return fetchone(cur)


def update_todo(user_id: int, todo_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    completed = int(data.get("completed", 0))
    priority = str(data.get("priority", "normal") or "normal").strip() or "normal"
    due_date = str(data.get("due_date", "")).strip() or None

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """UPDATE todos SET title=%s, completed=%s, priority=%s, due_date=%s, updated_at=%s
               WHERE id=%s AND user_id=%s RETURNING *""",
            (title, completed, priority, due_date, now, todo_id, user_id),
        )
        return fetchone(cur)


def delete_todo(user_id: int, todo_id: int) -> int:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "DELETE FROM todos WHERE id=%s AND user_id=%s",
            (todo_id, user_id),
        )
        return cur.rowcount
