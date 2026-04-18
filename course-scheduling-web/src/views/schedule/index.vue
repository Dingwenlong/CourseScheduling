<template>
  <PageContainer with-tabbar class="schedule-page">
    <PageHeader :title="pageTitle" />

    <div class="card animate-fade-in">
      <div class="section-title">{{ searchSectionTitle }}</div>
      <div v-if="isSearchLocked" class="role-guide">
        <div class="role-guide-title">{{ lockedScopeTitle }}</div>
        <div class="role-guide-text">{{ lockedScopeText }}</div>
      </div>
      <div class="search-wrapper">
        <n-select
          v-if="!isSearchLocked"
          v-model:value="queryType"
          :options="typeOptions"
          placeholder="查询类型"
          style="width: 150px"
        />
        <n-select
          v-if="!isSearchLocked"
          v-model:value="searchKeyword"
          filterable
          remote
          clearable
          :options="lookupOptions"
          :loading="lookupLoading"
          :placeholder="getPlaceholder"
          class="search-input"
          @search="handleLookupSearch"
          @focus="handleLookupFocus"
          @update:value="handleLookupChange"
        />
        <n-input
          v-else
          v-model:value="searchKeyword"
          :placeholder="getPlaceholder"
          class="search-input"
          :disabled="isSearchLocked"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon>
              <SearchOutline />
            </n-icon>
          </template>
        </n-input>
        <n-button type="primary" @click="handleSearch">{{ actionButtonText }}</n-button>
      </div>
      <div class="search-helper">
        <div class="search-tip">{{ searchTip }}</div>
        <div v-if="todayScheduleTip" class="today-tip">{{ todayScheduleTip }}</div>
        <div v-if="recentSearches.length > 0 && !isSearchLocked" class="recent-searches">
          <span class="recent-label">最近使用</span>
          <n-button
            v-for="item in recentSearches"
            :key="`${queryType}-${item.value}`"
            quaternary
            size="small"
            class="recent-chip"
            @click="applyRecentSearch(item)"
          >
            {{ item.label }}
          </n-button>
        </div>
      </div>
    </div>

    <div class="content-body">
      <n-spin :show="loading" class="loading-container">
        <div v-if="timetableId && courses.length > 0">
          <div class="card timetable-card">
            <div class="section-title desktop-only">{{ timetablePanelTitle }}</div>
            <div class="timetable-grid-wrapper">
              <div class="timetable-grid">
                <div class="timetable-header"></div>
                <div
                  v-for="day in 5"
                  :key="'h'+day"
                  class="timetable-header timetable-day-header"
                  :class="{ 'is-today': isTodayColumn(day) }"
                >
                  周{{ ['一', '二', '三', '四', '五'][day - 1] }}
                </div>
                <template v-for="slot in 10" :key="'s'+slot">
                  <div class="timetable-header timetable-slot-header">
                    <div class="timetable-slot-index">{{ slotLabel(slot) }}</div>
                    <div class="timetable-slot-time">{{ slotTimeRange(slot) }}</div>
                  </div>
                  <div
                    v-for="day in 5"
                    :key="'c'+day+'-'+slot"
                    class="timetable-cell"
                    :class="{
                      'has-course': getCourse(day, slot),
                      'is-today-column': isTodayColumn(day)
                    }"
                    @click="showCourseInfo(day, slot)"
                  >
                    <div v-if="getCourse(day, slot)" class="course-block">
                      <div class="course-block-name">{{ getCourse(day, slot).courseName }}</div>
                      <div class="course-block-info">{{ getCourseMeta(getCourse(day, slot)) }}</div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <div class="card mt-16 mobile-only">
            <div class="section-title">课程列表</div>
            <n-list>
              <n-list-item v-for="course in courses" :key="course.id">
                <template #header>
                  <div class="flex-between">
                    <div class="course-name">{{ course.courseName }}</div>
                  </div>
                </template>
                <div class="text-muted">{{ courseTimeText(course) }}</div>
                <div class="text-muted" style="font-size: 12px;">{{ getCourseMeta(course) }}</div>
              </n-list-item>
            </n-list>
          </div>
        </div>

        <n-empty v-else :description="emptyDescription" />
      </n-spin>
    </div>

    <n-modal v-model:show="showCoursePopup" preset="card" title="课程详情" :style="{ width: '500px' }" class="course-dialog">
      <div v-if="currentCourse" class="course-details">
        <n-descriptions :column="1" bordered>
          <n-descriptions-item label="课程名称">{{ currentCourse.courseName }}</n-descriptions-item>
          <n-descriptions-item v-if="showTeacherField" label="教师">{{ currentCourse.teacherName }}</n-descriptions-item>
          <n-descriptions-item v-if="showClassField" label="班级">{{ currentCourse.className }}</n-descriptions-item>
          <n-descriptions-item label="教室">{{ currentCourse.classroomName }}</n-descriptions-item>
          <n-descriptions-item label="时间">{{ courseTimeText(currentCourse) }}</n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import dayjs from 'dayjs'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { getLatestTimetable, getClassTimetable, getTeacherTimetable, getClassroomTimetable } from '@/api/timetable'
import { searchClasses, searchTeachers, searchClassrooms } from '@/api/lookup'
import { getCurrentSemester } from '@/utils/semester'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import {
  NButton,
  NIcon,
  NSpin,
  NInput,
  NSelect,
  NList,
  NListItem,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NEmpty
} from 'naive-ui'
import {
  SearchOutline
} from '@vicons/ionicons5'

const message = useMessage()
const userStore = useUserStore()

const loading = ref(false)
const timetableId = ref(null)
const courses = ref([])
const queryType = ref('class')
const searchKeyword = ref('')
const showCoursePopup = ref(false)
const currentCourse = ref(null)
const hasAutoLoaded = ref(false)
const hasSearched = ref(false)
const searchHistoryKey = 'schedule-search-history'
const recentSearchMap = ref(loadRecentSearchMap())
const lookupOptions = ref([])
const lookupLoading = ref(false)
const currentWeekday = ref(dayjs().day())

const userRole = computed(() => userStore.userInfo?.role)
const isTeacherView = computed(() => userRole.value === 'TEACHER')
const isStudentView = computed(() => userRole.value === 'STUDENT')
const isSearchLocked = computed(() => ['TEACHER', 'STUDENT'].includes(userRole.value))
const showTeacherField = computed(() => !isTeacherView.value)
const showClassField = computed(() => !isStudentView.value)
const pageTitle = computed(() => {
  if (isTeacherView.value) {
    return '我的授课表'
  }
  if (isStudentView.value) {
    return '我的课表'
  }
  return '课表查询'
})
const searchSectionTitle = computed(() => isSearchLocked.value ? '当前查看范围' : '查询条件')
const actionButtonText = computed(() => isSearchLocked.value ? '刷新课表' : '查询')
const timetablePanelTitle = computed(() => {
  if (isTeacherView.value) {
    return '你的授课安排'
  }
  if (isStudentView.value) {
    return '你的课程安排'
  }
  return '课表'
})
const typeOptions = computed(() => {
  if (userRole.value === 'TEACHER') {
    return [{ label: '按教师查询', value: 'teacher' }]
  }
  if (userRole.value === 'STUDENT') {
    return [{ label: '按班级查询', value: 'class' }]
  }
  return [
    { label: '按班级查询', value: 'class' },
    { label: '按教师查询', value: 'teacher' },
    { label: '按教室查询', value: 'classroom' }
  ]
})

watch(typeOptions, (options) => {
  if (!options.some(option => option.value === queryType.value)) {
    queryType.value = options[0]?.value || 'class'
  }
}, { immediate: true })

const getPlaceholder = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '系统已自动填入当前教师'
  }
  if (userRole.value === 'STUDENT') {
    return '系统已自动填入当前班级'
  }
  const map = {
    'class': '搜索班级名称或班级编号',
    'teacher': '搜索教师姓名或教师编号',
    'classroom': '搜索教室名称、编号或楼栋'
  }
  return map[queryType.value]
})

const lockedScopeTitle = computed(() => {
  if (isTeacherView.value) {
    return '当前教师课表'
  }
  if (isStudentView.value) {
    return '当前班级课表'
  }
  return ''
})

const lockedScopeText = computed(() => {
  if (isTeacherView.value) {
    return `${userStore.userInfo?.realName || '当前教师'} 的授课安排已自动带入，绿色课块下方会显示授课班级和教室。`
  }
  if (isStudentView.value) {
    return '当前班级课表已自动带入，绿色课块下方会显示任课教师和教室。'
  }
  return ''
})

const searchTip = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '系统已自动绑定当前教师课表，页面加载后会直接查询。'
  }
  if (userRole.value === 'STUDENT') {
    return '系统已自动绑定当前班级课表，页面加载后会直接查询。'
  }
  const map = {
    class: '管理员可按班级名称或编号查询课表，最近使用记录会保存在当前浏览器。',
    teacher: '管理员可按教师姓名或编号查询课表，最近使用记录会保存在当前浏览器。',
    classroom: '管理员可按教室名称、编号或楼栋查询课表，最近使用记录会保存在当前浏览器。'
  }
  return map[queryType.value]
})

const weekdayLabels = ['一', '二', '三', '四', '五']
const slotMeta = {
  1: { label: '上午1', time: '08:00-08:45' },
  2: { label: '上午2', time: '08:55-09:40' },
  3: { label: '上午3', time: '10:10-10:55' },
  4: { label: '上午4', time: '11:05-11:50' },
  5: { label: '下午1', time: '14:00-14:45' },
  6: { label: '下午2', time: '14:55-15:40' },
  7: { label: '下午3', time: '16:10-16:55' },
  8: { label: '下午4', time: '17:05-17:50' },
  9: { label: '晚上1', time: '19:00-19:45' },
  10: { label: '晚上2', time: '19:55-20:40' }
}

const recentSearches = computed(() => recentSearchMap.value[queryType.value] || [])

const emptyDescription = computed(() => {
  if (!timetableId.value) {
    return '当前学期暂无可用课表'
  }
  if (hasSearched.value) {
    return '未查询到对应课程，请检查筛选条件是否正确'
  }
  if (isSearchLocked.value) {
    return '正在加载当前身份对应的课表'
  }
  return '请选择查询对象后查看课表'
})

const lookupSearchApiMap = {
  class: searchClasses,
  teacher: searchTeachers,
  classroom: searchClassrooms
}

const courseMap = computed(() => {
  const map = new Map()
  for (const course of courses.value) {
    for (const slot of getOccupiedSlots(course)) {
      map.set(`${course.dayOfWeek}_${slot}`, course)
    }
  }
  return map
})

const getCourse = (day, slot) => {
  return courseMap.value.get(`${day}_${slot}`) || null
}

const slotLabel = (slotNo) => slotMeta[slotNo]?.label || `第${slotNo}节`

const slotTimeRange = (slotNo) => slotMeta[slotNo]?.time || ''

const getOccupiedSlots = (course) => {
  if (!course?.slotNo) {
    return []
  }
  return [course.slotNo, course.slotNo + 1]
}

const isTodayColumn = (dayOfWeek) => currentWeekday.value >= 1 && currentWeekday.value <= 5 && currentWeekday.value === dayOfWeek

const courseTimeText = (course) => {
  if (!course) return '-'
  const weekday = weekdayLabels[(course.dayOfWeek || 1) - 1] || course.dayOfWeek
  const occupiedSlots = getOccupiedSlots(course)
  const slot = occupiedSlots.length > 1
    ? `${slotLabel(occupiedSlots[0])}-${slotLabel(occupiedSlots[occupiedSlots.length - 1])}`
    : slotLabel(course.slotNo)
  const timeStart = slotTimeRange(occupiedSlots[0])
  const timeEnd = slotTimeRange(occupiedSlots[occupiedSlots.length - 1])
  const time = timeStart && timeEnd
    ? `${timeStart.split('-')[0]}-${timeEnd.split('-')[1]}`
    : timeStart
  return time ? `周${weekday} ${slot} · ${time}` : `周${weekday} ${slot}`
}

const getCourseMeta = (course) => {
  if (!course) return ''
  if (isTeacherView.value) {
    return [course.className, course.classroomName].filter(Boolean).join(' · ')
  }
  if (isStudentView.value) {
    return [course.teacherName, course.classroomName].filter(Boolean).join(' · ')
  }
  return [course.teacherName, course.classroomName].filter(Boolean).join(' · ')
}

const todayCourses = computed(() => {
  if (currentWeekday.value < 1 || currentWeekday.value > 5) {
    return []
  }
  return courses.value.filter(course => course.dayOfWeek === currentWeekday.value)
})

const todayScheduleTip = computed(() => {
  if (!courses.value.length || currentWeekday.value < 1 || currentWeekday.value > 5) {
    return ''
  }
  const weekday = weekdayLabels[currentWeekday.value - 1]
  if (todayCourses.value.length === 0) {
    return `今天周${weekday}，当前没有安排课程`
  }
  if (isTeacherView.value || isStudentView.value) {
    return `今天周${weekday}，你有 ${todayCourses.value.length} 节课`
  }
  return `今天周${weekday}，共有 ${todayCourses.value.length} 节课`
})

const showCourseInfo = (day, slot) => {
  const course = getCourse(day, slot)
  if (course) {
    currentCourse.value = course
    showCoursePopup.value = true
  }
}

const getDefaultSearchKeyword = () => {
  if (userRole.value === 'TEACHER') {
    return userStore.userInfo?.teacherId ? String(userStore.userInfo.teacherId) : ''
  }
  if (userRole.value === 'STUDENT') {
    return userStore.userInfo?.classId ? String(userStore.userInfo.classId) : ''
  }
  return ''
}

const syncDefaultSearchKeyword = () => {
  const defaultKeyword = getDefaultSearchKeyword()
  if (defaultKeyword) {
    searchKeyword.value = defaultKeyword
  }
}

const autoSearchIfReady = async () => {
  if (!isSearchLocked.value || hasAutoLoaded.value || !timetableId.value || !searchKeyword.value) {
    return
  }
  hasAutoLoaded.value = true
  await handleSearch()
}

function loadRecentSearchMap() {
  const normalizeHistoryItems = (items = []) => items
    .map((item) => {
      if (item && typeof item === 'object') {
        const value = Number(item.value)
        if (!value) {
          return null
        }
        return {
          value,
          label: item.label || String(value)
        }
      }

      const value = Number(item)
      if (!value) {
        return null
      }
      return {
        value,
        label: String(item)
      }
    })
    .filter(Boolean)
    .slice(0, 5)

  try {
    const raw = localStorage.getItem(searchHistoryKey)
    const parsed = raw ? JSON.parse(raw) : {}
    return {
      class: normalizeHistoryItems(parsed.class),
      teacher: normalizeHistoryItems(parsed.teacher),
      classroom: normalizeHistoryItems(parsed.classroom)
    }
  } catch (error) {
    return { class: [], teacher: [], classroom: [] }
  }
}

const persistRecentSearchMap = () => {
  localStorage.setItem(searchHistoryKey, JSON.stringify(recentSearchMap.value))
}

const findLookupLabel = (value) => {
  const normalizedValue = Number(value)
  if (!normalizedValue) {
    return ''
  }
  return lookupOptions.value.find(option => option.value === normalizedValue)?.label || String(normalizedValue)
}

const recordRecentSearch = (type, value, label) => {
  if (!type || !value || isSearchLocked.value) {
    return
  }
  const normalizedValue = Number(value)
  const normalizedLabel = label || findLookupLabel(normalizedValue)
  const history = recentSearchMap.value[type] || []
  recentSearchMap.value = {
    ...recentSearchMap.value,
    [type]: [
      { value: normalizedValue, label: normalizedLabel },
      ...history.filter(item => item.value !== normalizedValue)
    ].slice(0, 5)
  }
  persistRecentSearchMap()
}

const applyRecentSearch = async (item) => {
  searchKeyword.value = item.value
  ensureCurrentLookupOption(item.value, item.label)
  await handleSearch()
}

const ensureCurrentLookupOption = (value, label) => {
  const normalizedValue = Number(value)
  if (!normalizedValue) {
    return
  }
  if (!lookupOptions.value.some(option => option.value === normalizedValue)) {
    lookupOptions.value = [{ value: normalizedValue, label: label || String(value) }, ...lookupOptions.value]
  }
}

const fetchLookupOptions = async (keyword = '') => {
  const requestApi = lookupSearchApiMap[queryType.value]
  if (!requestApi || isSearchLocked.value) {
    return
  }
  lookupLoading.value = true
  try {
    const res = await requestApi({ keyword, limit: 20 })
    lookupOptions.value = res.data || []
    if (searchKeyword.value) {
      ensureCurrentLookupOption(searchKeyword.value)
    }
  } catch (error) {
    lookupOptions.value = []
  } finally {
    lookupLoading.value = false
  }
}

const handleLookupSearch = async (keyword) => {
  await fetchLookupOptions(keyword)
}

const handleLookupFocus = async () => {
  if (!lookupOptions.value.length) {
    await fetchLookupOptions('')
  }
}

const resetSearchResult = () => {
  courses.value = []
  hasSearched.value = false
  currentCourse.value = null
  showCoursePopup.value = false
}

const handleLookupChange = async (value, option) => {
  if (!value) {
    searchKeyword.value = ''
    resetSearchResult()
    return
  }

  ensureCurrentLookupOption(value, option?.label)
  await handleSearch()
}

const handleSearch = async () => {
  const keyword = String(searchKeyword.value || '').trim()
  searchKeyword.value = isSearchLocked.value ? keyword : (keyword ? Number(keyword) : null)

  if (!keyword) {
    message.warning('请输入查询条件')
    return
  }
  if (!/^\d+$/.test(keyword)) {
    message.warning('请输入对应的数字编号')
    return
  }

  loading.value = true
  try {
    if (!timetableId.value) {
      const semester = getCurrentSemester()
      const timetableRes = await getLatestTimetable(semester)
      if (timetableRes.data) {
        timetableId.value = timetableRes.data.id
      } else {
        message.warning('暂无可用课表')
        return
      }
    }

    let res
    const id = keyword
    if (queryType.value === 'class') {
      res = await getClassTimetable(timetableId.value, id)
    } else if (queryType.value === 'teacher') {
      res = await getTeacherTimetable(timetableId.value, id)
    } else {
      res = await getClassroomTimetable(timetableId.value, id)
    }
    courses.value = res.data || []
    hasSearched.value = true
    recordRecentSearch(queryType.value, id, findLookupLabel(id))
  } catch (e) {
    message.error(e.message || '查询失败')
    courses.value = []
    hasSearched.value = true
  } finally {
    loading.value = false
  }
}

watch(
  () => [userRole.value, userStore.userInfo?.teacherId, userStore.userInfo?.classId, timetableId.value, queryType.value],
  async () => {
    syncDefaultSearchKeyword()
    lookupOptions.value = []
    if (!isSearchLocked.value) {
      hasAutoLoaded.value = false
      return
    }
    await autoSearchIfReady()
  },
  { immediate: true }
)

onMounted(async () => {
  try {
    syncDefaultSearchKeyword()
    const semester = getCurrentSemester()
    const res = await getLatestTimetable(semester)
    if (res.data) {
      timetableId.value = res.data.id
      await autoSearchIfReady()
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.schedule-page {
  position: relative;
  animation: fadeIn 0.3s ease-out;
  padding-bottom: var(--spacing-xl);
}

.schedule-page::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 240px;
  background:
    radial-gradient(circle at top left, rgba(184, 102, 89, 0.14), transparent 42%),
    radial-gradient(circle at top right, rgba(111, 137, 163, 0.14), transparent 34%);
  pointer-events: none;
  opacity: 0.85;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

.card {
  position: relative;
  border: 1px solid rgba(145, 120, 91, 0.18);
  border-radius: var(--radius-xl);
  background: var(--fabric-surface), rgba(255, 250, 243, 0.78);
  backdrop-filter: blur(14px);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-md);
}

.card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  border: 1px solid rgba(255, 255, 255, 0.34);
  pointer-events: none;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-lg);
  letter-spacing: 0.01em;
}

.search-wrapper {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex-wrap: wrap;
  padding: var(--spacing-md);
  border-radius: calc(var(--radius-lg) - 2px);
  background: rgba(255, 252, 247, 0.72);
  border: 1px solid rgba(145, 120, 91, 0.12);
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.content-body {
  margin-top: var(--spacing-lg);
}

.search-helper {
  margin-top: var(--spacing-md);
  display: grid;
  gap: var(--spacing-sm);
  padding-top: var(--spacing-sm);
  border-top: 1px dashed rgba(145, 120, 91, 0.16);
}

.search-tip {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.role-guide {
  margin-bottom: var(--spacing-md);
  padding: 14px 16px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, rgba(114, 137, 103, 0.1), rgba(255, 250, 243, 0.95));
  border: 1px solid rgba(114, 137, 103, 0.14);
}

.role-guide-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.role-guide-text {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.today-tip {
  font-size: 12px;
  color: var(--primary-color);
  font-weight: 600;
}

.recent-searches {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.recent-label {
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 0.04em;
}

.recent-chip {
  border-radius: 999px;
  background: rgba(255, 248, 238, 0.82);
  border: 1px solid rgba(145, 120, 91, 0.14);
}

.timetable-card {
  overflow: hidden;
  padding-bottom: var(--spacing-lg);
}

.timetable-grid-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  padding: var(--spacing-sm);
  border-radius: calc(var(--radius-lg) - 4px);
  background: rgba(255, 250, 243, 0.5);
}

.timetable-grid {
  display: grid;
  grid-template-columns: 50px repeat(5, 1fr);
  gap: 8px;
  background: transparent;
  min-width: 600px;
}

.timetable-header {
  background: rgba(244, 237, 222, 0.92);
  padding: var(--spacing-sm);
  text-align: center;
  font-weight: 600;
  font-size: 12px;
  color: var(--text-secondary);
  border-radius: var(--radius-md);
  border: 1px solid rgba(145, 120, 91, 0.1);
}

.timetable-slot-header {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.timetable-slot-index {
  font-weight: 700;
  color: var(--text-primary);
}

.timetable-slot-time {
  font-size: 11px;
  line-height: 1.3;
  color: var(--text-muted);
}

.timetable-day-header.is-today {
  background: rgba(114, 137, 103, 0.18);
  color: var(--text-primary);
}

.timetable-cell {
  background: rgba(255, 252, 247, 0.72);
  min-height: 88px;
  padding: var(--spacing-xs);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), background var(--transition-fast);
  cursor: pointer;
  border-radius: var(--radius-md);
  border: 1px solid rgba(145, 120, 91, 0.09);
}

.timetable-cell:hover {
  background: rgba(255, 250, 243, 0.96);
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(101, 78, 52, 0.06);
}

.timetable-cell.has-course {
  background: linear-gradient(180deg, rgba(81, 202, 186, 0.08), rgba(255, 255, 255, 0.68));
}

.timetable-cell.is-today-column {
  background-color: rgba(114, 137, 103, 0.04);
}

.timetable-cell.is-today-column.has-course {
  background: linear-gradient(135deg, rgba(114, 137, 103, 0.16) 0%, rgba(81, 202, 186, 0.08) 100%);
}

.course-block {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0)),
    var(--primary-gradient);
  color: white;
  padding: var(--spacing-sm);
  border-radius: var(--radius-md);
  font-size: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.24);
  box-shadow: 0 12px 24px rgba(82, 105, 71, 0.18);
}

.course-block-name {
  font-weight: 600;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.course-block-info {
  opacity: 0.9;
  font-size: 10px;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.text-muted {
  color: var(--text-muted);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--spacing-3xl) 0;
}

.course-dialog {
  margin: var(--spacing-md);
}

.course-details {
  padding: var(--spacing-md) 0;
}

.mt-16 {
  margin-top: 16px;
}

.mobile-only.card {
  padding-top: var(--spacing-lg);
}

:deep(.schedule-page .n-button) {
  min-height: 44px;
}

:deep(.schedule-page .n-base-selection) {
  border-radius: var(--radius-md);
}

:deep(.schedule-page .n-list-item) {
  padding: var(--spacing-md) 0;
}

@media (min-width: 1024px) {
  .timetable-grid {
    grid-template-columns: 80px repeat(5, 1fr);
  }

  .timetable-header {
    padding: var(--spacing-md);
    font-size: 14px;
  }

  .timetable-slot-time {
    font-size: 12px;
  }

  .timetable-cell {
    min-height: 112px;
    padding: var(--spacing-sm);
  }

  .course-block {
    padding: var(--spacing-sm);
    font-size: 12px;
  }
}

@media (min-width: 1440px) {
  .timetable-grid {
    grid-template-columns: 100px repeat(5, 1fr);
  }
}

@media (min-width: 1920px) {
  .timetable-cell {
    min-height: 120px;
  }

  .course-block-name {
    font-size: 14px;
    margin-bottom: var(--spacing-xs);
  }

  .course-block-info {
    font-size: 11px;
  }
}

@media (min-width: 2560px) {
  .timetable-cell {
    min-height: 150px;
  }

  .course-block-name {
    font-size: 16px;
  }

  .course-block-info {
    font-size: 13px;
  }
}

@media (max-width: 767px) {
  .card {
    padding: var(--spacing-lg);
    border-radius: var(--radius-lg);
  }

  .search-wrapper {
    flex-direction: column;
    align-items: stretch;
    padding: var(--spacing-sm);
  }

  .search-input {
    min-width: auto;
  }

  .timetable-grid-wrapper {
    padding: 0;
    background: transparent;
  }

  .timetable-slot-time {
    font-size: 9px;
  }
}
</style>
