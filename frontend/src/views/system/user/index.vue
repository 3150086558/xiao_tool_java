<template>
  <div class="app-container">
    <!-- 搜索条件（左侧） -->
    <el-card shadow="never" class="filter-container">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="请输入姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="所属组织">
          <el-tree-select
            v-model="query.orgId"
            :data="orgTree"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            check-strictly
            clearable
            placeholder="请选择组织"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" class="table-container">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增人员</el-button>
        <el-button :icon="Delete" :disabled="!selection.length" @click="handleBatchDelete">批量删除</el-button>
      </div>
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="姓名" width="100" show-overflow-tooltip />
        <el-table-column prop="orgName" label="所属组织" width="140" show-overflow-tooltip />
        <el-table-column prop="positionNames" label="职位" min-width="140" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link :type="row.status === 1 ? 'danger' : 'success'" @click="handleToggle(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!form.id">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="初始密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属组织" prop="orgId">
              <el-tree-select
                v-model="form.orgId"
                :data="orgTree"
                :props="{ label: 'name', children: 'children', value: 'id' }"
                check-strictly
                clearable
                placeholder="请选择组织"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="职位" prop="positionIds">
              <el-select
                v-model="form.positionIds"
                multiple
                filterable
                placeholder="请选择职位（可多选）"
                style="width: 100%"
              >
                <el-option
                  v-for="p in positionOptions"
                  :key="p.id"
                  :label="p.name"
                  :value="p.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Search, Refresh } from '@element-plus/icons-vue'
import {
  getUserPage,
  createUser,
  updateUser,
  deleteUser,
  resetUserPassword,
  toggleUserStatus
} from '@/api/system/user'
import { getOrgTree } from '@/api/system/org'
import { getPositionList } from '@/api/system/position'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const selection = ref([])
const orgTree = ref([])
const positionOptions = ref([])

const query = reactive({
  name: '',
  username: '',
  orgId: null,
  status: null,
  page: 1,
  size: 10
})

async function loadData() {
  loading.value = true
  try {
    const res = await getUserPage(query)
    tableData.value = res.data?.records || res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e) {
    tableData.value = mockData()
    total.value = tableData.value.length
  } finally {
    loading.value = false
  }
}

function mockData() {
  return [
    { id: 1, username: 'admin', name: '管理员', orgName: '总公司', orgId: 1, positionNames: '技术总监', positionIds: [1], phone: '13800000001', email: 'admin@test.com', status: 1, createTime: '2026-01-01 10:00:00' },
    { id: 2, username: 'zhangsan', name: '张三', orgName: '研发中心', orgId: 11, positionNames: '前端工程师', positionIds: [2], phone: '13800000002', email: 'zs@test.com', status: 1, createTime: '2026-02-01 10:00:00' }
  ]
}

function handleSearch() {
  query.page = 1
  loadData()
}
function handleReset() {
  query.name = ''
  query.username = ''
  query.orgId = null
  query.status = null
  handleSearch()
}
function handleSelectionChange(val) {
  selection.value = val
}

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  username: '',
  name: '',
  password: '',
  phone: '',
  email: '',
  orgId: null,
  positionIds: [],
  status: 1
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  orgId: [{ required: true, message: '请选择组织', trigger: 'change' }]
}

function handleAdd() {
  dialogTitle.value = '新增人员'
  Object.assign(form, {
    id: null, username: '', name: '', password: '123456',
    phone: '', email: '', orgId: null, positionIds: [], status: 1
  })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑人员'
  Object.assign(form, {
    id: row.id, username: row.username, name: row.name, password: '',
    phone: row.phone, email: row.email, orgId: row.orgId,
    positionIds: row.positionIds || [], status: row.status
  })
  dialogVisible.value = true
}

function resetForm() {
  formRef.value && formRef.value.resetFields()
}

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateUser(form.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createUser(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定删除选中的 ${selection.value.length} 条数据吗？`, '提示', { type: 'warning' })
  await Promise.all(selection.value.map((row) => deleteUser(row.id)))
  ElMessage.success('删除成功')
  loadData()
}

async function handleResetPwd(row) {
  const { value } = await ElMessageBox.prompt(`重置「${row.name}」的密码`, '重置密码', {
    inputPlaceholder: '请输入新密码',
    inputValue: '123456',
    inputValidator: (v) => (v && v.length >= 6) || '密码至少6位'
  })
  await resetUserPassword(row.id, { password: value })
  ElMessage.success('密码已重置')
}

async function handleToggle(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await toggleUserStatus(row.id, { status: newStatus })
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  loadData()
}

async function loadOrgTree() {
  try {
    const res = await getOrgTree()
    orgTree.value = res.data || []
  } catch (e) {
    orgTree.value = []
  }
}

async function loadPositions() {
  try {
    const res = await getPositionList()
    positionOptions.value = res.data || []
  } catch (e) {
    positionOptions.value = []
  }
}

onMounted(() => {
  loadOrgTree()
  loadPositions()
  loadData()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
}
.pagination-container {
  margin-top: 12px;
}
</style>
