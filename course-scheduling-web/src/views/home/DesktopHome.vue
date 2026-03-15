<template>
  <div class="desktop-home-page" role="main" aria-label="仪表盘">
    <div class="page-header animate-fade-in-up">
      <h2 class="page-title">仪表盘</h2>
      <p class="page-subtitle">欢迎回来，管理员</p>
    </div>

    <div class="stats-grid grid-adaptive stagger-animation" role="region" aria-label="统计数据">
      <div class="stat-card-desktop" tabindex="0" role="article" aria-label="已排课程统计">
        <div class="stat-icon" style="background: #eff6ff;">
          <n-icon size="32" color="#51caba">
            <CalendarOutline />
          </n-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalCourses }}</div>
          <div class="stat-label">已排课程</div>
        </div>
      </div>

      <div class="stat-card-desktop" tabindex="0" role="article" aria-label="总学时统计">
        <div class="stat-icon" style="background: #ecfdf5;">
          <n-icon size="32" color="#10b981">
            <TimeOutline />
          </n-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalHours }}</div>
          <div class="stat-label">总学时</div>
        </div>
      </div>

      <div class="stat-card-desktop" tabindex="0" role="article" aria-label="教学任务统计">
        <div class="stat-icon" style="background: #fef3c7;">
          <n-icon size="32" color="#f59e0b">
            <ClipboardOutline />
          </n-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalTasks }}</div>
          <div class="stat-label">教学任务</div>
        </div>
      </div>

      <div class="stat-card-desktop" tabindex="0" role="article" aria-label="冲突数量统计">
        <div class="stat-icon" style="background: #fef2f2;">
          <n-icon size="32" color="#ef4444">
            <WarningOutline />
          </n-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.conflicts }}</div>
          <div class="stat-label">冲突数量</div>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <div class="main-section">
        <div class="card desktop-card animate-slide-in-left" role="region" aria-label="最新课表">
          <div class="card-header">
            <h3 class="card-title">最新课表</h3>
            <n-button type="primary" size="small" class="touch-target" @click="goToTimetable" aria-label="查看全部课表">
              查看全部
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
                </div>
                <n-tag :type="getStatusType(latestTimetable.status)" size="large">
                  {{ getStatusText(latestTimetable.status) }}
                </n-tag>
              </div>
              <div class="timetable-stats">
                <div class="stat-item">
                  <span class="stat-num">{{ latestTimetable.taskCount }}</span>
                  <span class="stat-text">任务数</span>
                </div>
                <div class="stat-item">
                  <span class="stat-num text-success">{{ latestTimetable.scheduledCount }}</span>
                  <span class="stat-text">已排课</span>
                </div>
                <div class="stat-item">
                  <span class="stat-num text-danger">{{ latestTimetable.conflictCount }}</span>
                  <span class="stat-text">冲突</span>
                </div>
                <div class="stat-item">
                  <span class="stat-num text-primary">{{ latestTimetable.utilizationRate ? latestTimetable.utilizationRate.toFixed(1) : 0 }}%</span>
                  <span class="stat-text">利用率</span>
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
            <router-link to="/timetable" class="quick-action touch-target" aria-label="生成课表">
              <n-icon size="28" color="#51caba">
                <CalendarOutline />
              </n-icon>
              <span>生成课表</span>
            </router-link>
            <router-link to="/task" class="quick-action touch-target" aria-label="教学任务">
              <n-icon size="28" color="#10b981">
                <ClipboardOutline />
              </n-icon>
              <span>教学任务</span>
            </router-link>
            <router-link to="/schedule" class="quick-action touch-target" aria-label="课表查询">
              <n-icon size="28" color="#f59e0b">
                <SearchOutline />
              </n-icon>
              <span>课表查询</span>
            </router-link>
            <router-link to="/adjustment" class="quick-action touch-target" aria-label="调课申请">
              <n-icon size="28" color="#ef4444">
                <SwapHorizontalOutline />
              </n-icon>
              <span>调课申请</span>
            </router-link>
            <router-link to="/statistics" class="quick-action touch-target" aria-label="统计分析">
              <n-icon size="28" color="#8b5cf6">
                <BarChartOutline />
              </n-icon>
              <span>统计分析</span>
            </router-link>
            <router-link to="/profile" class="quick-action touch-target" aria-label="系统设置">
              <n-icon size="28" color="#6b7280">
                <SettingsOutline />
              </n-icon>
              <span>系统设置</span>
            </router-link>
          </div>
        </div>
      </div>

      <div class="side-section">
        <div class="card desktop-card animate-slide-in-right" role="region" aria-label="系统状态">
          <div class="card-header">
            <h3 class="card-title">系统状态</h3>
          </div>
          <div class="system-status">
            <div class="status-item">
              <n-icon size="20" color="#10b981">
                <CheckmarkCircleOutline />
              </n-icon>
              <span>数据库连接正常</span>
            </div>
            <div class="status-item">
              <n-icon size="20" color="#10b981">
                <CheckmarkCircleOutline />
              </n-icon>
              <span>算法服务运行中</span>
            </div>
            <div class="status-item">
              <n-icon size="20" color="#10b981">
                <CheckmarkCircleOutline />
              </n-icon>
              <span>缓存服务正常</span>
            </div>
          </div>
        </div>

        <div class="card desktop-card animate-slide-in-right" style="animation-delay: 0.1s;" role="region" aria-label="最近活动">
          <div class="card-header">
            <h3 class="card-title">最近活动</h3>
          </div>
          <div class="activity-list">
            <div class="activity-item">
              <div class="activity-icon" style="background: #eff6ff;">
                <n-icon size="16" color="#51caba">
                  <AddOutline />
                </n-icon>
              </div>
              <div class="activity-content">
                <div class="activity-text">新增教学任务</div>
                <div class="activity-time">10分钟前</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="activity-icon" style="background: #ecfdf5;">
                <n-icon size="16" color="#10b981">
                  <CheckmarkCircleOutline />
                </n-icon>
              </div>
              <div class="activity-content">
                <div class="activity-text">课表生成成功</div>
                <div class="activity-time">30分钟前</div>
              </div>
            </div>
            <div class="activity-item">
              <div class="activity-icon" style="background: #fef3c7;">
                <n-icon size="16" color="#f59e0b">
                  <CreateOutline />
                </n-icon>
              </div>
              <div class="activity-content">
                <div class="activity-text">更新课程信息</div>
                <div class="activity-time">1小时前</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getLatestTimetable } from '@/api/timetable'
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
  CreateOutline
} from '@vicons/ionicons5'

const router = useRouter()
const loading = ref(false)
const latestTimetable = ref(null)

const stats = ref({
  totalCourses: 0,
  totalHours: 0,
  totalTasks: 0,
  conflicts: 0
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
  router.push('/timetable')
}

onMounted(async () => {
  loading.value = true
  try {
    const semester = dayjs().format('YYYY') + (dayjs().month() < 7 ? '-1' : '-2')
    const res = await getLatestTimetable(semester)
    latestTimetable.value = res.data
    if (res.data) {
      stats.value.totalCourses = res.data.scheduledCount || 0
      stats.value.totalHours = (res.data.scheduledCount || 0) * 2
      stats.value.totalTasks = res.data.taskCount || 0
      stats.value.conflicts = res.data.conflictCount || 0
    }
  } catch (e) {
    console.error(e)
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

.stats-grid {
  margin-bottom: var(--spacing-xl);
  --grid-min-width: 260px;
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
  box-shadow: 0 6px 16px rgba(81, 202, 186, 0.4);
  transform: translateY(-2px);
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
