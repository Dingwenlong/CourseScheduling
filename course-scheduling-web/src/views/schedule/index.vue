<template>
  <div class="page page-with-tabbar">
    <van-nav-bar title="课表查询" class="custom-nav" />

    <div class="search-wrapper mt-16">
      <van-dropdown-menu>
        <van-dropdown-item v-model="queryType" :options="typeOptions" />
      </van-dropdown-menu>
      <van-search
        v-model="searchKeyword"
        :placeholder="getPlaceholder"
        @search="handleSearch"
        class="flex-1"
      />
    </div>

    <van-loading v-if="loading" class="loading-container" />

    <template v-else-if="timetableId">
      <div class="card">
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

      <div class="card">
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
.course-popup {
  padding: var(--spacing-xl);
}

.custom-nav {
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

@media (min-width: 768px) {
  .custom-nav {
    display: none;
  }
}

.timetable-container {
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

@media (min-width: 1024px) {
  .page {
    max-width: var(--content-max-width);
    margin: 0 auto;
  }
  
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
  .page {
    max-width: var(--content-max-width-wide);
  }
  
  .timetable-grid {
    grid-template-columns: 100px repeat(5, 1fr);
  }
}

@media (min-width: 1920px) {
  .page {
    max-width: var(--content-max-width-ultra);
  }
  
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
  .page {
    max-width: var(--content-max-width-super);
  }

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
