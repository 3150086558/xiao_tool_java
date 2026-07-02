import request from '@/api/request'

// 登录
export function login(data) {
  return request({
    url: '/api/sys/login',
    method: 'post',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/api/sys/logout',
    method: 'post'
  })
}

// 获取当前用户信息（含菜单树）
export function getUserInfo() {
  return request({
    url: '/api/sys/userinfo',
    method: 'get'
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/api/sys/change-password',
    method: 'post',
    data
  })
}
