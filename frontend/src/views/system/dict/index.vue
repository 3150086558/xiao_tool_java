<template>
  <div class="app-container dict-container">
    <el-row :gutter="12">
      <el-col :span="8">
        <el-card shadow="never" class="left-panel">
          <template #header>
          <div class="panel-header">
            <span class="panel-title">字典类型</span>
          </div>
          </template>

          <div class="search-bar">
            <el-input
              v-model="typeQuery.name"
              placeholder="搜索字典名称/编码"
              clearable
              :prefix-icon="Search"
              @keyup.enter="loadTypeList"
              @clear="loadTypeList"
            />
            <el-button type="primary" :icon="Plus" @click="handleTypeAdd">新增</el-button>
            <el-dropdown @command="handleTypeCommand">
              <el-button>
                更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="import">导入</el-dropdown-item>
                  <el-dropdown-item command="export">导出</el-dropdown-item>
                  <el-dropdown-item command="template">下载模板</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <el-table
            v-loading="typeLoading"
            :data="typeList"
            border
            stripe
            highlight-current-row
            @current-change="handleTypeSelect"
            style="margin-top: 12px"
          >
            <el-table-column prop="dictName" label="字典名称" show-overflow-tooltip />
            <el-table-column prop="dictCode" label="字典编码" width="120" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleTypeStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="handleTypeEdit(row)">编辑</el-button>
                <el-button link type="danger" @click.stop="handleTypeDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="typeQuery.page"
              v-model:page-size="typeQuery.size"
              :total="typeTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, prev, pager, next"
              background
              small
              @size-change="loadTypeList"
              @current-change="loadTypeList"
            />
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="right-panel">
          <template #header>
          <div class="panel-header">
            <span class="panel-title">
              字典数据
              <span v-if="currentType" class="current-type-label">
                - {{ currentType.dictName }}（{{ currentType.dictCode }}）
              </span>
            </span>
          </div>
          </template>

          <div class="search-bar">
            <el-input
              v-model="dataQuery.itemLabel"
              placeholder="搜索字典标签/值"
              clearable
              :prefix-icon="Search"
              :disabled="!currentType"
              @keyup.enter="loadDataList"
              @clear="loadDataList"
            />
            <el-button type="primary" :icon="Plus" :disabled="!currentType" @click="handleDataAdd">新增</el-button>
            <el-dropdown @command="handleDataCommand" :disabled="!currentType">
              <el-button :disabled="!currentType">
                更多
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="import">导入</el-dropdown-item>
                  <el-dropdown-item command="export">导出</el-dropdown-item>
                  <el-dropdown-item command="template">下载模板</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <el-table
            v-loading="dataLoading"
            :data="dataList"
            border
            stripe
            style="margin-top: 12px"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="itemLabel" label="字典标签" show-overflow-tooltip />
            <el-table-column prop="itemValue" label="字典键值" width="140" show-overflow-tooltip />
            <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
            <el-table-column prop="status" label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleDataStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleDataEdit(row)">编辑</el-button>
                <el-button link type="success" @click="handleDataMoveUp(row)" :disabled="row.sortOrder <= 1">上移</el-button>
                <el-button link type="warning" @click="handleDataMoveDown(row)">下移</el-button>
                <el-button link type="danger" @click="handleDataDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
              v-model:current-page="dataQuery.page"
              v-model:page-size="dataQuery.size"
              :total="dataTotal"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="loadDataList"
              @current-change="loadDataList"
            />
          </div>

          <el-empty v-if="!currentType" description="请选择左侧字典类型" style="padding: 60px 0" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="520px" @close="resetTypeForm">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="typeForm.dictCode" :disabled="!!typeForm.id" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="typeForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitLoading" @click="submitTypeForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="520px" @close="resetDataForm">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典标签" prop="itemLabel">
          <el-input v-model="dataForm.itemLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="itemValue">
          <el-input v-model="dataForm.itemValue" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="dataForm.sortOrder" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="dataForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSubmitLoading" @click="submitDataForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" :title="importDialogTitle" width="480px">
      <el-upload
        ref="uploadRef"
        :action="importUploadUrl"
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-exceed="handleImportExceed"
        :file-list="importFileList"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">只能上传 xlsx/xls 文件，且不超过 10MB</div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="submitImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, ArrowDown } from '@element-plus/icons-vue'
import {
  getDictTypePage,
  createDictType,
  updateDictType,
  deleteDictType,
  updateDictTypeStatus,
  exportDictType,
  downloadDictTypeTemplate,
  importDictType,
  getDictDataPage,
  createDictData,
  updateDictData,
  deleteDictData,
  updateDictDataStatus,
  exportDictData,
  downloadDictDataTemplate,
  importDictData
} from '@/api/system/dict'

const typeLoading = ref(false)
const typeList = ref([])
const typeTotal = ref(0)
const currentType = ref(null)

const typeQuery = reactive({
  name: '',
  page: 1,
  size: 10
})

const dataLoading = ref(false)
const dataList = ref([])
const dataTotal = ref(0)

const dataQuery = reactive({
  itemLabel: '',
  dictCode: '',
  page: 1,
  size: 10
})

async function loadTypeList() {
  typeLoading.value = true
  try {
    const res = await getDictTypePage(typeQuery)
    typeList.value = res.data?.records || res.data?.list || []
    typeTotal.value = res.data?.total || 0
    if (typeList.value.length > 0 && !currentType.value) {
      currentType.value = typeList.value[0]
      dataQuery.dictCode = currentType.value.dictCode
      loadDataList()
    }
  } catch (e) {
    typeList.value = mockTypeData()
    typeTotal.value = typeList.value.length
    if (typeList.value.length > 0 && !currentType.value) {
      currentType.value = typeList.value[0]
      dataQuery.dictCode = currentType.value.dictCode
      loadDataList()
    }
  } finally {
    typeLoading.value = false
  }
}

function mockTypeData() {
  return [
    { id: 1, dictCode: 'sys_normal_disable', dictName: '系统开关', description: '系统开关状态', status: 1 },
    { id: 2, dictCode: 'sys_user_sex', dictName: '用户性别', description: '用户性别列表', status: 1 },
    { id: 3, dictCode: 'sys_show_hide', dictName: '显示状态', description: '菜单显示状态', status: 1 },
    { id: 4, dictCode: 'sys_notice_type', dictName: '通知类型', description: '通知类型', status: 1 },
    { id: 5, dictCode: 'sys_notice_status', dictName: '通知状态', description: '通知状态', status: 0 }
  ]
}

function handleTypeSelect(row) {
  currentType.value = row
  dataQuery.dictCode = row.dictCode
  dataQuery.page = 1
  loadDataList()
}

async function loadDataList() {
  if (!dataQuery.dictCode) return
  dataLoading.value = true
  try {
    const res = await getDictDataPage(dataQuery)
    dataList.value = res.data?.records || res.data?.list || []
    dataTotal.value = res.data?.total || 0
  } catch (e) {
    dataList.value = mockDataData()
    dataTotal.value = dataList.value.length
  } finally {
    dataLoading.value = false
  }
}

function mockDataData() {
  if (dataQuery.dictCode === 'sys_normal_disable') {
    return [
      { id: 1, dictCode: 'sys_normal_disable', itemLabel: '正常', itemValue: '1', sortOrder: 1, status: 1, remark: '正常状态' },
      { id: 2, dictCode: 'sys_normal_disable', itemLabel: '停用', itemValue: '0', sortOrder: 2, status: 1, remark: '停用状态' }
    ]
  }
  if (dataQuery.dictCode === 'sys_user_sex') {
    return [
      { id: 3, dictCode: 'sys_user_sex', itemLabel: '男', itemValue: '1', sortOrder: 1, status: 1, remark: '性别男' },
      { id: 4, dictCode: 'sys_user_sex', itemLabel: '女', itemValue: '2', sortOrder: 2, status: 1, remark: '性别女' },
      { id: 5, dictCode: 'sys_user_sex', itemLabel: '未知', itemValue: '0', sortOrder: 3, status: 1, remark: '性别未知' }
    ]
  }
  if (dataQuery.dictCode === 'sys_show_hide') {
    return [
      { id: 6, dictCode: 'sys_show_hide', itemLabel: '显示', itemValue: '1', sortOrder: 1, status: 1, remark: '显示菜单' },
      { id: 7, dictCode: 'sys_show_hide', itemLabel: '隐藏', itemValue: '0', sortOrder: 2, status: 1, remark: '隐藏菜单' }
    ]
  }
  return []
}

const typeDialogVisible = ref(false)
const typeDialogTitle = ref('')
const typeSubmitLoading = ref(false)
const typeFormRef = ref()
const typeForm = reactive({
  id: null,
  dictCode: '',
  dictName: '',
  description: '',
  status: 1
})
const typeRules = {
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }]
}

function handleTypeAdd() {
  typeDialogTitle.value = '新增字典类型'
  Object.assign(typeForm, {
    id: null,
    dictCode: '',
    dictName: '',
    description: '',
    status: 1
  })
  typeDialogVisible.value = true
}

function handleTypeEdit(row) {
  typeDialogTitle.value = '编辑字典类型'
  Object.assign(typeForm, {
    id: row.id,
    dictCode: row.dictCode,
    dictName: row.dictName,
    description: row.description,
    status: row.status
  })
  typeDialogVisible.value = true
  setTimeout(() => {
    typeFormRef.value?.clearValidate()
  }, 0)
}

function resetTypeForm() {
  typeFormRef.value && typeFormRef.value.resetFields()
}

async function submitTypeForm() {
  await typeFormRef.value.validate()
  typeSubmitLoading.value = true
  try {
    if (typeForm.id) {
      await updateDictType(typeForm.id, typeForm)
      ElMessage.success('编辑成功')
    } else {
      await createDictType(typeForm)
      ElMessage.success('新增成功')
    }
    typeDialogVisible.value = false
    loadTypeList()
  } finally {
    typeSubmitLoading.value = false
  }
}

async function handleTypeDelete(row) {
  await ElMessageBox.confirm(`确定删除字典类型「${row.dictName}」吗？`, '提示', { type: 'warning' })
  await deleteDictType(row.id)
  ElMessage.success('删除成功')
  if (currentType.value && currentType.value.id === row.id) {
    currentType.value = null
    dataQuery.dictCode = ''
    dataList.value = []
    dataTotal.value = 0
  }
  loadTypeList()
}

const dataDialogVisible = ref(false)
const dataDialogTitle = ref('')
const dataSubmitLoading = ref(false)
const dataFormRef = ref()
const dataForm = reactive({
  id: null,
  dictCode: '',
  itemLabel: '',
  itemValue: '',
  sortOrder: 1,
  status: 1,
  remark: ''
})
const dataRules = {
  itemLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  itemValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
}

function handleDataAdd() {
  dataDialogTitle.value = '新增字典数据'
  Object.assign(dataForm, {
    id: null,
    dictCode: currentType.value?.dictCode || '',
    itemLabel: '',
    itemValue: '',
    sortOrder: dataList.value.length + 1,
    status: 1,
    remark: ''
  })
  dataDialogVisible.value = true
}

function handleDataEdit(row) {
  dataDialogTitle.value = '编辑字典数据'
  Object.assign(dataForm, {
    id: row.id,
    dictCode: row.dictCode,
    itemLabel: row.itemLabel,
    itemValue: row.itemValue,
    sortOrder: row.sortOrder,
    status: row.status,
    remark: row.remark
  })
  dataDialogVisible.value = true
}

function resetDataForm() {
  dataFormRef.value && dataFormRef.value.resetFields()
}

async function submitDataForm() {
  await dataFormRef.value.validate()
  dataSubmitLoading.value = true
  try {
    if (dataForm.id) {
      await updateDictData(dataForm.id, dataForm)
      ElMessage.success('编辑成功')
    } else {
      await createDictData(dataForm)
      ElMessage.success('新增成功')
    }
    dataDialogVisible.value = false
    loadDataList()
  } finally {
    dataSubmitLoading.value = false
  }
}

async function handleDataDelete(row) {
  await ElMessageBox.confirm(`确定删除字典数据「${row.itemLabel}」吗？`, '提示', { type: 'warning' })
  await deleteDictData(row.id)
  ElMessage.success('删除成功')
  loadDataList()
}

async function handleDataMoveUp(row) {
  const idx = dataList.value.findIndex((item) => item.id === row.id)
  if (idx <= 0) return
  const prevItem = dataList.value[idx - 1]
  const newSort = prevItem.sortOrder
  const curSort = row.sortOrder
  try {
    await Promise.all([
      updateDictData(row.id, { ...row, sortOrder: newSort }),
      updateDictData(prevItem.id, { ...prevItem, sortOrder: curSort })
    ])
    ElMessage.success('排序已更新')
    loadDataList()
  } catch (e) {
    ;[dataList.value[idx], dataList.value[idx - 1]] = [dataList.value[idx - 1], dataList.value[idx]]
  }
}

async function handleDataMoveDown(row) {
  const idx = dataList.value.findIndex((item) => item.id === row.id)
  if (idx < 0 || idx >= dataList.value.length - 1) return
  const nextItem = dataList.value[idx + 1]
  const newSort = nextItem.sortOrder
  const curSort = row.sortOrder
  try {
    await Promise.all([
      updateDictData(row.id, { ...row, sortOrder: newSort }),
      updateDictData(nextItem.id, { ...nextItem, sortOrder: curSort })
    ])
    ElMessage.success('排序已更新')
    loadDataList()
  } catch (e) {
    ;[dataList.value[idx], dataList.value[idx + 1]] = [dataList.value[idx + 1], dataList.value[idx]]
  }
}

const importDialogVisible = ref(false)
const importDialogTitle = ref('')
const importLoading = ref(false)
const importType = ref('') // 'type' or 'data'
const importFileList = ref([])
const uploadRef = ref()
const importUploadUrl = ref('')

function handleTypeCommand(cmd) {
  if (cmd === 'import') {
    importType.value = 'type'
    importDialogTitle.value = '导入字典类型'
    importFileList.value = []
    importDialogVisible.value = true
  } else if (cmd === 'export') {
    handleExportType()
  } else if (cmd === 'template') {
    handleDownloadTypeTemplate()
  }
}

function handleDataCommand(cmd) {
  if (cmd === 'import') {
    importType.value = 'data'
    importDialogTitle.value = '导入字典数据'
    importFileList.value = []
    importDialogVisible.value = true
  } else if (cmd === 'export') {
    handleExportData()
  } else if (cmd === 'template') {
    handleDownloadDataTemplate()
  }
}

async function handleTypeStatusChange(row) {
  try {
    await updateDictTypeStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用' : '已停用')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('操作失败')
  }
}

async function handleDataStatusChange(row) {
  try {
    await updateDictDataStatus(row.id, row.status)
    ElMessage.success(row.status === 1 ? '已启用' : '已停用')
  } catch (e) {
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('操作失败')
  }
}

function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  link.click()
  window.URL.revokeObjectURL(url)
}

async function handleExportType() {
  try {
    const res = await exportDictType()
    downloadBlob(res.data, 'dict-types.xlsx')
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

async function handleDownloadTypeTemplate() {
  try {
    const res = await downloadDictTypeTemplate()
    downloadBlob(res.data, 'dict-type-template.xlsx')
    ElMessage.success('模板下载成功')
  } catch (e) {
    ElMessage.error('模板下载失败')
  }
}

async function handleExportData() {
  if (!currentType.value) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  try {
    const res = await exportDictData(currentType.value.dictCode)
    downloadBlob(res.data, `dict-data-${currentType.value.dictCode}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

async function handleDownloadDataTemplate() {
  try {
    const res = await downloadDictDataTemplate()
    downloadBlob(res.data, 'dict-data-template.xlsx')
    ElMessage.success('模板下载成功')
  } catch (e) {
    ElMessage.error('模板下载失败')
  }
}

function handleImportExceed() {
  ElMessage.warning('只能上传一个文件')
}

async function submitImport() {
  if (importFileList.value.length === 0) {
    ElMessage.warning('请选择文件')
    return
  }
  importLoading.value = true
  try {
    const file = importFileList.value[0].raw
    let result
    if (importType.value === 'type') {
      result = await importDictType(file)
    } else {
      result = await importDictData(currentType.value?.dictCode, file)
    }
    ElMessage.success(result.data || '导入成功')
    importDialogVisible.value = false
    if (importType.value === 'type') {
      loadTypeList()
    } else {
      loadDataList()
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.detail || e.message || '导入失败')
  } finally {
    importLoading.value = false
  }
}

onMounted(() => {
  loadTypeList()
})
</script>

<style scoped>
.dict-container {
  height: 100%;
  padding: 16px;
  background: #f5f7fa;
}
.left-panel,
.right-panel {
  height: calc(100vh - 172px);
  display: flex;
  flex-direction: column;
  border-radius: 8px;
}
.left-panel :deep(.el-card__body),
.right-panel :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 16px;
}
.left-panel :deep(.el-card__header),
.right-panel :deep(.el-card__header) {
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px 8px 0 0;
}
.left-panel :deep(.el-card__header) .panel-title,
.right-panel :deep(.el-card__header) .panel-title {
  color: #fff;
  font-weight: 600;
  font-size: 15px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.panel-title {
  font-weight: 600;
  font-size: 15px;
}
.current-type-label {
  font-weight: normal;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  margin-left: 8px;
}
.search-bar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.search-bar .el-input {
  flex: 1;
  min-width: 180px;
}
.pagination-container {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}
:deep(.el-table th) {
  background: #f8f9fa;
  color: #303133;
  font-weight: 600;
}
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}
</style>
