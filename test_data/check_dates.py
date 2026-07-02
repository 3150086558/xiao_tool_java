#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sqlite3
from collections import Counter

conn = sqlite3.connect('data/accounting.db')
cur = conn.cursor()

# 查看所有记录的月份分布
cur.execute("SELECT strftime('%Y-%m', record_date) as month, COUNT(*) as cnt FROM records GROUP BY month ORDER BY month DESC")
rows = cur.fetchall()
print("各月份记录数:")
for month, cnt in rows:
    print(f"  {month}: {cnt} 条")

# 查看当前月份（2026-07）有多少条
cur.execute("SELECT COUNT(*) FROM records WHERE strftime('%Y-%m', record_date) = '2026-07'")
print(f"\n2026年7月记录数: {cur.fetchone()[0]}")

# 查看最早和最晚日期
cur.execute("SELECT MIN(record_date), MAX(record_date) FROM records")
min_date, max_date = cur.fetchone()
print(f"\n日期范围: {min_date} 至 {max_date}")

conn.close()
