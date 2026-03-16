<template>
  <PageContainer with-tabbar class="home-page">
    <PageHeader title="首页" subtitle="教学排课工作台" />

    <div class="stat-card animate-slide-up">
      <div class="stat-card-content">
        <div class="stat-card-meta">当前学期概览</div>
        <div class="stat-card-title">本学期已排课程</div>
        <div class="stat-card-value">{{ stats.totalCourses }}</div>
        <div class="stat-card-desc">共 {{ stats.totalHours }} 学时</div>
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
      <div class="section-title">最新课表</div>
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
            </div>
            <n-tag :type="getStatusType(latestTimetable.status)" class="status-tag-custom">
              {{ getStatusText(latestTimetable.status) }}
            </n-tag>
          </div>
          <div class="mt-16 info-group">
            <n-descriptions :column="1" :bordered="false" size="medium">
              <n-descriptions-item label="排课任务">{{ latestTimetable.taskCount }} 个</n-descriptions-item>
              <n-descriptions-item label="已排课程">{{ latestTimetable.scheduledCount }} 个</n-descriptions-item>
              <n-descriptions-item label="冲突数量">{{ latestTimetable.conflictCount }} 个</n-descriptions-item>
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
import { getLatestTimetable } from '@/api/timetable'
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
  SettingsOutline
} from '@vicons/ionicons5'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const latestTimetable = ref(null)
const screenWidth = ref(window.innerWidth)

const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const gridColumns = computed(() => {
  if (screenWidth.value >= 1600) return 6
  if (screenWidth.value >= 1024) return 3
  return 3
})

const quickActions = computed(() => {
  const actions = [
    { to: '/timetable', text: '生成课表', icon: CalendarOutline, color: '#728967' },
    { to: '/task', text: '教学任务', icon: ClipboardOutline, color: '#7d9563' },
    { to: '/schedule', text: '课表查询', icon: SearchOutline, color: '#c69054' },
    { to: '/adjustment', text: '调课申请', icon: SwapHorizontalOutline, color: '#b86659' },
    { to: '/statistics', text: '统计分析', icon: BarChartOutline, color: '#6f89a3' },
    { to: '/profile', text: '系统设置', icon: SettingsOutline, color: '#7d7064' }
  ]
  
  if (isAdmin.value) {
    actions.splice(5, 0, { to: '/users', text: '用户管理', icon: PeopleOutline, color: '#9b7652' })
  }
  
  return actions
})

const updateScreenWidth = () => {
  screenWidth.value = window.innerWidth
}

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
  window.addEventListener('resize', updateScreenWidth)
  loading.value = true
  try {
    const semester = getCurrentSemester()
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

.view-btn {
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.view-btn:active {
  transform: translateY(1px);
}

.stat-card-meta {
  display: inline-flex;
  align-items: center;
  padding: 4px 10px;
  margin-bottom: 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
  letter-spacing: 0.06em;
  font-weight: 600;
}

@media (max-width: 480px) {
  .timetable-name {
    font-size: 16px;
  }
  
  .view-btn {
    height: 44px;
    font-size: 15px;
  }
}
</style>
