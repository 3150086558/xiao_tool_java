import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

const service = axios.create({
  baseURL: '/',
  timeout: 15000
})

// 请求拦截器：自动添加 Authorization
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 是否正在刷新登录跳转，避免 401 重复弹框
let isReloginShown = false

// 响应拦截器：统一处理错误
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 二进制流直接返回
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
      return response
    }
    // 约定后端返回 { code, message, data }
    if (res.code !== undefined && res.code !== 0 && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      // 401 未授权
      if (res.code === 401) {
        handleUnauthorized()
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    const status = error.response && error.response.status
    const data = error.response && error.response.data
    const detail = data?.detail || data?.message || data?.error || error.message
    if (status === 401) {
      handleUnauthorized()
    } else if (status === 403) {
      ElMessage.error(detail || '没有权限访问该资源')
    } else if (status === 422) {
      ElMessage.error(detail || '参数校验失败')
    } else if (status === 500) {
      ElMessage.error(detail || '服务器内部错误')
    } else if (status === 404) {
      ElMessage.error(detail || '请求的资源不存在')
    } else if (status === 400) {
      ElMessage.error(detail || '请求错误')
    } else {
      ElMessage.error(detail || '网络异常')
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  if (isReloginShown) return
  isReloginShown = true
  ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      removeToken()
      router.push('/login')
    })
    .finally(() => {
      isReloginShown = false
    })
}

export default service
