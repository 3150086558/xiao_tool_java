<template>
  <div class="sidebar">
    <div class="logo">
      <el-icon class="logo-icon"><Platform /></el-icon>
      <span v-show="!collapse" class="logo-title">小肖的自用工具</span>
    </div>
    <el-scrollbar class="menu-scroll">
      <el-menu
        :default-active="activeMenu"
        :collapse="collapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <SidebarItem
          v-for="route in menuRoutes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
        />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '@/store/permission'
import { useUserStore } from '@/store/user'
import SidebarItem from './SidebarItem.vue'

defineProps({
  collapse: { type: Boolean, default: false }
})

const route = useRoute()
const permissionStore = usePermissionStore()
const userStore = useUserStore()

// 侧栏路由：优先用 permission store 的 sidebarRoutes，回退到用户菜单转换
const menuRoutes = computed(() => {
  if (permissionStore.sidebarRoutes && permissionStore.sidebarRoutes.length) {
    return permissionStore.sidebarRoutes.filter((r) => !r.hidden)
  }
  return []
})

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta && meta.activeMenu) return meta.activeMenu
  return path
})

void userStore
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: #2b2f3a;
  color: #fff;
  overflow: hidden;
}
.logo-icon {
  font-size: 22px;
  color: #409eff;
}
.logo-title {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}
.menu-scroll {
  flex: 1;
}
.menu-scroll :deep(.el-menu) {
  border-right: none;
}
</style>
