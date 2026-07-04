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

def test_login():
    global TOKEN
    print("\n=== 测试用户认证 ===")
    try:
        resp = requests.post(f"{BASE_URL}/api/sys/login", json={
            "username": "admin",
            "password": "admin123"
        }, timeout=10)
        print(f"  登录状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应数据: {json.dumps(data, ensure_ascii=False)[:300]}")
            if data.get("code") == 200 or data.get("success") == True:
                TOKEN = data.get("data", {}).get("token") or data.get("token")
                if not TOKEN:
                    log_issue("认证", "登录成功但未返回token", "high")
                else:
                    print(f"  登录成功, token长度: {len(TOKEN)}")
            else:
                log_issue("认证", f"登录失败: {data.get('message', data)}", "high")
        else:
            log_issue("认证", f"登录HTTP错误: {resp.status_code}", "high")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("认证", f"登录异常: {str(e)}", "high")
        traceback.print_exc()

def get_headers():
    return {
        "Authorization": f"Bearer {TOKEN}",
        "Content-Type": "application/json"
    }

def test_get_user_info():
    print("\n=== 测试获取用户信息 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/userinfo", headers=get_headers(), timeout=10)
        print(f"  状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  用户信息: {json.dumps(data, ensure_ascii=False)[:300]}")
            if data.get("code") != 200 and data.get("success") != True:
                log_issue("用户信息", "获取用户信息返回失败", "medium")
        else:
            log_issue("用户信息", f"获取用户信息失败: {resp.status_code}", "high")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("用户信息", f"获取用户信息异常: {str(e)}", "high")
        traceback.print_exc()

def test_menu():
    print("\n=== 测试菜单/权限 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/menu/tree", headers=get_headers(), timeout=10)
        print(f"  菜单树状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
            if data.get("code") == 200 or data.get("success") == True:
                menu_data = data.get("data", [])
                if isinstance(menu_data, list):
                    print(f"  菜单项数: {len(menu_data)}")
                    if len(menu_data) == 0:
                        log_issue("菜单", "菜单树为空", "high")
                else:
                    print(f"  data不是列表: {type(menu_data)}")
            else:
                log_issue("菜单", f"获取菜单树返回失败: {data.get('message', data)}", "high")
        else:
            log_issue("菜单", f"获取菜单树失败: {resp.status_code}", "high")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("菜单", f"获取菜单异常: {str(e)}", "high")
        traceback.print_exc()

def test_user_management():
    print("\n=== 测试用户管理 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/user/page?pageNum=1&pageSize=10", headers=get_headers(), timeout=10)
        print(f"  用户列表状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("用户管理", f"获取用户列表失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("用户管理", f"用户管理异常: {str(e)}", "medium")
        traceback.print_exc()

def test_role_management():
    print("\n=== 测试角色管理 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/role/list", headers=get_headers(), timeout=10)
        print(f"  角色列表状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("角色管理", f"获取角色列表失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("角色管理", f"角色管理异常: {str(e)}", "medium")
        traceback.print_exc()

def test_position_management():
    print("\n=== 测试岗位管理 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/position/page?pageNum=1&pageSize=10", headers=get_headers(), timeout=10)
        print(f"  岗位列表状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("岗位管理", f"获取岗位列表失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("岗位管理", f"岗位管理异常: {str(e)}", "medium")
        traceback.print_exc()

def test_org_management():
    print("\n=== 测试组织管理 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/org/tree", headers=get_headers(), timeout=10)
        print(f"  组织树状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("组织管理", f"获取组织树失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("组织管理", f"组织管理异常: {str(e)}", "medium")
        traceback.print_exc()

def test_todo():
    print("\n=== 测试待办事项 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/app/todo/page?page=1&size=10", headers=get_headers(), timeout=10)
        print(f"  待办列表状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("待办事项", f"获取待办列表失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")

        todo_data = {
            "title": "测试待办",
            "description": "测试描述",
            "priority": "medium",
            "status": "pending"
        }
        resp2 = requests.post(f"{BASE_URL}/api/app/todo", json=todo_data, headers=get_headers(), timeout=10)
        print(f"  新增待办状态码: {resp2.status_code}")
        if resp2.status_code == 200:
            data2 = resp2.json()
            print(f"  新增响应: {json.dumps(data2, ensure_ascii=False)[:200]}")
        elif resp2.status_code == 422:
            log_issue("待办事项", "新增待办返回422验证错误", "medium")
            print(f"  422响应: {resp2.text[:300]}")
        else:
            log_issue("待办事项", f"新增待办失败: {resp2.status_code}", "medium")
            print(f"  响应: {resp2.text[:300]}")
    except Exception as e:
        log_issue("待办事项", f"待办事项异常: {str(e)}", "medium")
        traceback.print_exc()

def test_notes():
    print("\n=== 测试备忘录 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/app/note/page?page=1&size=10", headers=get_headers(), timeout=10)
        print(f"  备忘录列表状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("备忘录", f"获取备忘录列表失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")

        note_data = {
            "title": "测试备忘录",
            "content": "测试内容",
            "tags": "test"
        }
        resp2 = requests.post(f"{BASE_URL}/api/app/note", json=note_data, headers=get_headers(), timeout=10)
        print(f"  新增备忘录状态码: {resp2.status_code}")
        if resp2.status_code == 200:
            data2 = resp2.json()
            print(f"  新增响应: {json.dumps(data2, ensure_ascii=False)[:200]}")
        elif resp2.status_code == 422:
            log_issue("备忘录", "新增备忘录返回422验证错误", "medium")
            print(f"  422响应: {resp2.text[:300]}")
        else:
            log_issue("备忘录", f"新增备忘录失败: {resp2.status_code}", "medium")
            print(f"  响应: {resp2.text[:300]}")
    except Exception as e:
        log_issue("备忘录", f"备忘录异常: {str(e)}", "medium")
        traceback.print_exc()

def test_accounting():
    print("\n=== 测试记账管理 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/app/accounting/page?page=1&size=10", headers=get_headers(), timeout=10)
        print(f"  记账列表状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("记账管理", f"获取记账列表失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")

        record_data = {
            "amount": 100.5,
            "type": "expense",
            "category": "餐饮",
            "project": "测试项目",
            "record_date": "2026-07-03",
            "remark": "测试备注"
        }
        resp2 = requests.post(f"{BASE_URL}/api/app/accounting", json=record_data, headers=get_headers(), timeout=10)
        print(f"  新增记账状态码: {resp2.status_code}")
        if resp2.status_code == 200:
            data2 = resp2.json()
            print(f"  新增响应: {json.dumps(data2, ensure_ascii=False)[:200]}")
        elif resp2.status_code == 422:
            log_issue("记账管理", "新增记账返回422验证错误", "medium")
            print(f"  422响应: {resp2.text[:300]}")
        else:
            log_issue("记账管理", f"新增记账失败: {resp2.status_code}", "medium")
            print(f"  响应: {resp2.text[:300]}")
    except Exception as e:
        log_issue("记账管理", f"记账管理异常: {str(e)}", "medium")
        traceback.print_exc()

def test_stats():
    print("\n=== 测试统计功能 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/app/stats/summary", headers=get_headers(), timeout=10)
        print(f"  统计概览状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("统计", f"获取统计概览失败: {resp.status_code}", "low")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("统计", f"统计功能异常: {str(e)}", "low")
        traceback.print_exc()

def test_db_query():
    print("\n=== 测试数据库查询 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/app/db-connections", headers=get_headers(), timeout=10)
        print(f"  数据库连接列表状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  响应: {json.dumps(data, ensure_ascii=False)[:300]}")
        else:
            log_issue("数据库查询", f"获取数据库连接列表失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:300]}")
    except Exception as e:
        log_issue("数据库查询", f"数据库查询异常: {str(e)}", "medium")
        traceback.print_exc()

def main():
    print("=" * 60)
    print("项目功能问题检测")
    print("=" * 60)

    test_login()
    if not TOKEN:
        print("\n登录失败，无法继续测试其他功能！")
        print_summary()
        return

    test_get_user_info()
    test_menu()
    test_user_management()
    test_role_management()
    test_position_management()
    test_org_management()
    test_todo()
    test_notes()
    test_accounting()
    test_stats()
    test_db_query()

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
