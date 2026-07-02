<template>
  <el-container class="app-wrapper">
    <!-- 侧栏 -->
    <el-aside :width="sidebarWidth" class="sidebar-container">
      <Sidebar :collapse="isCollapse" />
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部 -->
      <el-header class="navbar">
        <div class="navbar-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <Breadcrumb />
        </div>
        <div class="navbar-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="avatar-wrapper">
              <el-avatar :size="32" :src="avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ nickname }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 标签页导航 -->
      <TagsView />

      <!-- 内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="submitPwd">确认</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Sidebar from './Sidebar.vue'
import TagsView from './TagsView.vue'
import Breadcrumb from './Breadcrumb.vue'
import { useUserStore } from '@/store/user'
import { changePassword } from '@/api/system/auth'
import { resetRouter } from '@/router'

const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const sidebarWidth = computed(() => (isCollapse.value ? '64px' : '210px'))

function toggleSidebar() {
  isCollapse.value = !isCollapse.value
}

const nickname = computed(() => userStore.userInfo.nickname || userStore.userInfo.username || '用户')
const avatar = computed(() => userStore.userInfo.avatar || '')

// 下拉菜单
function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'password') {
    pwdVisible.value = true
  } else if (cmd === 'logout') {
    handleLogout()
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    resetRouter()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (e) {
    // 取消
  }
}

// 修改密码
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdFormRef = ref()
const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        if (value !== pwdForm.value.newPassword) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

async function submitPwd() {
  await pwdFormRef.value.validate()
  pwdLoading.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    pwdVisible.value = false
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
    await userStore.logout()
    resetRouter()
    router.push('/login')
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.app-wrapper {
  height: 100%;
}
.sidebar-container {
  background: #304156;
  transition: width 0.28s;
  overflow: hidden;
}
.main-container {
  height: 100%;
  overflow: hidden;
}
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 50px;
}
.navbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #5a5e66;
}
.navbar-right {
  display: flex;
  align-items: center;
}
.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}
.username {
  font-size: 14px;
  color: #333;
}
.app-main {
  background: #f0f2f5;
  overflow-y: auto;
  padding: 12px;
}
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
