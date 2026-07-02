#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
重置admin用户密码
"""
import sqlite3
import hashlib
import os

db_path = os.path.join(os.path.dirname(__file__), 'data', 'accounting.db')

if not os.path.exists(db_path):
    print(f"数据库不存在: {db_path}")
    exit(1)

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# 查看现有用户
print("当前用户列表：")
cursor.execute("SELECT id, username FROM users")
for row in cursor.fetchall():
    print(f"  ID: {row[0]}, 用户名: {row[1]}")

# 重置admin密码
new_password = "Mkld@2026"
password_hash = hashlib.sha256(new_password.encode()).hexdigest()

cursor.execute("SELECT id FROM users WHERE username = ?", ("admin",))
if cursor.fetchone():
    # 更新密码
    cursor.execute("UPDATE users SET password = ? WHERE username = ?", (password_hash, "admin"))
    print(f"\n✅ admin用户密码已重置为: {new_password}")
else:
    # 创建admin用户
    from datetime import datetime
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    cursor.execute("INSERT INTO users (username, password, created_at) VALUES (?, ?, ?)",
                   ("admin", password_hash, now))
    user_id = cursor.lastrowid
    print(f"\n✅ 创建admin用户，密码为: {new_password}")
    
    # 初始化菜单
    menus = [
        (user_id, 0, "财务管理", "💰", 1, now),
        (user_id, 0, "日常工具", "🔧", 2, now),
        (user_id, 1, "记账", "📊", 1, now),
        (user_id, 1, "统计报表", "📈", 2, now),
        (user_id, 2, "待办事项", "✅", 1, now),
        (user_id, 2, "备忘录", "📝", 2, now),
    ]
    for menu in menus:
        cursor.execute("""
            INSERT INTO menus(user_id, parent_id, name, icon, sort_order, created_at)
            VALUES(?, ?, ?, ?, ?, ?)
        """, menu)
    print("✅ 为admin用户初始化菜单")

conn.commit()
conn.close()

print("\n🎉 完成！")
print(f"请使用用户名 'admin' 和密码 '{new_password}' 登录")
