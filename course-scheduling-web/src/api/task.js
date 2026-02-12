import request from '@/utils/request'

export function getTaskList(params) {
  return request.get('/task/page', { params })
}

export function getTaskById(id) {
  return request.get(`/task/${id}`)
}

export function createTask(data) {
  return request.post('/task', data)
}

export function updateTask(data) {
  return request.put('/task', data)
}

export function deleteTask(id) {
  return request.delete(`/task/${id}`)
}
