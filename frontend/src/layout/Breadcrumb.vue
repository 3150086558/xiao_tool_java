<template>
  <el-breadcrumb class="breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item
        v-for="(item, index) in levelList"
        :key="item.path"
      >
        <span v-if="index === levelList.length - 1" class="no-redirect">
          {{ item.meta.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const levelList = ref([])

function getBreadcrumb() {
  const matched = route.matched.filter((item) => item.meta && item.meta.title)
  levelList.value = matched
}

watch(
  () => route.path,
  () => getBreadcrumb(),
  { immediate: true }
)

function handleLink(item) {
  router.push(item.redirect || item.path)
}
</script>

<style scoped>
.breadcrumb {
  display: inline-flex;
  font-size: 14px;
  line-height: 50px;
}
.no-redirect {
  color: #97a8be;
  cursor: text;
}
.breadcrumb a {
  cursor: pointer;
  color: #606266;
}
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}
.breadcrumb-enter-from,
.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
