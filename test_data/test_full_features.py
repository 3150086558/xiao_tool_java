import requests
import json

BASE_PYTHON = "http://127.0.0.1:8000/api/app"
BASE_JAVA = "http://127.0.0.1:8081/api/sys"

print("=" * 60)
print("全面功能测试")
print("=" * 60)

# 1. 登录测试
print("\n[1] Java 后端登录测试")
try:
    r = requests.post(f"{BASE_JAVA}/login", json={"username": "admin", "password": "admin123"}, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        token = r.json()["data"]["token"]
        headers = {"Authorization": f"Bearer {token}"}
        print(f"    ✅ 登录成功")
    else:
        print(f"    ❌ 登录失败: {r.text[:200]}")
        headers = {}
except Exception as e:
    print(f"    ❌ 连接失败: {e}")
    headers = {}

# 2. 用户分页测试
print("\n[2] 用户分页测试")
try:
    r = requests.get(f"{BASE_JAVA}/user/page?page=1&size=5", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()["data"]
        records = data.get("records", data.get("list", []))
        print(f"    ✅ 成功, 总数: {data.get('total', len(records))}, 当前页: {len(records)}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 3. 职位列表测试
print("\n[3] 职位列表测试")
try:
    r = requests.get(f"{BASE_JAVA}/position", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json().get("data", [])
        print(f"    ✅ 成功, 数量: {len(data)}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 4. 角色列表测试
print("\n[4] 角色列表测试")
try:
    r = requests.get(f"{BASE_JAVA}/role", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json().get("data", [])
        print(f"    ✅ 成功, 数量: {len(data)}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 5. Python 后端记账列表
print("\n[5] Python 后端记账列表")
try:
    r = requests.get(f"{BASE_PYTHON}/accounting/page?page=1&size=5", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()["data"]
        print(f"    ✅ 成功, 总数: {data.get('total', 0)}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 6. 删除全部记账（关键功能）
print("\n[6] 删除全部记账功能测试")
try:
    r = requests.delete(f"{BASE_PYTHON}/accounting/all", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        print(f"    ✅ 删除全部成功")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 7. 待办列表
print("\n[7] 待办列表测试")
try:
    r = requests.get(f"{BASE_PYTHON}/todo/page?page=1&size=5", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()["data"]
        print(f"    ✅ 成功, 总数: {data.get('total', 0)}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 8. 新增待办
print("\n[8] 新增待办测试")
try:
    new_todo = {
        "title": "测试待办事项",
        "priority": "high",
        "due_date": "2024-12-31",
        "remark": "这是测试备注"
    }
    r = requests.post(f"{BASE_PYTHON}/todo", json=new_todo, headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        todo_id = r.json()["data"]["id"]
        print(f"    ✅ 新增成功, ID: {todo_id}")

        # 9. 切换完成状态
        print("\n[9] 切换待办完成状态测试")
        r = requests.put(f"{BASE_PYTHON}/todo/{todo_id}/done", json={"completed": True}, headers=headers, timeout=5)
        print(f"    Status: {r.status_code}")
        if r.status_code == 200:
            print(f"    ✅ 切换状态成功")
        else:
            print(f"    ⚠️   返回: {r.text[:200]}")

        # 10. 删除待办
        print("\n[10] 删除待办测试")
        r = requests.delete(f"{BASE_PYTHON}/todo/{todo_id}", headers=headers, timeout=5)
        print(f"    Status: {r.status_code}")
        if r.status_code == 200:
            print(f"    ✅ 删除成功")
        else:
            print(f"    ⚠️   返回: {r.text[:200]}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 11. 备忘录列表
print("\n[11] 备忘录列表测试")
try:
    r = requests.get(f"{BASE_PYTHON}/note/page?page=1&size=5", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        data = r.json()["data"]
        print(f"    ✅ 成功, 总数: {data.get('total', 0)}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 12. 新增备忘录
print("\n[12] 新增备忘录测试")
try:
    new_note = {
        "title": "测试备忘录",
        "content": "这是备忘录内容",
        "tags": ["测试", "功能验证"]
    }
    r = requests.post(f"{BASE_PYTHON}/note", json=new_note, headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        note_id = r.json()["data"]["id"]
        print(f"    ✅ 新增成功, ID: {note_id}")

        # 13. 删除备忘录
        print("\n[13] 删除备忘录测试")
        r = requests.delete(f"{BASE_PYTHON}/note/{note_id}", headers=headers, timeout=5)
        print(f"    Status: {r.status_code}")
        if r.status_code == 200:
            print(f"    ✅ 删除成功")
        else:
            print(f"    ⚠️   返回: {r.text[:200]}")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 14. 统计功能
print("\n[14] 统计功能测试")
try:
    r = requests.get(f"{BASE_PYTHON}/stats/summary", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        print(f"    ✅ 统计成功")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

# 15. 分类统计
print("\n[15] 分类统计测试")
try:
    r = requests.get(f"{BASE_PYTHON}/stats/category?type=expense", headers=headers, timeout=5)
    print(f"    Status: {r.status_code}")
    if r.status_code == 200:
        print(f"    ✅ 分类统计成功")
    else:
        print(f"    ⚠️   返回: {r.text[:200]}")
except Exception as e:
    print(f"    ⚠️   异常: {e}")

print("\n" + "=" * 60)
print("测试完成！")
print("前端地址: http://localhost:5174/")
print("=" * 60)
