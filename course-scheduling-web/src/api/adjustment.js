import request from '@/utils/request'

export function checkAdjustment(data) {
  return request.post('/adjustment/check', data)
}

export function applyAdjustment(data) {
  return request.post('/adjustment/apply', data)
}

export function getPendingAdjustment(params) {
  return request.get('/adjustment/pending', { params })
}

export function cancelAdjustment(applicationId) {
  return request.post(`/adjustment/cancel/${applicationId}`)
}

export function executeAdjustment(data) {
  return request.post('/adjustment/execute', data)
}

export function checkSwapAdjustment(data) {
  return request.post('/adjustment/swap/check', data)
}

export function applySwapAdjustment(data) {
  return request.post('/adjustment/swap/apply', data)
}

export function getPendingSwapAdjustment(params) {
  return request.get('/adjustment/swap/pending', { params })
}

export function cancelSwapAdjustment(applicationId) {
  return request.post(`/adjustment/swap/cancel/${applicationId}`)
}

export function executeSwapAdjustment(data) {
  return request.post('/adjustment/swap/execute', data)
}

export function swapCourses(timetableId, detailId1, detailId2) {
  return request.post('/adjustment/swap', null, {
    params: { timetableId, detailId1, detailId2 }
  })
}
