<template>
  <div class="app-container db-query-page">
    <el-row :gutter="16">
      <el-col :span="7">
        <el-card shadow="never" class="conn-card">
          <template #header>
            <div class="card-header">
              <span>数据库连接</span>
              <div class="header-actions">
                <el-button type="primary" size="small" :icon="Plus" @click="handleAddConn">新增</el-button>
                <el-button size="small" :disabled="!currentConn.id" @click="handleEditConn">编辑</el-button>
                <el-button size="small" type="danger" :disabled="!currentConn.id" @click="handleDeleteConn">删除</el-button>
              </div>
            </div>
          </template>

          <div class="conn-list">
            <div
              v-for="conn in connections"
              :key="conn.id"
              class="conn-item"
              :class="{ active: currentConn.id === conn.id }"
              @click="handleSelectConn(conn)"
            >
              <el-icon class="db-icon"><Coin /></el-icon>
              <div class="conn-info">
                <div class="conn-name">{{ conn.name }}</div>
                <div class="conn-type">{{ conn.dbTypeLabel }} / {{ connectionSummary(conn) }}</div>
              </div>
              <el-tag :type="conn.connected ? 'success' : 'info'" size="small">
                {{ conn.connected ? '可用' : '未验证' }}
              </el-tag>
            </div>
            <el-empty v-if="!connections.length" description="暂无连接" />
          </div>
        </el-card>

        <el-card v-if="currentConn.id" shadow="never" class="table-card">
          <template #header>
            <div class="card-header">
              <span>数据表</span>
              <el-button size="small" :loading="tableLoading" @click="loadTables(true)">刷新</el-button>
            </div>
          </template>

          <el-input v-model="tableKeyword" placeholder="搜索表名" clearable style="margin-bottom: 8px" />

          <div class="table-list" v-loading="tableLoading">
            <div v-if="!filteredTables.length" class="empty-tables">
              <el-empty description="暂无表数据" :image-size="80" />
            </div>
            <div
              v-for="table in filteredTables"
              :key="table"
              class="table-item"
              :class="{ active: selectedTable === table }"
              @click="selectedTable = table"
              @dblclick="previewTableData(table)"
            >
              <span class="table-name">{{ table }}</span>
              <div class="table-item-actions">
                <el-button link type="primary" @click.stop="showTableSchema(table)">结构</el-button>
                <el-button link @click.stop="previewTableData(table)">数据</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="17">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>数据库查询</span>
              <div class="header-actions">
                <el-button :icon="VideoPlay" type="primary" :disabled="!currentConn.id" :loading="execLoading" @click="execSql">
                  执行 SQL
                </el-button>
                <el-button :disabled="!selectedTable" @click="showTableSchema()">查表结构</el-button>
                <el-button :disabled="!selectedTable" @click="previewTableData()">预览数据</el-button>
                <el-button :icon="RefreshLeft" @click="formatSql">格式化</el-button>
                <el-button :icon="Delete" @click="clearSql">清空</el-button>
              </div>
            </div>
          </template>

          <div class="sql-meta">
            <span>当前连接：</span>
            <el-tag v-if="currentConn.id" type="success">{{ currentConn.name }}</el-tag>
            <span v-else class="warn-text">请先选择左侧连接</span>
            <span class="meta-divider">|</span>
            <span>数据库类型：</span>
            <el-tag size="small">{{ currentConn.dbTypeLabel || '-' }}</el-tag>
            <span class="meta-divider">|</span>
            <span>当前表：</span>
            <el-tag size="small" type="info">{{ selectedTable || '-' }}</el-tag>
          </div>

          <el-input
            v-model="sql"
            type="textarea"
            :rows="9"
            placeholder="请输入 SQL，例如：SELECT * FROM records LIMIT 100;"
            resize="vertical"
            @keydown.f5.prevent="execSql"
          />

          <div class="result-section">
            <div class="result-header">
              <span>{{ resultTitle }}</span>
              <span v-if="executed" class="result-info">耗时 {{ execTime }} ms，{{ resultRows.length }} 条结果</span>
            </div>

            <el-table v-if="resultColumns.length" :data="resultRows" border stripe max-height="420" size="small">
              <el-table-column
                v-for="column in resultColumns"
                :key="column"
                :prop="column"
                :label="column"
                min-width="140"
                show-overflow-tooltip
              />
            </el-table>
            <el-empty v-else description="暂无结果，请执行查询或选择表结构/数据预览" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="connDialogVisible" :title="connDialogTitle" width="520px" @close="resetConnForm">
      <el-form ref="connFormRef" :model="connForm" :rules="connRules" label-width="100px">
        <el-form-item label="连接名称" prop="name">
          <el-input v-model="connForm.name" placeholder="如：开发库" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="type">
          <el-select v-model="connForm.type" placeholder="请选择" style="width: 100%" @change="handleDbTypeChange">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="SQLite" value="sqlite" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="connForm.type !== 'sqlite'" label="主机地址" prop="host">
          <el-input v-model="connForm.host" placeholder="127.0.0.1" />
        </el-form-item>
        <el-form-item v-if="connForm.type !== 'sqlite'" label="端口" prop="port">
          <el-input-number v-model="connForm.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="connForm.type !== 'sqlite'" label="数据库名" prop="database">
          <el-input v-model="connForm.database" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item v-if="connForm.type !== 'sqlite'" label="用户名" prop="username">
          <el-input v-model="connForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="connForm.type !== 'sqlite'" label="密码" prop="password">
          <el-input v-model="connForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="connForm.type === 'sqlite'" label="SQLite 路径" prop="sqlite_path">
          <el-input v-model="connForm.sqlite_path" placeholder="如：D:/data/test.db" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="connDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="connSubmitLoading" @click="submitConn">保存连接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coin, Delete, Plus, RefreshLeft, VideoPlay } from '@element-plus/icons-vue'
import request from '@/api/request'

const connections = ref([])
const currentConn = ref({})
const tables = ref([])
const selectedTable = ref('')
const tableKeyword = ref('')
const tableLoading = ref(false)

const sql = ref('SELECT * FROM records LIMIT 100;')
const execLoading = ref(false)
const executed = ref(false)
const execTime = ref(0)
const resultTitle = ref('查询结果')
const resultColumns = ref([])
const resultRows = ref([])

const connDialogVisible = ref(false)
const connDialogTitle = ref('')
const connSubmitLoading = ref(false)
const connFormRef = ref()
const connForm = reactive({
  id: null,
  name: '',
  type: 'mysql',
  host: '127.0.0.1',
  port: 3306,
  database: '',
  username: '',
  password: '',
  sqlite_path: ''
})

const connRules = {
  name: [{ required: true, message: '请输入连接名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据库类型', trigger: 'change' }]
}

const filteredTables = computed(() => {
  const keyword = tableKeyword.value.trim().toLowerCase()
  if (!keyword) return tables.value
  return tables.value.filter((item) => item.toLowerCase().includes(keyword))
})

function normalizeConnection(row = {}) {
  const dbType = row.db_type || row.type || 'mysql'
  return {
    ...row,
    type: dbType,
    db_type: dbType,
    dbTypeLabel: dbType === 'postgresql' ? 'PostgreSQL' : dbType === 'sqlite' ? 'SQLite' : 'MySQL',
    connected: Boolean(row.connected)
  }
}

function connectionSummary(conn) {
  if ((conn.db_type || conn.type) === 'sqlite') {
    return conn.sqlite_path || '-'
  }
  return `${conn.host || '-'}:${conn.port || '-'} / ${conn.database || '-'}`
}

function buildDbConfig(conn) {
  return {
    db_type: conn.db_type || conn.type,
    host: conn.host || '',
    port: conn.port || 0,
    username: conn.username || '',
    password: conn.password || '',
    database: conn.database || '',
    sqlite_path: conn.sqlite_path || ''
  }
}

function markConnectionStatus(connId, connected) {
  connections.value = connections.value.map((item) => {
    if (item.id === connId) {
      return { ...item, connected }
    }
    return item
  })
  if (currentConn.value.id === connId) {
    currentConn.value = connections.value.find((item) => item.id === connId) || {}
  }
}

async function loadConnections() {
  try {
    const res = await request({ url: '/api/app/db-connections', method: 'get' })
    connections.value = (res.connections || []).map(normalizeConnection)
    if (currentConn.value.id) {
      const selected = connections.value.find((item) => item.id === currentConn.value.id)
      currentConn.value = selected || {}
    }
  } catch (error) {
    connections.value = []
    currentConn.value = {}
    ElMessage.error(error.response?.data?.detail || error.message || '连接列表获取失败')
  }
}

async function loadTables(showMessage = false) {
  if (!currentConn.value.id) return
  tableLoading.value = true
  try {
    const res = await request({
      url: '/api/app/db/query',
      method: 'post',
      data: {
        action: 'tables',
        config: buildDbConfig(currentConn.value)
      }
    })
    tables.value = res.tables || []
    if (!selectedTable.value || !tables.value.includes(selectedTable.value)) {
      selectedTable.value = tables.value[0] || ''
    }
    markConnectionStatus(currentConn.value.id, true)
    if (showMessage) {
      ElMessage.success(`已加载 ${tables.value.length} 张表`)
    }
  } catch (error) {
    tables.value = []
    selectedTable.value = ''
    markConnectionStatus(currentConn.value.id, false)
    ElMessage.error(error.response?.data?.detail || error.message || '表列表获取失败')
  } finally {
    tableLoading.value = false
  }
}

function handleSelectConn(conn) {
  currentConn.value = normalizeConnection(conn)
  tables.value = []
  selectedTable.value = ''
  loadTables(false)
}

function handleDbTypeChange() {
  if (connForm.type === 'sqlite') {
    connForm.host = ''
    connForm.port = 0
    connForm.database = ''
    connForm.username = ''
    connForm.password = ''
  } else if (connForm.type === 'mysql') {
    connForm.port = 3306
  } else if (connForm.type === 'postgresql') {
    connForm.port = 5432
  }
}

function handleAddConn() {
  connDialogTitle.value = '新增连接'
  Object.assign(connForm, {
    id: null,
    name: '',
    type: 'mysql',
    host: '127.0.0.1',
    port: 3306,
    database: '',
    username: '',
    password: '',
    sqlite_path: ''
  })
  connDialogVisible.value = true
}

function handleEditConn() {
  if (!currentConn.value.id) {
    ElMessage.warning('请先选择连接')
    return
  }
  connDialogTitle.value = '编辑连接'
  Object.assign(connForm, {
    id: currentConn.value.id,
    name: currentConn.value.name || '',
    type: currentConn.value.db_type || currentConn.value.type || 'mysql',
    host: currentConn.value.host || '127.0.0.1',
    port: currentConn.value.port || 3306,
    database: currentConn.value.database || '',
    username: currentConn.value.username || '',
    password: '',
    sqlite_path: currentConn.value.sqlite_path || ''
  })
  connDialogVisible.value = true
}

async function handleDeleteConn() {
  if (!currentConn.value.id) return
  try {
    await ElMessageBox.confirm(`确定删除连接“${currentConn.value.name}”吗？`, '提示', { type: 'warning' })
    await request({ url: `/api/app/db-connections/${currentConn.value.id}`, method: 'delete' })
    ElMessage.success('连接已删除')
    currentConn.value = {}
    tables.value = []
    selectedTable.value = ''
    await loadConnections()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.response?.data?.detail || error.message || '删除连接失败')
    }
  }
}

function resetConnForm() {
  connFormRef.value?.resetFields()
}

async function submitConn() {
  await connFormRef.value.validate()
  connSubmitLoading.value = true
  try {
    const payload = {
      name: connForm.name,
      db_type: connForm.type,
      host: connForm.host,
      port: connForm.port,
      username: connForm.username,
      password: connForm.password,
      database: connForm.database,
      sqlite_path: connForm.sqlite_path
    }
    if (connForm.id) {
      await request({ url: `/api/app/db-connections/${connForm.id}`, method: 'put', data: payload })
    } else {
      await request({ url: '/api/app/db-connections', method: 'post', data: payload })
    }
    ElMessage.success('连接保存成功')
    connDialogVisible.value = false
    await loadConnections()
  } catch (error) {
    ElMessage.error(error.response?.data?.detail || error.message || '连接保存失败')
  } finally {
    connSubmitLoading.value = false
  }
}

function quoteIdentifier(table) {
  const dbType = currentConn.value.db_type || currentConn.value.type
  if (dbType === 'mysql') {
    return `\`${String(table).replace(/`/g, '``')}\``
  }
  return `"${String(table).replace(/"/g, '""')}"`
}

function mapRows(columns, rows) {
  return (rows || []).map((row) => {
    if (Array.isArray(row)) {
      return columns.reduce((acc, column, index) => {
        acc[column] = row[index]
        return acc
      }, {})
    }
    return row
  })
}

function resetResult() {
  resultColumns.value = []
  resultRows.value = []
}

async function execSql(showSuccess = true) {
  if (!currentConn.value.id) {
    ElMessage.warning('请先选择数据库连接')
    return
  }
  if (!sql.value.trim()) {
    ElMessage.warning('请输入 SQL')
    return
  }
  execLoading.value = true
  const start = Date.now()
  try {
    const res = await request({
      url: '/api/app/db/query',
      method: 'post',
      data: {
        action: 'query',
        config: buildDbConfig(currentConn.value),
        sql: sql.value.trim()
      }
    })
    resultColumns.value = res.columns || []
    resultRows.value = mapRows(resultColumns.value, res.rows || [])
    execTime.value = Date.now() - start
    resultTitle.value = 'SQL 查询结果'
    executed.value = true
    markConnectionStatus(currentConn.value.id, true)
    if (showSuccess) {
      ElMessage.success('SQL 执行成功')
    }
  } catch (error) {
    resetResult()
    execTime.value = Date.now() - start
    executed.value = true
    markConnectionStatus(currentConn.value.id, false)
    ElMessage.error(error.response?.data?.detail || error.message || 'SQL 执行失败')
  } finally {
    execLoading.value = false
  }
}

async function showTableSchema(table = selectedTable.value) {
  if (!currentConn.value.id || !table) {
    ElMessage.warning('请先选择数据表')
    return
  }
  execLoading.value = true
  const start = Date.now()
  try {
    const res = await request({
      url: '/api/app/db/query',
      method: 'post',
      data: {
        action: 'schema',
        config: buildDbConfig(currentConn.value),
        table
      }
    })
    resultRows.value = (res.columns || []).map((item) => ({
      字段名: item.name,
      数据类型: item.type,
      可空: item.nullable,
      默认值: item.default ?? ''
    }))
    resultColumns.value = ['字段名', '数据类型', '可空', '默认值']
    resultTitle.value = `表结构：${table}`
    execTime.value = Date.now() - start
    executed.value = true
    sql.value = `-- 表结构：${table}`
    markConnectionStatus(currentConn.value.id, true)
  } catch (error) {
    resetResult()
    execTime.value = Date.now() - start
    executed.value = true
    markConnectionStatus(currentConn.value.id, false)
    ElMessage.error(error.response?.data?.detail || error.message || '表结构查询失败')
  } finally {
    execLoading.value = false
  }
}

async function previewTableData(table = selectedTable.value) {
  if (!table) {
    ElMessage.warning('请先选择数据表')
    return
  }
  sql.value = `SELECT * FROM ${quoteIdentifier(table)} LIMIT 100;`
  selectedTable.value = table
  resultTitle.value = `表数据预览：${table}`
  await execSql(false)
}

function formatSql() {
  if (!sql.value.trim()) return
  sql.value = sql.value
    .split('\n')
    .map((line) => line.trimEnd())
    .join('\n')
    .trim()
  ElMessage.success('已格式化')
}

function clearSql() {
  sql.value = ''
  resultTitle.value = '查询结果'
  resetResult()
  executed.value = false
  execTime.value = 0
}

onMounted(loadConnections)
</script>

<style scoped>
.db-query-page :deep(.el-card) {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.conn-list,
.table-list {
  max-height: 320px;
  overflow-y: auto;
}

.conn-item,
.table-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.conn-item:hover,
.table-item:hover {
  background: #f5f7fa;
}

.conn-item.active,
.table-item.active {
  background: #ecf5ff;
  border-color: #409eff;
}

.db-icon {
  font-size: 20px;
  color: #409eff;
}

.conn-info {
  flex: 1;
  min-width: 0;
}

.conn-name,
.table-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.conn-type {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.table-item {
  justify-content: space-between;
}

.table-item-actions {
  display: flex;
  gap: 6px;
}

.empty-tables {
  padding: 12px 0;
}

.sql-meta {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #606266;
}

.meta-divider {
  color: #dcdfe6;
  margin: 0 4px;
}

.warn-text {
  color: #e6a23c;
}

.result-section {
  margin-top: 16px;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #303133;
  font-weight: 600;
}

.result-info {
  font-size: 12px;
  color: #909399;
  font-weight: 400;
}
</style>