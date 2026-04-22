import dayjs from 'dayjs'

const escapeCsv = (value) => {
  const normalized = value == null ? '' : String(value)
  const escaped = normalized.replaceAll('"', '""')
  return `"${escaped}"`
}

const escapeIcs = (value) => {
  return String(value || '')
    .replaceAll('\\', '\\\\')
    .replaceAll('\n', '\\n')
    .replaceAll(',', '\\,')
    .replaceAll(';', '\\;')
}

const toIcsDateTime = (date) => dayjs(date).format('YYYYMMDDTHHmmss')

const downloadBlob = (blob, filename) => {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 1000)
}

const buildFileSafeName = (value, fallback) => {
  const name = String(value || fallback || 'timetable').trim()
  return name.replace(/[\\/:*?"<>|]/g, '-')
}

const resolveSlotRange = (course, slotMeta) => {
  const startMeta = slotMeta[course.slotNo]
  const endMeta = slotMeta[(course.slotNo || 0) + 1] || startMeta
  const startText = startMeta?.time?.split('-')[0] || '08:00'
  const endText = endMeta?.time?.split('-')[1] || startMeta?.time?.split('-')[1] || '08:45'
  return { startText, endText }
}

const resolveUpcomingCourseDate = (dayOfWeek) => {
  const today = dayjs()
  const startOfWeek = today.startOf('week')
  let targetDate = startOfWeek.add(dayOfWeek, 'day')
  if (targetDate.endOf('day').isBefore(today)) {
    targetDate = targetDate.add(7, 'day')
  }
  return targetDate
}

export const exportCoursesAsCsv = ({ courses, filename, scopeLabel, slotMeta }) => {
  const headers = ['课程名称', '教师', '班级', '教室', '星期', '节次', '时间', '周次', '冲突状态']
  const rows = courses.map((course) => {
    const range = resolveSlotRange(course, slotMeta)
    const timeText = `${range.startText}-${range.endText}`
    const weekday = `周${['一', '二', '三', '四', '五', '六', '日'][(course.dayOfWeek || 1) - 1] || course.dayOfWeek}`
    const slotText = `第${course.slotNo || '-'}-${(course.slotNo || 0) + 1}节`
    return [
      course.courseName,
      course.teacherName,
      course.className,
      course.classroomName,
      weekday,
      slotText,
      timeText,
      course.weeks || '',
      course.isConflict === 1 ? '冲突' : '正常'
    ]
  })

  const content = [
    scopeLabel ? [scopeLabel].map(escapeCsv).join(',') : null,
    headers.map(escapeCsv).join(','),
    ...rows.map((row) => row.map(escapeCsv).join(','))
  ].filter(Boolean).join('\r\n')

  downloadBlob(
    new Blob([`\uFEFF${content}`], { type: 'text/csv;charset=utf-8;' }),
    `${buildFileSafeName(filename, '课表')}.csv`
  )
}

export const exportCoursesAsIcs = ({ courses, filename, scopeLabel, slotMeta }) => {
  const nowStamp = toIcsDateTime(dayjs())
  const events = courses.map((course, index) => {
    const baseDate = resolveUpcomingCourseDate(course.dayOfWeek || 1)
    const range = resolveSlotRange(course, slotMeta)
    const startDateTime = dayjs(`${baseDate.format('YYYY-MM-DD')} ${range.startText}`)
    const endDateTime = dayjs(`${baseDate.format('YYYY-MM-DD')} ${range.endText}`)
    const descriptionParts = [
      course.teacherName ? `教师：${course.teacherName}` : '',
      course.className ? `班级：${course.className}` : '',
      course.weeks ? `周次：${course.weeks}` : '',
      course.isConflict === 1 ? '状态：存在冲突' : '状态：安排正常'
    ].filter(Boolean)
    return [
      'BEGIN:VEVENT',
      `UID:${escapeIcs(`${buildFileSafeName(filename, 'timetable')}-${course.id || index}@course-scheduling`)}`,
      `DTSTAMP:${nowStamp}`,
      `DTSTART:${toIcsDateTime(startDateTime)}`,
      `DTEND:${toIcsDateTime(endDateTime)}`,
      `SUMMARY:${escapeIcs(course.courseName || '课程')}`,
      `LOCATION:${escapeIcs(course.classroomName || '待定教室')}`,
      `DESCRIPTION:${escapeIcs(descriptionParts.join('\\n'))}`,
      'END:VEVENT'
    ].join('\r\n')
  })

  const content = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Course Scheduling System//Timetable Export//CN',
    'CALSCALE:GREGORIAN',
    scopeLabel ? `X-WR-CALNAME:${escapeIcs(scopeLabel)}` : '',
    ...events,
    'END:VCALENDAR'
  ].filter(Boolean).join('\r\n')

  downloadBlob(
    new Blob([content], { type: 'text/calendar;charset=utf-8;' }),
    `${buildFileSafeName(filename, '课表')}.ics`
  )
}

export const printCurrentPage = (title) => {
  const previousTitle = document.title
  if (title) {
    document.title = title
  }
  window.print()
  window.setTimeout(() => {
    document.title = previousTitle
  }, 300)
}
