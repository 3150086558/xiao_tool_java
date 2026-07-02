# 小肖的自用工具 - 网页版多功能工具箱

一个轻量级网页版个人工具箱，集成记账管理、待办事项、备忘录、数据库查询、用户管理等功能。适合：

1. Windows 电脑本地运行；
2. Linux 服务器部署；
3. Docker 部署；
4. 使用 SQLite / PostgreSQL / MySQL 存储数据。

---

## 一、技术栈

### 前端

- HTML5 + CSS3 + 原生 JavaScript
- 响应式布局，手机浏览器可以直接访问
- 不依赖 Vue / React，部署简单

### 后端

- Python 3
- Python 标准库 HTTP Server
- REST API 接口
- 支持 CSV / Excel 导入导出

### 数据库

- SQLite：默认，无需单独安装，数据存在 `data/accounting.db`
- PostgreSQL：适合正式服务器部署
- MySQL：适合正式服务器部署

### 打包 / 部署

- Windows：`run_windows.bat` 一键运行
- Linux：`install_ubuntu_env.sh` 安装环境，`run_server.sh` 启动
- Docker：Dockerfile 已提供
- Windows exe：`build_windows_exe.bat`

---

## 二、功能模块

### 财务管理

- **记账**：新增、编辑、删除账单，支持收入/支出分类
  - 按月份、类型、关键词筛选
  - 自动统计收入、支出、结余
  - 分类汇总
  - 导入 Excel（含进度条，防重复导入）
  - 导出 Excel / CSV
  - 删除全部数据
- **统计报表**：按月/分类/类型可视化统计

### 日常工具

- **待办事项**：增删改查，支持完成状态切换
- **备忘录**：增删改查，支持搜索
- **数据库查询**：连接外部 MySQL / PostgreSQL 数据库
  - 配置数据库连接（主机、端口、用户名、密码、数据库名）
  - 测试连接
  - 获取并浏览表列表
  - 查看表结构（列名、类型、是否可空、默认值）
  - 执行 SQL 查询（SELECT / SHOW / DESCRIBE / EXPLAIN）

### 系统管理（仅管理员）

- **用户管理**：
  - 查看所有用户列表（含记账/待办/备忘录条数统计）
  - 重置用户密码
  - 删除用户（连同所有关联数据）

### 通用功能

- 多用户支持，注册/登录/退出
- 修改密码
- 左侧菜单导航，动态菜单树
- 手机端自适应布局

---

## 三、页面结构

```text
├── 登录页 / 注册页
├── 首页（概览仪表盘）
├── 记账页
│   ├── 顶部：收入/支出/结余统计卡片
│   ├── 中间：新增账单表单
│   ├── 筛选条件（月份、收入/支出、关键词）
│   ├── 账单列表（支持编辑、删除）
│   └── 底部：分类汇总 + 导入/导出/删除全部按钮
├── 统计报表页
├── 待办事项页
├── 备忘录页
├── 数据库查询页
│   ├── 连接配置表单
│   ├── 表列表（可点击查看结构）
│   ├── 表结构展示
│   └── SQL 查询编辑器 + 结果表格
└── 用户管理页（仅管理员可见）
```

---

## 四、目录说明

```text
my_project/
├── app.py                  后端服务入口（Python）
├── public/
│   ├── index.html          前端主页面
│   ├── login.html          登录/注册页面
│   ├── style.css           页面样式
│   └── app.js              前端交互逻辑
├── data/
│   └── accounting.db       SQLite 数据库文件，首次运行自动生成
├── test_data/              测试文件目录（非核心代码）
├── requirements.txt        Python 依赖（psycopg2-binary, PyMySQL, pyinstaller）
├── .env.example            环境变量配置模板
├── install_ubuntu_env.sh   Ubuntu/Linux 环境安装脚本
├── run_server.sh           Linux 启动脚本
├── run_windows.bat          Windows 启动脚本
├── Dockerfile              Docker 部署文件
├── setup_postgres.sql      PostgreSQL 初始化参考脚本
├── setup_mysql.sql         MySQL 初始化参考脚本
└── build_windows_exe.bat   Windows exe 打包脚本
```

---

## 五、启动与关闭

### 方式一：双击脚本（推荐）

- **Windows**：双击 `run_windows.bat` 启动，关闭命令行窗口即可关闭服务
- **Linux**：`./run_server.sh` 启动，按 `Ctrl + C` 关闭

### 方式二：命令行手动启动

```bash
# 进入项目目录
cd my_project

# 启动服务
python app.py
```

启动后浏览器打开：`http://127.0.0.1:8000`

### 方式三：后台运行（Linux/Mac）

```bash
# 后台启动，日志写入 app.log
nohup python app.py > app.log 2>&1 &

# 查看进程
ps aux | grep app.py

# 关闭服务（按 PID 结束）
kill <PID>
```

### 关闭服务

| 方式 | 操作 |
|------|------|
| 命令行窗口 | 直接关闭窗口，或按 `Ctrl + C` |
| 后台进程 | `kill <Python进程PID>` |
| Windows PowerShell | `Get-Process python \| Stop-Process -Force` |

---

## 六、Windows 电脑运行

### 1. 安装环境

Windows 电脑需要先安装 Python 3.10 或以上版本，安装时记得勾选 `Add Python to PATH`。

### 2. 启动应用

双击 `run_windows.bat`，它会自动创建虚拟环境、安装依赖、启动服务。

启动后浏览器打开：`http://127.0.0.1:8000`

默认管理员账号：admin / Mkld@2026（首次注册时创建）

---

## 七、Ubuntu / Linux 服务器部署

```bash
# 上传代码到服务器
cd /opt/my_project

# 安装环境
chmod +x install_ubuntu_env.sh run_server.sh
./install_ubuntu_env.sh

# 配置环境变量
cp .env.example .env
nano .env

# 启动
./run_server.sh
```

服务器本机访问：`http://127.0.0.1:8000`
外部访问：`http://服务器IP:8000`（需安全组放行 TCP 8000）

---

## 八、数据库配置

### SQLite（默认）

无需额外安装，数据保存在 `data/accounting.db`。

```env
DB_TYPE=sqlite
```

### PostgreSQL

```env
DB_TYPE=postgres
DB_HOST=127.0.0.1
DB_PORT=5432
DB_NAME=accounting
DB_USER=accounting_user
DB_PASSWORD=请改成强密码
```

### MySQL

```env
DB_TYPE=mysql
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=accounting
DB_USER=accounting_user
DB_PASSWORD=请改成强密码
```

---

## 九、Docker 部署

### SQLite 模式

```bash
docker build -t my-project .
docker run -d --name my-project -p 8000:8000 -v $PWD/data:/app/data my-project
```

### PostgreSQL / MySQL 模式

```bash
docker run -d --name my-project -p 8000:8000 \
  -e DB_TYPE=postgres \
  -e DB_HOST=PostgreSQL地址 \
  -e DB_PORT=5432 \
  -e DB_NAME=accounting \
  -e DB_USER=accounting_user \
  -e DB_PASSWORD=密码 \
  my-project
```

---

## 十、手机访问

电脑运行后，手机和电脑连接同一 WiFi，手机浏览器打开 `http://电脑IP:8000` 即可。

---

## 十一、API 接口

### 记账

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/records | 查询账单列表 |
| POST | /api/records | 新增账单 |
| PUT | /api/records/{id} | 修改账单 |
| DELETE | /api/records/{id} | 删除账单 |
| DELETE | /api/clear-all | 删除全部账单 |
| GET | /api/summary | 统计汇总 |
| GET | /api/export.csv | 导出 CSV |
| GET | /api/export.xlsx | 导出 Excel |
| POST | /api/import | 导入 Excel |

### 待办事项

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/todos | 查询待办列表 |
| POST | /api/todos | 新增待办 |
| PUT | /api/todos/{id} | 修改待办 |
| DELETE | /api/todos/{id} | 删除待办 |

### 备忘录

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/notes | 查询备忘录列表 |
| POST | /api/notes | 新增备忘录 |
| PUT | /api/notes/{id} | 修改备忘录 |
| DELETE | /api/notes/{id} | 删除备忘录 |

### 数据库查询

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/db-query | 连接测试/获取表列表/查看表结构/执行SQL |

### 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users | 获取用户列表（仅admin） |
| DELETE | /api/users/{id} | 删除用户（仅admin） |
| PUT | /api/users/reset-password | 重置密码（仅admin） |

### 通用

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/login | 登录 |
| POST | /api/register | 注册 |
| POST | /api/change-password | 修改密码 |
| POST | /api/logout | 退出 |
| GET | /api/menus | 获取用户菜单 |
| GET | /api/health | 健康检查 |

---

## 十二、数据表结构

应用启动时自动创建以下表：

- `users` — 用户表
- `records` — 记账记录表
- `todos` — 待办事项表
- `notes` — 备忘录表
- `menus` — 用户菜单表

---

## 十三、生产部署建议

- 个人使用：SQLite + `run_windows.bat` 即可
- 服务器部署：PostgreSQL/MySQL + Nginx + HTTPS
- 最省事迁移：Docker + SQLite 数据目录挂载
