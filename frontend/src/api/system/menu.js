import request from '@/api/request'

// 菜单树
export function getMenuTree(params) {
  return request({
    url: '/api/sys/menu/tree',
    method: 'get',
    params
  })
}

// 菜单详情
export function getMenuDetail(id) {
  return request({
    url: '/api/sys/menu/' + id,
    method: 'get'
  })
}

// 新增菜单
export function createMenu(data) {
  return request({
    url: '/api/sys/menu',
    method: 'post',
    data
  })
}

// 编辑菜单
export function updateMenu(id, data) {
  return request({
    url: '/api/sys/menu/' + id,
    method: 'put',
    data
  })
}

// 删除菜单
export function deleteMenu(id) {
  return request({
    url: '/api/sys/menu/' + id,
    method: 'delete'
  })
}
