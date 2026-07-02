import request from '@/api/request'

// 记账分页列表
export function getAccountingPage(params) {
  return request({
    url: '/api/app/accounting/page',
    method: 'get',
    params
  })
}

// 新增记账
export function createAccounting(data) {
  return request({
    url: '/api/app/accounting',
    method: 'post',
    data
  })
}

// 编辑记账
export function updateAccounting(id, data) {
  return request({
    url: '/api/app/accounting/' + id,
    method: 'put',
    data
  })
}

// 删除记账
export function deleteAccounting(id) {
  return request({
    url: '/api/app/accounting/' + id,
    method: 'delete'
  })
}

// 删除全部记账
export function deleteAllAccounting() {
  return request({
    url: '/api/app/accounting/all',
    method: 'delete'
  })
}

// 导入记账
export function importAccounting(formData, onUploadProgress) {
  return request({
    url: '/api/app/accounting/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress
  })
}

// 导出记账
export function exportAccounting(params, onDownloadProgress) {
  return request({
    url: '/api/app/accounting/export',
    method: 'get',
    params,
    responseType: 'blob',
    onDownloadProgress
  })
}

// 下载导入模板
export function downloadTemplate() {
  return request({
    url: '/api/app/download-template',
    method: 'get',
    responseType: 'blob'
  })
}
