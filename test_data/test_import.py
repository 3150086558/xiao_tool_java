#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""创建测试Excel文件"""
from openpyxl import Workbook

wb = Workbook()
ws = wb.active

# 表头
ws.append(["日期", "类型", "项目", "消费分类", "金额", "备注", "星期几", "是否取消"])

# 测试数据
test_data = [
    ["2026/2/26", "支出", "早餐，一个包子，一个鸡蛋", "变动支出.饮食", -6.85, "", "星期四", ""],
    ["2026/2/26", "支出", "地铁", "变动支出.交通", -4.00, "上班通勤", "星期四", ""],
    ["2026/2/27", "收入", "3月工资", "收入.工资", 15000.00, "2026年3月工资", "星期五", ""],
    ["2026/2/28", "支出", "午餐", "变动支出.饮食", -25.50, "和同事一起吃", "星期六", ""],
]

for row in test_data:
    ws.append(row)

wb.save("test_import.xlsx")
print("✅ 测试文件 test_import.xlsx 已创建")
print("\n文件内容预览：")
for row in ws.iter_rows(values_only=True):
    print(row)
