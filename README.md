# 组织权限管理与个人工具系统

## 项目概述

一个基于 **Java Spring Boot 3.2 + Python FastAPI + Vue 3 + PostgreSQL** 的企业级组织权限管理系统，同时集成了个人记账、待办事项和备忘录等个人工具功能。

## 技术栈

| 层级 | 技术 | 端口 |
|------|------|------|
| 前端 | Vue 3 + Element Plus + Vite | 5173 |
| Java后端 | Spring Boot 3.2 + Spring Security + MyBatis Plus | 8081 |
| Python后端 | FastAPI + Uvicorn + Psycopg2 | 8000 |
| 数据库 | PostgreSQL 16 | 5432 |

## Java 后端功能（/api/sys/*）

负责系统管理和认证鉴权核心功能：

| 模块 | API 前缀 | 说明 |
|------|----------|------|
| 认证模块 | `/api/sys/login`, `/api/sys/logout`, `/api/sys/userinfo` | JWT 登录/登出/用户信息 |
| 组织管理 | `/api/sys/org/*` | 树形组织 CRUD |
| 人员管理 | `/api/sys/user/*`, `/api/sys/user/page` | 用户管理、分页查询、职位分配、密码重置 |
| 职位管理 | `/api/sys/position/*`, `/api/sys/position/page` | 职位 CRUD、分页查询、角色分配、数据权限配置 |
| 角色管理 | `/api/sys/role/*`, `/api/sys/role/page` | 角色 CRUD、分页查询、菜单权限分配 |
| 菜单管理 | `/api/sys/menu/*` | 菜单树 CRUD |
| 数据权限 | `/api/sys/data-scope/*` | 获取可见用户/组织列表 |

**核心特性**：
- ✅ RBAC 权限模型（用户→职位→角色→菜单）
- ✅ 5种数据权限策略（本人/本部门/本部门及子部门/自定义/全部）
- ✅ JWT 无状态认证
- ✅ BCrypt 密码加密
- ✅ 支持用户名/手机号两种方式登录
- ✅ 人员列表显示创建时间字段

## Python 后端功能（/api/app/*）

负责业务功能模块：

| 模块 | API 前缀 | 说明 |
|------|----------|------|
| 记账管理 | `/api/app/accounting/*` | 收支记录 CRUD、分页查询、**删除全部** |
| 统计报表 | `/api/app/stats/*` | 收支汇总、分类统计、时间趋势 |
| 导入导出 | `/api/app/accounting/import`, `/api/app/accounting/export`, `/api/app/download-template` | Excel 导入导出、模板下载、进度条显示 |
| 待办事项 | `/api/app/todo/*` | 待办 CRUD、完成状态切换、优先级设置、到期提醒 |
| 备忘录 | `/api/app/note/*` | 备忘录 CRUD、标签管理、富文本支持 |
| 数据库查询 | `/api/app/db-query`, `/api/app/db-connections/*` | 外部数据库查询工具 |

**核心特性**：
- ✅ 验证 Java 签发的 JWT 令牌
- ✅ 支持数据权限过滤
- ✅ Excel 模板下载和导入
- ✅ **导入导出进度条弹窗显示**（含成功/失败条数提示）
- ✅ 待办完成状态字段自动映射（前端 done ↔ 后端 completed）
- ✅ 记账：支持按项目/分类/日期范围筛选

## 项目结构

```
my_project/
├── backend-java/          # Java Spring Boot 后端
│   ├── src/main/java/com/xiao/sys/
│   │   ├── controller/   # 控制器层
│   │   ├── service/      # 业务逻辑层
│   │   ├── mapper/       # MyBatis Plus Mapper
│   │   ├── entity/       # 数据库实体
│   │   └── dto/          # 数据传输对象
│   ├── src/main/resources/
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── target/backend-java-1.0.0.jar
├── backend-python/        # Python FastAPI 后端
│   ├── app/
│   │   ├── routers/      # 路由模块
│   │   ├── services/     # 业务逻辑
│   │   └── database.py   # 数据库连接
│   └── requirements.txt
├── frontend/              # Vue 3 前端
│   ├── src/
│   │   ├── views/        # 页面组件
│   │   ├── api/          # API 封装
│   │   ├── store/        # Pinia 状态管理
│   │   └── router/       # 路由配置
│   ├── vite.config.js
│   └── package.json
├── sql/                   # 数据库脚本
│   ├── init_postgres.sql
│   ├── init_mysql.sql
│   └── init_sqlite.sql
├── test_data/             # 测试脚本目录
│   ├── test_python_api.py
│   ├── test_java_api.py
│   └── test_all_fixed.py
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

# 方式一：使用 Maven Wrapper 运行
mvnw.cmd spring-boot:run

# 方式二：运行已编译的 jar（推荐）
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

浏览器打开 **http://localhost:5173**

**默认账号**：`admin` / `admin123`

## API 访问

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端 | http://localhost:5173 | Vue SPA（浏览器访问） |
| Java API | http://127.0.0.1:8081/api/sys/ | 系统管理接口 |
| Python API | http://127.0.0.1:8000/api/app/ | 业务功能接口 |
| Python Docs | http://127.0.0.1:8000/docs | FastAPI Swagger 文档 |

## 前端代理配置

前端通过 Vite 代理访问后端（`vite.config.js`）：
- `/api/sys/*` → http://127.0.0.1:8081
- `/api/app/*` → http://127.0.0.1:8000

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
- Python: 环境变量 `JWT_SECRET` 或代码中配置

### 字段映射说明

**待办事项**（前后端字段统一转换）：
- `done` (前端) ↔ `completed` (后端)
- `dueDate` (前端) ↔ `due_date` (后端)
- `createTime` / `updateTime` 自动填充

### 数据库表新增列

```sql
-- 备忘录表新增 tags 列
ALTER TABLE notes ADD COLUMN IF NOT EXISTS tags TEXT;

-- 待办表新增 remark 列
ALTER TABLE todos ADD COLUMN IF NOT EXISTS remark TEXT;
```

## 功能模块说明

### 1. 记账管理
- ✅ 收支记录增删改查
- ✅ Excel 导入（带进度弹窗，显示成功/失败条数）
- ✅ Excel 导出（带进度弹窗）
- ✅ 导入模板下载
- ✅ 按项目、分类、日期范围筛选
- ✅ **删除全部记录**（带确认对话框）
- ✅ 合计统计（收入/支出）

### 2. 待办事项
- ✅ 待办增删改查
- ✅ 完成状态一键切换
- ✅ 优先级设置（高/中/低）
- ✅ 到期日期设置
- ✅ 备注/详情字段

### 3. 备忘录
- ✅ 备忘录增删改查
- ✅ 标签管理
- ✅ 创建/更新时间显示

### 4. 组织管理
- ✅ 树形组织架构
- ✅ 组织增删改查
- ✅ 支持多级子组织

### 5. 人员管理
- ✅ 用户增删改查
- ✅ 分页查询
- ✅ 职位分配（支持多职位）
- ✅ 密码重置
- ✅ 状态启用/禁用
- ✅ **创建时间字段**显示

### 6. 职位管理
- ✅ 职位增删改查
- ✅ **分页查询**
- ✅ 角色分配
- ✅ 数据权限配置

### 7. 角色管理
- ✅ 角色增删改查
- ✅ **分页查询**
- ✅ 菜单权限分配

### 8. 菜单管理
- ✅ 菜单树增删改查
- ✅ 菜单类型（目录/菜单/按钮）
- ✅ 显示排序

### 9. 数据库查询工具
- ✅ 外部数据库连接管理
- ✅ SQL 查询执行
- ✅ 查询结果展示

## Docker 部署

```bash
# 一键启动所有服务
docker-compose up -d
```

## 常见问题

### Q1: Java 后端启动后访问报错 404
请确保端口是 **8081**（不是 8080），正确的 API 地址是：
`http://127.0.0.1:8081/api/sys/user/page`

### Q2: 删除全部记账时 422 报错
已修复路由顺序问题，`/accounting/all` 现在会优先匹配。

### Q3: 新增待办/备忘录后列表不刷新
已修复字段映射问题，后端统一处理字段转换。

### Q4: 导入时没有进度条
已新增进度条弹窗，大文件上传时会显示上传百分比。

## 更新日志

### v1.1.0 (最新)
- ✅ 修复记账管理删除全部 422 报错
- ✅ 修复待办字段映射（done/completed）
- ✅ 修复备忘录字段映射（createTime/updateTime）
- ✅ 新增导入导出进度条弹窗
- ✅ 新增用户/职位/角色分页 API
- ✅ 人员列表新增创建时间字段
- ✅ 支持手机号登录
- ✅ 数据库表新增 tags/remark 列

### v1.0.0
- ✅ 基础组织权限管理系统
- ✅ 记账管理模块
- ✅ 待办事项模块
- ✅ 备忘录模块
- ✅ 数据库查询工具模块

## License

MIT License
