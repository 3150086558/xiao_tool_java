#!/usr/bin/env python3
import urllib.request
import json
import http.cookies

base_url = "http://127.0.0.1:8000"
session_cookie = None

def request(path, method="GET", data=None):
    global session_cookie
    url = f"{base_url}{path}"
    headers = {}
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
    except Exception as e:
        print(f"错误: {e}")
        return None, None

# 1. 登录
print("1. 登录")
status, resp = request("/api/login", "POST", {"username": "admin", "password": "Mkld@2026"})
print(f"登录: {resp}")

# 2. 添加待办
print("\n2. 添加待办事项")
status, resp = request("/api/todos", "POST", {"title": "完成项目报告", "priority": 1})
print(f"添加待办: {resp}")

status, resp = request("/api/todos", "POST", {"title": "买水果", "priority": 0, "due_date": "2026-07-05"})
print(f"添加待办: {resp}")

# 3. 获取待办列表
print("\n3. 获取待办列表")
status, resp = request("/api/todos", "GET")
print(f"待办列表: {resp}")

# 4. 添加备忘录
print("\n4. 添加备忘录")
status, resp = request("/api/notes", "POST", {"title": "会议记录", "content": "讨论了新项目的计划和时间安排"})
print(f"添加备忘录: {resp}")

status, resp = request("/api/notes", "POST", {"title": "购物清单", "content": "牛奶、面包、鸡蛋"})
print(f"添加备忘录: {resp}")

# 5. 获取备忘录列表
print("\n5. 获取备忘录列表")
status, resp = request("/api/notes", "GET")
print(f"备忘录列表: {resp}")

print("\n✅ 测试完成！")
