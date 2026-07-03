# -*- coding: utf-8 -*-
import requests
import json

BASE_URL = "http://127.0.0.1:8081"

def test_db_connections():
    # 1. 登录获取token
    login_data = {
        "username": "admin",
        "password": "Admin@123"
    }
    print("1. 登录...")
    resp = requests.post(f"{BASE_URL}/api/sys/login", json=login_data)
    print(f"登录响应状态: {resp.status_code}")
    print(f"登录响应: {json.dumps(resp.json(), ensure_ascii=False, indent=2)}")
    
    if resp.status_code != 200:
        print("登录失败!")
        return
    
    token = resp.json().get("data", {}).get("token")
    if not token:
        print("获取token失败!")
        return
    
    print(f"\n获取到token: {token[:20]}...")
    
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    # 2. 测试db-connections列表接口
    print("\n2. 测试数据库连接列表接口...")
    resp = requests.get(f"{BASE_URL}/api/app/db-connections", headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False, indent=2)}")
    
    # 3. 测试新增连接接口
    print("\n3. 测试新增数据库连接...")
    new_conn = {
        "name": "测试连接-PostgreSQL",
        "db_type": "postgresql",
        "host": "127.0.0.1",
        "port": 5432,
        "database": "org_sys",
        "username": "postgres",
        "password": "123456",
        "sqlite_path": ""
    }
    resp = requests.post(f"{BASE_URL}/api/app/db-connections", json=new_conn, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False, indent=2)}")
    
    # 4. 测试表列表查询
    print("\n4. 测试表列表查询...")
    query_data = {
        "action": "tables",
        "config": {
            "db_type": "postgresql",
            "host": "127.0.0.1",
            "port": 5432,
            "database": "org_sys",
            "username": "postgres",
            "password": "123456"
        }
    }
    resp = requests.post(f"{BASE_URL}/api/app/db/query", json=query_data, headers=headers)
    print(f"响应状态: {resp.status_code}")
    result = resp.json()
    print(f"响应内容: {json.dumps(result, ensure_ascii=False, indent=2)[:500]}...")
    
    if resp.status_code == 200 and result.get("code") == 200:
        tables = result.get("data", {}).get("tables", [])
        print(f"获取到 {len(tables)} 张表")
        if tables:
            print(f"前5张表: {tables[:5]}")

if __name__ == "__main__":
    test_db_connections()
