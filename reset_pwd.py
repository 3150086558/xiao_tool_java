import bcrypt

# 验证旧密码
hashed = b'$2b$10$Q9UkKxciLyB5dtwXp.4OcOrKn3ADE05cM8efHhMUuqb7Qk6LdnWtC'
print('admin123:', bcrypt.checkpw(b'admin123', hashed))
print('Admin@123:', bcrypt.checkpw(b'Admin@123', hashed))

# 生成新密码
new_pwd = bcrypt.hashpw(b'Admin@123', bcrypt.gensalt())
print('新密码hash:', new_pwd.decode())

import psycopg2
conn = psycopg2.connect(
    host='localhost',
    port=5432,
    database='org_sys',
    user='postgres',
    password='123456'
)
cur = conn.cursor()
cur.execute("UPDATE sys_user SET password = %s WHERE username = 'admin'", (new_pwd.decode(),))
conn.commit()
print('密码已更新')
conn.close()
