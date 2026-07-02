# -*- coding: utf-8 -*-
"""组织权限管理系统 - 辅服务（记账 / 待办 / 备忘录等）

启动命令：
    uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
"""
from fastapi import FastAPI, HTTPException, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.routers import db_query, import_export, notes, records, stats, todos

app = FastAPI(
    title="组织权限管理系统 - 辅服务",
    description="记账 / 待办 / 备忘录 / 数据库查询等业务功能",
    version="1.0.0",
)

# CORS：允许所有来源
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================== 统一异常处理 ====================
@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):
    detail = exc.detail if isinstance(exc.detail, str) else "请求错误"
    return JSONResponse(status_code=exc.status_code, content={"error": detail})


@app.exception_handler(HTTPException)
async def fastapi_http_exception_handler(request: Request, exc: HTTPException):
    detail = exc.detail if isinstance(exc.detail, str) else "请求错误"
    return JSONResponse(status_code=exc.status_code, content={"error": detail})


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    errors = exc.errors()
    msg = "; ".join(
        f"{'/'.join(str(e.get('loc', [])))}: {e.get('msg', '')}" for e in errors
    )
    return JSONResponse(status_code=422, content={"error": msg or "参数校验失败"})


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    return JSONResponse(status_code=500, content={"error": str(exc) or "服务器内部错误"})


# ==================== 健康检查 ====================
@app.get("/api/health")
def health():
    return {"ok": True, "service": "backend-python"}


# ==================== 注册路由 ====================
app.include_router(records.router)
app.include_router(stats.router)
app.include_router(import_export.router)
app.include_router(todos.router)
app.include_router(notes.router)
app.include_router(db_query.router)
