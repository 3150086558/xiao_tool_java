<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>菜单管理</span>
          <div>
            <el-button :icon="Search" @click="toggleSearch">{{ searchVisible ? '收起搜索' : '搜索' }}</el-button>
            <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增顶级菜单</el-button>
            <el-button :icon="Sort" @click="toggleExpand">{{ isExpandAll ? '折叠全部' : '展开全部' }}</el-button>
          </div>
        </div>
      </template>

      <div v-if="searchVisible" class="filter-container">
        <el-form :inline="true" :model="query" class="search-form">
          <el-form-item label="菜单名称">
            <el-input v-model="query.title" placeholder="请输入菜单名称" clearable @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="tableData"
        row-key="id"
        border
        :default-expand-all="isExpandAll"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="title" label="菜单名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="160" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="70" align="center" />
        <el-table-column prop="visible" label="显示" width="70" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.visible === false ? 'info' : 'success'">
              {{ row.visible === false ? '隐藏' : '显示' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Plus" @click="handleAdd(row)">新增</el-button>
            <el-button link type="warning" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="24">
            <el-form-item label="上级菜单" prop="parentId">
              <el-tree-select
                v-model="form.parentId"
                :data="menuOptions"
                :props="{ label: 'title', children: 'children', value: 'id' }"
                check-strictly
                clearable
                placeholder="不选则为顶级菜单"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio :value="1">目录</el-radio>
                <el-radio :value="2">菜单</el-radio>
                <el-radio :value="3">按钮</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单图标" prop="icon">
              <el-input v-model="form.icon" placeholder="如：Setting、User" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="title">
              <el-input v-model="form.title" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.type !== 3">
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="form.path" placeholder="如：/system/user" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.type === 2">
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="form.component" placeholder="如：system/user/index" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.type === 3">
            <el-form-item label="权限标识" prop="permission">
              <el-input v-model="form.permission" placeholder="如：system:user:add" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.type !== 3">
            <el-form-item label="是否显示" prop="visible">
              <el-radio-group v-model="form.visible">
                <el-radio :value="true">显示</el-radio>
                <el-radio :value="false">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="是否缓存" prop="noCache">
              <el-switch v-model="form.noCache" :active-value="false" inactive-value="true" />
              <span style="margin-left: 8px; color: #909399; font-size: 12px;">开启缓存</span>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Sort } from '@element-plus/icons-vue'
import {
  getMenuTree,
  createMenu,
  updateMenu,
  deleteMenu
} from '@/api/system/menu'

const loading = ref(false)
const tableData = ref([])
const tableRef = ref()
const isExpandAll = ref(true)
const searchVisible = ref(false)
const query = reactive({ title: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getMenuTree(query)
    tableData.value = res.data || []
    menuOptions.value = [{ id: 0, title: '顶级菜单', children: tableData.value }]
  } catch (e) {
    tableData.value = mockData()
    menuOptions.value = [{ id: 0, title: '顶级菜单', children: tableData.value }]
  } finally { loading.value = false }
}

function mockData() {
  return [
    {
      id: 1, title: '系统管理', icon: 'Setting', type: 1, path: '/system', sort: 1, visible: true,
      children: [
        { id: 11, title: '组织管理', icon: 'OfficeBuilding', type: 2, path: 'org', component: 'system/org/index', sort: 1, visible: true },
        { id: 12, title: '人员管理', icon: 'User', type: 2, path: 'user', component: 'system/user/index', sort: 2, visible: true },
        { id: 13, title: '职位管理', icon: 'Briefcase', type: 2, path: 'position', component: 'system/position/index', sort: 3, visible: true },
        { id: 14, title: '角色管理', icon: 'UserFilled', type: 2, path: 'role', component: 'system/role/index', sort: 4, visible: true },
        { id: 15, title: '菜单管理', icon: 'Menu', type: 2, path: 'menu', component: 'system/menu/index', sort: 5, visible: true }
      ]
    },
    {
      id: 2, title: '应用中心', icon: 'Grid', type: 1, path: '/app', sort: 2, visible: true,
      children: [
        { id: 21, title: '记账管理', icon: 'Wallet', type: 2, path: '/accounting', component: 'accounting/index', sort: 1, visible: true }
      ]
    }
  ]
}

function typeText(t) {
  return { 1: '目录', 2: '菜单', 3: '按钮' }[t] || ''
}
function typeTag(t) {
  return { 1: '', 2: 'success', 3: 'warning' }[t] || ''
}

function toggleSearch() { searchVisible.value = !searchVisible.value }
function toggleExpand() {
  isExpandAll.value = !isExpandAll.value
  toggleRowExpansionAll(tableData.value, isExpandAll.value)
}
function toggleRowExpansionAll(data, expanded) {
  data.forEach((row) => {
    tableRef.value && tableRef.value.toggleRowExpansion(row, expanded)
    if (row.children) toggleRowExpansionAll(row.children, expanded)
  })
}

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()
const menuOptions = ref([])
const form = reactive({
  id: null, parentId: null, type: 2, title: '', icon: '',
  path: '', component: '', permission: '', sort: 0, visible: true, noCache: false
})
const rules = {
  title: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

function handleAdd(parent) {
  dialogTitle.value = '新增菜单'
  Object.assign(form, {
    id: null, parentId: parent ? parent.id : null, type: 2, title: '', icon: '',
    path: '', component: '', permission: '', sort: 0, visible: true, noCache: false
  })
  menuOptions.value = [{ id: 0, title: '顶级菜单', children: tableData.value }]
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑菜单'
  Object.assign(form, {
    id: row.id, parentId: row.parentId || null, type: row.type || 2,
    title: row.title, icon: row.icon || '', path: row.path || '',
    component: row.component || '', permission: row.permission || '',
    sort: row.sort || 0, visible: row.visible !== false, noCache: row.noCache === true ? true : false
  })
  menuOptions.value = [{ id: 0, title: '顶级菜单', children: tableData.value }]
  dialogVisible.value = true
}
function resetForm() { formRef.value && formRef.value.resetFields() }

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) { await updateMenu(form.id, form); ElMessage.success('编辑成功') }
    else { await createMenu(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除菜单「${row.title}」吗？子菜单将一并删除`, '提示', { type: 'warning' })
  await deleteMenu(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData(); nextTick() })
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
</style>
