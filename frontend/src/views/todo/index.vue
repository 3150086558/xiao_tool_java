<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧分类 -->
      <el-col :span="6">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>待办分类</span>
            </div>
          </template>
          <el-radio-group v-model="activeStatus" @change="handleFilter" style="width: 100%">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="undone">待办</el-radio-button>
            <el-radio-button value="done">已完成</el-radio-button>
          </el-radio-group>
          <el-divider />
          <div class="priority-filter">
            <div class="filter-title">优先级</div>
            <el-radio-group v-model="activePriority" @change="handleFilter" style="display: flex; flex-direction: column; gap: 8px; margin-top: 8px;">
              <el-radio value="">全部</el-radio>
              <el-radio value="high">高</el-radio>
              <el-radio value="normal">中</el-radio>
              <el-radio value="low">低</el-radio>
            </el-radio-group>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧列表 -->
      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>待办列表</span>
              <el-button type="primary" :icon="Plus" @click="handleAdd">新增待办</el-button>
            </div>
          </template>
          <el-input v-model="searchText" placeholder="搜索待办内容" :prefix-icon="Search" clearable style="margin-bottom: 12px" @input="handleFilter" />
          <div v-loading="loading">
            <div v-if="!filteredData.length" class="empty-tip">
              <el-empty description="暂无待办" />
            </div>
            <div
              v-for="item in filteredData"
              :key="item.id"
              class="todo-item"
              :class="{ done: item.done }"
            >
              <el-checkbox v-model="item.done" @change="handleToggle(item)" />
              <div class="todo-content">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-meta">
                  <el-tag size="small" :type="priorityType(item.priority)">{{ priorityText(item.priority) }}</el-tag>
                  <span v-if="item.dueDate" class="meta-item">
                    <el-icon><Calendar /></el-icon>{{ item.dueDate }}
                  </span>
                </div>
              </div>
              <div class="todo-ops">
                <el-button link type="primary" :icon="Edit" @click="handleEdit(item)" />
                <el-button link type="danger" :icon="Delete" @click="handleDelete(item)" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入待办标题" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio value="high">高</el-radio>
            <el-radio value="normal">中</el-radio>
            <el-radio value="low">低</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker v-model="form.dueDate" type="datetime" value-format="YYYY-MM-DD HH:mm" placeholder="选择截止时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Calendar } from '@element-plus/icons-vue'
import {
  getTodoPage,
  createTodo,
  updateTodo,
  deleteTodo,
  toggleTodoDone
} from '@/api/app/todo'

const loading = ref(false)
const tableData = ref([])
const searchText = ref('')
const activeStatus = ref('all')
const activePriority = ref('')

const filteredData = computed(() => {
  let list = tableData.value
  if (activeStatus.value === 'undone') list = list.filter((i) => !i.done)
  else if (activeStatus.value === 'done') list = list.filter((i) => i.done)
  if (activePriority.value) list = list.filter((i) => i.priority === activePriority.value)
  if (searchText.value) list = list.filter((i) => i.title.includes(searchText.value))
  return list
})

async function loadData() {
  loading.value = true
  try {
    const res = await getTodoPage({ page: 1, size: 999 })
    tableData.value = res.data?.records || []
  } catch (e) {
    tableData.value = mockData()
  } finally { loading.value = false }
}

function mockData() {
  return [
    { id: 1, title: '完成季度报告', priority: 'high', dueDate: '2026-07-05 18:00', done: false, remark: '需要包含数据分析' },
    { id: 2, title: '回复客户邮件', priority: 'medium', dueDate: '2026-07-03 12:00', done: false, remark: '' },
    { id: 3, title: '整理会议纪要', priority: 'low', dueDate: '', done: true, remark: '已完成' }
  ]
}

function priorityText(p) { return { high: '高', medium: '中', low: '低' }[p] || '中' }
function priorityType(p) { return { high: 'danger', medium: 'warning', low: 'info' }[p] || 'info' }

function handleFilter() {}

async function handleToggle(item) {
  try {
    await toggleTodoDone(item.id, { done: item.done })
    ElMessage.success(item.done ? '已标记完成' : '已标记待办')
  } catch (e) {
    item.done = !item.done
  }
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({ id: null, title: '', priority: 'normal', dueDate: '', remark: '' })
const rules = { title: [{ required: true, message: '请输入标题', trigger: 'blur' }] }

function handleAdd() {
  dialogTitle.value = '新增待办'
  Object.assign(form, { id: null, title: '', priority: 'normal', dueDate: '', remark: '' })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑待办'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
function resetForm() { formRef.value && formRef.value.resetFields() }

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const payload = { ...form, due_date: form.dueDate }
    if (form.id) { await updateTodo(form.id, payload); ElMessage.success('编辑成功') }
    else { await createTodo(payload); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.title}」吗？`, '提示', { type: 'warning' })
  await deleteTodo(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => { loadData() })
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  transition: background 0.2s;
}
.todo-item:hover { background: #f5f7fa; }
.todo-item.done .todo-title { text-decoration: line-through; color: #c0c4cc; }
.todo-content { flex: 1; }
.todo-title { font-size: 14px; color: #303133; margin-bottom: 6px; }
.todo-meta { display: flex; align-items: center; gap: 12px; font-size: 12px; color: #909399; }
.meta-item { display: inline-flex; align-items: center; gap: 4px; }
.todo-ops { display: flex; gap: 4px; }
.empty-tip { padding: 20px 0; }
</style>
