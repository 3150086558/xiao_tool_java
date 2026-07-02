<template>
  <div class="app-container org-container">
    <el-row :gutter="16">
      <!-- 左侧组织树 -->
      <el-col :span="8">
        <el-card shadow="never" class="tree-card">
          <template #header>
            <div class="card-header">
              <span>组织架构</span>
              <el-button type="primary" size="small" :icon="Plus" @click="handleAdd(null)">
                新增顶级
              </el-button>
            </div>
          </template>
          <div class="filter-container">
            <el-input v-model="filterText" placeholder="搜索组织名称" :prefix-icon="Search" clearable />
          </div>
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="{ label: 'name', children: 'children' }"
            node-key="id"
            highlight-current
            default-expand-all
            :filter-node-method="filterNode"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-icon><OfficeBuilding /></el-icon>
                <span class="node-label">{{ node.label }}</span>
                <span class="node-ops">
                  <el-icon @click.stop="handleAdd(data)" title="新增子组织"><CirclePlus /></el-icon>
                  <el-icon @click.stop="handleEdit(data)" title="编辑"><Edit /></el-icon>
                  <el-icon @click.stop="handleDelete(data)" title="删除"><Delete /></el-icon>
                </span>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>

      <!-- 右侧详情 -->
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <span>组织详情</span>
          </template>
          <el-empty v-if="!currentOrg.id" description="请选择左侧组织查看详情" />
          <el-descriptions v-else :column="2" border>
            <el-descriptions-item label="组织名称">{{ currentOrg.name }}</el-descriptions-item>
            <el-descriptions-item label="组织编码">{{ currentOrg.code }}</el-descriptions-item>
            <el-descriptions-item label="上级组织">{{ currentOrg.parentName || '无' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ currentOrg.sort || 0 }}</el-descriptions-item>
            <el-descriptions-item label="负责人">{{ currentOrg.leader || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentOrg.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentOrg.status === 1 ? 'success' : 'danger'">
                {{ currentOrg.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ currentOrg.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ currentOrg.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级组织" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            check-strictly
            clearable
            placeholder="不选则为顶级组织"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="组织名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入组织名称" />
        </el-form-item>
        <el-form-item label="组织编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入组织编码" />
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, CirclePlus } from '@element-plus/icons-vue'
import {
  getOrgTree,
  createOrg,
  updateOrg,
  deleteOrg
} from '@/api/system/org'

const treeRef = ref()
const treeData = ref([])
const filterText = ref('')
const currentOrg = ref({})

// 过滤
watch(filterText, (val) => {
  treeRef.value && treeRef.value.filter(val)
})
function filterNode(value, data) {
  if (!value) return true
  return (data.name || '').includes(value)
}

// 加载树
async function loadTree() {
  try {
    const res = await getOrgTree()
    treeData.value = res.data || []
  } catch (e) {
    treeData.value = mockTree()
  }
}

function mockTree() {
  return [
    {
      id: 1,
      name: '总公司',
      code: 'HQ',
      leader: '张总',
      phone: '13800000000',
      sort: 1,
      status: 1,
      remark: '集团总部',
      children: [
        { id: 11, name: '研发中心', code: 'RD', leader: '李工', sort: 1, status: 1, parentId: 1 },
        { id: 12, name: '市场部', code: 'MK', leader: '王经理', sort: 2, status: 1, parentId: 1 }
      ]
    }
  ]
}

function handleNodeClick(data) {
  currentOrg.value = data
}

// 树选项（用于上级选择）
const treeOptions = ref([])
function buildOptions() {
  treeOptions.value = treeData.value
}

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  parentId: null,
  name: '',
  code: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 1,
  remark: ''
})
const rules = {
  name: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入组织编码', trigger: 'blur' }]
}

function handleAdd(parent) {
  dialogTitle.value = '新增组织'
  Object.assign(form, {
    id: null,
    parentId: parent ? parent.id : null,
    name: '',
    code: '',
    leader: '',
    phone: '',
    sort: 0,
    status: 1,
    remark: ''
  })
  buildOptions()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogTitle.value = '编辑组织'
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || null,
    name: row.name,
    code: row.code,
    leader: row.leader || '',
    phone: row.phone || '',
    sort: row.sort || 0,
    status: row.status !== undefined ? row.status : 1,
    remark: row.remark || ''
  })
  buildOptions()
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除组织「${row.name}」吗？`, '提示', {
    type: 'warning'
  })
  try {
    await deleteOrg(row.id)
    ElMessage.success('删除成功')
    loadTree()
    if (currentOrg.value.id === row.id) currentOrg.value = {}
  } catch (e) {
    // ignore
  }
}

function resetForm() {
  formRef.value && formRef.value.resetFields()
}

async function submitForm() {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (form.id) {
      await updateOrg(form.id, form)
      ElMessage.success('编辑成功')
    } else {
      await createOrg(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadTree()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadTree()
})
</script>

<style scoped>
.org-container {
  height: 100%;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.tree-card {
  height: 100%;
}
.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
  padding-right: 8px;
}
.tree-node .node-label {
  margin-left: 4px;
  flex: 1;
}
.tree-node .node-ops {
  display: none;
  gap: 6px;
}
.tree-node:hover .node-ops {
  display: inline-flex;
}
.tree-node .node-ops .el-icon:hover {
  color: #409eff;
}
</style>
