# -*- coding: utf-8 -*-
"""统计路由"""
from typing import List
from fastapi import APIRouter, Depends, Query

from app.deps import CurrentUser, get_current_user, get_visible_user_ids
from app.database import exec_sql, fetchall, get_db
from app.services.records_service import build_where
from app.services import stats_service

router = APIRouter(prefix="/api/app", tags=["统计"])


def _summary(filters: dict, visible_user_ids: List[int]) -> dict:
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


def _trend(filters: dict, visible_user_ids: List[int]) -> list:
    where, params = build_where(filters, visible_user_ids)
    with get_db() as conn:
        cur = exec_sql(
            conn,
            f"""SELECT SUBSTRING(record_date, 1, 7) AS month,
                       type,
                       COALESCE(SUM(amount), 0) AS amount
                FROM records WHERE {where}
                GROUP BY SUBSTRING(record_date, 1, 7), type
                ORDER BY month""",
            params,
        )
        rows = fetchall(cur)
    result = {}
    for r in rows:
        month = r["month"]
        if month not in result:
            result[month] = {"month": month, "income": 0, "expense": 0}
        result[month][r["type"]] = r["amount"]
    return list(result.values())


@router.get("/stats/summary")
def stats_summary(
    month: str = Query("", description="月份过滤，如 2025-06"),
    user: CurrentUser = Depends(get_current_user),
):
    filters = {"month": (month or "").strip(), "type": "", "keyword": ""}
    visible = get_visible_user_ids(user.user_id, user.token)
    result = _summary(filters, visible)
    return {"code": 200, "data": result}


@router.get("/stats/category")
def stats_category(
    type: str = Query("", description="类型：income/expense"),
    month: str = Query("", description="月份过滤，如 2025-06"),
    user: CurrentUser = Depends(get_current_user),
):
    filters = {"month": (month or "").strip(), "type": (type or "").strip(), "keyword": ""}
    visible = get_visible_user_ids(user.user_id, user.token)
    result = _summary(filters, visible)
    categories = [c for c in result["categories"] if (not type) or c["type"] == type]
    return {"code": 200, "data": categories}


@router.get("/stats/trend")
def stats_trend(
    month: str = Query("", description="月份过滤，如 2025-06"),
    user: CurrentUser = Depends(get_current_user),
):
    filters = {"month": (month or "").strip(), "type": "", "keyword": ""}
    visible = get_visible_user_ids(user.user_id, user.token)
    result = _trend(filters, visible)
    return {"code": 200, "data": result}
