<template>
  <PageContainer with-tabbar>
    <div class="statistics-page">
      <PageHeader title="统计分析" subtitle="查看排课数据统计与分析">
        <template #actions>
          <n-select
            v-model:value="selectedTimetable"
            :options="timetableOptions"
            placeholder="请选择课表"
            style="width: 240px"
            @update:value="loadStatistics"
          />
        </template>
      </PageHeader>

      <n-spin :show="loading" class="loading-container">
        <div v-if="selectedTimetable">
          <div class="stat-card animate-fade-in">
            <div class="stat-card-content">
              <div class="stat-card-title">总排课学时</div>
              <div class="stat-card-value">{{ totalHours }}</div>
              <div class="stat-card-desc">{{ courseCount }} 门课程</div>
            </div>
          </div>

          <div class="tabs-container">
            <n-tabs v-model:value="activeTab" class="statistics-tabs">
              <n-tab-pane name="classroom" tab="教室利用率">
                <div class="card">
                  <div class="section-title">
                    <span>教室利用率排行</span>
                  </div>
                  <div class="stat-grid-layout grid-adaptive-lg">
                    <div v-for="item in classroomUtilization" :key="item.classroomId" class="stat-grid-item">
                      <div class="stat-item-left">
                        <div class="stat-item-name">{{ item.classroomName }}</div>
                      </div>
                      <div class="stat-item-right">
                        <span class="stat-item-value">{{ item.utilizationRate ? item.utilizationRate.toFixed(1) : 0 }}%</span>
                        <span class="stat-item-desc text-muted">{{ item.usedSlots }}/{{ item.totalSlots }} 节</span>
                      </div>
                    </div>
                  </div>
                </div>
              </n-tab-pane>

              <n-tab-pane name="teacher" tab="教师工作量">
                <div class="card">
                  <div class="section-title">
                    <span>教师工作量统计</span>
                  </div>
                  <div class="stat-grid-layout grid-adaptive-lg">
                    <div v-for="item in teacherWorkload" :key="item.teacherId" class="stat-grid-item">
                      <div class="stat-item-left">
                        <div class="stat-item-name">{{ item.teacherName }}</div>
                      </div>
                      <div class="stat-item-right">
                        <span class="stat-item-value">{{ item.totalHours }} 学时</span>
                        <span class="stat-item-desc text-muted">{{ item.courseCount }} 门课</span>
                      </div>
                    </div>
                  </div>
                </div>
              </n-tab-pane>

              <n-tab-pane name="conflict" tab="冲突报告">
                <div class="card">
                  <div class="section-title">
                    <span>冲突统计</span>
                  </div>
                  <n-grid :x-gap="16" :y-gap="16" :cols="4" class="conflict-grid">
                    <n-grid-item>
                      <div class="stat-value text-danger">{{ conflictReport.totalConflicts }}</div>
                      <div class="stat-label">总冲突</div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-value">{{ conflictReport.teacherConflicts }}</div>
                      <div class="stat-label">教师冲突</div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-value">{{ conflictReport.classroomConflicts }}</div>
                      <div class="stat-label">教室冲突</div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-value">{{ conflictReport.classConflicts }}</div>
                      <div class="stat-label">班级冲突</div>
                    </n-grid-item>
                  </n-grid>
                </div>

                <div v-if="conflictReport.conflictDetails && conflictReport.conflictDetails.length > 0" class="card mt-16">
                  <div class="section-title">
                    <span>冲突详情</span>
                  </div>
                  <n-list>
                    <n-list-item v-for="item in conflictReport.conflictDetails" :key="item.detailId" class="conflict-item">
                      <template #header>
                        <div class="flex-between">
                          <div class="conflict-name">{{ item.courseName }}</div>
                          <n-tag type="error">
                            {{ item.conflictType || '冲突' }}
                          </n-tag>
                        </div>
                      </template>
                      周{{ item.dayOfWeek }} 第{{ item.slotNo }}节
                    </n-list-item>
                  </n-list>
                </div>
                <n-empty v-else description="暂无冲突" class="empty-state" />
              </n-tab-pane>
            </n-tabs>
          </div>
        </div>

        <n-empty v-else description="请选择课表" class="empty-state" />
      </n-spin>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { getTimetableList } from '@/api/timetable'
import { getStatisticsOverview } from '@/api/statistics'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import {
  NSpin,
  NTabs,
  NTabPane,
  NSelect,
  NGrid,
  NGridItem,
  NList,
  NListItem,
  NTag,
  NEmpty
} from 'naive-ui'

const message = useMessage()

const loading = ref(false)
const activeTab = ref('classroom')
const selectedTimetable = ref('')
const timetableOptions = ref([{ label: '请选择课表', value: '' }])

const totalHours = ref(0)
const courseCount = ref(0)
const classroomUtilization = ref([])
const teacherWorkload = ref([])
const conflictReport = ref({})

const loadTimetables = async () => {
  try {
    const res = await getTimetableList({ current: 1, size: 20 })
    timetableOptions.value = [
      { label: '请选择课表', value: '' },
      ...res.data.records.map(t => ({ label: t.name, value: t.id.toString() }))
    ]
    if (res.data.records.length > 0) {
      selectedTimetable.value = res.data.records[0].id.toString()
      loadStatistics()
    }
  } catch (e) {
    console.error(e)
    message.error(e.message || '加载课表列表失败')
  }
}

const loadStatistics = async () => {
  if (!selectedTimetable.value) return

  loading.value = true
  try {
    const res = await getStatisticsOverview(selectedTimetable.value)
    const overview = res.data || {}
    totalHours.value = overview.totalHours || 0
    courseCount.value = overview.courseCount || 0
    classroomUtilization.value = overview.classroomUtilization || []
    teacherWorkload.value = overview.teacherWorkload || []
    conflictReport.value = overview.conflictReport || {}
  } catch (e) {
    console.error(e)
    message.error(e.message || '加载统计数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadTimetables()
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

.statistics-page {
  width: 100%;
}

.tabs-container {
  margin-top: var(--spacing-lg);
}

.statistics-tabs {
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  padding: var(--spacing-sm);
  border: 1px solid var(--border-color);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--spacing-md);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.section-title::before {
  content: "";
  width: 4px;
  height: 18px;
  background: var(--primary-color);
  border-radius: 2px;
  flex-shrink: 0;
}

.stat-card {
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.16), transparent 32%),
    linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-card);
}

.stat-card-content {
  color: white;
  text-align: center;
}

.stat-card-title {
  font-size: 16px;
  font-weight: 500;
  opacity: 0.9;
  margin-bottom: var(--spacing-sm);
}

.stat-card-value {
  font-size: 48px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: var(--spacing-xs);
}

.stat-card-desc {
  font-size: 14px;
  opacity: 0.8;
}

.card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.stat-grid-layout {
  display: grid;
  gap: var(--spacing-sm);
}

.stat-grid-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.stat-grid-item:hover {
  background: rgba(255, 251, 245, 0.72);
  box-shadow: var(--shadow-xs);
}

.stat-item-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.stat-item-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.stat-item-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-item-desc {
  font-size: 12px;
}

.text-muted {
  color: var(--text-muted);
}

.text-danger {
  color: var(--text-danger);
}

.conflict-grid {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md) 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: var(--spacing-xs);
  text-align: center;
}

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
}

.conflict-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.conflict-item {
  margin-bottom: var(--spacing-sm);
  border-radius: var(--radius-md);
  background: var(--bg-secondary);
  padding: var(--spacing-md);
}

.empty-state {
  margin-top: var(--spacing-2xl);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--spacing-3xl) 0;
}

.mt-16 {
  margin-top: 16px;
}

@media (min-width: 1024px) {
  .statistics-tabs {
    padding: var(--spacing-md);
  }

  .section-title {
    font-size: 18px;
  }

  .section-title::before {
    height: 20px;
  }

  .stat-item-value {
    font-size: 18px;
  }

  .grid-adaptive-lg {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 1440px) {
  .tabs-container {
    margin-top: var(--spacing-xl);
  }

  .grid-adaptive-lg {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767px) {
  .section-title {
    font-size: 15px;
  }

  .section-title::before {
    height: 16px;
  }

  .stat-item-value {
    font-size: 15px;
  }

  .stat-card-value {
    font-size: 36px;
  }
}
</style>
