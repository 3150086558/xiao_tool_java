import request from '@/api/request'

// 角色列表
export function getRoleList(params) {
  return request({
    url: '/api/sys/role/list',
    method: 'get',
    params
  })
}

// 角色分页
export function getRolePage(params) {
  return request({
    url: '/api/sys/role/page',
    method: 'get',
    params
  })
}

// 角色详情
export function getRoleDetail(id) {
  return request({
    url: '/api/sys/role/' + id,
    method: 'get'
  })
}

// 新增角色
export function createRole(data) {
  return request({
    url: '/api/sys/role',
    method: 'post',
    data
  })
}

// 编辑角色
export function updateRole(id, data) {
  return request({
    url: '/api/sys/role/' + id,
    method: 'put',
    data
  })
}

// 删除角色
export function deleteRole(id) {
  return request({
    url: '/api/sys/role/' + id,
    method: 'delete'
  })
}

// 获取角色已分配菜单ID
export function getRoleMenuIds(id) {
  return request({
    url: '/api/sys/role/' + id + '/menu-ids',
    method: 'get'
  })
}

// 分配角色菜单
export function assignRoleMenus(id, data) {
  return request({
    url: '/api/sys/role/' + id + '/menus',
    method: 'put',
    data
  })
}
