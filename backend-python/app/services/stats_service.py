# -*- coding: utf-8 -*-
"""统计汇总业务逻辑"""
from typing import List

from app.database import exec_sql, fetchall, get_db
from app.services.records_service import build_where


def summary(filters: dict, visible_user_ids: List[int]) -> dict:
    where, params = build_where(filters, visible_user_ids)
    with get_db() as conn:
        cur = exec_sql(
            conn,
            f"""SELECT type, COALESCE(SUM(amount), 0) AS total
                FROM records WHERE {where} GROUP BY type""",
            params,
        )
        summary_rows = fetchall(cur)
        income = sum(r["total"] for r in summary_rows if r["type"] == "income")
        expense = sum(r["total"] for r in summary_rows if r["type"] == "expense")

        cur = exec_sql(
            conn,
            f"""SELECT type, category, COALESCE(SUM(amount), 0) AS amount
                FROM records WHERE {where}
                GROUP BY type, category ORDER BY amount DESC""",
            params,
        )
        category_rows = fetchall(cur)
    return {
        "income": income,
        "expense": expense,
        "balance": income - expense,
        "categories": category_rows,
    }
