# 组织权限管理系统 - 实现方案

## 一、整体架构

```
┌─────────────────────────────────────────────────┐
│         Vue 3 + Element Plus + Vite             │
│              (前端 SPA，统一入口)                 │
└──────────────────┬──────────────────────────────┘
                   │ HTTP
            ┌──────┴──────┐
            │  Nginx 反代  │  (按路径转发)
            └──┬───────┬──┘
               │       │
      /api/sys/*│       │/api/app/*
               ▼       ▼
    ┌──────────────┐  ┌──────────────────┐
    │ Java Spring   │  │ Python FastAPI    │
    │ Boot 主后端   │  │ 辅服务(现有功能)   │
    │              │  │                  │
    │ · 组织管理    │  │ · 记账管理        │
    │ · 人员管理    │  │ · 待办事项        │
    │ · 职位管理    │  │ · 备忘录          │
    │ · 权限管理    │  │ · 统计报表        │
    │ · 认证鉴权    │  │ · Excel导入导出   │
    │ · 统一登录    │  │ · 数据库查询工具   │
    └──────┬───────┘  └────────┬─────────┘
           │                   │
           └─────────┬─────────┘
                     ▼
            ┌──────────────────┐
            │   PostgreSQL     │
            │   (统一数据库)    │
            └──────────────────┘
```

### 职责划分

| 模块 | 服务 | 说明 |
|------|------|------|
| 用户认证/登录/登出 | **Java** | 统一认证中心，签发 JWT |
| 组织/职位/人员/角色/权限管理 | **Java** | 核心 RBAC 业务 |
| 菜单/功能权限分配 | **Java** | 按职位配置可见菜单和按钮权限 |
| 数据权限规则配置 | **Java** | 按职位配置数据范围 |
| 记账/待办/备忘录 | **Python** | 迁移现有 app.py 逻辑到 FastAPI |
| 统计报表/导入导出 | **Python** | 保留现有数据处理能力 |
| 数据库查询工具 | **Python** | 保留现有功能 |

### 通信机制

- **认证**：Java 签发 JWT，Python 服务验证同一 JWT（共享密钥）
- **数据权限**：Java 提供"当前用户可见的数据范围"接口，Python 服务调用该接口或解析 JWT 中的权限标识来过滤数据
- **反向代理**：Nginx 按 URL 前缀转发（`/api/sys/*` → Java:8080，`/api/app/*` → Python:8000）

---

## 二、权限模型设计（RBAC + 数据权限）

### 2.1 模型关系图

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

### 2.2 数据权限策略

| 策略码 | 名称 | 说明 |
|--------|------|------|
| `self` | 仅本人 | 只能查看自己创建的数据 |
| `dept` | 本部门 | 查看同组织下所有人员的数据 |
| `dept_and_sub` | 本部门及子部门 | 查看本组织和所有下级组织的数据 |
| `custom` | 自定义部门 | 查看指定组织列表的数据 |
| `all` | 全部数据 | 查看所有数据 |

数据权限应用于：记账记录、待办、备忘录等业务数据，通过 `user_id → user_position → position_data_scope` 链路解析出可见的 user_id 集合，业务查询时加 `WHERE user_id IN (可见集合)`。

---

## 三、数据库设计（PostgreSQL）

### 3.1 新增表（Java 主后端管理）

```sql
-- 组织表（树形）
CREATE TABLE sys_org (
    id          SERIAL PRIMARY KEY,
    parent_id   INTEGER NOT NULL DEFAULT 0,   -- 0=根节点
    org_name    VARCHAR(100) NOT NULL,
    org_code    VARCHAR(50) UNIQUE NOT NULL,  -- 组织编码
    sort_order  INTEGER NOT NULL DEFAULT 0,
    status      SMALLINT NOT NULL DEFAULT 1,  -- 1=启用 0=禁用
    leader_id   INTEGER,                       -- 负责人user_id
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sys_org_parent ON sys_org(parent_id);

-- 职位表
CREATE TABLE sys_position (
    id          SERIAL PRIMARY KEY,
    org_id      INTEGER NOT NULL REFERENCES sys_org(id),
    position_name VARCHAR(100) NOT NULL,
    position_code VARCHAR(50) NOT NULL,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    status      SMALLINT NOT NULL DEFAULT 1,
    description TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sys_position_org ON sys_position(org_id);

-- 人员表（重构现有 users 表，或新建 sys_user 做映射）
CREATE TABLE sys_user (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(50) UNIQUE NOT NULL,
    password    VARCHAR(100) NOT NULL,        -- bcrypt 加密
    real_name   VARCHAR(50),
    email       VARCHAR(100),
    phone       VARCHAR(20),
    org_id      INTEGER REFERENCES sys_org(id),
    status      SMALLINT NOT NULL DEFAULT 1,  -- 1=启用 0=禁用
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 用户-职位关联表（一人多职位）
CREATE TABLE sys_user_position (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    position_id INTEGER NOT NULL REFERENCES sys_position(id) ON DELETE CASCADE,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE, -- 是否主职位
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, position_id)
);

-- 角色表
CREATE TABLE sys_role (
    id          SERIAL PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status      SMALLINT NOT NULL DEFAULT 1,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 职位-角色关联表
CREATE TABLE sys_position_role (
    id          SERIAL PRIMARY KEY,
    position_id INTEGER NOT NULL REFERENCES sys_position(id) ON DELETE CASCADE,
    role_id     INTEGER NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(position_id, role_id)
);

-- 菜单/权限表（树形，含目录、菜单、按钮三级）
CREATE TABLE sys_menu (
    id          SERIAL PRIMARY KEY,
    parent_id   INTEGER NOT NULL DEFAULT 0,
    menu_name   VARCHAR(50) NOT NULL,
    menu_type   CHAR(1) NOT NULL,             -- D=目录 M=菜单 B=按钮
    path        VARCHAR(200),                  -- 前端路由路径
    component   VARCHAR(200),                  -- Vue组件路径
    permission  VARCHAR(100),                  -- 权限标识如 system:user:add
    icon        VARCHAR(50),
    sort_order  INTEGER NOT NULL DEFAULT 0,
    visible     SMALLINT NOT NULL DEFAULT 1,
    status      SMALLINT NOT NULL DEFAULT 1,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_sys_menu_parent ON sys_menu(parent_id);

-- 角色-菜单关联表
CREATE TABLE sys_role_menu (
    id          SERIAL PRIMARY KEY,
    role_id     INTEGER NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    menu_id     INTEGER NOT NULL REFERENCES sys_menu(id) ON DELETE CASCADE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(role_id, menu_id)
);

-- 职位数据权限配置表
CREATE TABLE sys_position_data_scope (
    id          SERIAL PRIMARY KEY,
    position_id INTEGER NOT NULL REFERENCES sys_position(id) ON DELETE CASCADE,
    scope_type  VARCHAR(20) NOT NULL,          -- self/dept/dept_and_sub/custom/all
    -- scope_type=custom 时，关联的自定义组织ID存于下表
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(position_id)
);

-- 数据权限自定义组织范围
CREATE TABLE sys_data_scope_org (
    id          SERIAL PRIMARY KEY,
    scope_id    INTEGER NOT NULL REFERENCES sys_position_data_scope(id) ON DELETE CASCADE,
    org_id      INTEGER NOT NULL REFERENCES sys_org(id) ON DELETE CASCADE,
    UNIQUE(scope_id, org_id)
);

-- 操作日志表（审计）
CREATE TABLE sys_oper_log (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER,
    username    VARCHAR(50),
    oper_module VARCHAR(50),
    oper_type   VARCHAR(20),                   -- CREATE/UPDATE/DELETE/LOGIN等
    oper_desc   TEXT,
    oper_ip     VARCHAR(50),
    oper_time   TIMESTAMP NOT NULL DEFAULT NOW(),
    cost_ms     INTEGER
);
CREATE INDEX idx_sys_oper_log_user ON sys_oper_log(user_id);
CREATE INDEX idx_sys_oper_log_time ON sys_oper_log(oper_time);
```

### 3.2 现有表改造

现有 `users / records / todos / notes / menus / db_connections` 表保留，做以下调整：

| 表 | 改造内容 |
|----|----------|
| `users` | 新增 `real_name / email / phone / org_id / status` 字段（或迁移到 sys_user，建视图映射） |
| `records / todos / notes` | 新增 `org_id` 字段冗余（便于数据权限按组织过滤），或保持仅 `user_id` 通过用户关联查 org_id |
| `menus` | 弃用 per-user 菜单机制，改由 Java 的 sys_menu + 角色权限统一管理 |
| `db_connections` | 密码改为 AES 加密存储 |

**迁移策略**：现有 `users.id` 与 `sys_user.id` 做映射，Python 服务通过 JWT 中的 user_id 查询现有业务表，数据权限通过调用 Java 接口或解析 JWT 中的 scope 信息实现。

---

## 四、后端 API 设计

### 4.1 Java 主后端 API（`/api/sys/*`）

#### 认证模块
| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/sys/login` | 登录，返回 JWT |
| POST | `/api/sys/logout` | 登出 |
| GET | `/api/sys/userinfo` | 获取当前用户信息+权限+菜单 |
| POST | `/api/sys/change-password` | 修改密码 |

#### 组织管理
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/sys/org/tree` | 获取组织树 |
| POST | `/api/sys/org` | 新增组织 |
| PUT | `/api/sys/org/{id}` | 编辑组织 |
| DELETE | `/api/sys/org/{id}` | 删除组织 |
| GET | `/api/sys/org/{id}/users` | 获取组织下人员 |

#### 职位管理
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/sys/position` | 职位列表（支持按组织过滤） |
| POST | `/api/sys/position` | 新增职位 |
| PUT | `/api/sys/position/{id}` | 编辑职位 |
| DELETE | `/api/sys/position/{id}` | 删除职位 |
| PUT | `/api/sys/position/{id}/roles` | 分配角色 |
| PUT | `/api/sys/position/{id}/data-scope` | 设置数据权限 |

#### 人员管理
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/sys/user` | 人员列表（分页、搜索、按组织过滤） |
| POST | `/api/sys/user` | 新增人员 |
| PUT | `/api/sys/user/{id}` | 编辑人员 |
| DELETE | `/api/sys/user/{id}` | 删除人员 |
| PUT | `/api/sys/user/{id}/positions` | 分配职位 |
| PUT | `/api/sys/user/{id}/reset-password` | 重置密码 |
| PUT | `/api/sys/user/{id}/status` | 启用/禁用 |

#### 角色管理
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/sys/role` | 角色列表 |
| POST | `/api/sys/role` | 新增角色 |
| PUT | `/api/sys/role/{id}` | 编辑角色 |
| DELETE | `/api/sys/role/{id}` | 删除角色 |
| PUT | `/api/sys/role/{id}/menus` | 分配菜单权限 |

#### 菜单管理
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/sys/menu/tree` | 获取菜单树（管理用） |
| GET | `/api/sys/menu/user-tree` | 获取当前用户可见菜单树 |
| POST | `/api/sys/menu` | 新增菜单 |
| PUT | `/api/sys/menu/{id}` | 编辑菜单 |
| DELETE | `/api/sys/menu/{id}` | 删除菜单 |

#### 数据权限
| 方法 | 路径 | 功能 |
|------|------|------|
| GET | `/api/sys/data-scope/visible-users` | 获取当前用户可见的user_id列表（供Python服务调用） |
| GET | `/api/sys/data-scope/visible-orgs` | 获取当前用户可见的组织ID列表 |

### 4.2 Python 辅服务 API（`/api/app/*`）

迁移现有 app.py 的业务接口，统一加 `/api/app` 前缀：

| 现有路径 | 新路径 | 功能 |
|----------|--------|------|
| `/api/records` | `/api/app/records` | 记账CRUD |
| `/api/summary` | `/api/app/summary` | 收支汇总 |
| `/api/todos` | `/api/app/todos` | 待办CRUD |
| `/api/notes` | `/api/app/notes` | 备忘录CRUD |
| `/api/import` | `/api/app/import` | Excel导入 |
| `/api/export.xlsx` | `/api/app/export.xlsx` | Excel导出 |
| `/api/export.csv` | `/api/app/export.csv` | CSV导出 |
| `/api/download-template` | `/api/app/download-template` | 下载模板 |
| `/api/db-query` | `/api/app/db-query` | 数据库查询 |
| `/api/db-connections` | `/api/app/db-connections` | 连接管理 |

**认证改造**：Python 服务不再自己管理 session，改为验证 Java 签发的 JWT。

---

## 五、前端设计（Vue 3 + Element Plus）

### 5.1 技术栈

| 项 | 选型 |
|----|------|
| 框架 | Vue 3 (Composition API + `<script setup>`) |
| 构建 | Vite 5 |
| UI | Element Plus |
| 路由 | Vue Router 4 |
| 状态 | Pinia |
| HTTP | Axios |
| 语言 | JavaScript（可选 TypeScript） |

### 5.2 目录结构

```
frontend/
├── src/
│   ├── api/                  # API 请求封装
│   │   ├── system/           # Java 系统管理 API
│   │   │   ├── org.js
│   │   │   ├── user.js
│   │   │   ├── position.js
│   │   │   ├── role.js
│   │   │   └── menu.js
│   │   └── app/              # Python 业务 API
│   │       ├── accounting.js
│   │       ├── todo.js
│   │       └── note.js
│   ├── components/           # 公共组件
│   │   ├── TreeSelect/       # 树形选择器
│   │   └── DataScope/        # 数据权限配置组件
│   ├── layout/               # 布局
│   │   ├── index.vue         # 主布局（侧栏+顶栏+内容）
│   │   ├── Sidebar.vue       # 动态菜单侧栏
│   │   └── TagsView.vue      # 标签页导航
│   ├── router/               # 路由
│   │   └── index.js          # 动态路由（基于权限菜单生成）
│   ├── store/                # Pinia
│   │   ├── user.js           # 用户信息+权限
│   │   └── permission.js     # 动态路由生成
│   ├── utils/
│   │   ├── request.js        # axios 封装（JWT拦截器）
│   │   └── auth.js           # token 管理
│   └── views/
│       ├── login/            # 登录页
│       ├── system/           # 系统管理页面
│       │   ├── org/          # 组织管理（树形+右侧详情）
│       │   ├── user/         # 人员管理
│       │   ├── position/     # 职位管理
│       │   ├── role/         # 角色管理
│       │   └── menu/         # 菜单管理
│       ├── accounting/       # 记账管理
│       ├── todo/             # 待办事项
│       ├── notes/            # 备忘录
│       ├── stats/            # 统计报表
│       └── db-query/         # 数据库查询工具
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js            # 配置代理（/api/sys→8080, /api/app→8000）
└── package.json
```

### 5.3 核心页面设计

#### 组织管理页面
- **左侧**：Element Plus `el-tree` 展示组织树，支持拖拽排序、右键菜单（新增子组织/编辑/删除）
- **右侧**：选中组织后显示详情（基本信息、负责人、下属职位列表、人员列表）

#### 人员管理页面
- **顶部搜索栏**：姓名/用户名搜索 + 组织树筛选 + 状态筛选
- **表格**：`el-table` 展示人员列表（头像、姓名、用户名、组织、职位、状态、操作）
- **表单弹窗**：新增/编辑人员（基本信息 + 组织选择 + 职位多选）

#### 职位管理页面
- **表格**：职位列表（职位名、所属组织、关联角色数、数据权限范围）
- **表单弹窗**：新增/编辑职位
- **权限分配抽屉**：`el-drawer` 内含两个 Tab
  - Tab1 功能权限：`el-tree` 勾选菜单+按钮
  - Tab2 数据权限：单选策略(self/dept/dept_and_sub/custom/all) + custom 时显示组织树多选

#### 角色管理页面
- **表格**：角色列表
- **权限分配**：`el-tree` 勾选菜单权限

#### 动态路由与菜单
- 登录后调用 `/api/sys/userinfo` 获取用户菜单树
- Pinia 中将后端菜单转为 Vue Router 动态路由（`component` 字段映射到 `views/` 下的 `.vue` 文件）
- `el-menu` 根据路由树渲染侧栏

---

## 六、项目目录结构（最终）

```
my_project/
├── frontend/                 # Vue 前端
├── backend-java/             # Java Spring Boot 主后端
│   ├── src/main/java/com/xiao/sys/
│   │   ├── config/           # 配置（Security、JWT、CORS）
│   │   ├── controller/       # 控制器
│   │   ├── service/          # 业务逻辑
│   │   ├── mapper/           # MyBatis Mapper
│   │   ├── entity/           # 实体类
│   │   ├── dto/              # 数据传输对象
│   │   ├── security/         # JWT 过滤器、权限处理
│   │   └── common/           # 通用工具（Result、PageResult等）
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── mapper/           # MyBatis XML
│   └── pom.xml
├── backend-python/           # Python FastAPI 辅服务
│   ├── app/
│   │   ├── main.py           # FastAPI 入口
│   │   ├── routers/          # 路由（records/todos/notes等）
│   │   ├── services/         # 业务逻辑（迁移自 app.py）
│   │   ├── models/           # 数据模型
│   │   ├── deps.py           # JWT 验证依赖
│   │   └── database.py       # PG 连接
│   └── requirements.txt
├── nginx/
│   └── nginx.conf            # 反向代理配置
├── sql/
│   ├── init_postgres.sql     # 完整建库脚本
│   └── migrate_data.sql      # 数据迁移脚本
├── docker-compose.yml        # 一键启动 PG + Java + Python + Nginx
└── app.py                    # 保留旧版（过渡期兼容）
```

---

## 七、技术选型明细

### Java 后端

| 组件 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.x | 框架 |
| Spring Security | 6.x | 认证鉴权 |
| MyBatis Plus | 3.5.x | ORM |
| PostgreSQL Driver | 42.x | 数据库驱动 |
| JJWT | 0.12.x | JWT 生成验证 |
| Lombok | 最新 | 简化代码 |
| SpringDoc OpenAPI | 2.x | API 文档 |

### Python 后端

| 组件 | 版本 | 用途 |
|------|------|------|
| FastAPI | 0.110+ | Web 框架 |
| Uvicorn | 最新 | ASGI 服务器 |
| psycopg2-binary | 2.9+ | PG 驱动 |
| openpyxl | 3.1+ | Excel 处理 |
| PyJWT | 2.8+ | JWT 验证 |
| python-multipart | 最新 | 文件上传 |

### 前端

| 组件 | 版本 | 用途 |
|------|------|------|
| Vue | 3.4+ | 框架 |
| Element Plus | 2.6+ | UI 组件 |
| Vite | 5.x | 构建 |
| Vue Router | 4.x | 路由 |
| Pinia | 2.x | 状态管理 |
| Axios | 1.x | HTTP |

---

## 八、实施步骤

### 阶段 1：基础设施搭建
1. 创建 PostgreSQL 数据库，执行完整建表脚本
2. 搭建 Java Spring Boot 项目骨架（Security + JWT + MyBatis Plus）
3. 搭建 Python FastAPI 项目骨架（JWT 验证 + PG 连接）
4. 搭建 Vue 3 前端项目骨架（Vite + Element Plus + Router + Pinia）
5. 配置 Nginx 反向代理

### 阶段 2：认证与权限核心（Java）
1. 实现登录/登出/JWT 签发
2. 实现 JWT 安全过滤器
3. 实现菜单管理 CRUD
4. 实现角色管理 + 角色菜单分配
5. 实现组织树 CRUD
6. 实现职位管理 + 职位角色分配
7. 实现人员管理 + 人员职位分配
8. 实现数据权限配置（职位数据范围）
9. 实现 userinfo 接口（返回菜单树 + 权限标识 + 数据范围）

### 阶段 3：前端系统管理页面（Vue）
1. 实现登录页 + JWT 存储
2. 实现主布局（动态侧栏菜单 + 顶栏 + 路由）
3. 实现动态路由生成（基于后端菜单）
4. 实现组织管理页面
5. 实现人员管理页面
6. 实现职位管理页面（含权限分配抽屉）
7. 实现角色管理页面
8. 实现菜单管理页面

### 阶段 4：Python 业务迁移
1. 将 app.py 的记账逻辑迁移到 FastAPI
2. 迁移待办、备忘录、统计报表
3. 迁移 Excel 导入导出
4. 迁移数据库查询工具
5. 对接 Java 的数据权限接口，业务查询加数据范围过滤
6. 将 session 认证改为 JWT 验证

### 阶段 5：前端业务页面迁移（Vue）
1. 实现记账管理页面
2. 实现待办事项页面
3. 实现备忘录页面
4. 实现统计报表页面
5. 实现数据库查询工具页面
6. 实现 Excel 导入导出交互

### 阶段 6：联调与部署
1. Java ↔ Python 数据权限联调
2. 前端 ↔ 双后端全链路联调
3. 现有数据迁移到新表结构
4. 编写 docker-compose.yml 一键部署
5. 测试验证

---

## 九、数据迁移方案

1. **用户数据**：现有 `users` 表数据迁移到 `sys_user`，密码需重新用 bcrypt 加密（或保留旧哈希做兼容登录后强制改密）
2. **业务数据**：`records / todos / notes` 保留，新增 `org_id` 字段回填（根据用户关联的组织）
3. **菜单数据**：弃用旧 `menus` 表，统一由 `sys_menu` 管理
4. **数据库连接**：`db_connections` 密码字段改 AES 加密存储

---

## 十、安全改进

| 问题 | 现状 | 改进 |
|------|------|------|
| 密码存储 | SHA-256 无 salt | bcrypt |
| 会话管理 | 内存字典 | JWT 无状态 |
| 权限控制 | 硬编码 admin | RBAC 角色权限 |
| CSRF | 无防护 | JWT + SameSite Cookie |
| 数据库密码 | 明文存储 | AES 加密 |
| SQL 占位符 | ? 不兼容 PG | MyBatis / 参数化查询 |
| 错误信息 | 透传 str(e) | 统一异常处理，脱敏 |
