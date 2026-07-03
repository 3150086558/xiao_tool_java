import httpx

BASE_URL = "http://127.0.0.1:8081"
PYTHON_URL = "http://127.0.0.1:8000"

# 登录
print("=" * 50)
print("测试登录接口")
r = httpx.post(f"{BASE_URL}/api/sys/login", json={"username": "admin", "password": "Admin@123"})
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    token = data["data"]["token"]
    print("登录成功")
else:
    print(f"登录失败: {r.text}")
    exit(1)

headers = {"Authorization": f"Bearer {token}"}

# 获取用户信息
print("\n" + "=" * 50)
print("测试用户信息接口")
r = httpx.get(f"{BASE_URL}/api/sys/userinfo", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"用户名: {data['data']['username']}")
    print(f"真实姓名: {data['data']['realName']}")
    print(f"角色数: {len(data['data']['roles'])}")
    print(f"菜单数: {len(data['data']['menus'])}")
else:
    print(f"失败: {r.text}")

# 获取用户列表
print("\n" + "=" * 50)
print("测试用户列表接口")
r = httpx.get(f"{BASE_URL}/api/sys/user/page?page=1&size=10", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"总记录数: {data['data']['total']}")
    print(f"当前页: {data['data'].get('pageNum', data['data'].get('current', 'N/A'))}")
else:
    print(f"失败: {r.text}")

# 获取职位列表
print("\n" + "=" * 50)
print("测试职位列表接口")
r = httpx.get(f"{BASE_URL}/api/sys/position/list", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"职位数: {len(data['data'])}")
else:
    print(f"失败: {r.text}")

# 获取角色列表
print("\n" + "=" * 50)
print("测试角色列表接口")
r = httpx.get(f"{BASE_URL}/api/sys/role/list", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"角色数: {len(data['data'])}")
else:
    print(f"失败: {r.text}")

# 获取角色菜单权限
print("\n" + "=" * 50)
print("测试角色菜单权限接口")
r = httpx.get(f"{BASE_URL}/api/sys/role/1/menu-ids", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"菜单ID数: {len(data['data'])}")
else:
    print(f"失败: {r.text}")

# 获取职位权限
print("\n" + "=" * 50)
print("测试职位权限接口")
r = httpx.get(f"{BASE_URL}/api/sys/position/1/permissions", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"角色ID: {data['data']['roleIds']}")
    print(f"数据权限: {data['data']['dataScope']}")
else:
    print(f"失败: {r.text}")

# Python后端健康检查
print("\n" + "=" * 50)
print("测试Python后端健康检查")
r = httpx.get(f"{PYTHON_URL}/api/health")
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    print("Python后端正常")
else:
    print(f"失败: {r.text}")

# Python后端记账接口
print("\n" + "=" * 50)
print("测试记账列表接口")
r = httpx.get(f"{PYTHON_URL}/api/app/accounting/page", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    print("记账接口正常")
else:
    print(f"失败: {r.text}")

# Python后端待办接口
print("\n" + "=" * 50)
print("测试待办列表接口")
r = httpx.get(f"{PYTHON_URL}/api/app/todo/page", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"待办数: {data['data']['total']}")
else:
    print(f"失败: {r.text}")

# Python后端备忘录接口
print("\n" + "=" * 50)
print("测试备忘录列表接口")
r = httpx.get(f"{PYTHON_URL}/api/app/note/page", headers=headers)
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"备忘录数: {data['data']['total']}")
else:
    print(f"失败: {r.text}")

# 测试新增待办
print("\n" + "=" * 50)
print("测试新增待办接口")
r = httpx.post(f"{PYTHON_URL}/api/app/todo", headers=headers,
               json={"title": "测试待办", "priority": "high", "remark": "测试备注"})
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"创建成功: ID={data.get('data', {}).get('id')}")
else:
    print(f"失败: {r.text}")

# 测试新增记账
print("\n" + "=" * 50)
print("测试新增记账接口")
r = httpx.post(f"{PYTHON_URL}/api/app/accounting", headers=headers,
               json={"record_date": "2025-06-17", "type": "expense", "category": "测试项目", "amount": 100.0})
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"创建成功: ID={data.get('data', {}).get('id')}")
else:
    print(f"失败: {r.text}")

# 测试新增备忘录
print("\n" + "=" * 50)
print("测试新增备忘录接口")
r = httpx.post(f"{PYTHON_URL}/api/app/note", headers=headers,
               json={"title": "测试备忘录", "content": "测试内容"})
print(f"状态码: {r.status_code}")
if r.status_code == 200:
    data = r.json()
    print(f"创建成功: ID={data.get('data', {}).get('id')}")
else:
    print(f"失败: {r.text}")

print("\n" + "=" * 50)
print("所有API测试完成")