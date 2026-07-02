<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span>个人中心</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ userInfo.nickname }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ userInfo.id }}</el-descriptions-item>
        <el-descriptions-item label="头像">
          <el-avatar :size="40" :src="userInfo.avatar">
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
</script>
