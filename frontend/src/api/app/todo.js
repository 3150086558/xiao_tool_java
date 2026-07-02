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
