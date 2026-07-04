import request from '@/api/request'

// 待办分页列表
export function getTodoPage(params) {
  return request({
    url: '/api/app/todo/page',
    method: 'get',
    params
  })
}

// 新增待办
export function createTodo(data) {
  return request({
    url: '/api/app/todo',
    method: 'post',
    data
  })
}

// 编辑待办
export function updateTodo(id, data) {
  return request({
    url: '/api/app/todo/' + id,
    method: 'put',
    data
  })
}

// 删除待办
export function deleteTodo(id) {
  return request({
    url: '/api/app/todo/' + id,
    method: 'delete'
  })
}

// 切换完成状态
export function toggleTodoDone(id, data) {
  return request({
    url: '/api/app/todo/' + id + '/done',
    method: 'put',
    data
  })
}

// 导出待办
export function exportTodo(params) {
  return request({
    url: '/api/app/todo/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 下载导入模板
export function downloadTodoTemplate() {
  return request({
    url: '/api/app/todo/template',
    method: 'get',
    responseType: 'blob'
  })
}

// 导入待办
export function importTodo(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/app/todo/import',
    method: 'post',
    data: formData
  })
}
