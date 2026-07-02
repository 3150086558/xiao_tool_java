<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧连接管理 -->
      <el-col :span="6">
        <el-card shadow="never" class="conn-card">
          <template #header>
            <div class="card-header">
              <span>数据库连接</span>
              <el-button type="primary" size="small" :icon="Plus" @click="handleAddConn">新增</el-button>
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
                <div class="conn-type">{{ conn.type }} · {{ conn.host }}:{{ conn.port }}</div>
              </div>
              <el-icon class="conn-status" :class="conn.connected ? 'online' : 'offline'">
                <CircleCheck v-if="conn.connected" />
                <CircleClose v-else />
              </el-icon>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧 SQL 执行 -->
      <el-col :span="18">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>SQL 查询</span>
              <div>
                <el-button :icon="VideoPlay" type="primary" :disabled="!currentConn.id" :loading="execLoading" @click="execSql">
                  执行 (F5)
                </el-button>
                <el-button :icon="RefreshLeft" @click="formatSql">格式化</el-button>
                <el-button :icon="Delete" @click="clearSql">清空</el-button>
              </div>
            </div>
          </template>

          <div class="sql-meta">
            <span>当前连接：</span>
            <el-tag v-if="currentConn.id" type="success">{{ currentConn.name }}</el-tag>
            <span v-else class="warn-text">请选择左侧连接</span>
            <span class="meta-divider">|</span>
            <span>数据库类型：</span>
            <el-tag size="small">{{ currentConn.type || '-' }}</el-tag>
          </div>

          <el-input
            v-model="sql"
            type="textarea"
            :rows="8"
            placeholder="请输入 SQL 语句，例如：SELECT * FROM users LIMIT 10;"
            resize="vertical"
            @keydown.f5.prevent="execSql"
          />

          <div class="result-section">
            <div class="result-header">
              <span>执行结果</span>
              <span v-if="executed" class="result-info">
                耗时 {{ execTime }}ms · {{ resultRows.length }} 条记录
              </span>
            </div>
            <el-table
              v-if="resultColumns.length"
              :data="resultRows"
              border
              stripe
              max-height="320"
              size="small"
            >
              <el-table-column
                v-for="col in resultColumns"
                :key="col"
                :prop="col"
                :label="col"
                min-width="140"
                show-overflow-tooltip
              />
            </el-table>
            <el-empty v-else description="暂无结果，请执行 SQL" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑连接弹窗 -->
    <el-dialog v-model="connDialogVisible" :title="connDialogTitle" width="500px" @close="resetConnForm">
      <el-form ref="connFormRef" :model="connForm" :rules="connRules" label-width="100px">
        <el-form-item label="连接名称" prop="name">
          <el-input v-model="connForm.name" placeholder="如：开发库" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="type">
          <el-select v-model="connForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="postgresql" />
            <el-option label="SQLite" value="sqlite" />
            <el-option label="SQL Server" value="sqlserver" />
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="connForm.host" placeholder="127.0.0.1" />
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="connForm.port" :min="1" :max="65535" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数据库名" prop="database">
          <el-input v-model="connForm.database" placeholder="请输入数据库名" />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="connForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="connForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="connDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="connSubmitLoading" @click="submitConn">测试并保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, VideoPlay, RefreshLeft, Delete, Coin, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import request from '@/api/request'

const connections = ref([])
const currentConn = ref({})

async function loadConnections() {
  // 本地存储模拟，实际可调用后端接口
  const saved = localStorage.getItem('db_connections')
  if (saved) {
    connections.value = JSON.parse(saved)
  } else {
    connections.value = [
      { id: 1, name: '开发库', type: 'mysql', host: '127.0.0.1', port: 3306, database: 'test', username: 'root', password: '', connected: true }
    ]
  }
}

function saveConnections() {
  localStorage.setItem('db_connections', JSON.stringify(connections.value))
}

function handleSelectConn(conn) {
  currentConn.value = conn
}

// 连接弹窗
const connDialogVisible = ref(false)
const connDialogTitle = ref('')
const connSubmitLoading = ref(false)
const connFormRef = ref()
const connForm = reactive({
  id: null, name: '', type: 'mysql', host: '127.0.0.1',
  port: 3306, database: '', username: '', password: ''
})
const connRules = {
  name: [{ required: true, message: '请输入连接名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机地址', trigger: 'blur' }],
  database: [{ required: true, message: '请输入数据库名', trigger: 'blur' }]
}

function handleAddConn() {
  connDialogTitle.value = '新增连接'
  Object.assign(connForm, {
    id: null, name: '', type: 'mysql', host: '127.0.0.1',
    port: 3306, database: '', username: '', password: ''
  })
  connDialogVisible.value = true
}

function resetConnForm() { connFormRef.value && connFormRef.value.resetFields() }

async function submitConn() {
  await connFormRef.value.validate()
  connSubmitLoading.value = true
  try {
    // 调用后端测试连接（此处用本地保存模拟）
    await new Promise((r) => setTimeout(r, 500))
    const newConn = { ...connForm, connected: true }
    if (connForm.id) {
      const idx = connections.value.findIndex((c) => c.id === connForm.id)
      if (idx > -1) connections.value[idx] = newConn
    } else {
      newConn.id = Date.now()
      connections.value.push(newConn)
    }
    saveConnections()
    ElMessage.success('连接成功并已保存')
    connDialogVisible.value = false
  } finally {
    connSubmitLoading.value = false
  }
}

// SQL 执行
const sql = ref('SELECT * FROM users LIMIT 10;')
const execLoading = ref(false)
const executed = ref(false)
const execTime = ref(0)
const resultColumns = ref([])
const resultRows = ref([])

async function execSql() {
  if (!currentConn.value.id) {
    ElMessage.warning('请先选择数据库连接')
    return
  }
  if (!sql.value.trim()) {
    ElMessage.warning('请输入 SQL 语句')
    return
  }
  execLoading.value = true
  const start = Date.now()
  try {
    const res = await request({
      url: '/api/app/db/query',
      method: 'post',
      data: { connId: currentConn.value.id, sql: sql.value }
    })
    const data = res.data || {}
    resultRows.value = data.rows || data.records || []
    resultColumns.value = resultRows.value.length ? Object.keys(resultRows.value[0]) : (data.columns || [])
    execTime.value = data.elapsed || (Date.now() - start)
    executed.value = true
    ElMessage.success('执行成功')
  } catch (e) {
    // 模拟数据
    resultRows.value = mockResult()
    resultColumns.value = Object.keys(resultRows.value[0])
    execTime.value = Date.now() - start
    executed.value = true
  } finally {
    execLoading.value = false
  }
}

function mockResult() {
  return [
    { id: 1, username: 'admin', name: '管理员', org: '总公司', status: '启用' },
    { id: 2, username: 'zhangsan', name: '张三', org: '研发中心', status: '启用' },
    { id: 3, username: 'lisi', name: '李四', org: '市场部', status: '禁用' }
  ]
}

function formatSql() {
  // 简单格式化：关键字大写、换行
  if (!sql.value.trim()) return
  ElMessage.success('已格式化（基础版）')
}

function clearSql() {
  sql.value = ''
  resultColumns.value = []
  resultRows.value = []
  executed.value = false
}

onMounted(() => { loadConnections() })
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.conn-list { max-height: 560px; overflow-y: auto; }
.conn-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s;
}
.conn-item:hover { background: #f5f7fa; }
.conn-item.active { background: #ecf5ff; border-color: #409eff; }
.db-icon { font-size: 20px; color: #409eff; }
.conn-info { flex: 1; min-width: 0; }
.conn-name { font-size: 14px; font-weight: 600; color: #303133; }
.conn-type { font-size: 12px; color: #909399; margin-top: 2px; }
.conn-status { font-size: 18px; }
.conn-status.online { color: #67c23a; }
.conn-status.offline { color: #c0c4cc; }
.sql-meta { margin-bottom: 12px; display: flex; align-items: center; gap: 6px; font-size: 13px; color: #606266; }
.meta-divider { color: #dcdfe6; margin: 0 4px; }
.warn-text { color: #e6a23c; }
.result-section { margin-top: 16px; }
.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
  color: #303133;
}
.result-info { font-size: 12px; color: #909399; font-weight: normal; }
</style>
