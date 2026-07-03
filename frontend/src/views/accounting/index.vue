<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-container">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="项目">
          <el-input v-model="query.item" placeholder="请输入项目" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="消费分类">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="query.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
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
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        <el-button type="danger" :icon="Delete" @click="handleDeleteAll">删除全部</el-button>
        <el-upload :show-file-list="false" :before-upload="handleImport" accept=".xlsx,.xls,.csv">
          <el-button :icon="Upload">导入</el-button>
        </el-upload>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
        <el-button :icon="Document" @click="handleDownloadTemplate">下载模板</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe show-summary :summary-method="getSummaries">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="item" label="项目" min-width="140" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.type === 'income' ? 'text-income' : 'text-expense'">
              {{ row.type === 'income' ? '+' : '-' }}{{ formatMoney(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="消费分类" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type === 'income' ? 'success' : 'warning'">{{ row.category }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="收支类型" width="100" align="center">
          <template #default="{ row }">
            {{ row.type === 'income' ? '收入' : '支出' }}
          </template>
        </el-table-column>
        <el-table-column prop="payment" label="支付方式" width="120" align="center" />
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog
      v-model="progressDialogVisible"
      title="处理中"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="progress-wrap">
        <div class="progress-title">{{ progressTitle }}</div>
        <div class="progress-desc">{{ progressText }}</div>
        <el-progress :percentage="progressPercent" :stroke-width="18" status="success" />
      </div>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="日期" prop="date">
              <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收支类型" prop="type">
              <el-radio-group v-model="form.type">
                <el-radio value="expense">支出</el-radio>
                <el-radio value="income">收入</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目" prop="item">
              <el-input v-model="form.item" type="textarea" :rows="3" placeholder="请输入项目" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number v-model="form.amount" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="消费分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%" allow-create filterable>
                <el-option v-for="item in categories" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="支付方式" prop="payment">
              <el-select v-model="form.payment" placeholder="请选择" style="width: 100%">
                <el-option label="微信" value="微信" />
                <el-option label="支付宝" value="支付宝" />
                <el-option label="银行卡" value="银行卡" />
                <el-option label="现金" value="现金" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Document, Download, Plus, Refresh, Search, Upload } from '@element-plus/icons-vue'
import {
  createAccounting,
  deleteAccounting,
  deleteAllAccounting,
  downloadTemplate,
  exportAccounting,
  getAccountingPage,
  importAccounting,
  updateAccounting
} from '@/api/app/accounting'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const progressDialogVisible = ref(false)
const progressTitle = ref('')
const progressText = ref('')
const progressPercent = ref(0)
let progressTimer = null

const categories = ['餐饮', '交通', '购物', '娱乐', '住房', '医疗', '教育', '工资', '其他']

const query = reactive({ item: '', category: '', dateRange: [], page: 1, size: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const form = reactive({
  id: null,
  date: '',
  item: '',
  amount: 0,
  category: '',
  type: 'expense',
  payment: '',
  remark: ''
})

const rules = {
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  item: [{ required: true, message: '请输入项目', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function normalizeRecord(row = {}) {
  return {
    id: row.id,
    date: row.record_date || row.date || '',
    item: row.category || row.item || '',
    amount: Number(row.amount || 0),
    category: row.sub_category || row.category || '',
    type: row.type || 'expense',
    payment: row.account || row.payment || '',
    remark: row.note || row.remark || '',
    createTime: row.created_at || row.createTime || ''
  }
}

async function loadData() {
  loading.value = true
  try {
    const params = {
      item: query.item,
      category: query.category,
      startDate: query.dateRange?.[0] || '',
      endDate: query.dateRange?.[1] || '',
      page: query.page,
      size: query.size
    }
    const res = await getAccountingPage(params)
    tableData.value = (res.data?.records || []).map(normalizeRecord)
    total.value = res.data?.total || 0
  } catch (error) {
    tableData.value = []
    total.value = 0
    ElMessage.error(error.response?.data?.detail || error.message || '记账数据查询失败')
  } finally {
    loading.value = false
  }
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}

function getSummaries({ columns, data }) {
  const sums = []
  columns.forEach((column, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (column.property === 'amount') {
      const income = data.filter((item) => item.type === 'income').reduce((sum, item) => sum + Number(item.amount || 0), 0)
      const expense = data.filter((item) => item.type === 'expense').reduce((sum, item) => sum + Number(item.amount || 0), 0)
      sums[index] = `收入 ${formatMoney(income)} / 支出 ${formatMoney(expense)}`
      return
    }
    sums[index] = ''
  })
  return sums
}

function handleSearch() {
  query.page = 1
  loadData()
}

function handleReset() {
  query.item = ''
  query.category = ''
  query.dateRange = []
  handleSearch()
}

function handleAdd() {
  dialogTitle.value = '新增记账'
  Object.assign(form, {
    id: null,
    date: new Date().toISOString().slice(0, 10),
    item: '',
    amount: 0,
    category: '',
    type: 'expense',
    payment: '',
    remark: ''
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑记账'
  Object.assign(form, { ...row })
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
      record_date: form.date,
      type: form.type,
      category: form.item,
      sub_category: form.category,
      amount: form.amount,
      account: form.payment,
      note: form.remark
    }
    if (form.id) {
      await updateAccounting(form.id, payload)
      ElMessage.success('记账已更新')
    } else {
      await createAccounting(payload)
      ElMessage.success('记账已新增')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.detail || error.message || '记账保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除“${row.item}”吗？`, '提示', { type: 'warning' })
    await deleteAccounting(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.response?.data?.detail || error.message || '删除失败')
    }
  }
}

async function handleDeleteAll() {
  try {
    await ElMessageBox.confirm('确定删除全部记账记录吗？此操作不可恢复。', '警告', { type: 'warning' })
    await deleteAllAccounting()
    ElMessage.success('已删除全部记录')
    await loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.response?.data?.detail || error.message || '删除失败')
    }
  }
}

function clearProgressTimer() {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

function startProgress(title, text) {
  clearProgressTimer()
  progressTitle.value = title
  progressText.value = text
  progressPercent.value = 8
  progressDialogVisible.value = true
  progressTimer = setInterval(() => {
    if (progressPercent.value < 90) {
      progressPercent.value += progressPercent.value < 50 ? 6 : 2
    }
  }, 300)
}

function updateProgress(percent, text) {
  if (typeof percent === 'number') {
    progressPercent.value = Math.max(progressPercent.value, Math.min(95, percent))
  }
  if (text) {
    progressText.value = text
  }
}

function finishProgress(text) {
  clearProgressTimer()
  progressPercent.value = 100
  progressText.value = text
  setTimeout(() => {
    progressDialogVisible.value = false
    progressPercent.value = 0
    progressTitle.value = ''
    progressText.value = ''
  }, 600)
}

function closeProgress() {
  clearProgressTimer()
  progressDialogVisible.value = false
  progressPercent.value = 0
  progressTitle.value = ''
  progressText.value = ''
}

async function handleImport(file) {
  startProgress('导入记账数据', '正在上传文件...')
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await importAccounting(formData, (progressEvent) => {
      if (progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        updateProgress(percent, '正在上传文件...')
      }
    })
    updateProgress(96, '正在写入数据...')
    await loadData()
    const errorCount = res.data?.errors?.length || 0
    const successCount = res.data?.success || 0
    finishProgress('导入完成')
    ElMessage.success(errorCount ? `导入完成：成功 ${successCount} 条，失败 ${errorCount} 条` : `导入成功 ${successCount} 条`)
  } catch (error) {
    closeProgress()
    ElMessage.error(error.response?.data?.error || error.response?.data?.detail || error.message || '导入失败')
  }
  return false
}

async function handleExport() {
  startProgress('导出记账数据', '正在准备导出文件...')
  try {
    const params = {
      item: query.item,
      category: query.category,
      startDate: query.dateRange?.[0] || '',
      endDate: query.dateRange?.[1] || ''
    }
    const res = await exportAccounting(params, (progressEvent) => {
      if (progressEvent.total) {
        const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        updateProgress(percent, '正在下载导出文件...')
      }
    })
    updateProgress(98, '正在生成下载文件...')
    const blob = res.data
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = `记账数据_${Date.now()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    finishProgress('导出完成')
    ElMessage.success('导出成功')
  } catch (error) {
    closeProgress()
    ElMessage.error(error.response?.data?.error || error.response?.data?.detail || error.message || '导出失败')
  }
}

async function handleDownloadTemplate() {
  try {
    const res = await downloadTemplate()
    const blob = res.data
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = '记账导入模板.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error(error.response?.data?.error || error.response?.data?.detail || error.message || '模板下载失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 12px;
}

.text-income {
  color: #67c23a;
  font-weight: 600;
}

.text-expense {
  color: #f56c6c;
  font-weight: 600;
}

.progress-wrap {
  padding: 12px 0;
}

.progress-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.progress-desc {
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
}
</style>