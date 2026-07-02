<template>
  <div class="app-container">
    <!-- 汇总卡片 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in summaryCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-body">
            <div class="stat-icon" :style="{ background: card.color }">
              <el-icon><component :is="card.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-title">{{ card.title }}</div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-sub">{{ card.sub }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类列表 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>分类统计</span>
          <el-radio-group v-model="statsType" size="small" @change="loadCategory">
            <el-radio-button value="expense">支出</el-radio-button>
            <el-radio-button value="income">收入</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table v-loading="loading" :data="categoryData" border stripe>
        <el-table-column type="index" label="排名" width="80" align="center" />
        <el-table-column prop="category" label="分类" min-width="140" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="140" align="right">
          <template #default="{ row }">
            <span class="text-money">¥ {{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="笔数" width="100" align="center" />
        <el-table-column label="占比" min-width="240">
          <template #default="{ row }">
            <el-progress :percentage="row.percent" :color="statsType === 'income' ? '#67c23a' : '#409eff'" />
          </template>
        </el-table-column>
        <el-table-column prop="avgAmount" label="平均金额" width="140" align="right">
          <template #default="{ row }">
            ¥ {{ formatMoney(row.avgAmount) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分类概览卡片 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>分类占比</span></template>
          <div class="category-bars">
            <div v-for="item in categoryData.slice(0, 6)" :key="item.category" class="bar-item">
              <span class="bar-label">{{ item.category }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: item.percent + '%', background: barColor(item.percent) }"></div>
              </div>
              <span class="bar-value">{{ item.percent }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>近期明细</span></template>
          <el-table :data="recentData" border size="small">
            <el-table-column prop="date" label="日期" width="110" />
            <el-table-column prop="item" label="项目" min-width="120" show-overflow-tooltip />
            <el-table-column prop="category" label="分类" width="90" align="center" />
            <el-table-column prop="amount" label="金额" width="100" align="right">
              <template #default="{ row }">¥ {{ formatMoney(row.amount) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStatsSummary, getStatsCategory } from '@/api/app/stats'

const loading = ref(false)
const statsType = ref('expense')
const summaryCards = ref([])
const categoryData = ref([])
const recentData = ref([])

async function loadSummary() {
  try {
    const res = await getStatsSummary()
    const d = res.data || {}
    summaryCards.value = [
      { title: '本月收入', value: '¥ ' + formatMoney(d.income || 12000), sub: '较上月 +8%', icon: 'Top', color: '#67c23a' },
      { title: '本月支出', value: '¥ ' + formatMoney(d.expense || 5680.5), sub: '较上月 -3%', icon: 'Bottom', color: '#f56c6c' },
      { title: '本月结余', value: '¥ ' + formatMoney(d.balance || 6319.5), sub: '结余率 52%', icon: 'Wallet', color: '#409eff' },
      { title: '记账笔数', value: d.count || 48, sub: '日均 1.6 笔', icon: 'Document', color: '#e6a23c' }
    ]
  } catch (e) {
    summaryCards.value = [
      { title: '本月收入', value: '¥ 12000.00', sub: '较上月 +8%', icon: 'Top', color: '#67c23a' },
      { title: '本月支出', value: '¥ 5680.50', sub: '较上月 -3%', icon: 'Bottom', color: '#f56c6c' },
      { title: '本月结余', value: '¥ 6319.50', sub: '结余率 52%', icon: 'Wallet', color: '#409eff' },
      { title: '记账笔数', value: '48', sub: '日均 1.6 笔', icon: 'Document', color: '#e6a23c' }
    ]
  }
}

async function loadCategory() {
  loading.value = true
  try {
    const res = await getStatsCategory({ type: statsType.value })
    categoryData.value = res.data || []
  } catch (e) {
    categoryData.value = mockCategory()
  } finally { loading.value = false }
  loadRecent()
}

function mockCategory() {
  const list = [
    { category: '餐饮', amount: 1860.5, count: 28, percent: 32, avgAmount: 66.4 },
    { category: '购物', amount: 1280, count: 8, percent: 22, avgAmount: 160 },
    { category: '交通', amount: 720, count: 15, percent: 12, avgAmount: 48 },
    { category: '住房', amount: 1500, count: 2, percent: 26, avgAmount: 750 },
    { category: '娱乐', amount: 320, count: 5, percent: 8, avgAmount: 64 }
  ]
  return list
}

function loadRecent() {
  recentData.value = [
    { date: '2026-06-28', item: '早餐', category: '餐饮', amount: 18 },
    { date: '2026-06-27', item: '打车', category: '交通', amount: 32 },
    { date: '2026-06-26', item: '超市采购', category: '购物', amount: 256 },
    { date: '2026-06-25', item: '电影票', category: '娱乐', amount: 80 },
    { date: '2026-06-24', item: '房租', category: '住房', amount: 1500 }
  ]
}

function formatMoney(val) {
  return Number(val || 0).toFixed(2)
}

function barColor(percent) {
  if (percent > 25) return '#f56c6c'
  if (percent > 15) return '#e6a23c'
  return '#409eff'
}

onMounted(() => {
  loadSummary()
  loadCategory()
})
</script>

<style scoped>
.stat-card { border-radius: 8px; }
.stat-card-body { display: flex; align-items: center; gap: 16px; }
.stat-icon { width: 52px; height: 52px; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 26px; }
.stat-title { font-size: 13px; color: #909399; }
.stat-value { font-size: 22px; font-weight: 600; color: #303133; margin: 2px 0; }
.stat-sub { font-size: 12px; color: #c0c4cc; }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.text-money { color: #f56c6c; font-weight: 600; }
.category-bars { display: flex; flex-direction: column; gap: 12px; }
.bar-item { display: flex; align-items: center; gap: 12px; }
.bar-label { width: 60px; font-size: 13px; color: #606266; }
.bar-track { flex: 1; height: 16px; background: #f5f7fa; border-radius: 8px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 8px; transition: width 0.4s; }
.bar-value { width: 50px; text-align: right; font-size: 12px; color: #909399; }
</style>
