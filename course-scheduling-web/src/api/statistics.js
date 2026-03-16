import request from '@/utils/request'

export function getStatisticsOverview(timetableId) {
  return request.get(`/statistics/overview/${timetableId}`)
}

export function getClassroomUtilization(timetableId) {
  return request.get(`/statistics/classroom-utilization/${timetableId}`)
}

export function getTeacherWorkload(timetableId) {
  return request.get(`/statistics/teacher-workload/${timetableId}`)
}

export function getConflictReport(timetableId) {
  return request.get(`/statistics/conflict-report/${timetableId}`)
}

export function getTotalHours(timetableId) {
  return request.get(`/statistics/total-hours/${timetableId}`)
}

export function getCourseCount(timetableId) {
  return request.get(`/statistics/course-count/${timetableId}`)
}
