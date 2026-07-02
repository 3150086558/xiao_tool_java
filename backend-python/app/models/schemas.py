# -*- coding: utf-8 -*-
"""Pydantic 数据模型"""
from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


# ==================== 记账 ====================
class RecordBase(BaseModel):
    record_date: str = Field(..., description="日期，支持 YYYY-MM-DD / YYYY/MM/DD / YYYY年MM月DD日")
    type: str = Field(..., description="类型：收入/支出 或 income/expense")
    category: str = Field(..., description="项目")
    sub_category: str = Field("", description="消费分类")
    amount: float = Field(..., description="金额")
    account: str = Field("", description="账户")
    note: str = Field("", description="备注")


class RecordCreate(RecordBase):
    pass


class RecordUpdate(RecordBase):
    pass


# ==================== 待办 ====================
class TodoBase(BaseModel):
    title: str = Field(..., description="标题")
    priority: str = Field("normal", description="优先级：low/normal/high")
    due_date: Optional[str] = Field(None, description="截止日期")


class TodoCreate(TodoBase):
    pass


class TodoUpdate(BaseModel):
    title: str = Field(..., description="标题")
    completed: int = Field(0, description="是否完成：0/1")
    priority: str = Field("normal", description="优先级：low/normal/high")
    due_date: Optional[str] = Field(None, description="截止日期")


# ==================== 备忘录 ====================
class NoteBase(BaseModel):
    title: str = Field(..., description="标题")
    content: str = Field("", description="内容")


class NoteCreate(NoteBase):
    pass


class NoteUpdate(NoteBase):
    pass


# ==================== 数据库连接配置 ====================
class DbConnectionBase(BaseModel):
    name: str = Field(..., description="连接名称")
    db_type: str = Field(..., description="数据库类型：sqlite/postgres/mysql")
    host: str = Field("", description="主机")
    port: int = Field(0, description="端口")
    username: str = Field("", description="用户名")
    password: str = Field("", description="密码（更新时空表示不修改）")
    database: str = Field("", description="数据库名")
    sqlite_path: str = Field("", description="SQLite 文件路径")


class DbConnectionCreate(DbConnectionBase):
    pass


class DbConnectionUpdate(DbConnectionBase):
    pass


# ==================== 数据库查询 ====================
class DbQueryRequest(BaseModel):
    action: str = Field("connect", description="操作：connect/tables/schema/query")
    config: Dict[str, Any] = Field(default_factory=dict, description="数据库连接配置")
    table: Optional[str] = Field(None, description="表名（schema 操作使用）")
    sql: Optional[str] = Field(None, description="SQL 语句（query 操作使用）")
