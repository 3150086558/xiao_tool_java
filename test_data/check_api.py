#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import urllib.request
import json

print("测试 /api/records 接口:")
try:
    r = urllib.request.urlopen('http://127.0.0.1:8000/api/records')
    data = json.loads(r.read().decode())
    print(f"  返回记录数: {len(data['records'])}")
    if data['records']:
        print("  第一条记录:", json.dumps(data['records'][0], ensure_ascii=False, indent=2))
except Exception as e:
    print(f"  错误: {e}")

print("\n测试 /api/summary 接口:")
try:
    r = urllib.request.urlopen('http://127.0.0.1:8000/api/summary')
    data = json.loads(r.read().decode())
    print(f"  收入: {data['income']}")
    print(f"  支出: {data['expense']}")
    print(f"  结余: {data['balance']}")
except Exception as e:
    print(f"  错误: {e}")
