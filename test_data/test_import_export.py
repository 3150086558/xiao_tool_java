import requests
import json

BASE_URL = "http://localhost:8081"
TOKEN = None
ISSUES = []

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
        "Authorization": f"Bearer {TOKEN}"
    }

def log_issue(module, issue, severity="medium"):
    ISSUES.append({"module": module, "issue": issue, "severity": severity})
    print(f"  [问题-{severity}] {issue}")

def main():
    print("=" * 60)
    print("导入导出功能检测")
    print("=" * 60)

    if not login():
        log_issue("认证", "登录失败", "high")
        return

    print("\n=== 测试下载模板 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/app/download-template", headers=get_headers(), timeout=10)
        print(f"  状态码: {resp.status_code}")
        print(f"  Content-Type: {resp.headers.get('Content-Type')}")
        print(f"  Content-Disposition: {resp.headers.get('Content-Disposition')}")
        print(f"  内容长度: {len(resp.content)} bytes")
        if resp.status_code == 200:
            if len(resp.content) > 0:
                print("  模板下载成功")
            else:
                log_issue("导入导出", "下载模板返回空内容", "medium")
        else:
            log_issue("导入导出", f"下载模板失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:200]}")
    except Exception as e:
        log_issue("导入导出", f"下载模板异常: {str(e)}", "medium")

    print("\n=== 测试导出Excel ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/app/accounting/export", headers=get_headers(), timeout=30)
        print(f"  状态码: {resp.status_code}")
        print(f"  Content-Type: {resp.headers.get('Content-Type')}")
        print(f"  内容长度: {len(resp.content)} bytes")
        if resp.status_code == 200:
            if len(resp.content) > 0:
                print("  导出成功")
            else:
                log_issue("导入导出", "导出Excel返回空内容", "medium")
        else:
            log_issue("导入导出", f"导出Excel失败: {resp.status_code}", "medium")
            print(f"  响应: {resp.text[:200]}")
    except Exception as e:
        log_issue("导入导出", f"导出Excel异常: {str(e)}", "medium")

    print("\n=== 测试角色菜单ID接口 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/role/1/menu-ids", headers=get_headers(), timeout=10)
        print(f"  状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  菜单ID列表: {json.dumps(data, ensure_ascii=False)[:200]}")
        else:
            log_issue("角色管理", f"获取角色菜单ID失败: {resp.status_code}", "low")
    except Exception as e:
        log_issue("角色管理", f"获取角色菜单ID异常: {str(e)}", "low")

    print("\n=== 测试岗位权限接口 ===")
    try:
        resp = requests.get(f"{BASE_URL}/api/sys/position/1/permissions", headers=get_headers(), timeout=10)
        print(f"  状态码: {resp.status_code}")
        if resp.status_code == 200:
            data = resp.json()
            print(f"  权限数据: {json.dumps(data, ensure_ascii=False)[:200]}")
        else:
            log_issue("岗位管理", f"获取岗位权限失败: {resp.status_code}", "low")
    except Exception as e:
        log_issue("岗位管理", f"获取岗位权限异常: {str(e)}", "low")

    print("\n" + "=" * 60)
    print("问题汇总")
    print("=" * 60)
    if ISSUES:
        for i, issue in enumerate(ISSUES, 1):
            print(f"  {i}. [{issue['severity']}] [{issue['module']}] {issue['issue']}")
    else:
        print("未发现问题")

if __name__ == "__main__":
    main()
