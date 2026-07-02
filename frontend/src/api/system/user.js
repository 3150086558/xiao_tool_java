import request from '@/api/request'

// 人员分页列表
export function getUserPage(params) {
  return request({
    url: '/api/sys/user/page',
    method: 'get',
    params
  })
}

// 人员详情
export function getUserDetail(id) {
  return request({
    url: '/api/sys/user/' + id,
    method: 'get'
  })
}

// 新增人员
export function createUser(data) {
  return request({
    url: '/api/sys/user',
    method: 'post',
    data
  })
}

// 编辑人员
export function updateUser(id, data) {
  return request({
    url: '/api/sys/user/' + id,
    method: 'put',
    data
  })
}

// 删除人员
export function deleteUser(id) {
  return request({
    url: '/api/sys/user/' + id,
    method: 'delete'
  })
}

// 重置密码
export function resetUserPassword(id, data) {
  return request({
    url: '/api/sys/user/' + id + '/reset-password',
    method: 'put',
    data
  })
}

// 启用/禁用
export function toggleUserStatus(id, data) {
  return request({
    url: '/api/sys/user/' + id + '/status',
    method: 'put',
    data
  })
}
