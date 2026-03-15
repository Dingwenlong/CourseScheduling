import request from '@/utils/request'

export function getTimetableList(params) {
  return request.get('/timetable/page', { params })
}

export function getTimetableById(id) {
  return request.get(`/timetable/${id}`)
}

export function generateTimetable(data) {
  return request.post('/timetable/generate', data)
}

export function publishTimetable(id) {
  return request.post(`/timetable/${id}/publish`)
}

export function archiveTimetable(id) {
  return request.post(`/timetable/${id}/archive`)
}

export function deleteTimetable(id) {
  return request.delete(`/timetable/${id}`)
}

export function getTimetableDetails(id) {
  return request.get(`/timetable/${id}/details`)
}

export function getClassTimetable(timetableId, classId) {
  return request.get(`/timetable/${timetableId}/class/${classId}`)
}

export function getTeacherTimetable(timetableId, teacherId) {
  return request.get(`/timetable/${timetableId}/teacher/${teacherId}`)
}

export function getClassroomTimetable(timetableId, classroomId) {
  return request.get(`/timetable/${timetableId}/classroom/${classroomId}`)
}

export function getConflicts(id) {
  return request.get(`/timetable/${id}/conflicts`)
}

export function getLatestTimetable(semester) {
  return request.get('/timetable/latest', { params: { semester } })
}

export function getAlgorithms() {
  return request.get('/schedule/algorithms')
}
