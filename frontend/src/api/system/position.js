import request from '@/api/request'

// 职位列表
export function getPositionList(params) {
  return request({
    url: '/api/sys/position/list',
    method: 'get',
    params
  })
}

// 职位分页
export function getPositionPage(params) {
  return request({
    url: '/api/sys/position/page',
    method: 'get',
    params
  })
}

// 职位详情
export function getPositionDetail(id) {
  return request({
    url: '/api/sys/position/' + id,
    method: 'get'
  })
}

// 新增职位
export function createPosition(data) {
  return request({
    url: '/api/sys/position',
    method: 'post',
    data
  })
}

// 编辑职位
export function updatePosition(id, data) {
  return request({
    url: '/api/sys/position/' + id,
    method: 'put',
    data
  })
}

// 删除职位
export function deletePosition(id) {
  return request({
    url: '/api/sys/position/' + id,
    method: 'delete'
  })
}

// 保存职位权限（功能权限+数据权限）
export function savePositionPermissions(id, data) {
  return request({
    url: '/api/sys/position/' + id + '/permissions',
    method: 'put',
    data
  })
}

// 获取职位权限
export function getPositionPermissions(id) {
  return request({
    url: '/api/sys/position/' + id + '/permissions',
    method: 'get'
  })
}
