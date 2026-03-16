<template>
  <PageContainer with-tabbar class="schedule-page">
    <PageHeader title="课表查询" />

    <div class="card animate-fade-in">
      <div class="section-title">查询条件</div>
      <div class="search-wrapper">
        <n-select
          v-model:value="queryType"
          :options="typeOptions"
          placeholder="查询类型"
          style="width: 150px"
        />
        <n-input
          v-model:value="searchKeyword"
          :placeholder="getPlaceholder"
          class="search-input"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <n-icon>
              <SearchOutline />
            </n-icon>
          </template>
        </n-input>
        <n-button type="primary" @click="handleSearch">查询</n-button>
      </div>
    </div>

    <div class="content-body">
      <n-spin :show="loading" class="loading-container">
        <div v-if="timetableId && courses.length > 0">
          <div class="card timetable-card">
            <div class="section-title desktop-only">课表</div>
            <div class="timetable-grid-wrapper">
              <div class="timetable-grid">
                <div class="timetable-header"></div>
                <div v-for="day in 5" :key="'h'+day" class="timetable-header">
                  周{{ ['一', '二', '三', '四', '五'][day - 1] }}
                </div>
                <template v-for="slot in 10" :key="'s'+slot">
                  <div class="timetable-header">{{ slot }}</div>
                  <div
                    v-for="day in 5"
                    :key="'c'+day+'-'+slot"
                    class="timetable-cell"
                    :class="{ 'has-course': getCourse(day, slot) }"
                    @click="showCourseInfo(day, slot)"
                  >
                    <div v-if="getCourse(day, slot)" class="course-block">
                      <div class="course-block-name">{{ getCourse(day, slot).courseName }}</div>
                      <div class="course-block-info">{{ getCourse(day, slot).classroomName }}</div>
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
                <div class="text-muted">周{{ course.dayOfWeek }} 第{{ course.slotNo }}节</div>
                <div class="text-muted" style="font-size: 12px;">{{ course.classroomName }}</div>
              </n-list-item>
            </n-list>
          </div>
        </div>

        <n-empty v-else description="请先选择课表并查询" />
      </n-spin>
    </div>

    <n-modal v-model:show="showCoursePopup" preset="card" title="课程详情" :style="{ width: '500px' }" class="course-dialog">
      <div v-if="currentCourse" class="course-details">
        <n-descriptions :column="1" bordered>
          <n-descriptions-item label="课程名称">{{ currentCourse.courseName }}</n-descriptions-item>
          <n-descriptions-item label="教师">{{ currentCourse.teacherName }}</n-descriptions-item>
          <n-descriptions-item label="班级">{{ currentCourse.className }}</n-descriptions-item>
          <n-descriptions-item label="教室">{{ currentCourse.classroomName }}</n-descriptions-item>
          <n-descriptions-item label="时间">周{{ currentCourse.dayOfWeek }} 第{{ currentCourse.slotNo }}节</n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getLatestTimetable, getClassTimetable, getTeacherTimetable, getClassroomTimetable } from '@/api/timetable'
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

const loading = ref(false)
const timetableId = ref(null)
const courses = ref([])
const queryType = ref('class')
const searchKeyword = ref('')
const showCoursePopup = ref(false)
const currentCourse = ref(null)

const typeOptions = [
  { label: '按班级查询', value: 'class' },
  { label: '按教师查询', value: 'teacher' },
  { label: '按教室查询', value: 'classroom' }
]

const getPlaceholder = computed(() => {
  const map = {
    'class': '请输入班级ID',
    'teacher': '请输入教师ID',
    'classroom': '请输入教室ID'
  }
  return map[queryType.value]
})

const courseMap = computed(() => {
  const map = new Map()
  for (const course of courses.value) {
    map.set(`${course.dayOfWeek}_${course.slotNo}`, course)
  }
  return map
})

const getCourse = (day, slot) => {
  return courseMap.value.get(`${day}_${slot}`) || null
}

const showCourseInfo = (day, slot) => {
  const course = getCourse(day, slot)
  if (course) {
    currentCourse.value = course
    showCoursePopup.value = true
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value) {
    message.warning('请输入查询条件')
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
    const id = searchKeyword.value
    if (queryType.value === 'class') {
      res = await getClassTimetable(timetableId.value, id)
    } else if (queryType.value === 'teacher') {
      res = await getTeacherTimetable(timetableId.value, id)
    } else {
      res = await getClassroomTimetable(timetableId.value, id)
    }
    courses.value = res.data || []
  } catch (e) {
    message.error(e.message || '查询失败')
    courses.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const semester = getCurrentSemester()
    const res = await getLatestTimetable(semester)
    if (res.data) {
      timetableId.value = res.data.id
    }
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.schedule-page {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

.card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
}

.search-wrapper {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.content-body {
  margin-top: var(--spacing-md);
}

.timetable-card {
  overflow: hidden;
}

.timetable-grid-wrapper {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.timetable-grid {
  display: grid;
  grid-template-columns: 50px repeat(5, 1fr);
  gap: 1px;
  background: rgba(145, 120, 91, 0.14);
  min-width: 600px;
}

.timetable-header {
  background: var(--bg-secondary);
  padding: var(--spacing-sm);
  text-align: center;
  font-weight: 600;
  font-size: 12px;
  color: var(--text-secondary);
}

.timetable-cell {
  background: var(--bg-primary);
  min-height: 80px;
  padding: var(--spacing-xs);
  transition: all 0.2s;
  cursor: pointer;
}

.timetable-cell:hover {
  background: var(--bg-secondary);
}

.timetable-cell.has-course {
  background: rgba(81, 202, 186, 0.05);
}

.course-block {
  background: var(--primary-gradient);
  color: white;
  padding: var(--spacing-xs);
  border-radius: var(--radius-sm);
  font-size: 10px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: var(--shadow-sm);
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
  font-size: 9px;
}

.course-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
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

@media (min-width: 1024px) {
  .timetable-grid {
    grid-template-columns: 80px repeat(5, 1fr);
  }

  .timetable-header {
    padding: var(--spacing-md);
    font-size: 14px;
  }

  .timetable-cell {
    min-height: 100px;
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
  .search-wrapper {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    min-width: auto;
  }
}
</style>
