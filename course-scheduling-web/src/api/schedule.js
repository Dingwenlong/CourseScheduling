import request from '@/utils/request'

export function executeSchedule(data) {
  return request.post('/schedule/execute', data)
}

export function asyncSchedule(data) {
  return request.post('/schedule/async', data)
}

export function getAlgorithms() {
  return request.get('/schedule/algorithms')
}
