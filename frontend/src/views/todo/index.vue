<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>待办筛选</span>
            </div>
          </template>
          <el-radio-group v-model="activeStatus" style="width: 100%">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="undone">未完成</el-radio-button>
            <el-radio-button value="done">已完成</el-radio-button>
          </el-radio-group>
          <el-divider />
          <div class="filter-block">
            <div class="filter-title">优先级</div>
            <el-radio-group v-model="activePriority" class="priority-group">
              <el-radio value="">全部</el-radio>
              <el-radio value="high">高</el-radio>
              <el-radio value="normal">中</el-radio>
              <el-radio value="low">低</el-radio>
            </el-radio-group>
          </div>
        </el-card>
      </el-col>

      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-button type="primary" :icon="Plus" @click="handleAdd">新增待办</el-button>
            </div>
          </template>

          <el-input
            v-model="searchText"
            placeholder="搜索标题或备注"
            :prefix-icon="Search"
            clearable
            style="margin-bottom: 12px"
          />

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
              <el-checkbox :model-value="item.done" @change="handleToggle(item, $event)" />
              <div class="todo-content" @click="handleEdit(item)">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-meta">
                  <el-tag size="small" :type="priorityType(item.priority)">{{ priorityText(item.priority) }}</el-tag>
                  <span v-if="item.dueDate" class="meta-item">
                    <el-icon><Calendar /></el-icon>
                    {{ item.dueDate }}
                  </span>
                  <span v-if="item.remark" class="meta-item remark-text">{{ item.remark }}</span>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
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
        <el-form-item label="截止时间" prop="dueDate">
          <el-date-picker
            v-model="form.dueDate"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm"
            placeholder="选择截止时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="已完成">
          <el-switch v-model="form.done" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { createTodo, deleteTodo, getTodoPage, toggleTodoDone, updateTodo } from '@/api/app/todo'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const searchText = ref('')
const activeStatus = ref('all')
const activePriority = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null,
  title: '',
  priority: 'normal',
  dueDate: '',
  remark: '',
  done: false
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

const filteredData = computed(() => {
  const keyword = searchText.value.trim()
  return tableData.value.filter((item) => {
    if (activeStatus.value === 'undone' && item.done) return false
    if (activeStatus.value === 'done' && !item.done) return false
    if (activePriority.value && item.priority !== activePriority.value) return false
    if (!keyword) return true
    return [item.title, item.remark].some((value) => String(value || '').includes(keyword))
  })
})

function normalizeTodo(row = {}) {
  const done = row.done !== undefined ? Boolean(row.done) : Boolean(row.completed)
  return {
    id: row.id,
    title: row.title || '',
    priority: row.priority || 'normal',
    dueDate: row.dueDate || row.due_date || '',
    remark: row.remark || '',
    done,
    completed: done ? 1 : 0,
    createTime: row.createTime || row.created_at || '',
    updateTime: row.updateTime || row.updated_at || ''
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTodoPage({ page: 1, size: 999 })
    tableData.value = (res.data?.records || []).map(normalizeTodo)
  } catch (error) {
    tableData.value = []
    ElMessage.error(error.response?.data?.detail || error.message || '待办查询失败')
  } finally {
    loading.value = false
  }
}

function priorityText(priority) {
  return { high: '高', normal: '中', low: '低' }[priority] || '中'
}

function priorityType(priority) {
  return { high: 'danger', normal: 'warning', low: 'info' }[priority] || 'info'
}

function handleAdd() {
  dialogTitle.value = '新增待办'
  Object.assign(form, {
    id: null,
    title: '',
    priority: 'normal',
    dueDate: '',
    remark: '',
    done: false
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑待办'
  Object.assign(form, normalizeTodo(row))
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const payload = {
      title: form.title.trim(),
      priority: form.priority,
      due_date: form.dueDate || null,
      remark: form.remark || '',
      completed: form.done ? 1 : 0
    }
    if (form.id) {
      await updateTodo(form.id, payload)
      ElMessage.success('待办已更新')
    } else {
      await createTodo(payload)
      ElMessage.success('待办已新增')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.detail || error.message || '待办保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleToggle(item, checked) {
  const nextDone = Boolean(checked)
  item.done = nextDone
  item.completed = nextDone ? 1 : 0
  try {
    const res = await toggleTodoDone(item.id, { completed: nextDone ? 1 : 0 })
    Object.assign(item, normalizeTodo(res.data || item))
    ElMessage.success(nextDone ? '已标记完成' : '已恢复为未完成')
  } catch (error) {
    item.done = !nextDone
    item.completed = item.done ? 1 : 0
    ElMessage.error(error.response?.data?.detail || error.message || '状态更新失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除“${row.title}”吗？`, '提示', { type: 'warning' })
    await deleteTodo(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.response?.data?.detail || error.message || '删除失败')
    }
  }
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-block {
  padding-top: 4px;
}

.filter-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}

.priority-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: background-color 0.2s ease;
}

.todo-item:hover {
  background: #f5f7fa;
}

.todo-item.done .todo-title {
  color: #c0c4cc;
  text-decoration: line-through;
}

.todo-content {
  flex: 1;
  min-width: 0;
  cursor: pointer;
}

.todo-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  font-size: 12px;
  color: #909399;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.remark-text {
  max-width: 320px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.todo-ops {
  display: flex;
  gap: 4px;
}

.empty-tip {
  padding: 24px 0;
}
</style>