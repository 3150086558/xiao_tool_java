<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 左侧列表 -->
      <el-col :span="8">
        <el-card shadow="never" class="note-list-card">
          <template #header>
            <div class="card-header">
              <span>备忘录</span>
              <el-button type="primary" size="small" :icon="Plus" @click="handleAdd">新增</el-button>
            </div>
          </template>
          <el-input v-model="searchText" placeholder="搜索标题" :prefix-icon="Search" clearable style="margin-bottom: 8px" @input="handleSearch" />
          <div v-loading="loading" class="note-list">
            <div v-if="!filteredList.length" class="empty-tip"><el-empty description="暂无备忘" /></div>
            <div
              v-for="item in filteredList"
              :key="item.id"
              class="note-item"
              :class="{ active: currentNote.id === item.id }"
              @click="handleSelect(item)"
            >
              <div class="note-title">{{ item.title || '无标题' }}</div>
              <div class="note-time">{{ item.updateTime || item.createTime }}</div>
              <div class="note-summary">{{ stripHtml(item.content) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧编辑 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ isEditing ? (currentNote.id ? '编辑备忘' : '新增备忘') : '备忘详情' }}</span>
              <div v-if="isEditing || currentNote.id">
                <el-button type="primary" :icon="Check" @click="submitForm">保存</el-button>
                <el-button v-if="currentNote.id" type="danger" :icon="Delete" @click="handleDelete(currentNote)">删除</el-button>
              </div>
            </div>
          </template>
          <div v-if="!currentNote.id && !isEditing" class="empty-tip">
            <el-empty description="请选择或新增一条备忘" />
          </div>
          <el-form v-else :model="currentNote" label-width="0">
            <el-form-item>
              <el-input v-model="currentNote.title" placeholder="请输入标题" size="large" />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="currentNote.content"
                type="textarea"
                :rows="16"
                placeholder="请输入内容..."
                resize="none"
              />
            </el-form-item>
            <el-form-item>
              <el-tag v-for="t in currentNote.tags" :key="t" closable @close="removeTag(t)" style="margin-right: 6px">{{ t }}</el-tag>
              <el-input
                v-if="tagInputVisible"
                ref="tagInputRef"
                v-model="tagValue"
                size="small"
                style="width: 100px"
                @keyup.enter="addTag"
                @blur="addTag"
              />
              <el-button v-else size="small" :icon="Plus" @click="showTagInput">添加标签</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Delete, Check } from '@element-plus/icons-vue'
import {
  getNotePage,
  createNote,
  updateNote,
  deleteNote
} from '@/api/app/note'

const loading = ref(false)
const noteList = ref([])
const searchText = ref('')
const filteredList = ref([])
const isEditing = ref(false)
const currentNote = reactive({
  id: null, title: '', content: '', tags: [], createTime: '', updateTime: ''
})

async function loadData() {
  loading.value = true
  try {
    const res = await getNotePage({ page: 1, size: 999 })
    noteList.value = res.data?.records || []
    filteredList.value = noteList.value
  } catch (e) {
    noteList.value = mockData()
    filteredList.value = noteList.value
  } finally { loading.value = false }
}

function mockData() {
  return [
    { id: 1, title: '项目周会要点', content: '1. 本周完成首页开发\n2. 下周开始接口联调\n3. 周五前提交测试报告', tags: ['工作', '会议'], createTime: '2026-06-01 10:00', updateTime: '2026-06-01 14:00' },
    { id: 2, title: '读书笔记', content: '《代码整洁之道》读书笔记：函数应该短小精悍，只做一件事。', tags: ['学习'], createTime: '2026-06-02 09:00', updateTime: '2026-06-02 09:00' }
  ]
}

function stripHtml(str) {
  if (!str) return ''
  return str.replace(/\n/g, ' ').slice(0, 40)
}

function handleSearch() {
  filteredList.value = noteList.value.filter((n) =>
    (n.title || '').includes(searchText.value)
  )
}

function handleSelect(item) {
  isEditing.value = false
  Object.assign(currentNote, item, { tags: [...(item.tags || [])] })
}

function handleAdd() {
  isEditing.value = true
  Object.assign(currentNote, { id: null, title: '', content: '', tags: [], createTime: '', updateTime: '' })
}

async function submitForm() {
  if (!currentNote.title) {
    ElMessage.warning('请输入标题')
    return
  }
  try {
    const payload = { title: currentNote.title, content: currentNote.content }
    if (currentNote.id) {
      await updateNote(currentNote.id, payload)
      ElMessage.success('保存成功')
    } else {
      const res = await createNote(payload)
      const created = res.data
      if (created && created.id) {
        Object.assign(currentNote, created, { tags: [...(created.tags || [])] })
      }
      ElMessage.success('新增成功')
    }
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除「${row.title || '该备忘'}」吗？`, '提示', { type: 'warning' })
  await deleteNote(row.id)
  ElMessage.success('删除成功')
  isEditing.value = false
  Object.assign(currentNote, { id: null, title: '', content: '', tags: [] })
  loadData()
}

// 标签
const tagInputVisible = ref(false)
const tagValue = ref('')
const tagInputRef = ref()
function showTagInput() {
  tagInputVisible.value = true
  nextTick(() => tagInputRef.value && tagInputRef.value.focus())
}
function addTag() {
  if (tagValue.value && !currentNote.tags.includes(tagValue.value)) {
    currentNote.tags.push(tagValue.value)
  }
  tagInputVisible.value = false
  tagValue.value = ''
}
function removeTag(t) {
  currentNote.tags = currentNote.tags.filter((x) => x !== t)
}

onMounted(() => { loadData() })
</script>

<style scoped>
.card-header { display: flex; align-items: center; justify-content: space-between; }
.note-list { max-height: 600px; overflow-y: auto; }
.note-item {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}
.note-item:hover { background: #f5f7fa; }
.note-item.active { background: #ecf5ff; border-left: 3px solid #409eff; }
.note-title { font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.note-time { font-size: 12px; color: #c0c4cc; margin-bottom: 4px; }
.note-summary { font-size: 12px; color: #909399; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-tip { padding: 40px 0; }
</style>
