<template>
  <div class="tags-view-container">
    <el-scrollbar class="tags-scroll">
      <div class="tags-list">
        <router-link
          v-for="tag in visitedViews"
          :key="tag.fullPath"
          :to="tag.fullPath"
          class="tags-item"
          :class="{ active: isActive(tag) }"
          @contextmenu.prevent="openMenu(tag, $event)"
        >
          <span class="dot" v-if="isActive(tag)"></span>
          {{ tag.title }}
          <el-icon
            v-if="!tag.affix"
            class="close-icon"
            @click.prevent.stop="closeTag(tag)"
          >
            <Close />
          </el-icon>
        </router-link>
      </div>
    </el-scrollbar>

    <ul v-show="menuVisible" class="context-menu" :style="{ top: menuTop + 'px', left: menuLeft + 'px' }">
      <li @click="refreshTag(selectedTag)">刷新</li>
      <li v-if="!selectedTag.affix" @click="closeTag(selectedTag)">关闭</li>
      <li @click="closeOthers(selectedTag)">关闭其他</li>
      <li @click="closeAll(selectedTag)">关闭全部</li>
    </ul>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const visitedViews = ref([])
const menuVisible = ref(false)
const menuTop = ref(0)
const menuLeft = ref(0)
const selectedTag = ref({})

function isActive(tag) {
  return tag.fullPath === route.fullPath
}

function addTag(to) {
  if (to.name === 'Login' || to.name === 'NotFound') return
  if (!to.meta || !to.meta.title) return
  if (visitedViews.value.some((v) => v.fullPath === to.fullPath)) return
  visitedViews.value.push({
    fullPath: to.fullPath,
    title: to.meta.title,
    affix: to.meta.affix === true,
    name: to.name
  })
}

function initAffix() {
  // 固定首页标签
  visitedViews.value.push({
    fullPath: '/dashboard',
    title: '首页',
    affix: true,
    name: 'Dashboard'
  })
}

function closeTag(tag) {
  const idx = visitedViews.value.findIndex((v) => v.fullPath === tag.fullPath)
  if (idx === -1) return
  visitedViews.value.splice(idx, 1)
  if (isActive(tag)) {
    const next = visitedViews.value[idx] || visitedViews.value[idx - 1]
    if (next) router.push(next.fullPath)
    else router.push('/dashboard')
  }
}

function closeOthers(tag) {
  visitedViews.value = visitedViews.value.filter(
    (v) => v.affix || v.fullPath === tag.fullPath
  )
  router.push(tag.fullPath)
}

function closeAll() {
  visitedViews.value = visitedViews.value.filter((v) => v.affix)
  router.push('/dashboard')
}

function refreshTag() {
  // 刷新当前页：重新加载路由组件
  router.go(0)
}

function openMenu(tag, e) {
  selectedTag.value = tag
  menuLeft.value = e.clientX
  menuTop.value = e.clientY
  menuVisible.value = true
}

function closeMenu() {
  menuVisible.value = false
}

watch(
  () => route.fullPath,
  () => {
    addTag(route)
  }
)

onMounted(() => {
  initAffix()
  addTag(route)
  document.addEventListener('click', closeMenu)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeMenu)
})
</script>

<style scoped>
.tags-view-container {
  height: 34px;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  position: relative;
}
.tags-scroll {
  width: 100%;
}
.tags-list {
  display: flex;
  align-items: center;
  padding: 0 8px;
  white-space: nowrap;
}
.tags-item {
  display: inline-flex;
  align-items: center;
  height: 26px;
  line-height: 24px;
  padding: 0 8px;
  margin-right: 4px;
  border: 1px solid #d9d9d9;
  border-radius: 3px;
  font-size: 12px;
  color: #495060;
  background: #fff;
  cursor: pointer;
}
.tags-item.active {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}
.tags-item.active .close-icon {
  color: #fff;
}
.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fff;
  margin-right: 6px;
}
.close-icon {
  margin-left: 4px;
  font-size: 12px;
  border-radius: 50%;
}
.close-icon:hover {
  background: rgba(0, 0, 0, 0.2);
  color: #fff;
}
.context-menu {
  position: fixed;
  z-index: 3000;
  background: #fff;
  list-style: none;
  margin: 0;
  padding: 4px 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  font-size: 12px;
}
.context-menu li {
  padding: 6px 16px;
  cursor: pointer;
}
.context-menu li:hover {
  background: #f5f7fa;
  color: #409eff;
}
</style>
