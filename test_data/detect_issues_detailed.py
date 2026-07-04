import requests
import json
import sys
import traceback

BASE_URL = "http://localhost:8081"
TOKEN = None
ISSUES = []

def log_issue(module, issue, severity="medium"):
    ISSUES.append({
        "module": module,
        "issue": issue,
        "severity": severity
    })
    print(f"  [问题-{severity}] {issue}")

def login():
    global TOKEN
    resp = requests.post(f"{BASE_URL}/api/sys/login", json={
        "username": "admin",
        "password": "admin123"
    }, timeout=10)
    if resp.status_code == 200:
        data = resp.json()
        if data.get("code") == 200:
            TOKEN = data.get("data", {}).get("token")
            return True
    return False

def get_headers():
    return {
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json"
    }

def test_api(name, method, url, json_data=None, params=None, expected_status=200):
    """测试一个API，返回 (status_code, response_data)"""
    try:
        if method.upper() == "GET":
            resp = requests.get(f"{BASE_URL}{url}", headers=get_headers(), params=params, timeout=10)
        elif method.upper() == "POST":
            resp = requests.post(f"{BASE_URL}{url}", headers=get_headers(), json=json_data, timeout=10)
        elif method.upper() == "PUT":
            resp = requests.put(f"{BASE_URL}{url}", headers=get_headers(), json=json_data, timeout=10)
        elif method.upper() == "DELETE":
            resp = requests.delete(f"{BASE_URL}{url}", headers=get_headers(), timeout=10)
        else:
            return 0, None

        if resp.status_code != expected_status:
            print(f"  [警告] {name} 返回状态码 {resp.status_code}，期望 {expected_status}")
            print(f"    响应: {resp.text[:200]}")

        try:
            return resp.status_code, resp.json()
        except:
            return resp.status_code, resp.text
    except Exception as e:
        print(f"  [错误] {name} 异常: {str(e)}")
        return 0, None

def check_response_format(name, data, check_code=True):
    """检查响应格式是否为 {code, message, data}"""
    if not isinstance(data, dict):
        log_issue(name, "响应格式不是JSON对象", "medium")
        return False
    if check_code and "code" not in data:
        log_issue(name, "响应缺少code字段", "medium")
        return False
    return True

def main():
    print("=" * 60)
    print("项目功能详细问题检测")
    print("=" * 60)

    if not login():
        log_issue("认证", "登录失败，无法继续测试", "high")
        print_summary()
        return

    print("\n登录成功，开始详细测试...")

    # 1. 测试用户信息接口
    print("\n=== 1. 用户信息接口 ===")
    code, data = test_api("用户信息", "GET", "/api/sys/userinfo")
    if code == 200 and check_response_format("用户信息", data):
        user_data = data.get("data", {})
        print(f"  用户名: {user_data.get('username')}")
        print(f"  权限数: {len(user_data.get('permissions', []))}")
        if not user_data.get("permissions"):
            log_issue("用户信息", "用户权限为空", "medium")

    # 2. 测试菜单树接口
    print("\n=== 2. 菜单树接口 ===")
    code, data = test_api("菜单树", "GET", "/api/sys/menu/tree")
    if code == 200 and check_response_format("菜单树", data):
        menu_list = data.get("data", [])
        print(f"  顶级菜单数: {len(menu_list)}")
        if not menu_list:
            log_issue("菜单", "菜单树为空", "high")

    # 3. 测试用户管理
    print("\n=== 3. 用户管理 ===")
    code, data = test_api("用户列表", "GET", "/api/sys/user/page", params={"pageNum": 1, "pageSize": 5})
    if code == 200 and check_response_format("用户列表", data):
        page_data = data.get("data", {})
        user_list = page_data.get("list", page_data.get("records", []))
        total = page_data.get("total", 0)
        print(f"  用户总数: {total}")
        print(f"  当前页用户数: {len(user_list)}")
        if not isinstance(page_data, dict):
            log_issue("用户管理", "用户分页数据格式异常", "medium")

    # 4. 测试角色管理
    print("\n=== 4. 角色管理 ===")
    code, data = test_api("角色列表", "GET", "/api/sys/role/list")
    if code == 200 and check_response_format("角色列表", data):
        role_list = data.get("data", [])
        print(f"  角色数: {len(role_list)}")

    # 5. 测试岗位管理
    print("\n=== 5. 岗位管理 ===")
    code, data = test_api("岗位列表", "GET", "/api/sys/position/page", params={"pageNum": 1, "pageSize": 5})
    if code == 200 and check_response_format("岗位列表", data):
        page_data = data.get("data", {})
        pos_list = page_data.get("list", page_data.get("records", []))
        print(f"  岗位数: {len(pos_list)}")

    # 6. 测试组织管理
    print("\n=== 6. 组织管理 ===")
    code, data = test_api("组织树", "GET", "/api/sys/org/tree")
    if code == 200 and check_response_format("组织树", data):
        org_list = data.get("data", [])
        print(f"  顶级组织数: {len(org_list)}")

    # 7. 测试待办事项
    print("\n=== 7. 待办事项 ===")
    code, data = test_api("待办列表", "GET", "/api/app/todo/page", params={"page": 1, "size": 5})
    if code == 200 and check_response_format("待办列表", data):
        page_data = data.get("data", {})
        todo_list = page_data.get("list", page_data.get("records", []))
        total = page_data.get("total", 0)
        print(f"  待办总数: {total}")
        print(f"  当前页待办数: {len(todo_list)}")
        if todo_list:
            first = todo_list[0]
            has_done_field = "done" in first or "completed" in first
            print(f"  第一条待办: {first.get('title')}")
            print(f"  完成字段(done/completed): {has_done_field}")
            if "done" not in first and "completed" not in first:
                log_issue("待办事项", "待办数据缺少完成状态字段", "medium")

    # 测试新增待办
    print("\n  测试新增待办...")
    code, data = test_api("新增待办", "POST", "/api/app/todo", json_data={
        "title": "API测试待办",
        "description": "测试",
        "priority": "high",
        "status": "pending"
    })
    if code == 200 and check_response_format("新增待办", data):
        new_todo = data.get("data", {})
        todo_id = new_todo.get("id")
        print(f"  新增成功，ID: {todo_id}")
        if todo_id:
            # 测试切换完成状态
            print("  测试切换完成状态...")
            code2, data2 = test_api("切换完成", "PUT", f"/api/app/todo/{todo_id}/done", json_data={"completed": 1})
            if code2 != 200:
                log_issue("待办事项", "切换待办完成状态失败", "medium")
            
            # 测试删除待办
            print("  测试删除待办...")
            code3, data3 = test_api("删除待办", "DELETE", f"/api/app/todo/{todo_id}")
            if code3 != 200:
                log_issue("待办事项", "删除待办失败", "medium")
    else:
        log_issue("待办事项", "新增待办失败", "medium")

    # 8. 测试备忘录
    print("\n=== 8. 备忘录 ===")
    code, data = test_api("备忘录列表", "GET", "/api/app/note/page", params={"page": 1, "size": 5})
    if code == 200 and check_response_format("备忘录列表", data):
        page_data = data.get("data", {})
        note_list = page_data.get("list", page_data.get("records", []))
        total = page_data.get("total", 0)
        print(f"  备忘录总数: {total}")
        print(f"  当前页备忘录数: {len(note_list)}")

    # 测试新增备忘录
    print("\n  测试新增备忘录...")
    code, data = test_api("新增备忘录", "POST", "/api/app/note", json_data={
        "title": "API测试备忘录",
        "content": "测试内容",
        "tags": "test"
    })
    if code == 200 and check_response_format("新增备忘录", data):
        new_note = data.get("data", {})
        note_id = new_note.get("id")
        print(f"  新增成功，ID: {note_id}")
        if note_id:
            # 测试删除备忘录
            print("  测试删除备忘录...")
            code3, data3 = test_api("删除备忘录", "DELETE", f"/api/app/note/{note_id}")
            if code3 != 200:
                log_issue("备忘录", "删除备忘录失败", "medium")
    else:
        log_issue("备忘录", "新增备忘录失败", "medium")

    # 9. 测试记账管理
    print("\n=== 9. 记账管理 ===")
    code, data = test_api("记账列表", "GET", "/api/app/accounting/page", params={"page": 1, "size": 5})
    if code == 200 and check_response_format("记账列表", data):
        page_data = data.get("data", {})
        record_list = page_data.get("records", page_data.get("list", []))
        total = page_data.get("total", 0)
        print(f"  记账总数: {total}")
        print(f"  当前页记录数: {len(record_list)}")

    # 测试新增记账
    print("\n  测试新增记账...")
    code, data = test_api("新增记账", "POST", "/api/app/accounting", json_data={
        "amount": 99.99,
        "type": "expense",
        "category": "测试分类",
        "record_date": "2026-07-03",
        "remark": "API测试"
    })
    if code == 200 and check_response_format("新增记账", data):
        new_record = data.get("data", {})
        record_id = new_record.get("id")
        print(f"  新增成功，ID: {record_id}")
        if record_id:
            # 测试删除记账
            print("  测试删除记账...")
            code3, data3 = test_api("删除记账", "DELETE", f"/api/app/accounting/{record_id}")
            if code3 != 200:
                log_issue("记账管理", "删除记账失败", "medium")
    else:
        log_issue("记账管理", "新增记账失败", "medium")

    # 10. 测试统计功能
    print("\n=== 10. 统计功能 ===")
    code, data = test_api("统计概览", "GET", "/api/app/stats/summary")
    if code == 200 and check_response_format("统计概览", data):
        stats_data = data.get("data", {})
        print(f"  收入: {stats_data.get('income')}")
        print(f"  支出: {stats_data.get('expense')}")
        print(f"  结余: {stats_data.get('balance')}")

    # 11. 测试数据库连接
    print("\n=== 11. 数据库连接 ===")
    code, data = test_api("数据库连接列表", "GET", "/api/app/db-connections")
    if code == 200 and check_response_format("数据库连接列表", data):
        conn_data = data.get("data", {})
        # 检查data是列表还是包含connections的对象
        if isinstance(conn_data, dict) and "connections" in conn_data:
            conn_list = conn_data["connections"]
        elif isinstance(conn_data, list):
            conn_list = conn_data
        else:
            conn_list = []
            log_issue("数据库查询", "数据库连接列表数据格式异常", "medium")
        print(f"  连接数: {len(conn_list)}")

    # 12. 测试修改密码接口
    print("\n=== 12. 修改密码接口 ===")
    code, data = test_api("修改密码", "POST", "/api/sys/change-password", json_data={
        "oldPassword": "admin123",
        "newPassword": "Admin@123"
    })
    if code == 200 and check_response_format("修改密码", data):
        print("  修改密码接口正常")
        # 改回来
        code2, data2 = test_api("改回密码", "POST", "/api/sys/change-password", json_data={
            "oldPassword": "Admin@123",
            "newPassword": "admin123"
        })
    else:
        print(f"  修改密码返回: {code}")

    print_summary()

def print_summary():
    print("\n" + "=" * 60)
    print("问题汇总")
    print("=" * 60)
    if not ISSUES:
        print("未发现明显问题！")
    else:
        print(f"共发现 {len(ISSUES)} 个问题：\n")
        high = [i for i in ISSUES if i["severity"] == "high"]
        medium = [i for i in ISSUES if i["severity"] == "medium"]
        low = [i for i in ISSUES if i["severity"] == "low"]

        if high:
            print(f"【严重问题 {len(high)} 个】")
            for i, issue in enumerate(high, 1):
                print(f"  {i}. [{issue['module']}] {issue['issue']}")
            print()

        if medium:
            print(f"【中等问题 {len(medium)} 个】")
            for i, issue in enumerate(medium, 1):
                print(f"  {i}. [{issue['module']}] {issue['issue']}")
            print()

        if low:
            print(f"【轻微问题 {len(low)} 个】")
            for i, issue in enumerate(low, 1):
                print(f"  {i}. [{issue['module']}] {issue['issue']}")

    print("=" * 60)

if __name__ == "__main__":
    main()
