<template>
  <div class="app-container">
    <el-card shadow="never" class="filter-container">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="项目">
          <el-input v-model="query.item" placeholder="请输入项目" clearable @keyup.enter="handleSearch" style="width: 160px" />
        </el-form-item>
        <el-form-item label="消费分类">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
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
            style="width: 240px"
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
        <el-upload
          :show-file-list="false"
          :before-upload="handleImport"
          accept=".xlsx,.xls,.csv"
        >
          <el-button :icon="Upload">导入</el-button>
        </el-upload>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
        <el-button :icon="Document" @click="handleDownloadTemplate">下载模板</el-button>
      </div>
      <div v-if="importProgress > 0 && importProgress < 100" class="progress-bar">
        <el-progress :percentage="importProgress" :stroke-width="16" status="success">
          <template #default="{ percentage }">
            <span class="progress-text">导入中 {{ percentage }}%</span>
          </template>
        </el-progress>
      </div>
      <div v-if="exportProgress > 0 && exportProgress < 100" class="progress-bar">
        <el-progress :percentage="exportProgress" :stroke-width="16" status="success">
          <template #default="{ percentage }">
            <span class="progress-text">导出中 {{ percentage }}%</span>
          </template>
        </el-progress>
      </div>
      <el-table v-loading="loading" :data="tableData" border stripe show-summary :summary-method="getSummaries">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="item" label="项目" width="140" show-overflow-tooltip />
        <!-- 金额列在消费分类列前面 -->
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
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
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

    <!-- 新增/编辑弹窗 -->
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
              <el-input v-model="form.item" type="textarea" :rows="3" placeholder="请输入项目" style="width: 100%" />
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
                <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
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
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Upload, Download, Delete, Document } from '@element-plus/icons-vue'
import {
  getAccountingPage,
  createAccounting,
  updateAccounting,
  deleteAccounting,
  deleteAllAccounting,
  importAccounting,
  exportAccounting,
  downloadTemplate
} from '@/api/app/accounting'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const importProgress = ref(0)
const exportProgress = ref(0)
const categories = ['餐饮', '交通', '购物', '娱乐', '住房', '医疗', '教育', '工资', '其他']

const query = reactive({
  item: '', category: '', dateRange: [], page: 1, size: 10
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      item: query.item,
      category: query.category,
      startDate: query.dateRange && query.dateRange[0],
      endDate: query.dateRange && query.dateRange[1],
      page: query.page,
      size: query.size
    }
    const res = await getAccountingPage(params)
    tableData.value = (res.data?.records || []).map(r => ({
      id: r.id,
      date: r.record_date || r.date,
      item: r.category || r.item,
      amount: r.amount,
      category: r.sub_category || r.category,
      type: r.type,
      payment: r.account || r.payment,
      remark: r.note || r.remark,
      createTime: r.created_at || r.createTime
    }))
    total.value = res.data?.total || 0
  } catch (e) {
    tableData.value = mockData()
    total.value = tableData.value.length
  } finally { loading.value = false }
}

function mockData() {
  return [
    { id: 1, date: '2026-06-01', item: '午餐团建', amount: 168.5, category: '餐饮', type: 'expense', payment: '微信', remark: '部门聚餐', createTime: '2026-06-01 12:00:00' },
    { id: 2, date: '2026-06-02', item: '地铁通勤', amount: 12, category: '交通', type: 'expense', payment: '支付宝', remark: '', createTime: '2026-06-02 09:00:00' },
    { id: 3, date: '2026-06-03', item: '6月工资', amount: 12000, category: '工资', type: 'income', payment: '银行卡', remark: '月度工资', createTime: '2026-06-03 10:00:00' }
  ]
}

function formatMoney(val) {
  return Number(val || 0).toFixed(2)
}

function getSummaries({ columns, data }) {
  const sums = []
  columns.forEach((col, idx) => {
    if (idx === 0) { sums[idx] = '合计'; return }
    if (col.property === 'amount') {
      const expense = data.filter((d) => d.type === 'expense').reduce((s, d) => s + Number(d.amount || 0), 0)
      const income = data.filter((d) => d.type === 'income').reduce((s, d) => s + Number(d.amount || 0), 0)
      sums[idx] = `收入 ${formatMoney(income)} / 支出 ${formatMoney(expense)}`
    } else {
      sums[idx] = ''
    }
  })
  return sums
}

function handleSearch() { query.page = 1; loadData() }
function handleReset() {
  query.item = ''; query.category = ''; query.dateRange = []
  handleSearch()
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({
  id: null, date: '', item: '', amount: 0, category: '',
  type: 'expense', payment: '', remark: ''
})
const rules = {
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  item: [{ required: true, message: '请输入项目', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function handleAdd() {
  dialogTitle.value = '新增记账'
  Object.assign(form, {
    id: null, date: new Date().toISOString().slice(0, 10), item: '',
    amount: 0, category: '', type: 'expense', payment: '', remark: ''
  })
  dialogVisible.value = true
}
function handleEdit(row) {
  dialogTitle.value = '编辑记账'
  Object.assign(form, { ...row })
  dialogVisible.value = true
}
function resetForm() { formRef.value && formRef.value.resetFields() }

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const submitData = {
      record_date: form.date,
      type: form.type,
      category: form.item,
      sub_category: form.category,
      amount: form.amount,
      account: form.payment,
      note: form.remark
    }
    if (form.id) { await updateAccounting(form.id, submitData); ElMessage.success('编辑成功') }
    else { await createAccounting(submitData); ElMessage.success('新增成功') }
    dialogVisible.value = false
    loadData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.item}」吗？`, '提示', { type: 'warning' })
  await deleteAccounting(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleDeleteAll() {
  await ElMessageBox.confirm('确定删除全部记账记录吗？此操作不可恢复！', '警告', { type: 'warning' })
  await deleteAllAccounting()
  ElMessage.success('删除全部成功')
  loadData()
}

async function handleImport(file) {
  try {
    const fd = new FormData()
    fd.append('file', file)
    importProgress.value = 0
    const res = await importAccounting(fd, (progressEvent) => {
      if (progressEvent.total) {
        importProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      }
    })
    const msg = res.data?.errors?.length
      ? `导入完成：成功 ${res.data.success} 条，失败 ${res.data.errors.length} 条`
      : `导入成功 ${res.data?.success || 0} 条`
    ElMessage.success(msg)
    loadData()
  } catch (e) {
    const detail = e.response?.data?.error || e.response?.data?.message || e.message || '导入失败'
    ElMessage.error(detail)
  } finally {
    setTimeout(() => { importProgress.value = 0 }, 1500)
  }
  return false
}

async function handleExport() {
  try {
    exportProgress.value = 0
    const params = {
      item: query.item,
      category: query.category,
      startDate: query.dateRange && query.dateRange[0],
      endDate: query.dateRange && query.dateRange[1]
    }
    const res = await exportAccounting(params, (progressEvent) => {
      if (progressEvent.total) {
        exportProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      }
    })
    const blob = res.data
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = '记账数据_' + new Date().getTime() + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    const detail = e.response?.data?.error || e.message || '导出失败'
    ElMessage.error(detail)
  } finally {
    setTimeout(() => { exportProgress.value = 0 }, 1500)
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
  } catch (e) {
    const detail = e.response?.data?.error || e.message || '模板下载失败'
    ElMessage.error(detail)
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
.pagination-container { margin-top: 12px; }
.text-income { color: #67c23a; font-weight: 600; }
.text-expense { color: #f56c6c; font-weight: 600; }
</style>
