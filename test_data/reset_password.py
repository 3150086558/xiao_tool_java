import psycopg2

conn = psycopg2.connect(
    host='127.0.0.1',
    port=5432,
    dbname='org_sys',
    user='postgres',
    password='123456'
)
cur = conn.cursor()
cur.execute("UPDATE sys_user SET password = %s WHERE username = %s",
            ('$2b$12$kRzoYBfTDwulYfKTV.RATeAi7WHcnshEcLWRfgcSTkDFqVDY6sQKK', 'admin'))
conn.commit()
print('Password updated successfully')
conn.close()