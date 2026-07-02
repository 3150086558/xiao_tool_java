import requests

# 1. 测试用手机号登录（用户 wujiu 的手机号是 13800138007）
r = requests.post('http://127.0.0.1:8081/api/sys/login', json={'username': '13800138007', 'password': '123456'})
print(f"手机号登录: {r.status_code}, resp: {r.text[:200]}")

if r.status_code != 200:
    print(f"尝试用户 admin 默认密码...")
    r = requests.post('http://127.0.0.1:8081/api/sys/login', json={'username': 'admin', 'password': 'admin123'})
    print(f"admin 登录: {r.status_code}")

token = None
if r.status_code == 200 and r.json().get('data'):
    token = r.json()['data']['token']
    print(f"获取 Token OK: {token[:30]}...")

if token:
    headers = {'Authorization': f'Bearer {token}'}

    # 2. 测试获取用户列表
    r = requests.get('http://127.0.0.1:8081/api/sys/user/page?page=1&size=3', headers=headers)
    print(f"用户分页: {r.status_code}")
    if r.status_code == 200:
        data = r.json().get('data', {})
        records = data.get('records', [])
        print(f"  用户数: {data.get('total')}")
        for u in records[:3]:
            print(f"  - {u.get('username')}: {u.get('name')}, 组织: {u.get('orgName')}, 创建时间: {u.get('createTime')}")

# 3. 测试新增用户失败（之前没有正确保存 name）
    payload = {
        'username': 'test_new_user',
        'password': '123456',
        'name': '测试姓名',
        'phone': '13900000000',
        'email': 'test@test.com',
        'orgId': 1,
        'positionIds': [1],
        'status': 1
    }
    r = requests.post('http://127.0.0.1:8081/api/sys/user', json=payload, headers=headers)
    print(f"\n新增用户: {r.status_code}")
    print(f" 响应: {r.text[:300]}")
