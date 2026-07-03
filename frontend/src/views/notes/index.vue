<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never" class="note-list-card">
          <template #header>
            <div class="card-header">
              <span>备忘录</span>
              <el-button type="primary" size="small" :icon="Plus" @click="handleAdd">新增</el-button>
            </div>
          </template>

          <el-input
            v-model="searchText"
            placeholder="搜索标题或内容"
            :prefix-icon="Search"
            clearable
            style="margin-bottom: 8px"
          />

          <div v-loading="loading" class="note-list">
            <div v-if="!filteredList.length" class="empty-tip">
              <el-empty description="暂无备忘录" />
            </div>
            <div
              v-for="item in filteredList"
              :key="item.id"
              class="note-item"
              :class="{ active: currentNote.id === item.id }"
              @click="handleSelect(item)"
            >
              <div class="note-title">{{ item.title || '无标题' }}</div>
              <div class="note-time">{{ item.updateTime || item.createTime }}</div>
              <div class="note-summary">{{ summarize(item.content) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ currentNote.id ? '编辑备忘' : '新增备忘' }}</span>
              <div>
                <el-button type="primary" :icon="Check" :loading="submitLoading" @click="submitForm">保存</el-button>
                <el-button v-if="currentNote.id" type="danger" :icon="Delete" @click="handleDelete(currentNote)">删除</el-button>
              </div>
            </div>
          </template>

          <el-form :model="currentNote" label-width="0">
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
              <el-tag
                v-for="tag in currentNote.tags"
                :key="tag"
                closable
                style="margin-right: 6px"
                @close="removeTag(tag)"
              >
                {{ tag }}
              </el-tag>
              <el-input
                v-if="tagInputVisible"
                ref="tagInputRef"
                v-model="tagValue"
                size="small"
                style="width: 120px"
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
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Delete, Plus, Search } from '@element-plus/icons-vue'
import { createNote, deleteNote, getNotePage, updateNote } from '@/api/app/note'

const loading = ref(false)
const submitLoading = ref(false)
const noteList = ref([])
const searchText = ref('')
const currentNote = reactive({
  id: null,
  title: '',
  content: '',
  tags: [],
  createTime: '',
  updateTime: ''
})

const tagInputVisible = ref(false)
const tagValue = ref('')
const tagInputRef = ref()

const filteredList = computed(() => {
  const keyword = searchText.value.trim()
  if (!keyword) return noteList.value
  return noteList.value.filter((item) => {
    return [item.title, item.content].some((value) => String(value || '').includes(keyword))
  })
})

function normalizeNote(row = {}) {
  return {
    id: row.id ?? null,
    title: row.title || '',
    content: row.content || '',
    tags: Array.isArray(row.tags) ? [...row.tags] : [],
    createTime: row.createTime || row.created_at || '',
    updateTime: row.updateTime || row.updated_at || ''
  }
}

function assignCurrentNote(note = {}) {
  Object.assign(currentNote, normalizeNote(note))
}

function summarize(content) {
  return String(content || '').replace(/\s+/g, ' ').slice(0, 48)
}

async function loadData(targetId = null) {
  loading.value = true
  try {
    const res = await getNotePage({ page: 1, size: 999 })
    noteList.value = (res.data?.records || []).map(normalizeNote)

    if (targetId) {
      const target = noteList.value.find((item) => item.id === targetId)
      if (target) {
        assignCurrentNote(target)
        return
      }
    }

    if (currentNote.id) {
      const selected = noteList.value.find((item) => item.id === currentNote.id)
      if (selected) {
        assignCurrentNote(selected)
        return
      }
    }

    if (noteList.value.length) {
      assignCurrentNote(noteList.value[0])
    } else {
      assignCurrentNote()
    }
  } catch (error) {
    noteList.value = []
    assignCurrentNote()
    ElMessage.error(error.response?.data?.detail || error.message || '备忘录查询失败')
  } finally {
    loading.value = false
  }
}

function handleSelect(item) {
  assignCurrentNote(item)
}

function handleAdd() {
  assignCurrentNote()
  searchText.value = ''
}

async function submitForm() {
  const title = currentNote.title.trim()
  if (!title) {
    ElMessage.warning('请输入标题')
    return
  }
  submitLoading.value = true
  try {
    const payload = {
      title,
      content: currentNote.content || '',
      tags: JSON.stringify(Array.isArray(currentNote.tags) ? currentNote.tags : [])
    }
    let savedId = currentNote.id
    if (currentNote.id) {
      const res = await updateNote(currentNote.id, payload)
      savedId = res.data?.id || currentNote.id
      ElMessage.success('备忘录已保存')
    } else {
      const res = await createNote(payload)
      savedId = res.data?.id || null
      ElMessage.success('备忘录已新增')
    }
    await loadData(savedId)
  } catch (error) {
    ElMessage.error(error.response?.data?.detail || error.message || '备忘录保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除“${row.title || '该备忘录'}”吗？`, '提示', { type: 'warning' })
    await deleteNote(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.response?.data?.detail || error.message || '删除失败')
    }
  }
}

function showTagInput() {
  tagInputVisible.value = true
  nextTick(() => tagInputRef.value?.focus())
}

function addTag() {
  const value = tagValue.value.trim()
  if (value && !currentNote.tags.includes(value)) {
    currentNote.tags.push(value)
  }
  tagInputVisible.value = false
  tagValue.value = ''
}

function removeTag(tag) {
  currentNote.tags = currentNote.tags.filter((item) => item !== tag)
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.note-list {
  max-height: 600px;
  overflow-y: auto;
}

.note-item {
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.note-item:hover {
  background: #f5f7fa;
}

.note-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.note-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.note-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-bottom: 4px;
}

.note-summary {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tip {
  padding: 40px 0;
}
</style>