#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import urllib.request
import json
import uuid

print("="*50)
print("测试注册接口")
print("="*50)

url = "http://127.0.0.1:8000/api/register"
random_name = "user_" + str(uuid.uuid4())[:8]
data = {
    "username": random_name,
    "password": "Test1234",
    "confirm_password": "Test1234"
}

print(f"用户名: {random_name}")
try:
    req = urllib.request.Request(
        url,
        data=json.dumps(data).encode('utf-8'),
        headers={'Content-Type': 'application/json'},
        method='POST'
    )
    res = urllib.request.urlopen(req)
    print(f"状态码: {res.status}")
    print(f"响应: {res.read().decode('utf-8')}")
except Exception as e:
    print(f"错误: {e}")
    if hasattr(e, 'read'):
        print(f"错误详情: {e.read().decode('utf-8')}")

print("\n" + "="*50)
print("测试登录接口")
print("="*50)

try:
    req = urllib.request.Request(
        "http://127.0.0.1:8000/api/login",
        data=json.dumps({"username": random_name, "password": "Test1234"}).encode('utf-8'),
        headers={'Content-Type': 'application/json'},
        method='POST'
    )
    res = urllib.request.urlopen(req)
    print(f"状态码: {res.status}")
    print(f"响应: {res.read().decode('utf-8')}")
except Exception as e:
    print(f"错误: {e}")
