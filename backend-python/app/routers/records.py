# -*- coding: utf-8 -*-
"""记账管理路由"""
import io
from fastapi import APIRouter, Depends, HTTPException, Query, UploadFile, File
from fastapi.responses import StreamingResponse

from app.deps import CurrentUser, get_current_user, get_visible_user_ids
from app.models.schemas import RecordCreate, RecordUpdate
from app.services import records_service
from app.services import excel_service

router = APIRouter(prefix="/api/app", tags=["记账管理"])


def _filters(item: str, category: str, startDate: str, endDate: str) -> dict:
    return {
        "item": (item or "").strip(),
        "category": (category or "").strip(),
        "startDate": (startDate or "").strip(),
        "endDate": (endDate or "").strip(),
    }


@router.get("/accounting/page")
def accounting_page(
    page: int = Query(1, ge=1),
    size: int = Query(10, ge=1, le=100),
    item: str = Query("", description="项目关键字"),
    category: str = Query("", description="消费分类"),
    startDate: str = Query("", description="开始日期，如 2025-06-01"),
    endDate: str = Query("", description="结束日期，如 2025-06-30"),
    user: CurrentUser = Depends(get_current_user),
):
    filters = _filters(item, category, startDate, endDate)
    visible = get_visible_user_ids(user.user_id, user.token)
    rows = records_service.list_records(filters, visible)
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


@router.post("/accounting")
def create_accounting(payload: RecordCreate, user: CurrentUser = Depends(get_current_user)):
    try:
        row = records_service.create_record(user.user_id, payload.model_dump())
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return {"code": 200, "data": row}


@router.put("/accounting/{record_id}")
def update_accounting(record_id: int, payload: RecordUpdate, user: CurrentUser = Depends(get_current_user)):
    try:
        row = records_service.update_record(user.user_id, record_id, payload.model_dump())
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    if not row:
        raise HTTPException(status_code=404, detail="记录不存在")
    return {"code": 200, "data": row}


@router.delete("/accounting/{record_id}")
def delete_accounting(record_id: int, user: CurrentUser = Depends(get_current_user)):
    deleted = records_service.delete_record(user.user_id, record_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="记录不存在")
    return {"code": 200, "data": {"ok": True}}


@router.post("/accounting/import")
def import_accounting(
    file: UploadFile = File(...),
    user: CurrentUser = Depends(get_current_user),
):
    try:
        file_data = file.file.read()
        result = excel_service.import_excel(user.user_id, file_data)
        return {"code": 200, "data": result}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.delete("/accounting/all")
def delete_all_accounting(user: CurrentUser = Depends(get_current_user)):
    deleted = records_service.delete_all_records(user.user_id)
    return {"code": 200, "data": {"deleted": deleted}}


@router.get("/accounting/export")
def export_accounting(
    item: str = Query("", description="项目关键字"),
    category: str = Query("", description="消费分类"),
    startDate: str = Query("", description="开始日期，如 2025-06-01"),
    endDate: str = Query("", description="结束日期，如 2025-06-30"),
    user: CurrentUser = Depends(get_current_user),
):
    try:
        filters = _filters(item, category, startDate, endDate)
        visible = get_visible_user_ids(user.user_id, user.token)
        rows = records_service.list_records_for_export(filters, visible)
        buf = io.BytesIO()
        excel_service.export_excel(rows, buf)
        buf.seek(0)
        return StreamingResponse(
            buf,
            media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            headers={"Content-Disposition": "attachment; filename=records.xlsx"},
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
