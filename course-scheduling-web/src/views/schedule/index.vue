<template>
  <div class="page page-with-tabbar schedule-page">
    <van-nav-bar title="课表查询" class="custom-nav" />

    <div class="table-container animate-fade-in">
      <div class="table-header">
        <div class="table-title desktop-only">查询条件</div>
        <div class="table-filters search-wrapper">
          <van-dropdown-menu class="type-dropdown">
            <van-dropdown-item v-model="queryType" :options="typeOptions" />
          </van-dropdown-menu>
          <van-search
            v-model="searchKeyword"
            :placeholder="getPlaceholder"
            @search="handleSearch"
            class="search-input"
          />
        </div>
      </div>

      <div class="content-body">
        <van-loading v-if="loading" class="loading-container" />

        <template v-else-if="timetableId">
          <div class="timetable-card">
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

          <div class="card mt-16 mobile-only">
            <div class="page-title">课程列表</div>
            <van-cell-group inset>
              <van-cell
                v-for="course in courses"
                :key="course.id"
                :title="course.courseName"
                :label="`周${course.dayOfWeek} 第${course.slotNo}节`"
              >
                <template #value>
                  <div class="text-muted" style="font-size: 12px;">{{ course.classroomName }}</div>
                </template>
              </van-cell>
            </van-cell-group>
          </div>
        </template>

        <van-empty v-else description="请先选择课表" />
      </div>
    </div>

    <van-popup v-model:show="showCoursePopup" position="bottom" round style="height: 40%;">
      <div class="course-popup" v-if="currentCourse">
        <div class="page-title">{{ currentCourse.courseName }}</div>
        <van-cell-group inset>
          <van-cell title="教师" :value="currentCourse.teacherName" />
          <van-cell title="班级" :value="currentCourse.className" />
          <van-cell title="教室" :value="currentCourse.classroomName" />
          <van-cell title="时间" :value="`周${currentCourse.dayOfWeek} 第${currentCourse.slotNo}节`" />
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import dayjs from 'dayjs'
import { getLatestTimetable, getClassTimetable, getTeacherTimetable, getClassroomTimetable } from '@/api/timetable'

const loading = ref(false)
const timetableId = ref(null)
const courses = ref([])
const queryType = ref('class')
const searchKeyword = ref('')
const showCoursePopup = ref(false)
const currentCourse = ref(null)

const typeOptions = [
  { text: '按班级查询', value: 'class' },
  { text: '按教师查询', value: 'teacher' },
  { text: '按教室查询', value: 'classroom' }
]

const getPlaceholder = computed(() => {
  const map = {
    'class': '请输入班级ID',
    'teacher': '请输入教师ID',
    'classroom': '请输入教室ID'
  }
  return map[queryType.value]
})

const getCourse = (day, slot) => {
  return courses.value.find(c => c.dayOfWeek === day && c.slotNo === slot)
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
    showToast('请输入查询条件')
    return
  }

  loading.value = true
  try {
    if (!timetableId.value) {
      const semester = dayjs().format('YYYY') + (dayjs().month() < 7 ? '-1' : '-2')
      const timetableRes = await getLatestTimetable(semester)
      if (timetableRes.data) {
        timetableId.value = timetableRes.data.id
      } else {
        showToast('暂无可用课表')
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
    showToast('查询失败')
    courses.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const semester = dayjs().format('YYYY') + (dayjs().month() < 7 ? '-1' : '-2')
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

.table-container {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  margin-top: var(--spacing-md);
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.table-filters {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex: 1;
  justify-content: flex-end;
}

.type-dropdown {
  width: 150px;
}

.search-input {
  flex: 1;
  max-width: 300px;
}

.content-body {
  padding: var(--spacing-xl);
}

.timetable-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  overflow-x: auto;
}

.course-popup {
  padding: var(--spacing-xl);
}

.custom-nav {
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

.desktop-only {
  display: none;
}

.mobile-only {
  display: block;
}

@media (min-width: 768px) {
  .custom-nav {
    display: none;
  }
  
  .desktop-only {
    display: block;
  }
  
  .mobile-only {
    display: none;
  }
  
  .table-container {
    margin-top: 0;
  }
}

.timetable-grid {
  display: grid;
  grid-template-columns: 50px repeat(5, 1fr);
  gap: 1px;
  background: var(--border-light);
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
</style>
