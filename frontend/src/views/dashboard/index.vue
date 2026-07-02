<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-body">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ card.title }}</div>
              <div class="stat-value">{{ card.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷入口</span>
            </div>
          </template>
          <div class="quick-entry">
            <div
              v-for="q in quickEntries"
              :key="q.path"
              class="entry-item"
              @click="router.push(q.path)"
            >
              <el-icon :color="q.color"><component :is="q.icon" /></el-icon>
              <span>{{ q.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>系统信息</span>
            </div>
          </template>
          <ul class="sys-info">
            <li><span>当前用户</span><span>{{ nickname }}</span></li>
            <li><span>系统版本</span><span>v1.0.0</span></li>
            <li><span>前端框架</span><span>Vue 3 + Element Plus</span></li>
            <li><span>构建工具</span><span>Vite 5</span></li>
            <li><span>状态管理</span><span>Pinia</span></li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const nickname = computed(
  () => userStore.userInfo.nickname || userStore.userInfo.username || '用户'
)

const statCards = [
  { title: '组织总数', value: '12', icon: 'OfficeBuilding', color: '#409eff' },
  { title: '人员总数', value: '128', icon: 'User', color: '#67c23a' },
  { title: '角色总数', value: '8', icon: 'UserFilled', color: '#e6a23c' },
  { title: '菜单总数', value: '36', icon: 'Menu', color: '#f56c6c' }
]

const quickEntries = [
  { title: '组织管理', path: '/system/org', icon: 'OfficeBuilding', color: '#409eff' },
  { title: '人员管理', path: '/system/user', icon: 'User', color: '#67c23a' },
  { title: '角色管理', path: '/system/role', icon: 'UserFilled', color: '#e6a23c' },
  { title: '菜单管理', path: '/system/menu', icon: 'Menu', color: '#f56c6c' },
  { title: '记账管理', path: '/finance/accounting', icon: 'Wallet', color: '#909399' },
  { title: '待办事项', path: '/tools/todo', icon: 'List', color: '#9c27b0' },
  { title: '备忘录', path: '/tools/notes', icon: 'Notebook', color: '#00bcd4' },
  { title: '统计报表', path: '/finance/stats', icon: 'TrendCharts', color: '#ff5722' },
  { title: '数据库查询', path: '/tools/db-query', icon: 'Connection', color: '#1890ff' }
]
</script>

<style scoped>
.stat-card {
  border-radius: 8px;
}
.stat-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
}
.stat-title {
  font-size: 13px;
  color: #909399;
}
.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  margin-top: 4px;
}
.card-header {
  font-weight: 600;
}
.quick-entry {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 0;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  font-size: 13px;
  color: #606266;
}
.entry-item:hover {
  background: #f5f7fa;
}
.entry-item .el-icon {
  font-size: 26px;
}
.sys-info {
  list-style: none;
  margin: 0;
  padding: 0;
}
.sys-info li {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
  font-size: 13px;
  color: #606266;
}
.sys-info li:last-child {
  border-bottom: none;
}
</style>
