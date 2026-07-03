# -*- coding: utf-8 -*-
import requests
import json
import os

BASE_URL = "http://127.0.0.1:8081"

def test_accounting():
    login_data = {
        "username": "admin",
        "password": "Admin@123"
    }
    print("1. 登录...")
    resp = requests.post(f"{BASE_URL}/api/sys/login", json=login_data)
    print(f"登录响应状态: {resp.status_code}")
    
    if resp.status_code != 200:
        print("登录失败!")
        return
    
    token = resp.json().get("data", {}).get("token")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    
    # 2. 测试新增记账
    print("\n2. 测试新增记账...")
    new_record = {
        "record_date": "2026-07-04",
        "type": "expense",
        "category": "测试项目",
        "sub_category": "餐饮",
        "amount": 20.00,
        "account": "微信",
        "note": "测试备注"
    }
    resp = requests.post(f"{BASE_URL}/api/app/accounting", json=new_record, headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False, indent=2)}")
    
    # 3. 测试查询记账列表
    print("\n3. 测试查询记账列表...")
    resp = requests.get(f"{BASE_URL}/api/app/accounting/page", params={"page": 1, "size": 10}, headers=headers)
    print(f"响应状态: {resp.status_code}")
    result = resp.json()
    print(f"响应内容: {json.dumps(result, ensure_ascii=False, indent=2)[:800]}...")
    
    # 4. 测试下载模板
    print("\n4. 测试下载模板...")
    resp = requests.get(f"{BASE_URL}/api/app/download-template", headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应头 Content-Type: {resp.headers.get('Content-Type')}")
    if resp.status_code == 200 and len(resp.content) > 0:
        template_path = "d:\\MyPersonalize\\InstallPosition\\AI_data\\Trae_data\\20260617_project_generate\\my_project\\test_data\\test_template.xlsx"
        with open(template_path, 'wb') as f:
            f.write(resp.content)
        print(f"模板文件已保存: {template_path}")
    
    # 5. 测试导入（使用刚才下载的模板）
    print("\n5. 测试导入功能...")
    template_path = "d:\\MyPersonalize\\InstallPosition\\AI_data\\Trae_data\\20260617_project_generate\\my_project\\test_data\\test_template.xlsx"
    if os.path.exists(template_path):
        with open(template_path, 'rb') as f:
            files = {'file': ('test_template.xlsx', f, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')}
            resp = requests.post(f"{BASE_URL}/api/app/accounting/import", files=files, headers={"Authorization": f"Bearer {token}"})
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False, indent=2)}")
    else:
        print("模板文件不存在，跳过导入测试")
    
    # 6. 测试导出
    print("\n6. 测试导出功能...")
    resp = requests.get(f"{BASE_URL}/api/app/accounting/export", headers=headers)
    print(f"响应状态: {resp.status_code}")
    print(f"响应头 Content-Type: {resp.headers.get('Content-Type')}")
    if resp.status_code == 200 and len(resp.content) > 0:
        export_path = "d:\\MyPersonalize\\InstallPosition\\AI_data\\Trae_data\\20260617_project_generate\\my_project\\test_data\\test_export.xlsx"
        with open(export_path, 'wb') as f:
            f.write(resp.content)
        print(f"导出文件已保存: {export_path}")
    
    # 7. 测试更新记账
    print("\n7. 测试更新记账...")
    record_id = result.get("data", {}).get("records", [{}])[0].get("id")
    if record_id:
        update_data = {
            "record_date": "2026-07-05",
            "type": "income",
            "category": "测试项目-修改",
            "sub_category": "工资",
            "amount": 50.00,
            "account": "银行卡",
            "note": "测试备注-修改"
        }
        resp = requests.put(f"{BASE_URL}/api/app/accounting/{record_id}", json=update_data, headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False, indent=2)}")
    
    # 8. 测试删除记账
    print("\n8. 测试删除记账...")
    if record_id:
        resp = requests.delete(f"{BASE_URL}/api/app/accounting/{record_id}", headers=headers)
        print(f"响应状态: {resp.status_code}")
        print(f"响应内容: {json.dumps(resp.json(), ensure_ascii=False, indent=2)}")

if __name__ == "__main__":
    test_accounting()
