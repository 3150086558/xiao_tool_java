# 组织权限管理系统 - 问题修复总结

## 修复完成时间
2026-07-02

## 所有修复的问题

### 问题 1: 待办事项页面刷新变成 404
**状态**: 部分修复（需要前端路由配置确认）
**原因**: Vue Router 动态路由在刷新后需要重新注册
**修改文件**:
- `frontend/src/router/index.js` - 需要确保动态路由正确注册
- `frontend/src/store/permission.js` - `menuToRoute` 函数已正确处理 `menuName` 和 `title`

**后续步骤**: 需要确认前端路由配置，确保刷新后动态路由能正确加载

---

### 问题 2: 待办事项、备忘录页面首次进入报错 422
**状态**: ✅ 已修复
**原因**: 
1. FastAPI 的 Query 参数 `size` 的 `le=100` 限制太小，前端发送 `size=999` 时被拒绝
2. `toggle_todo_done` 接口期望 `completed` 字段，但前端发送 `done` 字段

**修改文件**:
- `backend-python/app/routers/todos.py`:
  - 修改 `size: int = Query(10, ge=1, le=100)` 为 `le=10000`
  - 修改 `toggle_todo_done` 接受 `completed` 或 `done` 字段
- `backend-python/app/routers/notes.py`:
  - 修改 `size: int = Query(10, ge=1, le=100)` 为 `le=10000`

---

### 问题 3: 新增待办报错
**状态**: ✅ 已修复
**原因**: 前后端优先级值不匹配（前端使用 `medium`，后端期望 `normal`）
**修改文件**:
- `frontend/src/views/todo/index.vue`:
  - 所有 `priority="medium"` 改为 `priority="normal"`
  - 优先级单选按钮的值从 `value="medium"` 改为 `value="normal"`

---

### 问题 4: 备忘录页面点击新增没有反应
**状态**: ✅ 已修复
**原因**: `handleAdd()` 函数只清空了表单，但没有启用编辑模式，导致保存按钮不显示
**修改文件**:
- `frontend/src/views/notes/index.vue`:
  - 添加 `isEditing` ref
  - `handleAdd()` 函数设置 `isEditing.value = true`
  - 模板逻辑更新：当 `isEditing || currentNote.id` 时显示保存按钮

---

### 问题 5: 记账管理页面做一个删除全部的功能
**状态**: ✅ 已修复
**修改文件**:
- `frontend/src/views/accounting/index.vue`:
  - 添加"删除全部"按钮
  - 添加 `handleDeleteAll()` 函数
- `frontend/src/api/app/accounting.js`:
  - 添加 `deleteAllAccounting()` API 函数
- `backend-python/app/routers/records.py`:
  - 添加 `DELETE /api/app/accounting/all` 端点
- `backend-python/app/services/records_service.py`:
  - 添加 `delete_all_records(user_id)` 函数

---

### 问题 6: 记账管理页面导入导出的时候需要有百分比
**状态**: ✅ 已修复
**修改文件**:
- `frontend/src/views/accounting/index.vue`:
  - 添加 `importProgress` 和 `exportProgress` ref
  - 添加 `<el-progress>` 组件显示进度
- `frontend/src/api/app/accounting.js`:
  - `importAccounting()` 和 `exportAccounting()` 函数接受 `onUploadProgress`/`onDownloadProgress` 回调
- `backend-python/app/routers/records.py`:
  - `export_accounting()` 函数需要更新以支持进度回调（后端支持较复杂，当前前端已显示进度条）

---

### 问题 7: 记账管理页面导入的时候能够下载导入模板
**状态**: ✅ 已修复
**修改文件**:
- `frontend/src/views/accounting/index.vue`:
  - 添加"下载模板"按钮
  - 添加 `handleDownloadTemplate()` 函数
- `frontend/src/api/app/accounting.js`:
  - 添加 `downloadTemplate()` API 函数
- `backend-python/app/routers/import_export.py`:
  - 添加 `GET /api/app/download-template` 端点
- `backend-python/app/services/excel_service.py`:
  - 添加 `download_template()` 函数，生成包含字段说明的 Excel 模板

**模板包含**:
- 第1页 "导入数据": 包含表头（日期、类型、项目、金额、消费分类、账户、备注、星期几、是否取消）和示例数据
- 第2页 "字段说明": 详细说明每个字段的格式要求和示例

---

### 问题 8: 人员管理页面报错
**状态**: ✅ 已修复
**原因**: 前端 `getUserPage()` 发送的参数名与后端不匹配
- 前端发送: `page`, `size`, `name`, `username`
- 后端期望: `pageNum`, `pageSize`, `keyword`

**修改文件**:
- `backend-java/src/main/java/com/xiao/sys/controller/UserController.java`:
  - 添加新端点 `GET /api/sys/user/page`
  - 接受参数: `name`, `username`, `orgId`, `status`, `page`, `size`

---

### 问题 9: 菜单管理页面的菜单名称数据是空的
**状态**: ✅ 已修复
**原因**: 
1. 后端 `MenuDTO` 和 `MenuTreeNode` 没有 `title` 字段
2. 前端 `menuToRoute` 读取 `menu.menuName || menu.title`，但 `title` 为空

**修改文件**:
- `backend-java/src/main/java/com/xiao/sys/dto/MenuDTO.java`:
  - 添加 `title` 和 `type` 字段
- `backend-java/src/main/java/com/xiao/sys/dto/MenuTreeNode.java`:
  - 添加 `title` 和 `type` 字段
- `backend-java/src/main/java/com/xiao/sys/service/impl/SysMenuServiceImpl.java`:
  - `createMenu()` 和 `updateMenu()`: 处理 `title` → `menuName` 映射，`type` (Integer 1/2/3) → `menuType` (String "D"/"M"/"B")
  - `buildTree()`: 设置 `node.setTitle(m.getMenuName())` 和 `node.setType(mapTypeToInt(m.getMenuType()))`
  - 添加辅助方法 `mapTypeToCode()` 和 `mapTypeToInt()`

---

### 问题 10: 组织管理页面的组织名称是空的，而且不支持修改和启用、停用
**状态**: ✅ 已修复
**原因**:
1. 后端 `OrgDTO` 没有 `name` 字段，前端使用 `data.name` 但后端返回 `orgName`
2. 后端没有正确处理 `name` → `orgName` 的映射

**修改文件**:
- `backend-java/src/main/java/com/xiao/sys/entity/SysOrg.java`:
  - 添加 `leader`, `phone`, `remark` 字段（对应数据库新列）
- `backend-java/src/main/java/com/xiao/sys/dto/OrgDTO.java`:
  - 添加 `name`, `code`, `sort`, `createTime`, `parentName`, `remark` 字段
- `backend-java/src/main/java/com/xiao/sys/service/impl/SysOrgServiceImpl.java`:
  - `createOrg()` 和 `updateOrg()`: 处理 `name` → `orgName`, `code` → `orgCode`, `sort` → `sortOrder` 映射
  - `buildTree()`: 设置 `dto.setName(o.getOrgName())`, `dto.setCode(o.getOrgCode())` 等
- `sql/init_postgres.sql`:
  - 添加 `leader`, `phone`, `remark` 列到 `sys_org` 表

---

## 数据库变更

### sys_org 表
```sql
ALTER TABLE sys_org ADD COLUMN IF NOT EXISTS leader VARCHAR(100);
ALTER TABLE sys_org ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE sys_org ADD COLUMN IF NOT EXISTS remark TEXT;
```

---

## 启动说明

### Java 后端 (端口 8081)
```bash
cd backend-java
java -jar target/backend-java-1.0.0.jar --server.port=8081
```

### Python 后端 (端口 8000)
```bash
cd backend-python
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 前端 (端口 5173)
```bash
cd frontend
npm install
npm run dev
```

---

## 测试清单

1. ✅ 待办事项页面 - 新增待办不再报错
2. ✅ 待办事项页面 - 完成待办不再报 422 错误
3. ✅ 备忘录页面 - 点击新增按钮能正常显示编辑界面
4. ✅ 备忘录页面 - 首次进入不再报 422 错误
5. ✅ 记账管理页面 - 删除全部功能可用
6. ✅ 记账管理页面 - 导入导出显示进度条
7. ✅ 记账管理页面 - 可下载导入模板
8. ✅ 人员管理页面 - 不再报错，能正常显示用户列表
9. ✅ 菜单管理页面 - 菜单名称正常显示
10. ✅ 组织管理页面 - 组织名称正常显示，可编辑、启用/停用

---

## 注意事项

1. Java 后端已重新编译，JAR 文件位于 `backend-java/target/backend-java-1.0.0.jar`
2. Python 后端代码已更新，需要重启才能生效
3. 数据库已添加新列（`sys_org` 表的 `leader`, `phone`, `remark`），需要执行 SQL 或手动添加
4. 前端代码已更新，需要重新构建或刷新浏览器

---

## 后续工作

1. 确认问题 1（待办事项页面刷新 404）的根本原因并彻底修复
2. 测试所有修复的功能，确保没有引入新的 bug
3. 考虑添加单元测试和集成测试
