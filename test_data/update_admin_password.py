# -*- coding: utf-8 -*-
import subprocess
import tempfile
import os

# 创建一个简单的 Java 程序来生成 bcrypt 哈希
java_code = """
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = args[0];
        String encoded = encoder.encode(password);
        System.out.println(encoded);
    }
}
"""

# 保存 Java 文件
with tempfile.NamedTemporaryFile(mode='w', suffix='.java', delete=False) as f:
    f.write(java_code)
    java_file = f.name

# 编译并运行（需要 spring-security-core jar）
# 由于环境限制，直接使用预计算的 bcrypt 哈希
# admin123 的 bcrypt 哈希（使用 BCryptPasswordEncoder 默认强度）
# 这里我们直接用 SQL 更新，假设数据库中已经有 bcrypt 编码器
# 我们需要通过后端 API 来重置密码

print("使用 Python 的 bcrypt 库生成密码...")
try:
    import bcrypt
    password = b"admin123"
    hashed = bcrypt.hashpw(password, bcrypt.gensalt())
    print(f"生成的哈希: {hashed.decode('utf-8')}")
    
    # 更新数据库
    import psycopg2
    conn = psycopg2.connect(
        host="127.0.0.1",
        port=5432,
        database="org_sys",
        user="postgres",
        password="123456"
    )
    cur = conn.cursor()
    cur.execute("UPDATE sys_user SET password = %s WHERE username = %s", 
                (hashed.decode('utf-8'), "admin"))
    conn.commit()
    print("密码更新成功！")
    cur.close()
    conn.close()
except ImportError:
    print("bcrypt 库未安装，请手动安装: pip install bcrypt")
