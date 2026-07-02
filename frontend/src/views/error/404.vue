<template>
  <div class="not-found">
    <div class="content">
      <div class="code">404</div>
      <div class="desc">抱歉，您访问的页面不存在</div>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.not-found {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.content {
  text-align: center;
}
.code {
  font-size: 120px;
  font-weight: 700;
  color: #409eff;
  line-height: 1;
}
.desc {
  font-size: 18px;
  color: #606266;
  margin: 16px 0 24px;
}
</style>
