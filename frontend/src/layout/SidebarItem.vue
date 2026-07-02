<template>
  <!-- 隐藏项不渲染 -->
  <template v-if="!item.hidden">
    <!-- 只有一个子菜单：直接渲染为单项 -->
    <template
      v-if="hasOneShowingChild(item.children, item) && (!onlyChild.children || !onlyChild.children.length) && !onlyChild.alwaysShow"
    >
      <el-menu-item :index="resolvePath(onlyChild.path)">
        <el-icon v-if="iconOf(onlyChild)"><component :is="iconOf(onlyChild)" /></el-icon>
        <template #title>
          <span>{{ titleOf(onlyChild) }}</span>
        </template>
      </el-menu-item>
    </template>

    <!-- 多子菜单：递归渲染 -->
    <el-sub-menu v-else :index="resolvePath(item.path)">
      <template #title>
        <el-icon v-if="iconOf(item)"><component :is="iconOf(item)" /></el-icon>
        <span>{{ titleOf(item) }}</span>
      </template>
      <SidebarItem
        v-for="child in item.children"
        :key="child.path"
        :item="child"
        :base-path="resolvePath(child.path)"
        :is-nest="true"
      />
    </el-sub-menu>
  </template>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  item: { type: Object, required: true },
  basePath: { type: String, default: '' },
  isNest: { type: Boolean, default: false }
})

const onlyChild = ref(null)

function hasOneShowingChild(children = [], parent) {
  const showing = children.filter((c) => {
    if (c.hidden) return false
    onlyChild.value = c
    return true
  })
  if (showing.length === 1) {
    return true
  }
  if (showing.length === 0) {
    onlyChild.value = { ...parent, path: '', alwaysShow: parent.alwaysShow }
    return true
  }
  return false
}

function resolvePath(routePath) {
  if (!routePath) return props.basePath
  if (/^https?:\/\//.test(routePath)) return routePath
  if (routePath.startsWith('/')) return routePath
  const base = props.basePath.endsWith('/') ? props.basePath.slice(0, -1) : props.basePath
  return base + '/' + routePath
}

function titleOf(node) {
  return (node.meta && node.meta.title) || node.name || '未命名'
}

function iconOf(node) {
  const icon = node.meta && node.meta.icon
  if (!icon) return null
  // 动态图标：用全局注册的组件名
  return icon
}
</script>
