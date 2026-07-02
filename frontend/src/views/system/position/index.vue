<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-container">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="职位名称">
          <el-input v-model="query.name" placeholder="请输入职位名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="组织">
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
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-container">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增职位</el-button>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="职位名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="职位编码" width="140" show-overflow-tooltip />
        <el-table-column prop="orgName" label="所属组织" width="160" show-overflow-tooltip />
        <el-table-column prop="level" label="职级" width="100" align="center" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handlePermission(row)">权限分配</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
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
        <el-form-item label="职位名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入职位名称" />
        </el-form-item>
        <el-form-item label="职位编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入职位编码" />
        </el-form-item>
        <el-form-item label="职级" prop="level">
          <el-input-number v-model="form.level" :min="1" :max="15" />
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
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 权限分配抽屉 -->
    <el-drawer
      v-model="permDrawer"
      :title="`权限分配 - ${currentPosition.name || ''}`"
      size="560px"
      direction="rtl"
    >
      <el-tabs v-model="permTab">
        <!-- Tab1 功能权限 -->
        <el-tab-pane label="功能权限" name="func">
          <div class="perm-toolbar">
            <el-checkbox v-model="treeExpandAll" @change="handleExpandAll">展开/折叠</el-checkbox>
            <el-checkbox v-model="treeCheckAll" @change="handleCheckAll">全选/全不选</el-checkbox>
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
        </el-tab-pane>

        <!-- Tab2 数据权限 -->
        <el-tab-pane label="数据权限" name="data">
          <el-form label-width="100px">
            <el-form-item label="数据策略">
              <el-radio-group v-model="dataPerm.dataScope" @change="handleScopeChange">
                <el-radio :value="1">全部数据</el-radio>
                <el-radio :value="2">本组织及子组织</el-radio>
                <el-radio :value="3">仅本组织</el-radio>
                <el-radio :value="4">仅本人</el-radio>
                <el-radio :value="5">自定义</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="dataPerm.dataScope === 5" label="自定义组织">
              <el-tree
                ref="dataTreeRef"
                :data="orgTree"
                show-checkbox
                node-key="id"
                :props="{ label: 'name', children: 'children' }"
                default-expand-all
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="permDrawer = false">取消</el-button>
        <el-button type="primary" :loading="permLoading" @click="submitPermission">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getPositionPage,
  createPosition,
  updatePosition,
  deletePosition,
  savePositionPermissions,
  getPositionPermissions
} from '@/api/system/position'
import { getOrgTree } from '@/api/system/org'
import { getMenuTree } from '@/api/system/menu'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const orgTree = ref([])
const menuTree = ref([])

const query = reactive({ name: '', orgId: null, page: 1, size: 10 })

async function loadData() {
  loading.value = true
  try {
    const res = await getPositionPage(query)
    tableData.value = res.data?.records || []
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
    { id: 1, name: '技术总监', code: 'TD', orgName: '研发中心', orgId: 11, level: 5, sort: 1, status: 1, createTime: '2026-01-01 10:00:00' },
    { id: 2, name: '前端工程师', code: 'FE', orgName: '研发中心', orgId: 11, level: 3, sort: 2, status: 1, createTime: '2026-01-02 10:00:00' }
  ]
}

function handleSearch() { query.page = 1; loadData() }
function handleReset() { query.name = ''; query.orgId = null; handleSearch() }

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({
  id: null, orgId: null, name: '', code: '', level: 1, sort: 0, status: 1, remark: ''
})
const rules = {
  name: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入职位编码', trigger: 'blur' }],
  orgId: [{ required: true, message: '请选择组织', trigger: 'change' }]
}

function handleAdd() {
  dialogTitle.value = '新增职位'
  Object.assign(form, { id: null, orgId: null, name: '', code: '', level: 1, sort: 0, status: 1, remark: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑职位'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
function resetForm() { formRef.value && formRef.value.resetFields() }

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) { await updatePosition(form.id, form); ElMessage.success('编辑成功') }
    else { await createPosition(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除职位「${row.name}」吗？`, '提示', { type: 'warning' })
  await deletePosition(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// 权限分配
const permDrawer = ref(false)
const permTab = ref('func')
const permLoading = ref(false)
const currentPosition = ref({})
const menuTreeRef = ref()
const dataTreeRef = ref()
const treeExpandAll = ref(true)
const treeCheckAll = ref(false)
const treeCheckStrictly = ref(false)

const dataPerm = reactive({
  dataScope: 1,
  menuIds: [],
  customOrgIds: []
})

async function handlePermission(row) {
  currentPosition.value = row
  permTab.value = 'func'
  treeCheckStrictly.value = false
  dataPerm.dataScope = 1
  permDrawer.value = true
  // 加载菜单树
  if (!menuTree.value.length) {
    try {
      const res = await getMenuTree()
      menuTree.value = res.data || []
    } catch (e) { menuTree.value = [] }
  }
  // 加载已有权限
  try {
    const res = await getPositionPermissions(row.id)
    const d = res.data || {}
    dataPerm.dataScope = d.dataScope || 1
    setTimeout(() => {
      menuTreeRef.value && menuTreeRef.value.setCheckedKeys(d.menuIds || [])
      dataTreeRef.value && dataTreeRef.value.setCheckedKeys(d.customOrgIds || [])
    }, 50)
  } catch (e) {
    setTimeout(() => {
      menuTreeRef.value && menuTreeRef.value.setCheckedKeys([])
    }, 50)
  }
}

function handleExpandAll(val) {
  const tree = menuTreeRef.value
  if (!tree) return
  val ? expandAll(tree.store.root) : collapseAll(tree.store.root)
}
function expandAll(node) {
  node.expanded = true
  node.childNodes.forEach(expandAll)
}
function collapseAll(node) {
  node.expanded = false
  node.childNodes.forEach(collapseAll)
}
function handleCheckAll(val) {
  const tree = menuTreeRef.value
  if (!tree) return
  val ? tree.setCheckedNodes(flattenTree(menuTree.value)) : tree.setCheckedKeys([])
}
function flattenTree(nodes) {
  let res = []
  nodes.forEach((n) => {
    res.push(n)
    if (n.children) res = res.concat(flattenTree(n.children))
  })
  return res
}

function handleScopeChange() {
  if (dataPerm.dataScope !== 5) {
    dataTreeRef.value && dataTreeRef.value.setCheckedKeys([])
  }
}

async function submitPermission() {
  permLoading.value = true
  try {
    const menuIds = menuTreeRef.value ? menuTreeRef.value.getCheckedKeys().concat(menuTreeRef.value.getHalfCheckedKeys()) : []
    const customOrgIds = dataPerm.dataScope === 5 && dataTreeRef.value ? dataTreeRef.value.getCheckedKeys() : []
    await savePositionPermissions(currentPosition.value.id, {
      menuIds,
      dataScope: dataPerm.dataScope,
      customOrgIds
    })
    ElMessage.success('权限保存成功')
    permDrawer.value = false
  } finally { permLoading.value = false }
}

async function loadOrgTree() {
  try {
    const res = await getOrgTree()
    orgTree.value = res.data || []
  } catch (e) { orgTree.value = [] }
}

onMounted(() => {
  loadOrgTree()
  loadData()
})
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
.pagination-container { margin-top: 12px; }
.perm-toolbar { margin-bottom: 8px; display: flex; gap: 16px; }
</style>
