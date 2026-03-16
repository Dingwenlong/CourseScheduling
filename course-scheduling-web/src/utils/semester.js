import dayjs from 'dayjs'

export function getCurrentSemester(date = dayjs()) {
  const year = date.year()
  return date.month() < 7 ? `${year - 1}-2` : `${year}-1`
}

export function buildSemesterOptions(startYear = dayjs().year(), count = 5, includeAll = false) {
  const options = includeAll ? [{ label: '全部学期', value: '' }] : []

  for (let i = 0; i < count; i++) {
    const currentYear = startYear - i
    options.push({ label: `${currentYear}-${currentYear + 1}学年第一学期`, value: `${currentYear}-1` })
    options.push({ label: `${currentYear}-${currentYear + 1}学年第二学期`, value: `${currentYear}-2` })
  }

  return options
}
