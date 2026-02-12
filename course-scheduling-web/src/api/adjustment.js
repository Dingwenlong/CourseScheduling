import request from '@/utils/request'

export function checkAdjustment(data) {
  return request.post('/adjustment/check', data)
}

export function executeAdjustment(data) {
  return request.post('/adjustment/execute', data)
}

export function swapCourses(timetableId, detailId1, detailId2) {
  return request.post('/adjustment/swap', null, {
    params: { timetableId, detailId1, detailId2 }
  })
}
