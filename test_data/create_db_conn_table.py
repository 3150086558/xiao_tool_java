# -*- coding: utf-8 -*-
import psycopg2

conn = psycopg2.connect(
    host="127.0.0.1",
    port=5432,
    database="org_sys",
    user="postgres",
    password="123456"
)
cur = conn.cursor()

sql = """
CREATE TABLE IF NOT EXISTS db_connections (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    name VARCHAR(200) NOT NULL,
    db_type VARCHAR(20) NOT NULL DEFAULT 'mysql',
    host VARCHAR(200),
    port INTEGER,
    database VARCHAR(200),
    username VARCHAR(200),
    password VARCHAR(500),
    sqlite_path VARCHAR(500),
    created_at VARCHAR(30),
    updated_at VARCHAR(30)
)
"""
cur.execute(sql)
conn.commit()
print("db_connections 表创建成功")

cur.close()
conn.close()
