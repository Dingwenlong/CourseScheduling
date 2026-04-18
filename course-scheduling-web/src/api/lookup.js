import request from '@/utils/request'

export function searchTeachers(params) {
  return request.get('/lookup/teachers', { params })
}

export function searchClasses(params) {
  return request.get('/lookup/classes', { params })
}

export function searchClassrooms(params) {
  return request.get('/lookup/classrooms', { params })
}

export function searchCourses(params) {
  return request.get('/lookup/courses', { params })
}
