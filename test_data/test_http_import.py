#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试HTTP导入API"""
import requests

url = "http://127.0.0.1:8000/api/import"
files = {'file': open('test_import.xlsx', 'rb')}

try:
    response = requests.post(url, files=files, timeout=10)
    print(f"Status: {response.status_code}")
    print(f"Response: {response.json()}")
except Exception as e:
    print(f"Error: {e}")
finally:
    files['file'].close()
