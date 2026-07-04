import request from '@/api/request'

export function getDictTypePage(params) {
  return request({
    url: '/api/sys/dict/type/page',
    method: 'get',
    params
  })
}

export function getDictTypeAll() {
  return request({
    url: '/api/sys/dict/type/all',
    method: 'get'
  })
}

export function createDictType(data) {
  return request({
    url: '/api/sys/dict/type',
    method: 'post',
    data
  })
}

export function updateDictType(id, data) {
  return request({
    url: '/api/sys/dict/type/' + id,
    method: 'put',
    data
  })
}

export function deleteDictType(id) {
  return request({
    url: '/api/sys/dict/type/' + id,
    method: 'delete'
  })
}

export function updateDictTypeStatus(id, status) {
  return request({
    url: '/api/sys/dict/type/' + id + '/status',
    method: 'put',
    data: { status }
  })
}

export function exportDictType() {
  return request({
    url: '/api/sys/dict/type/export',
    method: 'get',
    responseType: 'blob'
  })
}

export function downloadDictTypeTemplate() {
  return request({
    url: '/api/sys/dict/type/template',
    method: 'get',
    responseType: 'blob'
  })
}

export function importDictType(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/sys/dict/type/import',
    method: 'post',
    data: formData
  })
}

export function getDictDataPage(params) {
  return request({
    url: '/api/sys/dict/data/page',
    method: 'get',
    params
  })
}

export function getDictDataByCode(dictCode) {
  return request({
    url: '/api/sys/dict/data/code/' + dictCode,
    method: 'get'
  })
}

export function createDictData(data) {
  return request({
    url: '/api/sys/dict/data',
    method: 'post',
    data
  })
}

export function updateDictData(id, data) {
  return request({
    url: '/api/sys/dict/data/' + id,
    method: 'put',
    data
  })
}

export function deleteDictData(id) {
  return request({
    url: '/api/sys/dict/data/' + id,
    method: 'delete'
  })
}

export function updateDictDataStatus(id, status) {
  return request({
    url: '/api/sys/dict/data/' + id + '/status',
    method: 'put',
    data: { status }
  })
}

export function exportDictData(dictCode) {
  return request({
    url: '/api/sys/dict/data/export',
    method: 'get',
    params: { dictCode },
    responseType: 'blob'
  })
}

export function downloadDictDataTemplate() {
  return request({
    url: '/api/sys/dict/data/template',
    method: 'get',
    responseType: 'blob'
  })
}

export function importDictData(dictCode, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/sys/dict/data/import',
    method: 'post',
    params: { dictCode },
    data: formData
  })
}
