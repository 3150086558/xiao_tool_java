import { createRouter, createWebHashHistory } from 'vue-router'

// Layout 用动态 import，避免与 layout 模块形成循环依赖
const Layout = () => import('@/layout/index.vue')

// 静态路由：登录页、404、首页
export const constantRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    hidden: true,
    meta: { title: '登录' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    hidden: true,
    meta: { title: '404' }
  }
]

// 含布局的静态路由（首页等）
export const layoutRoutes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', affix: true }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    ...constantRoutes,
    ...layoutRoutes,
    // 兜底：未匹配路由进入 404（动态路由 addRoute 后再放行）
    { path: '/:pathMatch(.*)*', redirect: '/404', hidden: true }
  ],
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 全局前置守卫：动态路由生成 + 鉴权
const WHITELIST = ['/login', '/404']
let dynamicAdded = false
const dynamicRouteNames = new Set()

router.beforeEach(async (to, from, next) => {
  document.title = (to.meta && to.meta.title ? to.meta.title + ' - ' : '') + '小肖的自用工具'

  const hasToken = !!localStorage.getItem('org_sys_token')

  if (!hasToken) {
    if (WHITELIST.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }

  if (to.path === '/login') {
    next({ path: '/' })
    return
  }

  if (dynamicAdded) {
    next()
    return
  }

  // 首次进入：拉取用户信息并生成动态路由
  try {
    const { useUserStore } = await import('@/store/user')
    const { usePermissionStore } = await import('@/store/permission')
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()

    if (!userStore.userInfo || !userStore.userInfo.id) {
      await userStore.fetchUserInfo()
    }

    const menus = userStore.menus && userStore.menus.length ? userStore.menus : []
    const accessRoutes = permissionStore.generateRoutes(menus)

    // 路由已在 permission store 中完成 Layout 包裹，直接注册
    accessRoutes.forEach((route) => {
      if (!route.component) {
        route.component = Layout
      }
      router.addRoute(route)
      if (route.name) dynamicRouteNames.add(route.name)
    })

    dynamicAdded = true
    // 重新导航，确保 addRoute 生效
    next({ ...to, replace: true })
  } catch (e) {
    console.error('生成动态路由失败:', e)
    const { useUserStore } = await import('@/store/user')
    useUserStore().resetState()
    dynamicAdded = false
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
  }
})

// 重置动态路由（退出登录时调用，Vue Router 4 兼容）
export function resetRouter() {
  dynamicAdded = false
  dynamicRouteNames.forEach((name) => {
    router.removeRoute(name)
  })
  dynamicRouteNames.clear()
}

export default router
