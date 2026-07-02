#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""测试 HTTP 导出接口"""
import urllib.request
import urllib.error

print("测试 Excel 导出接口...")
try:
    url = "http://127.0.0.1:8000/api/export.xlsx"
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req, timeout=10) as response:
        data = response.read()
        print(f"✅ 响应状态: {response.status}")
        print(f"✅ 响应大小: {len(data)} bytes")
        print(f"✅ Content-Type: {response.headers.get('Content-Type')}")
        print(f"✅ Content-Disposition: {response.headers.get('Content-Disposition')}")
        
        # 保存到文件
        with open("http_export_test.xlsx", "wb") as f:
            f.write(data)
        print("✅ 已保存到 http_export_test.xlsx")
        
        # 验证文件格式
        if data.startswith(b'PK'):
            print("✅ 文件格式正确 (Excel xlsx 格式)")
        else:
            print("❌ 文件格式可能有问题")
            
except urllib.error.HTTPError as e:
    print(f"❌ HTTP 错误: {e.code}")
    print(f"   响应: {e.read().decode('utf-8', errors='ignore')}")
except Exception as e:
    print(f"❌ 错误: {type(e).__name__}: {e}")

print("\n" + "="*50)
print("测试 CSV 导出接口...")
try:
    url = "http://127.0.0.1:8000/api/export.csv"
    req = urllib.request.Request(url)
    with urllib.request.urlopen(req, timeout=10) as response:
        data = response.read()
        print(f"✅ 响应状态: {response.status}")
        print(f"✅ 响应大小: {len(data)} bytes")
        print(f"✅ Content-Type: {response.headers.get('Content-Type')}")
        print(f"✅ 内容预览: {data[:100]}...")
except Exception as e:
    print(f"❌ 错误: {type(e).__name__}: {e}")

print("\n✅ HTTP 测试完成!")
