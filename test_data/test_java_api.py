# -*- coding: utf-8 -*-
"""测试 Java 后端 API（迁移后）"""
import json
import urllib.request
import urllib.error

def api(method, path, data=None, token=None):
    url = 'http://127.0.0.1:8081' + path
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    body = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        result = json.loads(resp.read().decode('utf-8'))
        return resp.status, result
    except urllib.error.HTTPError as e:
        try:
            result = json.loads(e.read().decode('utf-8'))
        except:
            result = {'error': str(e)}
        return e.code, result

def ok(status, expected=200):
    return 'OK' if status == expected else 'FAIL'

print('=' * 60)
print('1. 登录获取 Token')
print('=' * 60)
code, res = api('POST', '/api/sys/login', {'username': 'admin', 'password': 'Admin@123'})
print(f'[{ok(code)}] 登录: status={code}')
token = res.get('data', {}).get('token') if code == 200 else None
if not token:
    print('登录失败，无法继续测试')
    exit(1)
print(f'Token: {token[:50]}...')

print()
print('=' * 60)
print('2. 测试待办事项 API')
print('=' * 60)

# 查询
code, res = api('GET', '/api/app/todo/page?page=1&size=10', token=token)
total = res.get('data', {}).get('total', 0) if res and res.get('data') else 0
print(f'[{ok(code)}] 查询待办: status={code}, res={str(res)[:200]}')

# 新增
code, res = api('POST', '/api/app/todo', {'title': 'Java后端测试待办', 'priority': 'high', 'remark': '测试备注'}, token=token)
print(f'[{ok(code)}] 新增待办: status={code}, id={res.get("data",{}).get("id")}')
todo_id = res.get('data', {}).get('id') if code == 200 else None

# 编辑
if todo_id:
    code, res = api('PUT', f'/api/app/todo/{todo_id}', {'title': 'Java后端测试待办-已编辑', 'priority': 'low', 'completed': 1, 'remark': '编辑后'}, token=token)
    print(f'[{ok(code)}] 编辑待办: status={code}')

# 删除
if todo_id:
    code, res = api('DELETE', f'/api/app/todo/{todo_id}', token=token)
    print(f'[{ok(code)}] 删除待办: status={code}')

print()
print('=' * 60)
print('3. 测试备忘录 API')
print('=' * 60)

# 查询
code, res = api('GET', '/api/app/note/page?page=1&size=10', token=token)
print(f'[{ok(code)}] 查询备忘录: status={code}, total={res.get("data",{}).get("total", 0)}')

# 新增
code, res = api('POST', '/api/app/note', {'title': 'Java后端测试备忘录', 'content': '测试内容', 'tags': '["标签1","标签2"]'}, token=token)
print(f'[{ok(code)}] 新增备忘录: status={code}, id={res.get("data",{}).get("id")}')
note_id = res.get('data', {}).get('id') if code == 200 else None

# 编辑
if note_id:
    code, res = api('PUT', f'/api/app/note/{note_id}', {'title': 'Java后端测试备忘录-已编辑', 'content': '编辑后', 'tags': '["新标签"]'}, token=token)
    print(f'[{ok(code)}] 编辑备忘录: status={code}')

# 删除
if note_id:
    code, res = api('DELETE', f'/api/app/note/{note_id}', token=token)
    print(f'[{ok(code)}] 删除备忘录: status={code}')

print()
print('=' * 60)
print('4. 测试记账 API')
print('=' * 60)

# 查询
code, res = api('GET', '/api/app/accounting/page?page=1&size=10', token=token)
print(f'[{ok(code)}] 查询记账: status={code}, total={res.get("data",{}).get("total", 0)}')

# 新增
code, res = api('POST', '/api/app/accounting', {'recordDate': '2026-07-03', 'type': 'expense', 'category': '餐饮', 'subCategory': '午餐', 'amount': 50.0, 'note': '测试记账'}, token=token)
print(f'[{ok(code)}] 新增记账: status={code}, id={res.get("data",{}).get("id")}')
record_id = res.get('data', {}).get('id') if code == 200 else None

# 删除
if record_id:
    code, res = api('DELETE', f'/api/app/accounting/{record_id}', token=token)
    print(f'[{ok(code)}] 删除记账: status={code}')

print()
print('=' * 60)
print('5. 测试统计 API')
print('=' * 60)

code, res = api('GET', '/api/app/stats/summary', token=token)
print(f'[{ok(code)}] 统计汇总: status={code}, data={str(res.get("data",{}))[:100]}')

print()
print('=' * 60)
print('所有测试完成！')
print('=' * 60)