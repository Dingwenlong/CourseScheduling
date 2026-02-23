<template>
  <div class="page page-with-tabbar home-page">
    <van-nav-bar title="首页" class="custom-nav" />

    <div class="stat-card animate-slide-up">
      <div class="stat-card-content">
        <div class="stat-card-title">本学期已排课程</div>
        <div class="stat-card-value">{{ stats.totalCourses }}</div>
        <div class="stat-card-desc">共 {{ stats.totalHours }} 学时</div>
      </div>
    </div>

    <div class="quick-actions-wrapper">
      <div class="section-title">快捷操作</div>
      <div class="quick-actions animate-slide-up" style="animation-delay: 0.1s;">
        <van-grid :column-num="3" :border="false" class="action-grid">
          <van-grid-item icon="calendar-o" text="生成课表" to="/timetable" class="action-item touch-target" />
          <van-grid-item icon="todo-list-o" text="教学任务" to="/task" class="action-item touch-target" />
          <van-grid-item icon="search" text="课表查询" to="/schedule" class="action-item touch-target" />
          <van-grid-item icon="exchange" text="调课申请" to="/adjustment" class="action-item touch-target" />
          <van-grid-item icon="chart-trending-o" text="统计分析" to="/statistics" class="action-item touch-target" />
          <van-grid-item icon="setting-o" text="系统设置" to="/profile" class="action-item touch-target" />
        </van-grid>
      </div>
    </div>

    <div class="card timetable-card animate-slide-up" style="animation-delay: 0.2s;">
      <div class="page-title">最新课表</div>
      <van-loading v-if="loading" class="loading-container" />
      <div v-else-if="latestTimetable" class="timetable-info">
        <div class="flex-between">
          <div class="flex-1">
            <div class="timetable-name">{{ latestTimetable.name }}</div>
            <div class="text-muted mt-8">
              {{ latestTimetable.semester }} · 第{{ latestTimetable.version }}版
            </div>
          </div>
          <van-tag :type="getStatusType(latestTimetable.status)" class="status-tag-custom">
            {{ getStatusText(latestTimetable.status) }}
          </van-tag>
        </div>
        <van-cell-group inset class="mt-16 info-group">
          <van-cell title="排课任务" :value="latestTimetable.taskCount + ' 个'" />
          <van-cell title="已排课程" :value="latestTimetable.scheduledCount + ' 个'" />
          <van-cell title="冲突数量" :value="latestTimetable.conflictCount + ' 个'" />
          <van-cell title="生成时间" :value="formatTime(latestTimetable.generateTime)" />
        </van-cell-group>
        <van-button
          round
          block
          type="primary"
          class="mt-16 view-btn touch-target"
          @click="viewDetail"
        >
          查看详情
        </van-button>
      </div>
      <van-empty v-else description="暂无课表数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { getLatestTimetable } from '@/api/timetable'

const router = useRouter()
const loading = ref(false)
const latestTimetable = ref(null)

const stats = ref({
  totalCourses: 0,
  totalHours: 0
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

onMounted(async () => {
  loading.value = true
  try {
    const semester = dayjs().format('YYYY') + (dayjs().month() < 7 ? '-1' : '-2')
    const res = await getLatestTimetable(semester)
    latestTimetable.value = res.data
    if (res.data) {
      stats.value.totalCourses = res.data.scheduledCount || 0
      stats.value.totalHours = (res.data.scheduledCount || 0) * 2
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-page {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.custom-nav {
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
  position: sticky;
  top: 0;
  z-index: 10;
}

:deep(.van-nav-bar) {
  background: var(--bg-primary);
}

.quick-actions-wrapper {
  margin: var(--spacing-md);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
  padding-left: 4px;
}

.quick-actions {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.action-grid {
  padding: var(--spacing-md) 0;
}

.action-item {
  transition: transform 0.2s ease;
}

.action-item:active {
  transform: scale(0.95);
}

:deep(.van-grid-item) {
  min-height: 80px;
}

:deep(.van-grid-item__icon) {
  color: var(--primary-color);
  margin-bottom: 8px;
}

:deep(.van-grid-item__text) {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.timetable-card {
  animation: slideUp 0.4s ease-out backwards;
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
  padding: 0 4px;
}

.timetable-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.status-tag-custom {
  flex-shrink: 0;
}

.info-group {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.view-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-gradient);
  border: none;
  box-shadow: 0 4px 12px rgba(81, 202, 186, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.view-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(81, 202, 186, 0.4);
}

.view-btn:active {
  transform: translateY(0);
}

@media (min-width: 768px) {
  .home-page {
    max-width: 800px;
    margin: 0 auto;
  }
  
  .quick-actions-wrapper {
    margin: var(--spacing-lg);
  }
  
  .action-grid {
    padding: var(--spacing-lg) 0;
  }
}

@media (max-width: 480px) {
  .stat-card {
    margin: var(--spacing-sm);
  }
  
  .quick-actions-wrapper {
    margin: var(--spacing-sm);
  }
  
  .timetable-card {
    margin: var(--spacing-sm);
  }
  
  .timetable-name {
    font-size: 16px;
  }
  
  .view-btn {
    height: 44px;
    font-size: 15px;
  }
}
</style>
