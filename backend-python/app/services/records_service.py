# -*- coding: utf-8 -*-
"""记账业务逻辑"""
from datetime import datetime
from typing import List, Tuple

from app.database import exec_sql, fetchall, fetchone, get_db


def validate_payload(data: dict) -> Tuple[str, str, str, str, float, str, str]:
    """校验并规范化记账数据，返回:
    (record_date, record_type, category, sub_category, amount, account, note)
    """
    record_date = str(data.get("record_date", "")).strip()
    record_type = str(data.get("type", "")).strip()
    category = str(data.get("category", "")).strip()
    sub_category = str(data.get("sub_category", "")).strip()
    account = str(data.get("account", "")).strip()
    note = str(data.get("note", "")).strip()

    # 支持多种日期格式
    parsed_date = None
    for fmt in ("%Y-%m-%d", "%Y/%m/%d", "%Y年%m月%d日"):
        try:
            parsed_date = datetime.strptime(record_date, fmt)
            break
        except ValueError:
            continue
    if parsed_date is None:
        raise ValueError("日期格式应为 YYYY-MM-DD")
    record_date = parsed_date.strftime("%Y-%m-%d")

    # 支持中文类型
    type_map = {"收入": "income", "支出": "expense", "income": "income", "expense": "expense"}
    if record_type not in type_map:
        raise ValueError("类型只能是收入或支出")
    record_type = type_map[record_type]

    if not category:
        raise ValueError("项目不能为空")

    try:
        amount = round(float(data.get("amount", 0)), 2)
    except Exception:
        raise ValueError("金额必须是数字")

    # 支持负数自动识别类型
    if amount < 0:
        amount = abs(amount)
        if record_type == "income":
            record_type = "expense"

    return record_date, record_type, category, sub_category, amount, account, note


def build_where(filters: dict, visible_user_ids: List[int]) -> Tuple[str, list]:
    """构造查询条件，支持项目、消费分类、日期范围过滤。"""
    where, params = ["user_id = ANY(%s)"], [list(visible_user_ids)]
    item = (filters.get("item") or "").strip()
    category = (filters.get("category") or "").strip()
    start_date = (filters.get("startDate") or "").strip()
    end_date = (filters.get("endDate") or "").strip()
    rtype = (filters.get("type") or "").strip()
    keyword = (filters.get("keyword") or "").strip()

    if item:
        where.append("category LIKE %s")
        params.append(f"%{item}%")
    if category:
        where.append("sub_category LIKE %s")
        params.append(f"%{category}%")
    if start_date:
        where.append("record_date >= %s")
        params.append(start_date)
    if end_date:
        where.append("record_date <= %s")
        params.append(end_date)
    if rtype:
        where.append("type = %s")
        params.append(rtype)
    if keyword:
        where.append("(category LIKE %s OR account LIKE %s OR note LIKE %s)")
        kw = f"%{keyword}%"
        params.extend([kw, kw, kw])
    return " AND ".join(where), params


def list_records(filters: dict, visible_user_ids: List[int]) -> list:
    where, params = build_where(filters, visible_user_ids)
    with get_db() as conn:
        cur = exec_sql(
            conn,
            f"""SELECT * FROM records WHERE {where}
                ORDER BY record_date DESC, id DESC LIMIT 1000""",
            params,
        )
        return fetchall(cur)


def create_record(user_id: int, data: dict) -> dict:
    record_date, record_type, category, sub_category, amount, account, note = validate_payload(data)
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """INSERT INTO records
               (user_id, record_date, type, category, sub_category, amount, account, note, created_at, updated_at)
               VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
               RETURNING *""",
            (user_id, record_date, record_type, category, sub_category, amount, account, note, now, now),
        )
        return fetchone(cur)


def update_record(user_id: int, record_id: int, data: dict) -> dict:
    record_date, record_type, category, sub_category, amount, account, note = validate_payload(data)
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with get_db() as conn:
        cur = exec_sql(
            conn,
            """UPDATE records SET record_date=%s, type=%s, category=%s, sub_category=%s,
                   amount=%s, account=%s, note=%s, updated_at=%s
               WHERE id=%s AND user_id=%s
               RETURNING *""",
            (record_date, record_type, category, sub_category, amount, account, note, now, record_id, user_id),
        )
        return fetchone(cur)


def delete_record(user_id: int, record_id: int) -> int:
    with get_db() as conn:
        cur = exec_sql(
            conn,
            "DELETE FROM records WHERE id=%s AND user_id=%s",
            (record_id, user_id),
        )
        return cur.rowcount


def clear_all(user_id: int) -> int:
    with get_db() as conn:
        cur = exec_sql(conn, "DELETE FROM records WHERE user_id=%s", (user_id,))
        return cur.rowcount


def delete_all_records(user_id: int) -> int:
    """删除当前用户的所有记账记录。"""
    with get_db() as conn:
        cur = exec_sql(conn, "DELETE FROM records WHERE user_id=%s", (user_id,))
        return cur.rowcount


def list_records_for_export(filters: dict, visible_user_ids: List[int]) -> list:
    """导出用：不限制 1000 条。"""
    where, params = build_where(filters, visible_user_ids)
    with get_db() as conn:
        cur = exec_sql(
            conn,
            f"""SELECT * FROM records WHERE {where}
                ORDER BY record_date DESC, id DESC""",
            params,
        )
        return fetchall(cur)
