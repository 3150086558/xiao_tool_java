# -*- coding: utf-8 -*-
"""备忘录业务逻辑"""
from datetime import datetime

from app.database import exec_sql, fetchone, get_db


def list_notes(user_id: int) -> list:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "SELECT * FROM notes WHERE user_id = %s ORDER BY updated_at DESC",
            (user_id,),
        )
        return [dict(r) for r in cur.fetchall()]


def create_note(user_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    content = str(data.get("content", "")).strip()

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """INSERT INTO notes (user_id, title, content, created_at, updated_at)
               VALUES (%s, %s, %s, %s, %s) RETURNING *""",
            (user_id, title, content, now, now),
        )
        return fetchone(cur)


def update_note(user_id: int, note_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    content = str(data.get("content", "")).strip()

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """UPDATE notes SET title=%s, content=%s, updated_at=%s
               WHERE id=%s AND user_id=%s RETURNING *""",
            (title, content, now, note_id, user_id),
        )
        return fetchone(cur)


def delete_note(user_id: int, note_id: int) -> int:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "DELETE FROM notes WHERE id=%s AND user_id=%s",
            (note_id, user_id),
        )
        return cur.rowcount
