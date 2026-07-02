import request from '@/api/request'

// 备忘录分页列表
export function getNotePage(params) {
  return request({
    url: '/api/app/note/page',
    method: 'get',
    params
  })
}

// 新增备忘录
export function createNote(data) {
  return request({
    url: '/api/app/note',
    method: 'post',
    data
  })
}

// 编辑备忘录
export function updateNote(id, data) {
  return request({
    url: '/api/app/note/' + id,
    method: 'put',
    data
  })
}

// 删除备忘录
export function deleteNote(id) {
  return request({
    url: '/api/app/note/' + id,
    method: 'delete'
  })
}
