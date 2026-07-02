# -*- coding: utf-8 -*-
"""认证与数据权限依赖"""
import os
from typing import List

import httpx
import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

# JWT 配置（与 Java 主服务共享密钥）
JWT_SECRET = os.getenv("JWT_SECRET", "xiao-sys-secret-key-2026-very-long-secret")
JWT_ALGORITHM = os.getenv("JWT_ALGORITHM", "HS256")

# Java 主服务地址，用于查询数据权限
JAVA_SERVICE_URL = os.getenv("JAVA_SERVICE_URL", "http://127.0.0.1:8080")
# 是否启用完整数据权限（默认 self 模式，仅查看自己的数据）
DATA_SCOPE_ENABLED = os.getenv("DATA_SCOPE_ENABLED", "false").strip().lower() == "true"

security = HTTPBearer(auto_error=False)


class CurrentUser:
    """当前登录用户信息"""

    def __init__(self, user_id: int, username: str, token: str):
        self.user_id = user_id
        self.username = username
        self.token = token


def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
) -> CurrentUser:
    """从 Authorization Bearer token 中解析当前用户。"""
    if credentials is None or not credentials.credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="未提供认证令牌",
        )
    token = credentials.credentials
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="认证令牌已过期"
        )
    except jwt.PyJWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="无效的认证令牌"
        )

    # 兼容多种字段命名
    user_id = payload.get("user_id") or payload.get("userId") or payload.get("uid") or payload.get("sub")
    username = payload.get("username") or payload.get("user_name") or payload.get("sub")
    if not user_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="令牌中缺少用户信息"
        )
    return CurrentUser(user_id=int(user_id), username=username or "", token=token)


def get_visible_user_ids(user_id: int, token: str) -> List[int]:
    """获取当前用户可见的用户 ID 列表。

    调用 Java 服务的 /api/sys/data-scope/visible-users 接口。
    简化实现：未启用数据权限或调用失败时，仅返回当前用户（self 模式）。
    """
    if not DATA_SCOPE_ENABLED:
        return [user_id]
    try:
        resp = httpx.get(
            f"{JAVA_SERVICE_URL}/api/sys/data-scope/visible-users",
            headers={"Authorization": f"Bearer {token}"},
            timeout=3.0,
        )
        if resp.status_code == 200:
            data = resp.json()
            ids = (
                data.get("user_ids")
                or data.get("data")
                or data.get("visible_users")
                or []
            )
            if isinstance(ids, list) and ids:
                return [int(i) for i in ids]
    except Exception:
        pass
    return [user_id]
