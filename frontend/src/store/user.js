import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/system/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref({})
  const menus = ref([]) // 后端返回的菜单树
  const permissions = ref([]) // 权限码列表

  // 登录
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const tk = res.data?.token || res.data?.access_token || res.token
    token.value = tk
    setToken(tk)
    return res
  }

  // 获取用户信息
  async function fetchUserInfo() {
    const res = await getUserInfo()
    const data = res.data || res
    userInfo.value = {
      id: data.id,
      username: data.username,
      nickname: data.nickname || data.username,
      avatar: data.avatar || ''
    }
    menus.value = data.menus || data.menuTree || []
    permissions.value = data.permissions || data.codes || []
    return data
  }

  // 退出
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // 忽略退出接口错误
    }
    resetState()
  }

  function resetState() {
    token.value = ''
    userInfo.value = {}
    menus.value = []
    permissions.value = []
    removeToken()
  }

  return {
    token,
    userInfo,
    menus,
    permissions,
    login,
    fetchUserInfo,
    logout,
    resetState
  }
})
