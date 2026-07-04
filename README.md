# 组织权限管理与个人工具系统

## 项目简介

这是一个前后端分离项目，当前主架构已经收敛为 `Java + Vue`：

- `backend-java`：Spring Boot 3 后端，负责系统管理、记账、统计、待办、备忘录、数据库查询、导入导出等能力
- `frontend`：Vue 3 + Vite + Element Plus 前端
- `sql`：PostgreSQL / MySQL / SQLite 初始化脚本
- `nginx`：反向代理示例配置

旧的 `Java + Python + Vue` 方案已经不再是当前主运行方式，本仓库应按 `Java + Vue` 项目理解和运行。

## 当前技术栈

| 层级 | 技术 | 默认端口 |
| --- | --- | --- |
| 前端 | Vue 3、Vite、Pinia、Vue Router、Element Plus | `5173` |
| 后端 | Spring Boot 3、Spring Security、MyBatis-Plus | `8081` |
| 数据库 | PostgreSQL（默认）、MySQL、SQLite | `5432` / 自定义 |

## 主要功能

### 系统管理

- 登录、退出、当前用户信息
- 组织管理
- 用户管理
- 岗位管理
- 角色管理
- 菜单管理
- 数据权限管理
- 数据字典管理

### 业务工具

- 记账管理
  - 分页查询、新增、编辑、删除、清空
  - Excel / CSV 导入
  - Excel 导出
  - 导入模板下载
- 统计报表
- 待办事项
  - 分页查询、新增、编辑、删除
  - 完成状态切换
- 备忘录
  - 列表查询、新增、编辑、删除
  - 标签与类型管理
- 数据库查询
  - 连接配置管理
  - 测试连接
  - 查表列表
  - 查表结构
  - 查具体数据
  - 执行 SQL

## 目录结构

```text
my_project/
├─ backend-java/
├─ frontend/
├─ sql/
├─ nginx/
├─ test_data/
├─ docker-compose.yml
├─ FIXES_SUMMARY.md
└─ README.md
```

## 运行前准备

### 1. 准备数据库

默认使用 PostgreSQL，数据库名为 `org_sys`。

```sql
CREATE DATABASE org_sys WITH ENCODING 'UTF8';
```

执行初始化脚本：

```bash
psql -d org_sys -U postgres -f sql/init_postgres.sql
```

如果你的数据库是旧版本结构，建议再补执行一次字段修复脚本：

```bash
psql -d org_sys -U postgres -f backend-java/src/main/resources/db/migration/V2__add_dict_and_fields.sql
```

### 2. 检查后端配置

配置文件：`backend-java/src/main/resources/application.yml`

当前默认值：

- 端口：`8081`
- 数据库：`jdbc:postgresql://127.0.0.1:5432/org_sys`
- 用户名：`postgres`
- 密码：`123456`

## 启动方式

### 启动后端

在项目根目录执行：

```bash
cd backend-java
mvnw.cmd spring-boot:run
```

如果你本机没有 Maven 全局命令，直接使用 `mvnw.cmd` 即可。
首次运行如果缺少依赖，会自动下载。

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认访问地址：

- 前端：[http://localhost:5173](http://localhost:5173)
- 后端：[http://localhost:8081](http://localhost:8081)

## 默认账号

初始化脚本内默认账号为：

- 用户名：`admin`
- 密码：`admin123`

如果登录失败，请以数据库实际初始化结果为准。

## 下载与导出说明

记账导出文件、模板下载文件不会落到仓库目录中，而是由浏览器直接下载到：

- 浏览器默认下载目录
- 或用户手动选择的保存位置

这属于当前产品设计，不是后端把文件保存到项目目录。

## 当前建议

- 日常开发按 `Java + Vue` 架构继续推进，不要再按双后端思路维护
- 新环境初始化后，优先验证：登录、待办、备忘录、记账导入导出、数据库查询
- 如果历史连接里仍有 `postgres` 类型数据，当前版本已经兼容并自动按 PostgreSQL 处理