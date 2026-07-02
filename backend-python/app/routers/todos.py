# -*- coding: utf-8 -*-
"""待办事项路由"""
from fastapi import APIRouter, Depends, HTTPException, Query

from app.deps import CurrentUser, get_current_user
from app.models.schemas import TodoCreate, TodoUpdate
from app.services import todos_service

router = APIRouter(prefix="/api/app", tags=["待办事项"])


@router.get("/todo/page")
def todo_page(
    page: int = Query(1, ge=1),
    size: int = Query(10, ge=1, le=10000),
    status: str = Query("", description="状态：pending/completed"),
    priority: str = Query("", description="优先级：low/normal/high"),
    user: CurrentUser = Depends(get_current_user),
):
    rows = todos_service.list_todos(user.user_id)
    if status:
        if status == "pending":
            rows = [r for r in rows if r.get("completed") == 0]
        elif status == "completed":
            rows = [r for r in rows if r.get("completed") == 1]
    if priority:
        rows = [r for r in rows if r.get("priority") == priority]
    total = len(rows)
    start = (page - 1) * size
    end = start + size
    return {
        "code": 200,
        "data": {
            "records": rows[start:end],
            "total": total,
            "page": page,
            "size": size
        }
    }


@router.post("/todo")
def create_todo(payload: TodoCreate, user: CurrentUser = Depends(get_current_user)):
    try:
        row = todos_service.create_todo(user.user_id, payload.model_dump())
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return {"code": 200, "data": row}


@router.put("/todo/{todo_id}")
def update_todo(todo_id: int, payload: TodoUpdate, user: CurrentUser = Depends(get_current_user)):
    try:
        row = todos_service.update_todo(user.user_id, todo_id, payload.model_dump())
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    if not row:
        raise HTTPException(status_code=404, detail="待办不存在")
    return {"code": 200, "data": row}


@router.delete("/todo/{todo_id}")
def delete_todo(todo_id: int, user: CurrentUser = Depends(get_current_user)):
    deleted = todos_service.delete_todo(user.user_id, todo_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="待办不存在")
    return {"code": 200, "data": {"ok": True}}


@router.put("/todo/{todo_id}/done")
def toggle_todo_done(todo_id: int, payload: dict, user: CurrentUser = Depends(get_current_user)):
    completed = int(payload.get("completed", payload.get("done", 0)))
    try:
        row = todos_service.update_todo(user.user_id, todo_id, {
            "title": "temp",
            "completed": completed,
            "priority": "normal",
            "due_date": None,
        })
    except ValueError:
        row = None
    if not row:
        raise HTTPException(status_code=404, detail="待办不存在")
    return {"code": 200, "data": row}
