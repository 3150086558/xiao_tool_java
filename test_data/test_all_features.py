# -*- coding: utf-8 -*-
import requests
import json

BASE_URL = "http://127.0.0.1:8081"

def get_headers(token):
    return {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

def test_all_features():
    print("=" * 60)
    print("测试所有功能")
    print("=" * 60)
    
    # 1. 登录
    print("\n1. 登录...")
    login_data = {"username": "admin", "password": "Admin@123"}
    resp = requests.post(f"{BASE_URL}/api/sys/login", json=login_data)
    if resp.status_code != 200:
        print(f"登录失败: {resp.text}")
        return
    token = resp.json()["data"]["token"]
    headers = get_headers(token)
    print("登录成功")
    
    # 2. 测试备忘录功能
    print("\n" + "=" * 40)
    print("2. 测试备忘录功能")
    print("=" * 40)
    
    # 2.1 新增备忘录
    print("\n2.1 新增备忘录...")
    note_data = {
        "title": "测试备忘录",
        "content": "这是测试内容",
        "tags": '["测试", "重要"]'
    }
    resp = requests.post(f"{BASE_URL}/api/app/note", json=note_data, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    note_id = None
    if resp.status_code == 200:
        note_id = resp.json()["data"]["id"]
    
    # 2.2 查询备忘录列表
    print("\n2.2 查询备忘录列表...")
    resp = requests.get(f"{BASE_URL}/api/app/note/page", params={"page": 1, "size": 10}, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)[:500]}...")
    
    # 2.3 更新备忘录
    if note_id:
        print("\n2.3 更新备忘录...")
        update_data = {
            "title": "测试备忘录-修改",
            "content": "修改后的内容",
            "tags": '["测试", "已修改"]'
        }
        resp = requests.put(f"{BASE_URL}/api/app/note/{note_id}", json=update_data, headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    
    # 2.4 删除备忘录
    if note_id:
        print("\n2.4 删除备忘录...")
        resp = requests.delete(f"{BASE_URL}/api/app/note/{note_id}", headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    
    # 3. 测试待办事项功能
    print("\n" + "=" * 40)
    print("3. 测试待办事项功能")
    print("=" * 40)
    
    # 3.1 新增待办
    print("\n3.1 新增待办...")
    todo_data = {
        "title": "测试待办",
        "description": "测试描述",
        "status": "undone",
        "remark": "测试备注"
    }
    resp = requests.post(f"{BASE_URL}/api/app/todo", json=todo_data, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    todo_id = None
    if resp.status_code == 200:
        todo_id = resp.json()["data"]["id"]
    
    # 3.2 查询待办列表
    print("\n3.2 查询待办列表...")
    resp = requests.get(f"{BASE_URL}/api/app/todo/page", params={"page": 1, "size": 10}, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)[:500]}...")
    
    # 3.3 更新待办状态
    if todo_id:
        print("\n3.3 更新待办状态...")
        update_data = {"status": "done"}
        resp = requests.put(f"{BASE_URL}/api/app/todo/{todo_id}", json=update_data, headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    
    # 3.4 删除待办
    if todo_id:
        print("\n3.4 删除待办...")
        resp = requests.delete(f"{BASE_URL}/api/app/todo/{todo_id}", headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    
    # 4. 测试记账功能
    print("\n" + "=" * 40)
    print("4. 测试记账功能")
    print("=" * 40)
    
    # 4.1 新增记账
    print("\n4.1 新增记账...")
    record_data = {
        "record_date": "2026-07-03",
        "type": "expense",
        "category": "测试项目",
        "sub_category": "餐饮",
        "amount": 50.00,
        "account": "微信",
        "note": "测试备注"
    }
    resp = requests.post(f"{BASE_URL}/api/app/accounting", json=record_data, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    record_id = None
    if resp.status_code == 200:
        record_id = resp.json()["data"]["id"]
    
    # 4.2 查询记账列表
    print("\n4.2 查询记账列表...")
    resp = requests.get(f"{BASE_URL}/api/app/accounting/page", params={"page": 1, "size": 10}, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)[:500]}...")
    
    # 4.3 更新记账
    if record_id:
        print("\n4.3 更新记账...")
        update_data = {
            "record_date": "2026-07-04",
            "type": "income",
            "category": "工资",
            "sub_category": "工资收入",
            "amount": 100.00,
            "account": "银行卡",
            "note": "修改后的备注"
        }
        resp = requests.put(f"{BASE_URL}/api/app/accounting/{record_id}", json=update_data, headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    
    # 4.4 删除记账
    if record_id:
        print("\n4.4 删除记账...")
        resp = requests.delete(f"{BASE_URL}/api/app/accounting/{record_id}", headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)}")
    
    # 5. 测试数据库连接功能
    print("\n" + "=" * 40)
    print("5. 测试数据库连接功能")
    print("=" * 40)
    
    # 5.1 查询连接列表
    print("\n5.1 查询连接列表...")
    resp = requests.get(f"{BASE_URL}/api/app/db-connections", headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False)[:500]}...")
    
    print("\n" + "=" * 60)
    print("所有功能测试完成！")
    print("=" * 60)

if __name__ == "__main__":
    test_all_features()
