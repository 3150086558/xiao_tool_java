# 组织权限管理系统

## 项目概述

一个基于 **Java Spring Boot + Python FastAPI + Vue 3 + PostgreSQL** 的组织权限管理系统，支持树形组织架构、职位管理、角色权限、数据权限等功能。

## 技术栈

| 层级 | 技术 | 端口 |
|------|------|------|
| 前端 | Vue 3 + Element Plus + Vite | 5173 |
| Java后端 | Spring Boot 3.2 + Spring Security + MyBatis Plus | 8080 |
| Python后端 | FastAPI + Uvicorn | 8000 |
| 数据库 | PostgreSQL 16 | 5432 |

## Java 后端功能（/api/sys/*）

负责系统管理和认证鉴权核心功能：

| 模块 | API 前缀 | 说明 |
|------|----------|------|
| 认证模块 | `/api/sys/login`, `/api/sys/logout`, `/api/sys/userinfo` | JWT 登录/登出/用户信息 |
| 组织管理 | `/api/sys/org/*` | 树形组织 CRUD |
| 人员管理 | `/api/sys/user/*` | 用户管理、职位分配、密码重置 |
| 职位管理 | `/api/sys/position/*` | 职位 CRUD、角色分配、数据权限配置 |
| 角色管理 | `/api/sys/role/*` | 角色 CRUD、菜单权限分配 |
| 菜单管理 | `/api/sys/menu/*` | 菜单树 CRUD |
| 数据权限 | `/api/sys/data-scope/*` | 获取可见用户/组织列表 |

**核心特性**：
- RBAC 权限模型（用户→职位→角色→菜单）
- 5种数据权限策略（本人/本部门/本部门及子部门/自定义/全部）
- JWT 无状态认证
- BCrypt 密码加密

## Python 后端功能（/api/app/*）

负责业务功能，迁移自原有记账系统：

| 模块 | API 前缀 | 说明 |
|------|----------|------|
| 记账管理 | `/api/app/records/*` | 收支记录 CRUD |
| 统计报表 | `/api/app/summary` | 收支汇总、分类统计 |
| 导入导出 | `/api/app/import`, `/api/app/export.*` | Excel/CSV 导入导出 |
| 待办事项 | `/api/app/todos/*` | 待办 CRUD |
| 备忘录 | `/api/app/notes/*` | 备忘录 CRUD |
| 数据库查询 | `/api/app/db-query`, `/api/app/db-connections/*` | 外部数据库查询工具 |

**核心特性**：
- 验证 Java 签发的 JWT
- 支持数据权限过滤
- Excel 模板下载和导入

## 项目结构

```
my_project/
├── backend-java/          # Java Spring Boot 后端
│   ├── src/main/java/com/xiao/sys/
│   ├── src/main/resources/
│   └── pom.xml
├── backend-python/        # Python FastAPI 后端
│   ├── app/
│   └── requirements.txt
├── frontend/              # Vue 3 前端
│   ├── src/
│   └── package.json
├── sql/                   # 数据库脚本
│   ├── init_postgres.sql
│   ├── init_mysql.sql
│   └── init_sqlite.sql
├── nginx/                 # Nginx 配置
├── docker-compose.yml     # Docker 一键部署
└── IMPLEMENTATION_PLAN.md # 实现方案文档
```

## 快速开始

### 环境要求
- JDK 17+
- Python 3.10+
- Node.js 20+
- PostgreSQL 16

### 1. 创建数据库

```sql
CREATE DATABASE org_sys WITH ENCODING 'UTF8';
```

### 2. 初始化数据库

```bash
psql -d org_sys -U postgres -f sql/init_postgres.sql
```

### 3. 启动 Java 后端

```bash
cd backend-java
mvnw.cmd spring-boot:run
# 或运行已编译的 jar
java -jar target/backend-java-1.0.0.jar
```

### 4. 启动 Python 后端

```bash
cd backend-python
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 6. 访问系统

浏览器打开 http://localhost:5173

**默认账号**：admin / admin123

## API 访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:5173 | Vue SPA |
| Java API | http://localhost:8080/api/sys/ | 系统管理 |
| Python API | http://localhost:8000/api/app/ | 业务功能 |
| Python Docs | http://localhost:8000/docs | API 文档 |

## Docker 部署

```bash
docker-compose up -d
```

## 权限模型

```
组织(org) ──树形── 组织
    │
    ├── 职位(position)
    │      │
    │      ├── 角色绑定(role_position)
    │      │      │
    │      │      └── 角色(role) ── 菜单权限(role_menu)
    │      │                              │
    │      │                              └── 菜单/按钮(menu)
    │      │
    │      └── 数据权限规则(position_data_scope)
    │
    └── 人员(user)
           │
           └── user_position (一人可多职位)
```

## 数据权限策略

| 策略 | 说明 |
|------|------|
| self | 仅本人 |
| dept | 本部门 |
| dept_and_sub | 本部门及子部门 |
| custom | 自定义部门 |
| all | 全部数据 |

## 开发说明

### JWT 密钥

Java 和 Python 共享同一密钥，配置在：
- Java: `application.yml` → `jwt.secret`
- Python: 环境变量 `JWT_SECRET` 或代码中硬编码

### 前后端联调

前端通过 Vite 代理访问后端：
- `/api/sys/*` → http://127.0.0.1:8080
- `/api/app/*` → http://127.0.0.1:8000
