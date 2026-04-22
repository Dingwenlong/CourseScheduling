<template>
  <PageContainer>
    <div class="desktop-detail-page">
      <PageHeader :title="timetable?.name || '课表详情'" :subtitle="timetable?.semester ? `${timetable.semester} · 第${timetable.version}版` : ''">
        <template #actions>
          <n-button quaternary @click="goBack" class="back-btn">
            <template #icon>
              <n-icon>
                <ArrowBackOutline />
              </n-icon>
            </template>
            返回
          </n-button>
          <n-button v-if="details.length > 0" quaternary class="no-print" @click="handlePrint">
            <template #icon>
              <n-icon>
                <PrintOutline />
              </n-icon>
            </template>
            打印方案
          </n-button>
          <n-button v-if="details.length > 0" quaternary class="no-print" @click="handleExportCsv">
            <template #icon>
              <n-icon>
                <DownloadOutline />
              </n-icon>
            </template>
            导出方案
          </n-button>
          <n-tag :type="getStatusTagType(timetable?.status)" size="large">
            {{ getStatusText(timetable?.status) }}
          </n-tag>
          <n-dropdown trigger="click" :options="dropdownOptions" @select="onDropdownSelect">
            <n-button>
              <template #icon>
                <n-icon>
                  <EllipsisHorizontalOutline />
                </n-icon>
              </template>
              操作
            </n-button>
          </n-dropdown>
        </template>
      </PageHeader>

    <n-spin :show="loading" class="loading-container">
      <div v-if="timetable">
        <div class="view-guide" v-if="isScopedViewer">
          <div class="view-guide-title">{{ viewGuideTitle }}</div>
          <div class="view-guide-text">{{ viewGuideText }}</div>
          <div class="view-guide-chips">
            <span class="view-guide-chip">{{ primaryGuideChip }}</span>
            <span class="view-guide-chip">白色空格表示该时段没有安排课程</span>
            <span class="view-guide-chip">红色虚线表示该课程存在时间冲突</span>
          </div>
        </div>

        <div class="stats-grid">
          <div
            v-for="card in summaryCards"
            :key="card.key"
            class="stat-card-desktop"
            :class="{ 'stat-card-warning': card.emphasis === 'danger' }"
            tabindex="0"
            role="article"
          >
            <div class="stat-icon" :style="{ background: card.background }">
              <n-icon size="32" :color="card.color">
                <component :is="card.icon" />
              </n-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value" :class="card.valueClass">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-hint">{{ card.hint }}</div>
            </div>
          </div>
        </div>

        <n-tabs v-model:value="activeTab" class="content-tabs">
          <n-tab-pane name="timetable" :tab="timetableTabLabel">
            <div class="card timetable-card">
              <div class="timetable-panel-header">
                <div>
                  <div class="panel-title">{{ panelTitle }}</div>
                  <div class="panel-helper">{{ panelHelper }}</div>
                </div>
                <div v-if="todayScheduleTip" class="today-pill">
                  {{ todayScheduleTip }}
                </div>
              </div>
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
                      'conflict': getCourse(day, slot)?.isConflict === 1,
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
          </n-tab-pane>

          <n-tab-pane name="courses" :tab="courseListTabLabel">
            <div class="card">
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>课程名称</th>
                      <th v-if="showTeacherColumn">教师</th>
                      <th v-if="showClassColumn">班级</th>
                      <th>教室</th>
                      <th>时间</th>
                      <th>状态</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="detail in details" :key="detail.id">
                      <td class="name-cell">{{ detail.courseName }}</td>
                      <td v-if="showTeacherColumn">{{ detail.teacherName }}</td>
                      <td v-if="showClassColumn">{{ detail.className }}</td>
                      <td>{{ detail.classroomName }}</td>
                      <td>{{ courseTimeText(detail) }}</td>
                      <td>
                        <n-tag v-if="detail.isConflict === 1" type="error">冲突</n-tag>
                        <n-tag v-else type="success">正常</n-tag>
                      </td>
                      <td>
                        <n-button size="small" type="primary" @click="showDetailInfo(detail)">查看</n-button>
                        <n-button v-if="canAdjust" size="small" type="default" @click="goAdjustmentFromDetail(detail)">调课</n-button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <n-empty v-if="details.length === 0" description="暂无数据" />
            </div>
          </n-tab-pane>

          <n-tab-pane name="conflicts" :tab="conflictTabLabel">
            <div class="card">
              <div v-if="conflicts.length === 0" class="empty-container">
                <n-icon size="64" color="var(--success-color)">
                  <CheckmarkCircleOutline />
                </n-icon>
                <div>暂无冲突</div>
              </div>
              <div v-else class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>课程名称</th>
                      <th v-if="showTeacherColumn">教师</th>
                      <th v-if="showClassColumn">班级</th>
                      <th>冲突信息</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="conflict in conflicts" :key="conflict.id">
                      <td class="name-cell">{{ conflict.courseName }}</td>
                      <td v-if="showTeacherColumn">{{ conflict.teacherName }}</td>
                      <td v-if="showClassColumn">{{ conflict.className }}</td>
                      <td class="text-danger">{{ conflict.conflictInfo }}</td>
                      <td>
                        <n-button v-if="canAdjust" size="small" type="error" @click="goAdjustmentFromDetail(conflict)">处理</n-button>
                        <n-button v-else size="small" type="primary" @click="showDetailInfo(conflict)">查看</n-button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </n-tab-pane>
        </n-tabs>
      </div>
    </n-spin>

    <n-modal v-model:show="showCoursePopup" preset="card" :title="courseDialogTitle" :style="{ width: isMobile ? 'calc(100% - 32px)' : '560px', maxWidth: '100%' }" class="course-dialog">
      <div v-if="currentCourse" class="course-details">
        <div class="course-detail-hero">
          <div class="course-detail-main">
            <div class="course-detail-name">{{ currentCourse.courseName }}</div>
            <div class="course-detail-time">{{ courseTimeText(currentCourse) }}</div>
          </div>
          <n-tag :type="currentCourse.isConflict === 1 ? 'error' : 'success'" round>
            {{ currentCourse.isConflict === 1 ? '存在冲突' : '安排正常' }}
          </n-tag>
        </div>

        <div class="course-detail-grid">
          <div class="course-detail-card">
            <div class="course-detail-card-label">{{ primaryAudienceLabel }}</div>
            <div class="course-detail-card-value">{{ primaryAudienceValue }}</div>
            <div class="course-detail-card-hint">{{ primaryAudienceHint }}</div>
          </div>
          <div class="course-detail-card">
            <div class="course-detail-card-label">{{ secondaryAudienceLabel }}</div>
            <div class="course-detail-card-value">{{ secondaryAudienceValue }}</div>
            <div class="course-detail-card-hint">{{ secondaryAudienceHint }}</div>
          </div>
        </div>

        <div class="course-detail-section">
          <div class="course-detail-section-title">上课安排</div>
          <div class="course-detail-list">
            <div class="course-detail-row">
              <span class="course-detail-row-label">时间</span>
              <span class="course-detail-row-value">{{ courseTimeText(currentCourse) }}</span>
            </div>
            <div class="course-detail-row" v-if="currentCourse.weeks">
              <span class="course-detail-row-label">周次</span>
              <span class="course-detail-row-value">{{ currentCourse.weeks }}</span>
            </div>
            <div class="course-detail-row">
              <span class="course-detail-row-label">教室</span>
              <span class="course-detail-row-value">{{ currentCourse.classroomName || '-' }}</span>
            </div>
          </div>
        </div>

        <div v-if="currentCourse.isConflict === 1" class="course-conflict-panel">
          <div class="course-conflict-title">需要处理的冲突</div>
          <div class="course-conflict-text">{{ currentCourse.conflictInfo }}</div>
        </div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button v-if="canAdjust" type="primary" @click="goAdjustment">
            {{ currentCourse?.isConflict === 1 ? '去处理冲突' : '申请调课' }}
          </n-button>
          <n-button @click="showCoursePopup = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import dayjs from 'dayjs'
import { getTimetableById, getTimetableDetails, getConflicts, publishTimetable, archiveTimetable, deleteTimetable } from '@/api/timetable'
import { exportCoursesAsCsv, printCurrentPage } from '@/utils/timetable-export'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { useLayoutStore } from '@/stores/layout'
import { useUserStore } from '@/stores/user'
import {
  NButton,
  NIcon,
  NSpin,
  NTag,
  NTabs,
  NTabPane,
  NEmpty,
  NModal,
  NDescriptions,
  NDescriptionsItem,
  NSpace,
  NDropdown
} from 'naive-ui'
import {
  ArrowBackOutline,
  EllipsisHorizontalOutline,
  DownloadOutline,
  ListOutline,
  PrintOutline,
  CheckmarkDoneOutline,
  WarningOutline,
  TrendingUpOutline,
  CheckmarkCircleOutline,
  PeopleOutline,
  CalendarOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const layoutStore = useLayoutStore()
const userStore = useUserStore()

const loading = ref(true)
const timetable = ref(null)
const details = ref([])
const conflicts = ref([])
const activeTab = ref('timetable')
const showCoursePopup = ref(false)
const currentCourse = ref(null)
const isMobile = ref(window.innerWidth < 768)
const userRole = computed(() => userStore.userInfo?.role)
const isTeacherView = computed(() => userRole.value === 'TEACHER')
const isStudentView = computed(() => userRole.value === 'STUDENT')
const isScopedViewer = computed(() => isTeacherView.value || isStudentView.value)
const canAdjust = computed(() => ['ADMIN', 'TEACHER'].includes(userRole.value))
const showTeacherColumn = computed(() => !isTeacherView.value)
const showClassColumn = computed(() => !isStudentView.value)
const currentWeekday = ref(dayjs().day())

const updateIsMobile = () => {
  isMobile.value = window.innerWidth < 768
}

const dropdownOptions = ref([])

const updateDropdownOptions = () => {
  const options = []
  if (timetable.value?.status === 'DRAFT') {
    options.push({ label: '发布课表', key: 'publish' })
  }
  if (timetable.value?.status !== 'ARCHIVED') {
    options.push({ label: '归档课表', key: 'archive' })
  }
  if (timetable.value?.status === 'DRAFT') {
    options.push({ label: '删除课表', key: 'delete' })
  }
  dropdownOptions.value = options.length > 0 ? options : [{ label: '无操作', key: 'none' }]
}

const getStatusText = (status) => {
  const map = { 'DRAFT': '草稿', 'PUBLISHED': '已发布', 'ARCHIVED': '已归档' }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = { 'DRAFT': 'warning', 'PUBLISHED': 'success', 'ARCHIVED': 'default' }
  return map[status] || 'default'
}

const uniqueCount = (items, key) => {
  return new Set(
    items
      .map(item => item?.[key])
      .filter(Boolean)
  ).size
}

const scopedTeachingDays = computed(() => uniqueCount(details.value, 'dayOfWeek'))

const viewGuideTitle = computed(() => {
  if (isTeacherView.value) {
    return '这是你的授课安排视图'
  }
  if (isStudentView.value) {
    return '这是你的上课安排视图'
  }
  return ''
})

const viewGuideText = computed(() => {
  if (isTeacherView.value) {
    return '页面只展示当前教师需要授课的课程。点击绿色课块可查看授课班级、教室、周次和调课入口。'
  }
  if (isStudentView.value) {
    return '页面只展示当前班级需要上的课程。点击绿色课块可查看任课教师、教室、周次等详细信息。'
  }
  return ''
})

const primaryGuideChip = computed(() => {
  if (isTeacherView.value) {
    return '绿色课块表示你要上的课'
  }
  if (isStudentView.value) {
    return '绿色课块表示你要上的课'
  }
  return ''
})

const timetableTabLabel = computed(() => {
  if (isTeacherView.value) {
    return '我的授课表'
  }
  if (isStudentView.value) {
    return '我的课表'
  }
  return '课表视图'
})

const courseListTabLabel = computed(() => {
  if (isTeacherView.value) {
    return '授课明细'
  }
  if (isStudentView.value) {
    return '课程明细'
  }
  return '课程列表'
})

const conflictTabLabel = computed(() => {
  if (isTeacherView.value) {
    return `待处理冲突(${conflicts.value.length})`
  }
  if (isStudentView.value) {
    return `冲突提醒(${conflicts.value.length})`
  }
  return `冲突(${conflicts.value.length})`
})

const panelTitle = computed(() => {
  if (isTeacherView.value) {
    return '按上课时间查看你的授课安排'
  }
  if (isStudentView.value) {
    return '按上课时间查看你的课程安排'
  }
  return '按时间查看当前课表'
})

const panelHelper = computed(() => {
  if (isTeacherView.value) {
    return '每个绿色课块都代表一节你需要授课的课程，课块下方会直接显示授课班级和教室；今天的列会额外高亮。'
  }
  if (isStudentView.value) {
    return '每个绿色课块都代表一节你需要参加的课程，课块下方会直接显示任课教师和教室；今天的列会额外高亮。'
  }
  return '点击课块可以查看课程、班级、教师、教室和冲突详情，今天所在列会额外高亮。'
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

const exportScopeLabel = computed(() => timetable.value?.name || '排课方案')

const courseDialogTitle = computed(() => {
  if (isTeacherView.value) {
    return '授课详情'
  }
  if (isStudentView.value) {
    return '课程详情'
  }
  return '排课详情'
})

const primaryAudienceLabel = computed(() => {
  if (isTeacherView.value) {
    return '授课班级'
  }
  if (isStudentView.value) {
    return '任课教师'
  }
  return '任课教师'
})

const primaryAudienceValue = computed(() => {
  if (!currentCourse.value) return '-'
  if (isTeacherView.value) {
    return currentCourse.value.className || '-'
  }
  return currentCourse.value.teacherName || '-'
})

const primaryAudienceHint = computed(() => {
  if (isTeacherView.value) {
    return '这节课要面对的班级'
  }
  if (isStudentView.value) {
    return '本节课的授课教师'
  }
  return '本节课的授课教师'
})

const secondaryAudienceLabel = computed(() => {
  if (isTeacherView.value) {
    return '授课地点'
  }
  if (isStudentView.value) {
    return '上课班级'
  }
  return '上课班级'
})

const secondaryAudienceValue = computed(() => {
  if (!currentCourse.value) return '-'
  if (isTeacherView.value) {
    return currentCourse.value.classroomName || '-'
  }
  return currentCourse.value.className || '-'
})

const secondaryAudienceHint = computed(() => {
  if (isTeacherView.value) {
    return '系统当前安排的教室'
  }
  if (isStudentView.value) {
    return '当前课程所属班级'
  }
  return '当前课程所属班级'
})

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

const todayCourses = computed(() => {
  if (currentWeekday.value < 1 || currentWeekday.value > 5) {
    return []
  }
  return details.value.filter(detail => detail.dayOfWeek === currentWeekday.value)
})

const todayScheduleTip = computed(() => {
  if (currentWeekday.value < 1 || currentWeekday.value > 5) {
    return ''
  }
  const weekday = weekdayLabels[currentWeekday.value - 1]
  if (todayCourses.value.length === 0) {
    return `今天周${weekday}，当前没有安排课程`
  }
  if (isTeacherView.value || isStudentView.value) {
    return `今天周${weekday}，你有 ${todayCourses.value.length} 节课要上`
  }
  return `今天周${weekday}，共有 ${todayCourses.value.length} 节课`
})

const summaryCards = computed(() => {
  if (isTeacherView.value) {
    return [
      {
        key: 'sessions',
        value: details.value.length,
        label: '本周课次',
        hint: '你本周实际要上的课次',
        icon: ListOutline,
        color: 'var(--primary-color)',
        background: 'rgba(114, 137, 103, 0.12)',
        valueClass: ''
      },
      {
        key: 'classes',
        value: uniqueCount(details.value, 'className'),
        label: '涉及班级',
        hint: '这张课表里你负责的班级数',
        icon: PeopleOutline,
        color: 'var(--success-color)',
        background: 'rgba(125, 149, 99, 0.14)',
        valueClass: 'text-success'
      },
      {
        key: 'conflicts',
        value: conflicts.value.length,
        label: '待处理冲突',
        hint: conflicts.value.length > 0 ? '建议优先处理冲突课次' : '当前没有时间冲突',
        icon: WarningOutline,
        color: 'var(--danger-color)',
        background: 'rgba(184, 102, 89, 0.14)',
        valueClass: 'text-danger',
        emphasis: conflicts.value.length > 0 ? 'danger' : ''
      },
      {
        key: 'days',
        value: `${scopedTeachingDays.value}天`,
        label: '上课天数',
        hint: '本周有课的工作日数量',
        icon: CalendarOutline,
        color: 'var(--warning-color)',
        background: 'rgba(198, 144, 84, 0.14)',
        valueClass: 'text-primary'
      }
    ]
  }

  if (isStudentView.value) {
    return [
      {
        key: 'sessions',
        value: details.value.length,
        label: '本周课次',
        hint: '你本周需要参加的课程数',
        icon: ListOutline,
        color: 'var(--primary-color)',
        background: 'rgba(114, 137, 103, 0.12)',
        valueClass: ''
      },
      {
        key: 'teachers',
        value: uniqueCount(details.value, 'teacherName'),
        label: '任课教师',
        hint: '当前课表涉及的授课教师数',
        icon: PeopleOutline,
        color: 'var(--success-color)',
        background: 'rgba(125, 149, 99, 0.14)',
        valueClass: 'text-success'
      },
      {
        key: 'conflicts',
        value: conflicts.value.length,
        label: '冲突提醒',
        hint: conflicts.value.length > 0 ? '请尽快联系教师或管理员' : '当前没有时间冲突',
        icon: WarningOutline,
        color: 'var(--danger-color)',
        background: 'rgba(184, 102, 89, 0.14)',
        valueClass: 'text-danger',
        emphasis: conflicts.value.length > 0 ? 'danger' : ''
      },
      {
        key: 'days',
        value: `${scopedTeachingDays.value}天`,
        label: '上课天数',
        hint: '本周有课的工作日数量',
        icon: CalendarOutline,
        color: 'var(--warning-color)',
        background: 'rgba(198, 144, 84, 0.14)',
        valueClass: 'text-primary'
      }
    ]
  }

  return [
    {
      key: 'tasks',
      value: timetable.value?.taskCount || 0,
      label: '任务数',
      hint: '参与排课的教学任务总数',
      icon: ListOutline,
      color: 'var(--primary-color)',
      background: 'rgba(114, 137, 103, 0.12)',
      valueClass: ''
    },
    {
      key: 'scheduled',
      value: timetable.value?.scheduledCount || 0,
      label: '已排课',
      hint: '已经生成的课程课次',
      icon: CheckmarkDoneOutline,
      color: 'var(--success-color)',
      background: 'rgba(125, 149, 99, 0.14)',
      valueClass: 'text-success'
    },
    {
      key: 'conflicts',
      value: timetable.value?.conflictCount || 0,
      label: '冲突',
      hint: '当前课表检测到的冲突数',
      icon: WarningOutline,
      color: 'var(--danger-color)',
      background: 'rgba(184, 102, 89, 0.14)',
      valueClass: 'text-danger',
      emphasis: (timetable.value?.conflictCount || 0) > 0 ? 'danger' : ''
    },
    {
      key: 'utilization',
      value: `${timetable.value?.utilizationRate ? timetable.value.utilizationRate.toFixed(1) : 0}%`,
      label: '利用率',
      hint: '教室与时段的整体利用情况',
      icon: TrendingUpOutline,
      color: 'var(--warning-color)',
      background: 'rgba(198, 144, 84, 0.14)',
      valueClass: 'text-primary'
    }
  ]
})

const detailMap = computed(() => {
  const map = new Map()
  for (const detail of details.value) {
    for (const slot of getOccupiedSlots(detail)) {
      map.set(`${detail.dayOfWeek}_${slot}`, detail)
    }
  }
  return map
})

const getCourse = (day, slot) => {
  return detailMap.value.get(`${day}_${slot}`) || null
}

const showCourseInfo = (day, slot) => {
  const course = getCourse(day, slot)
  if (course) {
    currentCourse.value = course
    showCoursePopup.value = true
  }
}

const showDetailInfo = (detail) => {
  currentCourse.value = detail
  showCoursePopup.value = true
}

const goAdjustment = () => {
  showCoursePopup.value = false
  router.push({
    path: '/adjustment',
    query: { timetableId: route.params.id, detailId: currentCourse.value.id }
  })
}

const goAdjustmentFromDetail = (detail) => {
  router.push({
    path: '/adjustment',
    query: { timetableId: route.params.id, detailId: detail.id }
  })
}

const onDropdownSelect = async (value) => {
  if (value === 'none') {
    return
  }
  if (value === 'publish') {
    dialog.warning({
      title: '确认发布',
      content: '发布后课表将对外可见，确定发布吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await publishTimetable(route.params.id)
          message.success('发布成功')
          loadData()
        } catch (e) {
          message.error(e.message || '发布失败')
        }
      }
    })
  } else if (value === 'archive') {
    dialog.warning({
      title: '确认归档',
      content: '归档后课表将不可修改，确定归档吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await archiveTimetable(route.params.id)
          message.success('归档成功')
          loadData()
        } catch (e) {
          message.error(e.message || '归档失败')
        }
      }
    })
  } else if (value === 'delete') {
    dialog.warning({
      title: '确认删除',
      content: '删除后数据将无法恢复，确定删除吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await deleteTimetable(route.params.id)
          message.success('删除成功')
          router.back()
        } catch (e) {
          message.error(e.message || '删除失败')
        }
      }
    })
  }
}

const goBack = () => router.back()

const handleExportCsv = () => {
  exportCoursesAsCsv({
    courses: details.value,
    filename: exportScopeLabel.value,
    scopeLabel: exportScopeLabel.value,
    slotMeta
  })
  message.success('排课方案已导出')
}

const handlePrint = () => {
  printCurrentPage(exportScopeLabel.value)
}

const loadData = async () => {
  loading.value = true
  try {
    const [timetableRes, detailsRes, conflictsRes] = await Promise.all([
      getTimetableById(route.params.id),
      getTimetableDetails(route.params.id),
      getConflicts(route.params.id)
    ])
    timetable.value = timetableRes.data
    details.value = detailsRes.data
    conflicts.value = conflictsRes.data
    updateDropdownOptions()
  } catch (e) {
    console.error(e)
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', updateIsMobile)
})

onUnmounted(() => {
  layoutStore.clearHeaderAction()
  window.removeEventListener('resize', updateIsMobile)
})
</script>

<style scoped>
.desktop-detail-page {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.header-title {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.view-guide {
  margin-bottom: var(--spacing-lg);
  padding: 18px 20px;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, rgba(114, 137, 103, 0.1), rgba(255, 250, 243, 0.95));
  border: 1px solid rgba(114, 137, 103, 0.15);
  box-shadow: var(--shadow-sm);
}

.view-guide-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.view-guide-text {
  margin-top: 6px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.view-guide-chips {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.view-guide-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(145, 120, 91, 0.12);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
}

.stat-card-desktop {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
}

.stat-card-desktop:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-1px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.stat-hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-muted);
}

.text-success {
  color: var(--text-success);
}

.text-danger {
  color: var(--text-danger);
}

.text-primary {
  color: var(--primary-color);
}

.content-tabs {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.card {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.timetable-card {
  padding: var(--spacing-xl);
}

.timetable-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.panel-helper {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.today-pill {
  align-self: center;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(114, 137, 103, 0.12);
  border: 1px solid rgba(114, 137, 103, 0.2);
  color: var(--primary-color);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.4;
}

.timetable-grid {
  display: grid;
  grid-template-columns: 80px repeat(5, 1fr);
  gap: 2px;
  background: var(--border-color);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.timetable-header {
  background: var(--bg-secondary);
  padding: var(--spacing-md);
  text-align: center;
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 14px;
}

.timetable-slot-header {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

.timetable-day-header.is-today {
  background: rgba(114, 137, 103, 0.18);
  color: var(--text-primary);
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

.timetable-cell {
  background: var(--bg-primary);
  min-height: 100px;
  padding: var(--spacing-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.timetable-cell:hover {
  background: var(--bg-secondary);
}

.timetable-cell.has-course {
  background: linear-gradient(135deg, rgba(81, 202, 186, 0.1) 0%, rgba(81, 202, 186, 0.05) 100%);
}

.timetable-cell.is-today-column {
  background-color: rgba(114, 137, 103, 0.04);
}

.timetable-cell.is-today-column.has-course {
  background: linear-gradient(135deg, rgba(114, 137, 103, 0.16) 0%, rgba(81, 202, 186, 0.08) 100%);
}

.timetable-cell.conflict {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.05) 100%);
  border: 1px dashed var(--text-danger);
}

.course-block {
  width: 100%;
  padding: var(--spacing-sm);
  background: var(--primary-color);
  border-radius: var(--radius-sm);
  color: white;
  text-align: center;
  min-height: 72px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.course-block-name {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  white-space: normal;
}

.course-block-info {
  font-size: 11px;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  line-height: 1.4;
}

.table-wrapper {
  overflow-x: auto;
  padding: var(--spacing-lg);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  min-width: 800px;
}

.data-table th {
  padding: var(--spacing-md) var(--spacing-lg);
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  white-space: nowrap;
}

.data-table td {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px dashed var(--border-light);
  color: var(--text-primary);
}

.data-table tbody tr:hover {
  background: var(--bg-secondary);
}

.name-cell {
  font-weight: 500;
  color: var(--primary-color);
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-3xl);
  color: var(--text-secondary);
}

.course-dialog {
  margin: var(--spacing-md);
}

.course-details {
  padding: var(--spacing-sm) 0;
}

.course-detail-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.course-detail-main {
  min-width: 0;
}

.course-detail-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.35;
}

.course-detail-time {
  margin-top: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.course-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-lg);
}

.course-detail-card {
  padding: 16px 18px;
  border-radius: var(--radius-lg);
  background: rgba(255, 250, 243, 0.72);
  border: 1px solid rgba(145, 120, 91, 0.12);
}

.course-detail-card-label {
  font-size: 12px;
  color: var(--text-muted);
  letter-spacing: 0.02em;
}

.course-detail-card-value {
  margin-top: 6px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
}

.course-detail-card-hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--text-secondary);
}

.course-detail-section {
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(145, 120, 91, 0.1);
  padding: 16px 18px;
}

.course-detail-section-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.course-detail-list {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.course-detail-row-label {
  font-size: 13px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.course-detail-row-value {
  font-size: 14px;
  color: var(--text-primary);
  text-align: right;
  line-height: 1.5;
}

.course-conflict-panel {
  margin-top: var(--spacing-lg);
  padding: 16px 18px;
  border-radius: var(--radius-lg);
  background: rgba(184, 102, 89, 0.08);
  border: 1px solid rgba(184, 102, 89, 0.18);
}

.course-conflict-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-danger);
}

.course-conflict-text {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-danger);
}

@media (min-width: 1440px) {
  .page-title {
    font-size: 28px;
  }

  .stats-grid {
    gap: var(--spacing-xl);
  }

  .timetable-grid {
    grid-template-columns: 100px repeat(5, 1fr);
  }

  .timetable-cell {
    min-height: 120px;
  }

  .stat-value {
    font-size: 28px;
  }
}

@media (min-width: 1920px) {
  .page-header {
    margin-bottom: var(--spacing-2xl);
  }

  .stats-grid {
    gap: var(--spacing-2xl);
    margin-bottom: var(--spacing-2xl);
  }

  .stat-card-desktop {
    padding: var(--spacing-xl);
  }

  .timetable-card {
    padding: var(--spacing-2xl);
  }

  .timetable-cell {
    min-height: 140px;
  }

  .stat-value {
    font-size: 32px;
  }

  .stat-label {
    font-size: 14px;
  }
}

@media (min-width: 2560px) {
  .page-title {
    font-size: 32px;
  }

  .timetable-cell {
    min-height: 160px;
  }

  .course-block-name {
    font-size: 15px;
  }

  .course-block-info {
    font-size: 13px;
  }

  .data-table {
    font-size: 15px;
  }
}

@media (max-width: 1439px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .timetable-grid {
    grid-template-columns: 60px repeat(5, 1fr);
  }

  .timetable-header {
    padding: var(--spacing-sm);
    font-size: 12px;
  }

  .timetable-cell {
    min-height: 80px;
  }
}

@media (max-width: 1199px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 767px) {
  .desktop-detail-page {
    padding-bottom: 20px;
  }

  .page-header {
    margin-bottom: var(--spacing-lg);
    gap: var(--spacing-md);
  }

  .page-title {
    font-size: 20px;
  }

  .page-subtitle {
    font-size: 13px;
  }

  .header-right {
    flex-wrap: wrap;
    gap: var(--spacing-sm);
  }

  .back-btn {
    padding: 6px 12px;
    font-size: 13px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-md);
    margin-bottom: var(--spacing-lg);
  }

  .view-guide {
    padding: var(--spacing-md);
    margin-bottom: var(--spacing-md);
  }

  .view-guide-title {
    font-size: 15px;
  }

  .view-guide-text {
    font-size: 13px;
  }

  .stat-card-desktop {
    padding: var(--spacing-md);
    gap: var(--spacing-sm);
  }

  .stat-icon {
    width: 44px;
    height: 44px;
  }

  .stat-value {
    font-size: 20px;
  }

  .stat-label {
    font-size: 12px;
  }

  .content-tabs {
    border-radius: var(--radius-lg);
  }

  .timetable-card {
    padding: var(--spacing-md);
    overflow-x: auto;
  }

  .timetable-panel-header {
    margin-bottom: var(--spacing-md);
    flex-direction: column;
    align-items: flex-start;
  }

  .panel-title {
    font-size: 15px;
  }

  .panel-helper {
    font-size: 12px;
  }

  .today-pill {
    padding: 6px 12px;
    font-size: 12px;
  }

  .timetable-grid {
    grid-template-columns: 50px repeat(5, minmax(60px, 1fr));
    min-width: 350px;
  }

  .timetable-header {
    padding: 8px 4px;
    font-size: 11px;
    font-weight: 500;
  }

  .timetable-slot-time {
    font-size: 9px;
  }

  .timetable-cell {
    min-height: 60px;
    padding: 4px;
  }

  .course-block {
    padding: 4px 2px;
    min-height: 48px;
  }

  .course-block-name {
    font-size: 10px;
    margin-bottom: 2px;
  }

  .course-block-info {
    font-size: 9px;
  }

  .table-wrapper {
    padding: var(--spacing-md);
    margin: 0 calc(-1 * var(--spacing-md));
    width: calc(100% + var(--spacing-md) * 2);
  }

  .data-table {
    font-size: 13px;
    min-width: 600px;
  }

  .data-table th,
  .data-table td {
    padding: var(--spacing-sm) var(--spacing-md);
  }

  .name-cell {
    font-size: 13px;
  }

  .empty-container {
    padding: var(--spacing-2xl) var(--spacing-lg);
  }

  .course-dialog {
    margin: var(--spacing-sm);
    width: calc(100% - var(--spacing-sm) * 2) !important;
    max-width: 100%;
  }

  .course-details {
    padding: var(--spacing-sm) 0;
  }

  .course-detail-hero {
    flex-direction: column;
    margin-bottom: var(--spacing-md);
  }

  .course-detail-name {
    font-size: 18px;
  }

  .course-detail-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-sm);
    margin-bottom: var(--spacing-md);
  }

  .course-detail-card,
  .course-detail-section,
  .course-conflict-panel {
    padding: 14px;
  }

  .course-detail-row {
    flex-direction: column;
    gap: 4px;
  }

  .course-detail-row-value {
    text-align: left;
  }
}

@media (max-width: 479px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--spacing-sm);
  }

  .stat-card-desktop {
    padding: var(--spacing-sm);
  }

  .stat-hint {
    font-size: 10px;
  }

  .stat-icon {
    width: 36px;
    height: 36px;
  }

  .stat-icon .n-icon {
    font-size: 20px !important;
  }

  .stat-value {
    font-size: 18px;
  }

  .stat-label {
    font-size: 11px;
  }

  .timetable-grid {
    grid-template-columns: 40px repeat(5, minmax(50px, 1fr));
    min-width: 290px;
  }

  .timetable-header {
    padding: 6px 2px;
    font-size: 10px;
  }

  .timetable-slot-time {
    font-size: 8px;
  }

  .timetable-cell {
    min-height: 50px;
  }

  .course-block-name {
    font-size: 9px;
  }

  .course-block-info {
    font-size: 8px;
  }

  .data-table {
    font-size: 12px;
    min-width: 500px;
  }

  .data-table th,
  .data-table td {
    padding: 8px var(--spacing-sm);
  }
}
</style>
