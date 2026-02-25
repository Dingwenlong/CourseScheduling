<template>
  <div class="page page-with-tabbar">
    <van-nav-bar title="统计分析" class="custom-nav" />

    <div class="search-wrapper mt-16">
      <van-dropdown-menu class="flex-1">
        <van-dropdown-item v-model="selectedTimetable" :options="timetableOptions" @change="loadStatistics" />
      </van-dropdown-menu>
    </div>

    <van-loading v-if="loading" class="loading-container" />

    <template v-else-if="selectedTimetable">
      <div class="stat-card">
        <div class="stat-card-title">总排课学时</div>
        <div class="stat-card-value">{{ totalHours }}</div>
        <div class="stat-card-desc">{{ courseCount }} 门课程</div>
      </div>

      <van-tabs v-model:active="activeTab" sticky>
        <van-tab title="教室利用率">
          <div class="card">
            <div class="page-title">教室利用率排行</div>
            <van-cell-group inset class="stat-grid-layout grid-adaptive-lg">
              <van-cell
                v-for="item in classroomUtilization"
                :key="item.classroomId"
                :title="item.classroomName"
                class="stat-grid-item"
              >
                <template #value>
                  <div class="flex-column" style="align-items: flex-end;">
                    <span>{{ item.utilizationRate ? item.utilizationRate.toFixed(1) : 0 }}%</span>
                    <span class="text-muted" style="font-size: 12px;">{{ item.usedSlots }}/{{ item.totalSlots }} 节</span>
                  </div>
                </template>
              </van-cell>
            </van-cell-group>
          </div>
        </van-tab>

        <van-tab title="教师工作量">
          <div class="card">
            <div class="page-title">教师工作量统计</div>
            <van-cell-group inset class="stat-grid-layout grid-adaptive-lg">
              <van-cell
                v-for="item in teacherWorkload"
                :key="item.teacherId"
                :title="item.teacherName"
                class="stat-grid-item"
              >
                <template #value>
                  <div class="flex-column" style="align-items: flex-end;">
                    <span>{{ item.totalHours }} 学时</span>
                    <span class="text-muted" style="font-size: 12px;">{{ item.courseCount }} 门课</span>
                  </div>
                </template>
              </van-cell>
            </van-cell-group>
          </div>
        </van-tab>

        <van-tab title="冲突报告">
          <div class="card">
            <div class="page-title">冲突统计</div>
            <van-grid :column-num="4" :border="false">
              <van-grid-item>
                <div class="stat-value text-danger">{{ conflictReport.totalConflicts }}</div>
                <div class="stat-label">总冲突</div>
              </van-grid-item>
              <van-grid-item>
                <div class="stat-value">{{ conflictReport.teacherConflicts }}</div>
                <div class="stat-label">教师冲突</div>
              </van-grid-item>
              <van-grid-item>
                <div class="stat-value">{{ conflictReport.classroomConflicts }}</div>
                <div class="stat-label">教室冲突</div>
              </van-grid-item>
              <van-grid-item>
                <div class="stat-value">{{ conflictReport.classConflicts }}</div>
                <div class="stat-label">班级冲突</div>
              </van-grid-item>
            </van-grid>
          </div>

          <div v-if="conflictReport.conflictDetails && conflictReport.conflictDetails.length > 0" class="card">
            <div class="page-title">冲突详情</div>
            <van-cell-group inset>
              <van-cell
                v-for="item in conflictReport.conflictDetails"
                :key="item.detailId"
                :title="item.courseName"
                :label="`周${item.dayOfWeek} 第${item.slotNo}节`"
              >
                <template #value>
                  <van-tag :class="['status-tag', 'tag-danger']">{{ item.conflictType || '冲突' }}</van-tag>
                </template>
              </van-cell>
            </van-cell-group>
          </div>
          <van-empty v-else description="暂无冲突" />
        </van-tab>
      </van-tabs>
    </template>

    <van-empty v-else description="请选择课表" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { getTimetableList } from '@/api/timetable'
import { getClassroomUtilization, getTeacherWorkload, getConflictReport, getTotalHours, getCourseCount } from '@/api/statistics'

const loading = ref(false)
const activeTab = ref(0)
const selectedTimetable = ref('')
const timetableOptions = ref([{ text: '请选择课表', value: '' }])

const totalHours = ref(0)
const courseCount = ref(0)
const classroomUtilization = ref([])
const teacherWorkload = ref([])
const conflictReport = ref({})

const loadTimetables = async () => {
  try {
    const res = await getTimetableList({ current: 1, size: 20 })
    timetableOptions.value = [
      { text: '请选择课表', value: '' },
      ...res.data.records.map(t => ({ text: t.name, value: t.id.toString() }))
    ]
    if (res.data.records.length > 0) {
      selectedTimetable.value = res.data.records[0].id.toString()
      loadStatistics()
    }
  } catch (e) {
    console.error(e)
  }
}

const loadStatistics = async () => {
  if (!selectedTimetable.value) return
  
  loading.value = true
  try {
    const id = selectedTimetable.value
    const [hoursRes, countRes, utilizationRes, workloadRes, conflictRes] = await Promise.all([
      getTotalHours(id),
      getCourseCount(id),
      getClassroomUtilization(id),
      getTeacherWorkload(id),
      getConflictReport(id)
    ])
    totalHours.value = hoursRes.data
    courseCount.value = countRes.data
    classroomUtilization.value = utilizationRes.data
    teacherWorkload.value = workloadRes.data
    conflictReport.value = conflictRes.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTimetables()
})
</script>

<style scoped>
.custom-nav {
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

@media (min-width: 768px) {
  .custom-nav {
    display: none;
  }
}

@media (min-width: 1024px) {
  .page {
    max-width: var(--content-max-width);
    margin: 0 auto;
  }
  
  .stat-grid-item {
    border-bottom: 1px solid var(--border-light);
  }
}

@media (min-width: 1440px) {
  .page {
    max-width: var(--content-max-width-wide);
  }
  
  .stat-grid-layout {
    padding: var(--spacing-lg);
  }
  
  .stat-grid-item {
    border: 1px solid var(--border-light);
    border-radius: var(--radius-md);
  }
}

@media (min-width: 1920px) {
  .page {
    max-width: var(--content-max-width-ultra);
  }
  
  .stat-card {
    padding: var(--spacing-2xl);
    margin: var(--spacing-xl);
  }
  
  .stat-card-value {
    font-size: 48px;
  }
}

@media (min-width: 2560px) {
  .page {
    max-width: var(--content-max-width-super);
  }

  .stat-card-value {
    font-size: 56px;
  }

  .page-title {
    font-size: 20px;
  }
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: var(--spacing-xs);
}
</style>
