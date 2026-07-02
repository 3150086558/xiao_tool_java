# -*- coding: utf-8 -*-
"""备忘录路由"""
from fastapi import APIRouter, Depends, HTTPException, Query

from app.deps import CurrentUser, get_current_user
from app.models.schemas import NoteCreate, NoteUpdate
from app.services import notes_service

router = APIRouter(prefix="/api/app", tags=["备忘录"])


@router.get("/note/page")
def note_page(
    page: int = Query(1, ge=1),
    size: int = Query(10, ge=1, le=10000),
    keyword: str = Query("", description="关键字"),
    user: CurrentUser = Depends(get_current_user),
):
    rows = notes_service.list_notes(user.user_id)
    if keyword:
        kw = keyword.lower()
        rows = [r for r in rows if kw in r.get("title", "").lower() or kw in r.get("content", "").lower()]
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


@router.post("/note")
def create_note(payload: NoteCreate, user: CurrentUser = Depends(get_current_user)):
    try:
        row = notes_service.create_note(user.user_id, payload.model_dump())
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return {"code": 200, "data": row}


@router.put("/note/{note_id}")
def update_note(note_id: int, payload: NoteUpdate, user: CurrentUser = Depends(get_current_user)):
    try:
        row = notes_service.update_note(user.user_id, note_id, payload.model_dump())
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    if not row:
        raise HTTPException(status_code=404, detail="备忘录不存在")
    return {"code": 200, "data": row}


@router.delete("/note/{note_id}")
def delete_note(note_id: int, user: CurrentUser = Depends(get_current_user)):
    deleted = notes_service.delete_note(user.user_id, note_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="备忘录不存在")
    return {"code": 200, "data": {"ok": True}}
