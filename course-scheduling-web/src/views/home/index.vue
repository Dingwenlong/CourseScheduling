<template>
  <div class="page page-with-tabbar">
    <van-nav-bar title="首页" />

    <div class="stat-card">
      <div class="stat-card-title">本学期已排课程</div>
      <div class="stat-card-value">{{ stats.totalCourses }}</div>
      <div class="stat-card-desc">共 {{ stats.totalHours }} 学时</div>
    </div>

    <div class="quick-actions">
      <van-grid :column-num="3" :border="false">
        <van-grid-item icon="calendar-o" text="生成课表" to="/timetable" />
        <van-grid-item icon="todo-list-o" text="教学任务" to="/task" />
        <van-grid-item icon="search" text="课表查询" to="/schedule" />
        <van-grid-item icon="exchange" text="调课申请" to="/adjustment" />
        <van-grid-item icon="chart-trending-o" text="统计分析" to="/statistics" />
        <van-grid-item icon="setting-o" text="系统设置" to="/profile" />
      </van-grid>
    </div>

    <div class="card">
      <div class="page-title">最新课表</div>
      <van-loading v-if="loading" class="loading-container" />
      <div v-else-if="latestTimetable" class="timetable-info">
        <div class="flex-between">
          <div>
            <div class="timetable-name">{{ latestTimetable.name }}</div>
            <div class="text-muted mt-8">
              {{ latestTimetable.semester }} · 第{{ latestTimetable.version }}版
            </div>
          </div>
          <van-tag :type="getStatusType(latestTimetable.status)">
            {{ getStatusText(latestTimetable.status) }}
          </van-tag>
        </div>
        <van-cell-group inset class="mt-16">
          <van-cell title="排课任务" :value="latestTimetable.taskCount + ' 个'" />
          <van-cell title="已排课程" :value="latestTimetable.scheduledCount + ' 个'" />
          <van-cell title="冲突数量" :value="latestTimetable.conflictCount + ' 个'" />
          <van-cell title="生成时间" :value="formatTime(latestTimetable.generateTime)" />
        </van-cell-group>
        <van-button
          round
          block
          type="primary"
          class="mt-16"
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
.quick-actions {
  margin: 12px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.timetable-info {
  padding: 0 4px;
}

.timetable-name {
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}
</style>
