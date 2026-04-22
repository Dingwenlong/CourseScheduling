<template>
  <PageContainer with-tabbar class="home-page">
    <PageHeader :title="pageTitle" :subtitle="pageSubtitle" />

    <div class="semester-overview-card animate-slide-up">
      <div class="semester-header">
        <div class="semester-badge">
          <n-icon size="14" color="#fff"><CalendarOutline /></n-icon>
          <span>{{ currentSemester }}</span>
        </div>
        <span class="semester-label">当前学期</span>
      </div>
      <div class="semester-helper">{{ semesterHelperText }}</div>
      <div class="semester-stats">
        <div class="semester-stat-item">
          <div class="semester-stat-value">{{ heroStats.primaryValue }}</div>
          <div class="semester-stat-label">{{ heroStats.primaryLabel }}</div>
        </div>
        <div class="stat-divider"></div>
        <div class="semester-stat-item">
          <div class="semester-stat-value">{{ stats.totalHours }}</div>
          <div class="semester-stat-label">{{ heroStats.secondaryLabel }}</div>
        </div>
        <div class="stat-divider"></div>
        <div class="semester-stat-item">
          <div class="semester-stat-value">{{ heroStats.tertiaryValue }}</div>
          <div class="semester-stat-label">{{ heroStats.tertiaryLabel }}</div>
        </div>
      </div>
    </div>

    <div class="quick-actions-section">
      <div class="section-title">常用入口</div>
      <div class="quick-actions animate-slide-up" style="animation-delay: 0.1s;">
        <n-grid :x-gap="12" :y-gap="12" :cols="gridColumns" class="action-grid">
          <n-grid-item v-for="action in quickActions" :key="action.to">
            <router-link :to="action.to" class="action-item touch-target">
              <n-icon size="28" :color="action.color">
                <component :is="action.icon" />
              </n-icon>
              <span class="action-text">{{ action.text }}</span>
            </router-link>
          </n-grid-item>
        </n-grid>
      </div>
    </div>

    <div class="card timetable-card animate-slide-up" style="animation-delay: 0.2s;">
      <div class="section-title">{{ timetableSectionTitle }}</div>
      <StateView
        :loading="loading"
        :empty="!loading && !latestTimetable"
        empty-text="暂无课表数据"
      >
        <div class="timetable-info">
          <div class="flex-between">
            <div class="flex-1">
              <div class="timetable-name">{{ latestTimetable.name }}</div>
              <div class="text-muted mt-8">
                {{ latestTimetable.semester }} · 第{{ latestTimetable.version }}版
              </div>
              <div class="text-muted mt-8">{{ timetableScopeHint }}</div>
              <div v-if="todayScheduleTip" class="today-tip mt-8">{{ todayScheduleTip }}</div>
            </div>
            <n-tag :type="getStatusType(latestTimetable.status)" class="status-tag-custom">
              {{ getStatusText(latestTimetable.status) }}
            </n-tag>
          </div>
          <div class="mt-16 info-group">
            <n-descriptions :column="1" :bordered="false" size="medium">
              <n-descriptions-item :label="summaryLabels.scope">{{ timetableScopeName }}</n-descriptions-item>
              <n-descriptions-item :label="summaryLabels.tasks">{{ stats.totalTasks }} 个</n-descriptions-item>
              <n-descriptions-item :label="summaryLabels.courses">{{ stats.totalCourses }} 门</n-descriptions-item>
              <n-descriptions-item label="冲突数量">{{ stats.conflicts }} 个</n-descriptions-item>
              <n-descriptions-item label="生成时间">{{ formatTime(latestTimetable.generateTime) }}</n-descriptions-item>
            </n-descriptions>
          </div>
          <n-button
            round
            block
            type="primary"
            class="mt-16 view-btn touch-target"
            @click="viewDetail"
          >
            查看详情
          </n-button>
        </div>
      </StateView>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getLatestTimetable, getClassTimetable, getTeacherTimetable } from '@/api/timetable'
import { getCurrentSemester } from '@/utils/semester'
import StateView from '@/components/ui/StateView.vue'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { useUserStore } from '@/stores/user'
import { NGrid, NGridItem, NTag, NButton, NDescriptions, NDescriptionsItem, NIcon } from 'naive-ui'
import {
  CalendarOutline,
  ClipboardOutline,
  SearchOutline,
  SwapHorizontalOutline,
  BarChartOutline,
  PeopleOutline,
  SettingsOutline,
  PersonOutline
} from '@vicons/ionicons5'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const latestTimetable = ref(null)
const screenWidth = ref(window.innerWidth)
const scopedDetails = ref([])

const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const userRole = computed(() => userStore.userInfo?.role)
const canAccessTeacherFeatures = computed(() => ['ADMIN', 'TEACHER'].includes(userStore.userInfo?.role))
const timetableScopeName = ref('全部班级')
const pageTitle = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '我的工作台'
  }
  if (userRole.value === 'STUDENT') {
    return '我的学习首页'
  }
  return '首页'
})
const pageSubtitle = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '查看本周授课安排、常用入口和课表摘要'
  }
  if (userRole.value === 'STUDENT') {
    return '查看本班课表、学习安排和常用入口'
  }
  return '教学排课工作台'
})

const gridColumns = computed(() => {
  if (screenWidth.value >= 1600) return 6
  if (screenWidth.value >= 1024) return 3
  return 3
})

const quickActions = computed(() => {
  const actions = [
    { to: '/schedule', text: userRole.value === 'TEACHER' ? '我的授课表' : userRole.value === 'STUDENT' ? '我的课表' : '课表查询', icon: SearchOutline, color: '#c69054' },
    { to: '/profile', text: '个人中心', icon: PersonOutline, color: '#7d7064' }
  ]

  if (isAdmin.value) {
    actions.unshift({ to: '/timetable', text: '课表管理', icon: CalendarOutline, color: '#728967' })
  }

  if (canAccessTeacherFeatures.value) {
    actions.splice(isAdmin.value ? 1 : 0, 0,
      { to: '/task', text: userRole.value === 'TEACHER' ? '我的课程任务' : '教学任务', icon: ClipboardOutline, color: '#7d9563' },
      { to: '/adjustment', text: userRole.value === 'TEACHER' ? '申请调课' : '调课管理', icon: SwapHorizontalOutline, color: '#b86659' },
      { to: '/statistics', text: userRole.value === 'TEACHER' ? '授课统计' : '统计分析', icon: BarChartOutline, color: '#6f89a3' }
    )
  }
  
  if (isAdmin.value) {
    actions.splice(actions.length - 1, 0,
      { to: '/settings', text: '系统设置', icon: SettingsOutline, color: '#90724e' },
      { to: '/users', text: '用户管理', icon: PeopleOutline, color: '#9b7652' }
    )
  }
  
  return actions
})

const updateScreenWidth = () => {
  screenWidth.value = window.innerWidth
}

const stats = ref({
  totalCourses: 0,
  totalHours: 0,
  totalTasks: 0,
  conflicts: 0,
  activeDays: 0
})

const currentSemester = ref('')
const summaryLabels = computed(() => {
  if (userRole.value === 'TEACHER') {
    return {
      scope: '当前教师',
      tasks: '本周课次',
      courses: '授课门数'
    }
  }
  if (userRole.value === 'STUDENT') {
    return {
      scope: '当前班级',
      tasks: '本周课次',
      courses: '课程门数'
    }
  }
  return {
    scope: '适用范围',
    tasks: '教学任务',
    courses: '已排课程'
  }
})

const timetableSectionTitle = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '我的课表'
  }
  if (userRole.value === 'STUDENT') {
    return '我的课程'
  }
  return '最新课表'
})

const timetableScopeHint = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '首页展示的是你在当前课表中的授课安排，不会混入其他教师的数据'
  }
  if (userRole.value === 'STUDENT') {
    return '首页展示的是当前班级的课程安排，方便你直接查看学习节奏'
  }
  return '管理员首页展示当前学期最新课表的全局概览'
})
const heroStats = computed(() => {
  if (userRole.value === 'TEACHER') {
    return {
      primaryValue: stats.value.totalCourses,
      primaryLabel: '授课门数',
      secondaryLabel: '总学时',
      tertiaryValue: stats.value.activeDays,
      tertiaryLabel: '上课天数'
    }
  }
  if (userRole.value === 'STUDENT') {
    return {
      primaryValue: stats.value.totalCourses,
      primaryLabel: '课程门数',
      secondaryLabel: '总学时',
      tertiaryValue: stats.value.activeDays,
      tertiaryLabel: '上课天数'
    }
  }
  return {
    primaryValue: stats.value.totalCourses,
    primaryLabel: '已排课程',
    secondaryLabel: '总学时',
    tertiaryValue: stats.value.totalTasks,
    tertiaryLabel: '教学任务'
  }
})
const todayScheduleTip = computed(() => {
  const today = dayjs().day()
  if (today < 1 || today > 5 || !scopedDetails.value.length) {
    return ''
  }
  const count = scopedDetails.value.filter(item => item.dayOfWeek === today).length
  const weekday = ['一', '二', '三', '四', '五'][today - 1]
  if (userRole.value === 'TEACHER') {
    return count > 0 ? `今天周${weekday}，你有 ${count} 节课要上` : `今天周${weekday}，你今天没有课`
  }
  if (userRole.value === 'STUDENT') {
    return count > 0 ? `今天周${weekday}，本班有 ${count} 节课` : `今天周${weekday}，本班今天没有课`
  }
  return ''
})
const semesterHelperText = computed(() => {
  if (userRole.value === 'TEACHER' || userRole.value === 'STUDENT') {
    return `当前查看范围：${timetableScopeName.value}${todayScheduleTip.value ? `，${todayScheduleTip.value}` : ''}`
  }
  return '当前展示的是本学期最新课表的整体概览'
})

const getStatusType = (status) => {
  const map = {
    'DRAFT': 'warning',
    'PUBLISHED': 'success',
    'ARCHIVED': 'default'
  }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'ARCHIVED': '已归档'
  }
  return map[status] || status
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'
}

const viewDetail = () => {
  if (latestTimetable.value) {
    router.push(`/timetable/detail/${latestTimetable.value.id}`)
  }
}

const resetStats = () => {
  stats.value = {
    totalCourses: 0,
    totalHours: 0,
    totalTasks: 0,
    conflicts: 0,
    activeDays: 0
  }
  timetableScopeName.value = '全部班级'
  scopedDetails.value = []
}

const applyAdminSummary = (timetable) => {
  timetableScopeName.value = '全部班级'
  scopedDetails.value = []
  stats.value.totalCourses = timetable?.scheduledCount || 0
  stats.value.totalHours = (timetable?.scheduledCount || 0) * 2
  stats.value.totalTasks = timetable?.taskCount || 0
  stats.value.conflicts = timetable?.conflictCount || 0
  stats.value.activeDays = 5
}

const applyScopedSummary = (details, scopeName) => {
  const scopedCourses = Array.isArray(details) ? details : []
  const uniqueCourseCount = new Set(
    scopedCourses.map(item => item?.courseName || item?.teachingTaskId || item?.id).filter(Boolean)
  ).size
  const activeDayCount = new Set(scopedCourses.map(item => item?.dayOfWeek).filter(Boolean)).size
  timetableScopeName.value = scopeName || '-'
  scopedDetails.value = scopedCourses
  stats.value.totalCourses = uniqueCourseCount
  stats.value.totalHours = scopedCourses.length * 2
  stats.value.totalTasks = scopedCourses.length
  stats.value.conflicts = scopedCourses.filter(item => Number(item?.isConflict) === 1).length
  stats.value.activeDays = activeDayCount
}

const loadScopedHomeSummary = async (timetable) => {
  if (!timetable?.id) {
    resetStats()
    return
  }

  if (userRole.value === 'TEACHER' && userStore.userInfo?.teacherId) {
    const res = await getTeacherTimetable(timetable.id, userStore.userInfo.teacherId)
    const details = res.data || []
    applyScopedSummary(details, details[0]?.teacherName || userStore.userInfo?.realName || '当前教师')
    return
  }

  if (userRole.value === 'STUDENT' && userStore.userInfo?.classId) {
    const res = await getClassTimetable(timetable.id, userStore.userInfo.classId)
    const details = res.data || []
    applyScopedSummary(details, details[0]?.className || '当前班级')
    return
  }

  applyAdminSummary(timetable)
}

onMounted(async () => {
  window.addEventListener('resize', updateScreenWidth)
  loading.value = true
  try {
    const semester = getCurrentSemester()
    currentSemester.value = semester
    const res = await getLatestTimetable(semester)
    latestTimetable.value = res.data
    if (res.data) {
      await loadScopedHomeSummary(res.data)
    } else {
      resetStats()
    }
  } catch (e) {
    console.error(e)
    resetStats()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenWidth)
})
</script>

<style scoped>
.home-page {
  animation: fadeIn 0.3s ease-out;
}

.home-page > * {
  margin-left: 0;
  margin-right: 0;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.quick-actions-section {
  margin-bottom: var(--spacing-lg);
}

.quick-actions {
  overflow: hidden;
  padding: 14px;
}

.action-grid {
  padding: 0;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 18px 14px;
  border-radius: 20px;
  text-decoration: none;
  color: var(--text-primary);
  transition:
    transform var(--transition-base),
    box-shadow var(--transition-base),
    background-color var(--transition-base);
  gap: 10px;
  min-height: 98px;
  background: rgba(255, 250, 243, 0.5);
  border: 1px solid rgba(145, 120, 91, 0.12);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

@media (hover: hover) {
  .action-item:hover {
    transform: translateY(-1px);
    background: rgba(255, 251, 245, 0.82);
    box-shadow: var(--shadow-xs);
  }
}

.action-item:active {
  transform: translateY(1px);
}

.action-text {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.timetable-card {
  animation: slideUp 0.4s ease-out backwards;
  padding: 18px;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.timetable-info {
  padding: 0;
}

.timetable-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.01em;
}

.status-tag-custom {
  flex-shrink: 0;
}

.info-group {
  border-radius: 18px;
  overflow: hidden;
  background: rgba(255, 250, 243, 0.46);
  border: 1px solid rgba(145, 120, 91, 0.12);
  padding: 6px 10px;
}

.today-tip {
  font-size: 13px;
  color: var(--primary-color);
  font-weight: 600;
}

.view-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.view-btn:active {
  transform: translateY(1px);
}

.semester-overview-card {
  background: linear-gradient(135deg, #728967 0%, #5a6e52 100%);
  border-radius: 20px;
  padding: 20px;
  margin-bottom: var(--spacing-lg);
  color: #fff;
  box-shadow: 0 4px 20px rgba(114, 137, 103, 0.25);
}

.semester-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.semester-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

.semester-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.semester-helper {
  margin-bottom: 18px;
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.84);
}

.semester-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px 16px;
  backdrop-filter: blur(4px);
}

.semester-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.semester-stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.semester-stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

@media (max-width: 480px) {
  .timetable-name {
    font-size: 16px;
  }

  .view-btn {
    height: 44px;
    font-size: 15px;
  }

  .semester-overview-card {
    padding: 16px;
  }

  .semester-stat-value {
    font-size: 24px;
  }

  .semester-stats {
    padding: 16px 12px;
  }
}
</style>
