import { defineStore } from 'pinia'
import { ref } from 'vue'
import { constantRoutes, layoutRoutes } from '@/router'
import Layout from '@/layout/index.vue'

// 使用 import.meta.glob 预加载 views 下所有 vue 文件
const modules = import.meta.glob('@/views/**/*.vue')

// 加载组件：根据 component 字符串返回异步组件
function loadComponent(component) {
  if (!component) return null
  if (component === 'Layout') return Layout
  if (component === 'ParentView' || component === 'RouterView') {
    return () => import('vue-router').then((m) => m.RouterView)
  }
  let p = component
  if (p.startsWith('/')) p = p.slice(1)
  if (p.endsWith('.vue')) p = p.slice(0, -4)
  const candidates = [
    `/src/views/${p}.vue`,
    `/src/views/${p}/index.vue`
  ]
  for (const key of candidates) {
    if (modules[key]) return modules[key]
  }
  console.warn('[permission] 未找到组件:', component, ', 可用模块:', Object.keys(modules).slice(0, 10))
  return () => import('@/views/error/404.vue')
}

// 后端菜单字段映射到路由
function menuToRoute(menu, isTopLevel = true) {
  const menuTitle = menu.menuName || menu.title || menu.name || '未命名'
  const route = {
    path: menu.path || '',
    name: menu.path ? menu.path.replace(/\//g, '-') : undefined,
    hidden: menu.hidden === true || menu.visible === false || menu.menuType === 'B',
    meta: {
      title: menuTitle,
      icon: menu.icon || '',
      roles: menu.roles || [],
      noCache: menu.noCache === true,
      affix: menu.affix === true,
      activeMenu: menu.activeMenu || ''
    }
  }

  const hasChildren = menu.children && menu.children.length > 0

  if (hasChildren) {
    route.component = isTopLevel ? Layout : (loadComponent('ParentView') || Layout)
    route.children = menu.children.map((c) => menuToRoute(c, false)).filter((r) => !r.hidden)
    if (route.children.length === 0) {
      delete route.children
      if (menu.component) {
        route.component = loadComponent(menu.component)
      }
    }
  } else {
    if (menu.component) {
      route.component = loadComponent(menu.component)
    }
  }
  return route
}

// 收集顶级叶子菜单（需要统一包裹到 Layout 下的单页面）
function wrapTopLevelRoutes(dynamic) {
  const directoryRoutes = []
  const orphanLeafs = []
  dynamic.forEach((route) => {
    if (route.children && route.children.length) {
      directoryRoutes.push(route)
    } else if (route.component) {
      // 顶级叶子，component 已映射为实际页面组件
      orphanLeafs.push(route)
    } else {
      // 无 component 无 children：忽略或当目录
      directoryRoutes.push(route)
    }
  })

  if (orphanLeafs.length) {
    // 多个扁平顶级菜单合并到一个 Layout 下，避免多个 path:'/' 冲突
    const onlyOne = orphanLeafs.length === 1
    const wrapper = {
      path: '/',
      component: Layout,
      name: 'OrphanLayout',
      // 单个时让子项直接显示；多个时作为一个目录显示
      hidden: false,
      meta: { title: onlyOne ? '' : '应用菜单', icon: 'Menu' },
      children: orphanLeafs.map((r) => ({
        path: r.path.startsWith('/') ? r.path.slice(1) : r.path,
        name: r.name,
        component: r.component,
        meta: r.meta
      }))
    }
    directoryRoutes.push(wrapper)
  }
  return directoryRoutes
}

export const usePermissionStore = defineStore('permission', () => {
  const routes = ref([]) // 动态生成的路由
  const sidebarRoutes = ref([]) // 侧栏展示的路由（含静态布局路由）

  // 根据后端菜单生成动态路由
  function generateRoutes(menus) {
    const raw = (menus || []).map((m) => menuToRoute(m, true))
    const dynamic = wrapTopLevelRoutes(raw)
    routes.value = dynamic
    sidebarRoutes.value = [...layoutRoutes, ...dynamic]
    return dynamic
  }

  function resetRoutes() {
    routes.value = []
    sidebarRoutes.value = []
  }

  return {
    routes,
    sidebarRoutes,
    generateRoutes,
    resetRoutes
  }
})

export { constantRoutes }
