# -*- coding: utf-8 -*-
"""备忘录业务逻辑"""
import json
from datetime import datetime

from app.database import exec_sql, fetchone, get_db


def _convert_note(row) -> dict:
    d = dict(row)
    d["createTime"] = d.get("created_at")
    d["updateTime"] = d.get("updated_at")
    if d.get("tags"):
        try:
            d["tags"] = json.loads(d["tags"]) if isinstance(d["tags"], str) else d["tags"]
        except (json.JSONDecodeError, TypeError):
            d["tags"] = []
    else:
        d["tags"] = []
    return d


def list_notes(user_id: int) -> list:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "SELECT * FROM notes WHERE user_id = %s ORDER BY updated_at DESC",
            (user_id,),
        )
        return [_convert_note(r) for r in cur.fetchall()]


def get_note(user_id: int, note_id: int) -> dict:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "SELECT * FROM notes WHERE id = %s AND user_id = %s",
            (note_id, user_id),
        )
        row = fetchone(cur)
        return _convert_note(row) if row else None


def create_note(user_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    content = str(data.get("content", "")).strip()
    tags = data.get("tags") or []

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """INSERT INTO notes (user_id, title, content, tags, created_at, updated_at)
               VALUES (%s, %s, %s, %s, %s, %s) RETURNING *""",
            (user_id, title, content, json.dumps(tags, ensure_ascii=False), now, now),
        )
        row = fetchone(cur)
        return _convert_note(row) if row else None


def update_note(user_id: int, note_id: int, data: dict) -> dict:
    title = str(data.get("title", "")).strip()
    if not title:
        raise ValueError("标题不能为空")
    content = str(data.get("content", "")).strip()
    tags = data.get("tags") or []

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """UPDATE notes SET title=%s, content=%s, tags=%s, updated_at=%s
               WHERE id=%s AND user_id=%s RETURNING *""",
            (title, content, json.dumps(tags, ensure_ascii=False), now, note_id, user_id),
        )
        row = fetchone(cur)
        return _convert_note(row) if row else None


def delete_note(user_id: int, note_id: int) -> int:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "DELETE FROM notes WHERE id=%s AND user_id=%s",
            (note_id, user_id),
        )
        return cur.rowcount
