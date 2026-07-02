<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-container">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="query.name" placeholder="请输入角色名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="query.code" placeholder="请输入角色编码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="角色名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="角色编码" width="160" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleMenu(row)">分配菜单</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="!!form.id" placeholder="如：admin" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配菜单抽屉 -->
    <el-drawer
      v-model="menuDrawer"
      :title="`分配菜单 - ${currentRole.name || ''}`"
      size="480px"
      direction="rtl"
    >
      <div class="perm-toolbar">
        <el-checkbox v-model="treeExpandAll" @change="handleExpandAll">展开/折叠</el-checkbox>
        <el-checkbox v-model="treeCheckStrictly">父子联动</el-checkbox>
      </div>
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :props="{ label: 'title', children: 'children' }"
        :default-expand-all="true"
        :check-strictly="!treeCheckStrictly"
      />
      <template #footer>
        <el-button @click="menuDrawer = false">取消</el-button>
        <el-button type="primary" :loading="menuLoading" @click="submitMenu">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getRolePage,
  createRole,
  updateRole,
  deleteRole,
  getRoleMenuIds,
  assignRoleMenus
} from '@/api/system/role'
import { getMenuTree } from '@/api/system/menu'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const menuTree = ref([])

const query = reactive({ name: '', code: '', page: 1, size: 10 })

async function loadData() {
  loading.value = true
  try {
    const res = await getRolePage(query)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    tableData.value = mockData()
    total.value = tableData.value.length
  } finally { loading.value = false }
}

function mockData() {
  return [
    { id: 1, name: '超级管理员', code: 'admin', sort: 1, status: 1, remark: '系统最高权限', createTime: '2026-01-01 10:00:00' },
    { id: 2, name: '普通用户', code: 'user', sort: 2, status: 1, remark: '基础查看权限', createTime: '2026-01-02 10:00:00' }
  ]
}

function handleSearch() { query.page = 1; loadData() }
function handleReset() { query.name = ''; query.code = ''; handleSearch() }

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', code: '', sort: 0, status: 1, remark: '' })
const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

function handleAdd() {
  dialogTitle.value = '新增角色'
  Object.assign(form, { id: null, name: '', code: '', sort: 0, status: 1, remark: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑角色'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
function resetForm() { formRef.value && formRef.value.resetFields() }

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) { await updateRole(form.id, form); ElMessage.success('编辑成功') }
    else { await createRole(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除角色「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// 分配菜单
const menuDrawer = ref(false)
const menuLoading = ref(false)
const currentRole = ref({})
const menuTreeRef = ref()
const treeExpandAll = ref(true)
const treeCheckStrictly = ref(false)

async function handleMenu(row) {
  currentRole.value = row
  treeCheckStrictly.value = false
  menuDrawer.value = true
  if (!menuTree.value.length) {
    try {
      const res = await getMenuTree()
      menuTree.value = res.data || []
    } catch (e) { menuTree.value = [] }
  }
  try {
    const res = await getRoleMenuIds(row.id)
    const ids = res.data || []
    setTimeout(() => {
      menuTreeRef.value && menuTreeRef.value.setCheckedKeys(ids)
    }, 50)
  } catch (e) {
    setTimeout(() => { menuTreeRef.value && menuTreeRef.value.setCheckedKeys([]) }, 50)
  }
}

function handleExpandAll(val) {
  const tree = menuTreeRef.value
  if (!tree) return
  val ? expandAll(tree.store.root) : collapseAll(tree.store.root)
}
function expandAll(node) { node.expanded = true; node.childNodes.forEach(expandAll) }
function collapseAll(node) { node.expanded = false; node.childNodes.forEach(collapseAll) }

async function submitMenu() {
  menuLoading.value = true
  try {
    const ids = menuTreeRef.value ? menuTreeRef.value.getCheckedKeys().concat(menuTreeRef.value.getHalfCheckedKeys()) : []
    await assignRoleMenus(currentRole.value.id, { menuIds: ids })
    ElMessage.success('分配成功')
    menuDrawer.value = false
  } finally { menuLoading.value = false }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
.pagination-container { margin-top: 12px; }
.perm-toolbar { margin-bottom: 8px; display: flex; gap: 16px; }
</style>
