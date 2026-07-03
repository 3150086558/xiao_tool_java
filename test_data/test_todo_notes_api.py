# -*- coding: utf-8 -*-
"""测试待办和备忘录 API"""
import jwt
import json
import urllib.request
import urllib.error

JWT_SECRET = 'xiao-sys-secret-key-2026-very-long-secret'
payload = {'user_id': 1, 'username': 'admin'}
token = jwt.encode(payload, JWT_SECRET, algorithm='HS256')

base_headers = {'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'}

def api(method, path, data=None):
    url = 'http://127.0.0.1:8000' + path
    body = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(url, data=body, headers=base_headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        result = json.loads(resp.read().decode('utf-8'))
        return resp.status, result
    except urllib.error.HTTPError as e:
        result = json.loads(e.read().decode('utf-8'))
        return e.code, result

def ok(status):
    return 'OK' if status == 200 else 'FAIL'

print('=' * 60)
print('全面测试待办事项 API')
print('=' * 60)

# 1. 查询列表
code, res = api('GET', '/api/app/todo/page?page=1&size=10')
print(f'[{ok(code)}] 查询待办列表: status={code}, total={res.get("data",{}).get("total", 0)}')

# 2. 新增待办
code, res = api('POST', '/api/app/todo', {'title': 'API测试待办', 'priority': 'high', 'remark': '测试备注'})
print(f'[{ok(code)}] 新增待办: status={code}, id={res.get("data",{}).get("id")}')
todo_id = res.get('data', {}).get('id') if code == 200 else None

# 3. 编辑待办
if todo_id:
    code, res = api('PUT', f'/api/app/todo/{todo_id}', {'title': 'API测试待办-已编辑', 'priority': 'low', 'completed': 1, 'remark': '编辑后的备注'})
    print(f'[{ok(code)}] 编辑待办: status={code}')

# 4. 切换完成状态
if todo_id:
    code, res = api('PUT', f'/api/app/todo/{todo_id}/done', {'completed': 0})
    print(f'[{ok(code)}] 切换待办状态: status={code}')

# 5. 删除待办
if todo_id:
    code, res = api('DELETE', f'/api/app/todo/{todo_id}')
    print(f'[{ok(code)}] 删除待办: status={code}')

# 6. 测试参数校验错误（422）
code, res = api('POST', '/api/app/todo', {'title': ''})
detail = res.get('detail', res.get('error', ''))
print(f'[{ok(code) if code==422 else "FAIL"}] 参数校验错误（空标题）: status={code}, detail={detail[:50]}')

print()
print('=' * 60)
print('全面测试备忘录 API')
print('=' * 60)

# 1. 查询列表
code, res = api('GET', '/api/app/note/page?page=1&size=10')
print(f'[{ok(code)}] 查询备忘录列表: status={code}, total={res.get("data",{}).get("total", 0)}')

# 2. 新增备忘录
code, res = api('POST', '/api/app/note', {'title': 'API测试备忘录', 'content': '测试内容', 'tags': ['标签1', '标签2']})
print(f'[{ok(code)}] 新增备忘录: status={code}, id={res.get("data",{}).get("id")}')
note_id = res.get('data', {}).get('id') if code == 200 else None

# 3. 编辑备忘录
if note_id:
    code, res = api('PUT', f'/api/app/note/{note_id}', {'title': 'API测试备忘录-已编辑', 'content': '编辑后的内容', 'tags': ['新标签']})
    print(f'[{ok(code)}] 编辑备忘录: status={code}')

# 4. 删除备忘录
if note_id:
    code, res = api('DELETE', f'/api/app/note/{note_id}')
    print(f'[{ok(code)}] 删除备忘录: status={code}')

# 5. 测试参数校验错误（422）
code, res = api('POST', '/api/app/note', {'title': ''})
detail = res.get('detail', res.get('error', ''))
print(f'[{ok(code) if code==422 else "FAIL"}] 参数校验错误（空标题）: status={code}, detail={detail[:50]}')

print()
print('=' * 60)
print('测试 size=999 兼容性')
print('=' * 60)

code, res = api('GET', '/api/app/todo/page?page=1&size=999')
print(f'[{ok(code)}] 待办 size=999: status={code}, total={res.get("data",{}).get("total", 0)}')

code, res = api('GET', '/api/app/note/page?page=1&size=999')
print(f'[{ok(code)}] 备忘录 size=999: status={code}, total={res.get("data",{}).get("total", 0)}')

print()
print('=' * 60)
print('测试待办状态过滤兼容性 (undone/done)')
print('=' * 60)

code, res = api('GET', '/api/app/todo/page?page=1&size=10&status=undone')
count = len(res.get('data',{}).get('records', [])) if code == 200 else 0
print(f'[{ok(code)}] status=undone: status={code}, count={count}')

code, res = api('GET', '/api/app/todo/page?page=1&size=10&status=done')
count = len(res.get('data',{}).get('records', [])) if code == 200 else 0
print(f'[{ok(code)}] status=done: status={code}, count={count}')

print()
print('所有测试完成！')
