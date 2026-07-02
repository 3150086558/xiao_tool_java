<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-banner">
        <div class="banner-content">
          <el-icon class="banner-icon"><Platform /></el-icon>
          <h1>小肖的自用工具</h1>
          <p>Xiao's Personal Tools</p>
          <p class="sub">组织管理、记账统计等个人工具集</p>
        </div>
      </div>
      <div class="login-form-wrapper">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
          @keyup.enter="handleLogin"
        >
          <h2 class="title">欢迎登录</h2>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
          <div class="tips">
            <span>默认账号：admin / admin123</span>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
      ? decodeURIComponent(route.query.redirect)
      : '/'
    router.push(redirect)
  } catch (e) {
    console.error('登录失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2a48 0%, #2c3e6e 100%);
}
.login-box {
  display: flex;
  width: 880px;
  height: 480px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}
.login-banner {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #1d6fd1 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.banner-content {
  text-align: center;
}
.banner-icon {
  font-size: 64px;
  margin-bottom: 20px;
}
.banner-content h1 {
  font-size: 26px;
  margin: 0 0 12px;
}
.banner-content p {
  margin: 6px 0;
  font-size: 14px;
  opacity: 0.9;
}
.banner-content .sub {
  margin-top: 18px;
  font-size: 13px;
  opacity: 0.75;
  line-height: 1.6;
}
.login-form-wrapper {
  width: 380px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.login-form {
  width: 100%;
}
.title {
  text-align: center;
  font-size: 22px;
  color: #303133;
  margin: 0 0 30px;
}
.login-btn {
  width: 100%;
}
.tips {
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>
