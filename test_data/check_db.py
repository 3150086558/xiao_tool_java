#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sqlite3

conn = sqlite3.connect('data/accounting.db')
cur = conn.cursor()

# 查看表结构
cur.execute("PRAGMA table_info(records)")
print("表结构:")
for col in cur.fetchall():
    print(f"  {col}")

# 查看记录数
cur.execute("SELECT COUNT(*) FROM records")
count = cur.fetchone()[0]
print(f"\n总记录数: {count}")

# 查看所有记录
cur.execute("SELECT * FROM records ORDER BY id DESC LIMIT 10")
rows = cur.fetchall()
print(f"\n最新10条记录:")
for r in rows:
    print(f"  {r}")

conn.close()
