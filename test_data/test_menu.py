import requests
import json

BASE_URL = "http://localhost:8081"

def login():
    resp = requests.post(f"{BASE_URL}/api/sys/login", json={
        "username": "admin",
        "password": "admin123"
    }, timeout=10)
    data = resp.json()
    return data.get("data", {}).get("token")

TOKEN = login()
headers = {"Authorization": f"Bearer {TOKEN}"}

print("=== 用户信息 (/api/sys/userinfo) ===")
resp = requests.get(f"{BASE_URL}/api/sys/userinfo", headers=headers, timeout=10)
print("状态码:", resp.status_code)
data = resp.json()
print("响应code:", data.get("code"))
print("响应message:", data.get("message"))
if data.get("code") == 200:
    user_info = data.get("data", {})
    print("\n用户:", user_info.get("realName"))
    print("菜单数量:", len(user_info.get("menus", [])))
    print("权限数量:", len(user_info.get("permissions", [])))
    print("角色:", user_info.get("roles"))
    
    menus = user_info.get("menus", [])
    print("\n=== 菜单树 ===")
    def print_menu(menu, level=0):
        name = menu.get("name") or menu.get("menuName") or menu.get("title") or "?"
        path = menu.get("path") or ""
        children = menu.get("children") or []
        print("  " * level + f"- {name} ({path}) [{len(children)}个children]")
        for child in children:
            print_menu(child, level + 1)

    for menu in menus:
        print_menu(menu)
    
    print("\n=== 第一个菜单字段 ===")
    if menus:
        first = menus[0]
        print(json.dumps(list(first.keys()), ensure_ascii=False, indent=2))
