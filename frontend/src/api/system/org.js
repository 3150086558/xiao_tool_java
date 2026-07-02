import request from '@/api/request'

// 组织树
export function getOrgTree(params) {
  return request({
    url: '/api/sys/org/tree',
    method: 'get',
    params
  })
}

// 组织列表
export function getOrgList(params) {
  return request({
    url: '/api/sys/org/list',
    method: 'get',
    params
  })
}

// 组织详情
export function getOrgDetail(id) {
  return request({
    url: '/api/sys/org/' + id,
    method: 'get'
  })
}

// 新增组织
export function createOrg(data) {
  return request({
    url: '/api/sys/org',
    method: 'post',
    data
  })
}

// 编辑组织
export function updateOrg(id, data) {
  return request({
    url: '/api/sys/org/' + id,
    method: 'put',
    data
  })
}

// 删除组织
export function deleteOrg(id) {
  return request({
    url: '/api/sys/org/' + id,
    method: 'delete'
  })
}
