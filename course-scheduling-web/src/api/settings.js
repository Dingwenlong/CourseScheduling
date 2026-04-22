import request from '@/utils/request'

export function previewDataSync(data) {
  return request.post('/settings/data-sync/preview', data)
}

export function applyDataSync(data) {
  return request.post('/settings/data-sync/apply', data)
}
