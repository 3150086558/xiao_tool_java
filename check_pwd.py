import psycopg2

conn = psycopg2.connect(
    host='localhost',
    port=5432,
    database='org_sys',
    user='postgres',
    password='123456'
)
cur = conn.cursor()
cur.execute("SELECT username, password FROM sys_user WHERE username = 'admin'")
row = cur.fetchone()
print('admin用户:', row)
conn.close()
