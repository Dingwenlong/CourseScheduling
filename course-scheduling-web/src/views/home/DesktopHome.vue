<template>
  <div class="desktop-home-page" role="main" aria-label="仪表盘">
    <div class="page-header animate-fade-in-up">
      <h2 class="page-title">{{ dashboardTitle }}</h2>
      <p class="page-subtitle">{{ welcomeSubtitle }}</p>
    </div>

    <div class="semester-overview-section animate-fade-in-up" role="region" aria-label="学期概览">
      <div class="semester-info-bar">
        <div class="semester-title-group">
          <div class="semester-badge-large">
            <n-icon size="20" color="#fff"><CalendarOutline /></n-icon>
            <span>{{ currentSemester }}</span>
          </div>
          <span class="semester-subtitle">{{ semesterSubtitle }}</span>
        </div>
        <n-tag type="success" size="medium" round>进行中</n-tag>
      </div>
      <div class="semester-guide">{{ semesterGuide }}</div>

      <div class="stats-grid grid-adaptive stagger-animation" role="region" aria-label="统计数据">
        <div class="stat-card-desktop stat-card-primary" tabindex="0" role="article" aria-label="已排课程统计">
          <div class="stat-icon" style="background: rgba(114, 137, 103, 0.12);">
            <n-icon size="32" color="#728967">
              <CalendarOutline />
            </n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewStats.primaryValue }}</div>
            <div class="stat-label">{{ overviewStats.primaryLabel }}</div>
          </div>
          <div class="stat-trend" v-if="stats.totalCourses > 0">
            <n-icon size="14" color="#10b981"><TrendingUpOutline /></n-icon>
            <span>{{ overviewStats.primaryHint }}</span>
          </div>
        </div>

        <div class="stat-card-desktop" tabindex="0" role="article" aria-label="总学时统计">
          <div class="stat-icon" style="background: rgba(81, 202, 186, 0.12);">
            <n-icon size="32" color="#51caba">
              <TimeOutline />
            </n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalHours }}</div>
            <div class="stat-label">{{ overviewStats.secondaryLabel }}</div>
          </div>
        </div>

        <div class="stat-card-desktop" tabindex="0" role="article" aria-label="教学任务统计">
          <div class="stat-icon" style="background: rgba(245, 158, 11, 0.12);">
            <n-icon size="32" color="#f59e0b">
              <ClipboardOutline />
            </n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overviewStats.tertiaryValue }}</div>
            <div class="stat-label">{{ overviewStats.tertiaryLabel }}</div>
          </div>
        </div>

        <div class="stat-card-desktop" tabindex="0" role="article" aria-label="冲突数量统计" :class="{ 'stat-card-warning': stats.conflicts > 0 }">
          <div class="stat-icon" :style="{ background: stats.conflicts > 0 ? 'rgba(239, 68, 68, 0.12)' : 'rgba(16, 185, 129, 0.12)' }">
            <n-icon size="32" :color="stats.conflicts > 0 ? '#ef4444' : '#10b981'">
              <WarningOutline v-if="stats.conflicts > 0" />
              <CheckmarkCircleOutline v-else />
            </n-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value" :class="{ 'text-danger': stats.conflicts > 0 }">{{ stats.conflicts }}</div>
            <div class="stat-label">{{ stats.conflicts > 0 ? overviewStats.conflictLabel : '无冲突' }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="main-section">
        <div class="card desktop-card animate-slide-in-left" role="region" aria-label="最新课表">
          <div class="card-header">
            <h3 class="card-title">{{ timetableCardTitle }}</h3>
            <n-button type="primary" size="small" class="touch-target" @click="goToTimetable" :aria-label="timetableActionText">
              {{ timetableActionText }}
            </n-button>
          </div>
          <StateView
            :loading="loading"
            :empty="!loading && !latestTimetable"
            empty-text="暂无课表数据"
          >
            <div class="timetable-detail">
              <div class="timetable-header">
                <div>
                  <div class="timetable-name">{{ latestTimetable.name }}</div>
                  <div class="timetable-meta">{{ latestTimetable.semester }} · 第{{ latestTimetable.version }}版</div>
                  <div class="timetable-meta">{{ timetableScopeHint }}</div>
                  <div v-if="todayScheduleTip" class="timetable-meta timetable-meta--strong">{{ todayScheduleTip }}</div>
                </div>
                <n-tag :type="getStatusType(latestTimetable.status)" size="large">
                  {{ getStatusText(latestTimetable.status) }}
                </n-tag>
              </div>
              <div class="timetable-stats">
                <div class="stat-item">
                  <span class="stat-num">{{ detailStats.primaryValue }}</span>
                  <span class="stat-text">{{ detailStats.primaryLabel }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-num text-success">{{ detailStats.secondaryValue }}</span>
                  <span class="stat-text">{{ detailStats.secondaryLabel }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-num text-danger">{{ detailStats.conflictValue }}</span>
                  <span class="stat-text">{{ detailStats.conflictLabel }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-num text-primary">{{ detailStats.extraValue }}</span>
                  <span class="stat-text">{{ detailStats.extraLabel }}</span>
                </div>
              </div>
              <div class="timetable-footer">
                <span class="generate-time">
                  <n-icon size="16">
                    <TimeOutline />
                  </n-icon>
                  生成时间：{{ formatTime(latestTimetable.generateTime) }}
                </span>
              </div>
            </div>
          </StateView>
        </div>

        <div class="card desktop-card animate-slide-in-left" style="animation-delay: 0.1s;" role="region" aria-label="快捷操作">
          <div class="card-header">
            <h3 class="card-title">快捷操作</h3>
          </div>
          <div class="quick-actions-desktop grid-adaptive">
            <router-link to="/timetable" class="quick-action touch-target" :aria-label="quickActionLabels.timetable">
              <n-icon size="28" color="#51caba">
                <CalendarOutline />
              </n-icon>
              <span>{{ quickActionLabels.timetable }}</span>
            </router-link>
            <router-link v-if="canAccessTeacherFeatures" to="/task" class="quick-action touch-target" :aria-label="quickActionLabels.task">
              <n-icon size="28" color="#10b981">
                <ClipboardOutline />
              </n-icon>
              <span>{{ quickActionLabels.task }}</span>
            </router-link>
            <router-link to="/schedule" class="quick-action touch-target" :aria-label="quickActionLabels.schedule">
              <n-icon size="28" color="#f59e0b">
                <SearchOutline />
              </n-icon>
              <span>{{ quickActionLabels.schedule }}</span>
            </router-link>
            <router-link v-if="canAccessTeacherFeatures" to="/adjustment" class="quick-action touch-target" :aria-label="quickActionLabels.adjustment">
              <n-icon size="28" color="#ef4444">
                <SwapHorizontalOutline />
              </n-icon>
              <span>{{ quickActionLabels.adjustment }}</span>
            </router-link>
            <router-link v-if="canAccessTeacherFeatures" to="/statistics" class="quick-action touch-target" :aria-label="quickActionLabels.statistics">
              <n-icon size="28" color="#8b5cf6">
                <BarChartOutline />
              </n-icon>
              <span>{{ quickActionLabels.statistics }}</span>
            </router-link>
            <router-link to="/profile" class="quick-action touch-target" aria-label="个人中心">
              <n-icon size="28" color="#6b7280">
                <SettingsOutline />
              </n-icon>
              <span>个人中心</span>
            </router-link>
          </div>
        </div>
      </div>

      <div class="side-section">
        <div class="card desktop-card animate-slide-in-right" role="region" :aria-label="sidePanelTitle">
          <div class="card-header">
            <h3 class="card-title">{{ sidePanelTitle }}</h3>
          </div>
          <div class="system-status">
            <div v-for="item in sidePanelItems" :key="item.text" class="status-item">
              <n-icon size="20" color="#10b981">
                <CheckmarkCircleOutline />
              </n-icon>
              <span>{{ item.text }}</span>
            </div>
          </div>
        </div>

        <div class="card desktop-card animate-slide-in-right" style="animation-delay: 0.1s;" role="region" :aria-label="activityTitle">
          <div class="card-header">
            <h3 class="card-title">{{ activityTitle }}</h3>
          </div>
          <div class="activity-list">
            <div v-for="item in activityItems" :key="item.text" class="activity-item">
              <div class="activity-icon" style="background: #eff6ff;">
                <n-icon size="16" color="#51caba">
                  <component :is="item.icon" />
                </n-icon>
              </div>
              <div class="activity-content">
                <div class="activity-text">{{ item.text }}</div>
                <div class="activity-time">{{ item.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { getLatestTimetable, getClassTimetable, getTeacherTimetable } from '@/api/timetable'
import { getCurrentSemester } from '@/utils/semester'
import StateView from '@/components/ui/StateView.vue'
import { NButton, NTag, NIcon } from 'naive-ui'
import {
  CalendarOutline,
  TimeOutline,
  ClipboardOutline,
  WarningOutline,
  SearchOutline,
  SwapHorizontalOutline,
  BarChartOutline,
  SettingsOutline,
  CheckmarkCircleOutline,
  AddOutline,
  CreateOutline,
  TrendingUpOutline
} from '@vicons/ionicons5'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const latestTimetable = ref(null)
const scopedDetails = ref([])
const userRole = computed(() => userStore.userInfo?.role)
const canAccessTeacherFeatures = computed(() => ['ADMIN', 'TEACHER'].includes(userStore.userInfo?.role))
const dashboardTitle = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '我的工作台'
  }
  if (userRole.value === 'STUDENT') {
    return '我的学习首页'
  }
  return '仪表盘'
})
const welcomeSubtitle = computed(() => {
  const roleMap = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  const roleText = roleMap[userStore.userInfo?.role] || '用户'
  const name = userStore.userInfo?.realName || '你好'
  return `${name}，欢迎回来。当前身份：${roleText}`
})

const stats = ref({
  totalCourses: 0,
  totalHours: 0,
  totalTasks: 0,
  conflicts: 0,
  activeDays: 0
})

const currentSemester = ref('')
const timetableScopeName = ref('全部班级')
const semesterSubtitle = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '本周授课概览'
  }
  if (userRole.value === 'STUDENT') {
    return '本周学习概览'
  }
  return '当前学期概览'
})
const timetableCardTitle = computed(() => {
  if (userRole.value === 'TEACHER') {
    return '我的课表'
  }
  if (userRole.value === 'STUDENT') {
    return '我的课程'
  }
  return '最新课表'
})
const quickActionLabels = computed(() => ({
  timetable: userRole.value === 'ADMIN' ? '课表管理' : '课表总览',
  task: userRole.value === 'TEACHER' ? '我的课程任务' : '教学任务',
  schedule: userRole.value === 'TEACHER' ? '我的授课表' : userRole.value === 'STUDENT' ? '我的课表' : '课表查询',
  adjustment: userRole.value === 'TEACHER' ? '申请调课' : '调课管理',
  statistics: userRole.value === 'TEACHER' ? '授课统计' : '统计分析'
}))

const timetableActionText = computed(() => userRole.value === 'ADMIN' ? '查看全部' : '查看课表')

const timetableScopeHint = computed(() => {
  if (userRole.value === 'TEACHER') {
    return `当前教师：${timetableScopeName.value}，这里只展示你自己的授课安排`
  }
  if (userRole.value === 'STUDENT') {
    return `当前班级：${timetableScopeName.value}，这里只展示本班课程安排`
  }
  return '当前学期全局排课概览'
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
const semesterGuide = computed(() => {
  if (userRole.value === 'TEACHER' || userRole.value === 'STUDENT') {
    return `当前查看范围：${timetableScopeName.value}${todayScheduleTip.value ? `，${todayScheduleTip.value}` : ''}`
  }
  return '当前展示的是本学期最新课表的整体概览与关键状态'
})
const overviewStats = computed(() => {
  if (userRole.value === 'TEACHER') {
    return {
      primaryValue: stats.value.totalCourses,
      primaryLabel: '授课门数',
      primaryHint: '正常',
      secondaryLabel: '总学时',
      tertiaryValue: stats.value.activeDays,
      tertiaryLabel: '上课天数',
      conflictLabel: '待处理冲突'
    }
  }
  if (userRole.value === 'STUDENT') {
    return {
      primaryValue: stats.value.totalCourses,
      primaryLabel: '课程门数',
      primaryHint: '正常',
      secondaryLabel: '总学时',
      tertiaryValue: stats.value.activeDays,
      tertiaryLabel: '上课天数',
      conflictLabel: '课程冲突'
    }
  }
  return {
    primaryValue: stats.value.totalCourses,
    primaryLabel: '已排课程',
    primaryHint: '正常',
    secondaryLabel: '总学时',
    tertiaryValue: stats.value.totalTasks,
    tertiaryLabel: '教学任务',
    conflictLabel: '待处理冲突'
  }
})

const detailStats = computed(() => {
  if (userRole.value === 'TEACHER') {
    return {
      primaryValue: stats.value.totalTasks,
      primaryLabel: '本周课次',
      secondaryValue: stats.value.totalCourses,
      secondaryLabel: '授课门数',
      conflictValue: stats.value.conflicts,
      conflictLabel: '冲突',
      extraValue: `${stats.value.totalHours}h`,
      extraLabel: '总学时'
    }
  }
  if (userRole.value === 'STUDENT') {
    return {
      primaryValue: stats.value.totalTasks,
      primaryLabel: '本周课次',
      secondaryValue: stats.value.totalCourses,
      secondaryLabel: '课程门数',
      conflictValue: stats.value.conflicts,
      conflictLabel: '冲突',
      extraValue: `${stats.value.totalHours}h`,
      extraLabel: '总学时'
    }
  }
  return {
    primaryValue: latestTimetable.value?.taskCount || 0,
    primaryLabel: '任务数',
    secondaryValue: latestTimetable.value?.scheduledCount || 0,
    secondaryLabel: '已排课',
    conflictValue: latestTimetable.value?.conflictCount || 0,
    conflictLabel: '冲突',
    extraValue: `${latestTimetable.value?.utilizationRate ? latestTimetable.value.utilizationRate.toFixed(1) : 0}%`,
    extraLabel: '利用率'
  }
})
const sidePanelTitle = computed(() => userRole.value === 'ADMIN' ? '系统状态' : '使用提醒')
const sidePanelItems = computed(() => {
  if (userRole.value === 'ADMIN') {
    return [
      { text: '数据库连接正常' },
      { text: '算法服务运行中' },
      { text: '缓存服务正常' }
    ]
  }
  return [
    { text: '绿色课表卡片表示当前课表范围已自动按你的身份过滤' },
    { text: '如需看完整周安排，可直接打开“我的授课表”或“我的课表”' },
    { text: '发现冲突时可先到“申请调课”或“授课统计”继续处理' }
  ]
})
const activityTitle = computed(() => userRole.value === 'ADMIN' ? '最近活动' : '今日提醒')
const activityItems = computed(() => {
  if (userRole.value === 'ADMIN') {
    return [
      { icon: AddOutline, text: '新增教学任务', time: '10分钟前' },
      { icon: CheckmarkCircleOutline, text: '课表生成成功', time: '30分钟前' },
      { icon: CreateOutline, text: '更新课程信息', time: '1小时前' }
    ]
  }
  return [
    { icon: CalendarOutline, text: todayScheduleTip.value || '今天暂无课程安排', time: '今日概览' },
    { icon: WarningOutline, text: stats.value.conflicts > 0 ? `当前还有 ${stats.value.conflicts} 个冲突需要关注` : '当前课表没有待处理冲突', time: '冲突提醒' },
    { icon: ClipboardOutline, text: `当前课表共 ${stats.value.totalTasks} 个课次，覆盖 ${stats.value.activeDays} 个上课日`, time: '本周节奏' }
  ]
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

const goToTimetable = () => {
  router.push(userRole.value === 'ADMIN' ? '/timetable' : '/schedule')
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
</script>

<style scoped>
.desktop-home-page {
  animation: fadeIn 0.4s ease-out;
  padding: 0;
}

.home-section {
  margin-bottom: var(--spacing-2xl);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  padding: 0 var(--page-px-mobile);
}

@media (min-width: 1024px) {
  .section-header {
    padding: 0 var(--page-px-desktop);
  }
}

@media (min-width: 1440px) {
  .section-header {
    padding: 0 var(--page-px-wide);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 var(--spacing-xs) 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.semester-overview-section {
  background: linear-gradient(135deg, #728967 0%, #5a6e52 100%);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
  box-shadow: 0 8px 32px rgba(114, 137, 103, 0.25);
}

.semester-info-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
}

.semester-title-group {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.semester-badge-large {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  backdrop-filter: blur(4px);
}

.semester-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 500;
}

.semester-guide {
  margin-bottom: var(--spacing-lg);
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.84);
}

.stats-grid {
  --grid-min-width: 220px;
  gap: var(--spacing-lg);
}

.stat-card-desktop {
  background: rgba(255, 255, 255, 0.95);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.stat-card-desktop:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px);
  background: #fff;
}

.stat-card-primary {
  border-left: 4px solid #728967;
}

.stat-card-warning {
  border-left: 4px solid #ef4444;
}

.stat-trend {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #10b981;
  font-weight: 500;
  background: rgba(16, 185, 129, 0.1);
  padding: 4px 8px;
  border-radius: 999px;
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

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--spacing-lg);
}

.desktop-card {
  margin-bottom: var(--spacing-lg);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.timetable-detail {
  padding: 0 4px;
}

.timetable-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.timetable-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.timetable-meta {
  font-size: 13px;
  color: var(--text-secondary);
}

.timetable-meta--strong {
  color: var(--primary-color);
  font-weight: 600;
}

.timetable-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.stat-item {
  text-align: center;
}

.stat-num {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-text {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.timetable-footer {
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--border-light);
}

.generate-time {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.quick-actions-desktop {
  --grid-min-width: 140px;
}

.quick-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  text-decoration: none;
  color: var(--text-primary);
  transition: all var(--transition-fast);
  gap: var(--spacing-sm);
}

.quick-action:hover {
  background: var(--bg-tertiary);
  transform: translateY(-2px);
}

.quick-action span {
  font-size: 13px;
  font-weight: 500;
}

.system-status {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.status-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 14px;
  color: var(--text-primary);
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.activity-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.activity-time {
  font-size: 12px;
  color: var(--text-muted);
}

@media (min-width: 1440px) {
  .stats-grid {
    gap: var(--spacing-xl);
  }
  
  .quick-actions-desktop {
    gap: var(--spacing-lg);
  }

  .stat-card-desktop {
    padding: var(--spacing-xl);
  }
}

@media (min-width: 1920px) {
  .stats-grid {
    gap: var(--spacing-2xl);
    --grid-min-width: 320px;
  }

  .stat-value {
    font-size: 32px;
  }

  .stat-label {
    font-size: 15px;
  }

  .content-grid {
    grid-template-columns: 3fr 1fr;
    gap: var(--spacing-2xl);
  }
}

@media (min-width: 2560px) {
  .stats-grid {
    gap: var(--spacing-3xl);
    --grid-min-width: 400px;
  }

  .stat-value {
    font-size: 40px;
  }

  .quick-action span {
    font-size: 16px;
  }
}

@media (max-width: 1439px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  
  .timetable-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767px) {
  .desktop-home-page {
    display: none;
  }
}
</style>
