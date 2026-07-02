# -*- coding: utf-8 -*-
"""导入导出路由"""
from fastapi import APIRouter, Depends, File, HTTPException, Query, UploadFile
from fastapi.responses import Response

from app.deps import CurrentUser, get_current_user, get_visible_user_ids
from app.services import excel_service, records_service

router = APIRouter(prefix="/api/app", tags=["导入导出"])


def _filters(month: str, rtype: str, keyword: str) -> dict:
    return {"month": (month or "").strip(), "type": (rtype or "").strip(), "keyword": (keyword or "").strip()}


@router.get("/export.csv")
def export_csv(
    month: str = Query(""),
    type: str = Query(""),
    keyword: str = Query(""),
    user: CurrentUser = Depends(get_current_user),
):
    filters = _filters(month, type, keyword)
    visible = get_visible_user_ids(user.user_id, user.token)
    rows = records_service.list_records_for_export(filters, visible)
    data = excel_service.export_csv(rows)
    return Response(
        content=data,
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": "attachment; filename=accounting-records.csv"},
    )


@router.get("/export.xlsx")
def export_excel(
    month: str = Query(""),
    type: str = Query(""),
    keyword: str = Query(""),
    user: CurrentUser = Depends(get_current_user),
):
    filters = _filters(month, type, keyword)
    visible = get_visible_user_ids(user.user_id, user.token)
    rows = records_service.list_records_for_export(filters, visible)
    data = excel_service.export_excel(rows)
    return Response(
        content=data,
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        headers={"Content-Disposition": "attachment; filename=accounting-records.xlsx"},
    )


@router.get("/download-template")
def download_template(user: CurrentUser = Depends(get_current_user)):
    data = excel_service.download_template()
    return Response(
        content=data,
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        headers={"Content-Disposition": "attachment; filename=AccountingTemplate.xlsx"},
    )


@router.post("/import")
async def import_excel(
    file: UploadFile = File(..., description="Excel 文件"),
    user: CurrentUser = Depends(get_current_user),
):
    if not file.filename or not (
        file.filename.lower().endswith(".xlsx") or file.filename.lower().endswith(".xls")
    ):
        raise HTTPException(status_code=400, detail="请上传 Excel 文件（.xlsx）")
    file_data = await file.read()
    try:
        result = excel_service.import_excel(user.user_id, file_data)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    return result
