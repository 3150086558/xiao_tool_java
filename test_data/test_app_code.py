#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""直接测试 app.py 中的代码"""
import sys
sys.path.insert(0, '.')

# 先读取 app.py 看看 do_GET 方法的实际内容
with open('app.py', 'r', encoding='utf-8') as f:
    content = f.read()

# 查找 do_GET 方法
import re
do_get_match = re.search(r'def do_GET\(self\):(.*?)(?=\n    def |\nclass |\nif __name__)', content, re.DOTALL)
if do_get_match:
    print("do_GET 方法内容:")
    print(do_get_match.group(1))
else:
    print("未找到 do_GET 方法")

# 检查 api_export_excel 函数是否存在
print("\n" + "="*50)
if 'def api_export_excel' in content:
    print("✅ api_export_excel 函数存在")
else:
    print("❌ api_export_excel 函数不存在")

# 测试一下文件的语法
print("\n" + "="*50)
print("检查 Python 语法...")
try:
    compile(content, 'app.py', 'exec')
    print("✅ 语法正确")
except SyntaxError as e:
    print(f"❌ 语法错误: {e}")
