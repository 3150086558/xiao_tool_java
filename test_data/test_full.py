#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import urllib.request
import urllib.parse
import json
import uuid
import http.cookies
import sys

base_url = "http://127.0.0.1:8000"
session_cookie = None

def request(path, method="GET", data=None, headers={}):
    global session_cookie
    url = f"{base_url}{path}"
    
    if session_cookie:
        headers['Cookie'] = session_cookie
    
    if method == "GET":
        req = urllib.request.Request(url, headers=headers)
    else:
        headers['Content-Type'] = 'application/json'
        req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers, method=method)
    
    try:
        with urllib.request.urlopen(req) as resp:
            set_cookie = resp.headers.get('Set-Cookie')
            if set_cookie:
                cookie = http.cookies.SimpleCookie()
                cookie.load(set_cookie)
                if 'session_id' in cookie:
                    session_cookie = f"session_id={cookie['session_id'].value}"
            
            return resp.status, resp.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')

print("=" * 60)
print("测试完整功能流程")
print("=" * 60)

# 1. 测试注册
print("\n1. 测试注册功能")
username = "testuser_" + str(uuid.uuid4())[:8]
password = "Test1234"
print(f"用户名: {username}")
status, resp = request("/api/register", "POST", {
    "username": username,
    "password": password,
    "confirm_password": password
})
print(f"状态码: {status}")
print(f"响应: {resp}")
if status != 200:
    print("❌ 注册失败")
    sys.exit(1)
print("✅ 注册成功")

# 2. 测试登录
print("\n2. 测试登录功能")
status, resp = request("/api/login", "POST", {
    "username": username,
    "password": password
})
print(f"状态码: {status}")
print(f"响应: {resp}")
if status != 200:
    print("❌ 登录失败")
    sys.exit(1)
print("✅ 登录成功")

# 3. 测试获取用户信息
print("\n3. 测试获取用户信息")
status, resp = request("/api/user", "GET")
print(f"状态码: {status}")
print(f"响应: {resp}")
if status != 200:
    print("❌ 获取用户信息失败")
    sys.exit(1)
print("✅ 获取用户信息成功")

# 4. 测试获取菜单
print("\n4. 测试获取菜单")
status, resp = request("/api/menus", "GET")
print(f"状态码: {status}")
print(f"响应: {resp}")
data = json.loads(resp)
if data.get('menus'):
    print(f"菜单数量: {len(data['menus'])}")
    for menu in data['menus']:
        print(f"  - {menu['icon']} {menu['name']}")
print("✅ 获取菜单成功")

# 5. 测试修改密码
print("\n5. 测试修改密码")
new_password = "NewPass123"
status, resp = request("/api/change-password", "POST", {
    "old_password": password,
    "new_password": new_password,
    "confirm_password": new_password
})
print(f"状态码: {status}")
print(f"响应: {resp}")
if status != 200:
    print("❌ 修改密码失败")
    sys.exit(1)
print("✅ 修改密码成功")

# 6. 测试旧密码登录（应该失败）
print("\n6. 测试旧密码登录（应该失败）")
status, resp = request("/api/login", "POST", {
    "username": username,
    "password": password
})
print(f"状态码: {status}")
print(f"响应: {resp}")
if status == 200:
    print("❌ 旧密码登录不应该成功")
    sys.exit(1)
print("✅ 旧密码登录失败，符合预期")

# 7. 测试新密码登录
print("\n7. 测试新密码登录")
status, resp = request("/api/login", "POST", {
    "username": username,
    "password": new_password
})
print(f"状态码: {status}")
print(f"响应: {resp}")
if status != 200:
    print("❌ 新密码登录失败")
    sys.exit(1)
print("✅ 新密码登录成功")

# 8. 测试添加记账记录
print("\n8. 测试添加记账记录")
status, resp = request("/api/records", "POST", {
    "record_date": "2025-06-17",
    "type": "expense",
    "category": "午餐",
    "sub_category": "餐饮",
    "amount": 25.5,
    "account": "微信",
    "note": "测试记录"
})
print(f"状态码: {status}")
print(f"响应: {resp}")
if status != 201:
    print("❌ 添加记录失败")
    sys.exit(1)
print("✅ 添加记录成功")

# 9. 测试获取记录列表
print("\n9. 测试获取记录列表")
status, resp = request("/api/records", "GET")
print(f"状态码: {status}")
data = json.loads(resp)
print(f"记录数量: {len(data['records'])}")
for r in data['records']:
    print(f"  - {r['record_date']} | {'收入' if r['type'] == 'income' else '支出'} | {r['category']} | ¥{r['amount']}")
print("✅ 获取记录列表成功")

# 10. 测试获取汇总数据
print("\n10. 测试获取汇总数据")
status, resp = request("/api/summary", "GET")
print(f"状态码: {status}")
data = json.loads(resp)
print(f"收入: ¥{data['income']}")
print(f"支出: ¥{data['expense']}")
print(f"结余: ¥{data['balance']}")
print("✅ 获取汇总数据成功")

print("\n" + "=" * 60)
print("✅ 所有测试通过！")
print("=" * 60)
