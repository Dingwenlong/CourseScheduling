<template>
  <PageContainer with-tabbar>
    <div class="statistics-page">
      <PageHeader :title="pageTitle" :subtitle="pageSubtitle">
        <template #actions>
          <n-select
            v-model:value="selectedTimetable"
            :options="timetableOptions"
            placeholder="请选择当前课表"
            style="width: 240px"
            @update:value="loadStatistics"
          />
        </template>
      </PageHeader>

      <n-spin :show="loading" class="loading-container">
        <div v-if="selectedTimetable">
          <div v-if="isTeacherView" class="role-guide animate-fade-in">
            <div class="role-guide-title">当前查看的是你的授课统计</div>
            <div class="role-guide-text">
              这里的工作量、冲突提醒和教室安排都只统计你在当前课表中的课程，不会混入其他教师的数据。
            </div>
          </div>

          <div class="stat-card animate-fade-in">
            <div class="stat-card-content">
              <div class="stat-card-title">{{ summaryTitle }}</div>
              <div class="stat-card-value">{{ totalHours }}</div>
              <div class="stat-card-desc">{{ summaryDescription }}</div>
              <div class="stat-card-note">{{ selectedTimetableLabel }}</div>
            </div>
          </div>

          <div class="tabs-container">
            <n-tabs v-model:value="activeTab" class="statistics-tabs">
              <n-tab-pane name="classroom" :tab="classroomTabLabel">
                <div class="card">
                  <div class="section-title">
                    <span>{{ classroomSectionTitle }}</span>
                  </div>
                  <div class="section-hint">{{ classroomSectionHint }}</div>
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

              <n-tab-pane name="teacher" :tab="teacherTabLabel">
                <div class="card">
                  <div class="section-title">
                    <span>{{ teacherSectionTitle }}</span>
                  </div>
                  <div class="section-hint">{{ teacherSectionHint }}</div>
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

              <n-tab-pane name="conflict" :tab="conflictTabLabel">
                <div class="card">
                  <div class="section-title">
                    <span>{{ conflictSectionTitle }}</span>
                  </div>
                  <div class="section-hint">{{ conflictSectionHint }}</div>
                    <n-grid :x-gap="16" :y-gap="16" :cols="4" class="conflict-grid">
                    <n-grid-item>
                      <div class="stat-value semantic-number semantic-number--danger">{{ conflictReport.totalConflicts }}</div>
                      <div class="stat-label">{{ totalConflictLabel }}</div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-value semantic-number semantic-number--warning">{{ conflictReport.teacherConflicts }}</div>
                      <div class="stat-label">教师冲突</div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-value semantic-number semantic-number--info">{{ conflictReport.classroomConflicts }}</div>
                      <div class="stat-label">教室冲突</div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-value semantic-number">{{ conflictReport.classConflicts }}</div>
                      <div class="stat-label">班级冲突</div>
                    </n-grid-item>
                  </n-grid>
                </div>

                <div v-if="conflictReport.conflictDetails && conflictReport.conflictDetails.length > 0" class="card mt-16">
                  <div class="section-title">
                    <span>{{ conflictDetailTitle }}</span>
                  </div>
                  <n-list>
                    <n-list-item v-for="item in conflictReport.conflictDetails" :key="item.detailId" class="conflict-item">
                      <template #header>
                        <div class="flex-between">
                          <div class="conflict-name">{{ item.courseName }}</div>
                          <n-tag type="error" class="semantic-tag semantic-tag--danger">
                            {{ item.conflictType || '冲突' }}
                          </n-tag>
                        </div>
                      </template>
                      <div class="conflict-meta">{{ courseTimeText(item) }}</div>
                      <div v-if="conflictScopeText(item)" class="conflict-subtext">{{ conflictScopeText(item) }}</div>
                    </n-list-item>
                  </n-list>
                </div>
                <n-empty v-else :description="conflictEmptyDescription" class="empty-state" />
              </n-tab-pane>
            </n-tabs>
          </div>
        </div>

        <n-empty v-else :description="pageEmptyDescription" class="empty-state" />
      </n-spin>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
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
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref(userStore.userInfo?.role === 'TEACHER' ? 'teacher' : 'classroom')
const selectedTimetable = ref('')
const timetableOptions = ref([{ label: '请选择课表', value: '' }])

const totalHours = ref(0)
const courseCount = ref(0)
const classroomUtilization = ref([])
const teacherWorkload = ref([])
const conflictReport = ref({})

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

const userRole = computed(() => userStore.userInfo?.role)
const isTeacherView = computed(() => userRole.value === 'TEACHER')
const pageTitle = computed(() => isTeacherView.value ? '我的授课统计' : '统计分析')
const pageSubtitle = computed(() => isTeacherView.value
  ? '查看当前课表下你的工作量、冲突提醒和教室安排'
  : '查看排课数据统计与分析')
const summaryTitle = computed(() => isTeacherView.value ? '我的授课总学时' : '总排课学时')
const summaryDescription = computed(() => isTeacherView.value
  ? `${courseCount.value} 门授课课程`
  : `${courseCount.value} 门课程`)
const selectedTimetableLabel = computed(() => {
  const current = timetableOptions.value.find(item => item.value === selectedTimetable.value)
  return current?.label ? `当前课表：${current.label}` : '当前课表未命名'
})
const classroomTabLabel = computed(() => isTeacherView.value ? '教室安排' : '教室利用率')
const teacherTabLabel = computed(() => isTeacherView.value ? '我的工作量' : '教师工作量')
const conflictTabLabel = computed(() => isTeacherView.value ? '冲突提醒' : '冲突报告')
const classroomSectionTitle = computed(() => isTeacherView.value ? '你本课表涉及的教室安排' : '教室利用率排行')
const classroomSectionHint = computed(() => isTeacherView.value
  ? '这里展示你当前授课涉及到的教室使用情况，方便你判断哪些教室更紧张。'
  : '按当前课表统计各教室的使用率和已排时段。')
const teacherSectionTitle = computed(() => isTeacherView.value ? '你的授课工作量' : '教师工作量统计')
const teacherSectionHint = computed(() => isTeacherView.value
  ? '这里会汇总你在当前课表中的总学时和授课门数，方便你快速判断本周的授课负担。'
  : '按当前课表统计各位教师的总学时和课程数量。')
const conflictSectionTitle = computed(() => isTeacherView.value ? '你的冲突提醒' : '冲突统计')
const conflictSectionHint = computed(() => isTeacherView.value
  ? '优先关注教师冲突和教室冲突，它们通常最影响你实际授课。'
  : '查看当前课表中教师、教室和班级的冲突分布。')
const totalConflictLabel = computed(() => isTeacherView.value ? '相关冲突' : '总冲突')
const conflictDetailTitle = computed(() => isTeacherView.value ? '需要你关注的冲突明细' : '冲突详情')
const conflictEmptyDescription = computed(() => isTeacherView.value ? '当前课表下你的授课安排没有冲突' : '暂无冲突')
const pageEmptyDescription = computed(() => isTeacherView.value ? '请选择课表后查看你的授课统计' : '请选择课表')

const slotLabel = (slotNo) => slotMeta[slotNo]?.label || `第${slotNo}节`
const slotTimeRange = (slotNo) => slotMeta[slotNo]?.time || ''
const courseTimeText = (item) => {
  if (!item) return '-'
  const weekday = weekdayLabels[(item.dayOfWeek || 1) - 1] || item.dayOfWeek
  const slot = slotLabel(item.slotNo)
  const time = slotTimeRange(item.slotNo)
  return time ? `周${weekday} ${slot} · ${time}` : `周${weekday} ${slot}`
}
const conflictScopeText = (item) => {
  if (!item) return ''
  const parts = []
  if (item.className) {
    parts.push(item.className)
  }
  if (!isTeacherView.value && item.teacherName) {
    parts.push(item.teacherName)
  }
  if (item.classroomName) {
    parts.push(item.classroomName)
  }
  return parts.join(' · ')
}

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
  position: relative;
  padding-bottom: var(--spacing-xl);
}

.tabs-container {
  margin-top: var(--spacing-lg);
}

.role-guide {
  margin-bottom: var(--spacing-lg);
  padding: 16px 18px;
  border-radius: var(--radius-xl);
  background: linear-gradient(135deg, rgba(114, 137, 103, 0.12), rgba(255, 250, 243, 0.92));
  border: 1px solid rgba(114, 137, 103, 0.16);
  box-shadow: var(--shadow-sm);
}

.role-guide-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.role-guide-text {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.statistics-tabs {
  background: var(--fabric-surface), rgba(255, 250, 243, 0.74);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  padding: var(--spacing-md);
  border: 1px solid rgba(145, 120, 91, 0.18);
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

.section-hint {
  margin: calc(var(--spacing-sm) * -1) 0 var(--spacing-md);
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.stat-card {
  position: relative;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.16), transparent 32%),
    radial-gradient(circle at bottom left, rgba(255, 255, 255, 0.12), transparent 28%),
    linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.stat-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: inherit;
  pointer-events: none;
}

.stat-card-content {
  color: white;
  text-align: center;
  position: relative;
  z-index: 1;
}

.stat-card-title {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.2);
  letter-spacing: 0.03em;
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
  letter-spacing: 0.02em;
}

.stat-card-note {
  margin-top: var(--spacing-sm);
  font-size: 12px;
  opacity: 0.88;
}

.card {
  border: 1px solid rgba(145, 120, 91, 0.18);
  border-radius: var(--radius-xl);
  background: var(--fabric-surface), rgba(255, 250, 243, 0.76);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  padding: var(--spacing-xl);
  margin-bottom: var(--spacing-lg);
}

.stat-grid-layout {
  display: grid;
  gap: var(--spacing-md);
}

.stat-grid-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-lg);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.5), rgba(255, 255, 255, 0.12)),
    rgba(244, 237, 222, 0.82);
  border-radius: var(--radius-md);
  border: 1px solid rgba(145, 120, 91, 0.1);
  transition: all var(--transition-base);
}

.stat-grid-item:hover {
  background: rgba(255, 251, 245, 0.9);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
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
  font-size: 17px;
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
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0.08)),
    rgba(244, 237, 222, 0.82);
  border: 1px solid rgba(145, 120, 91, 0.1);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
}

.stat-value {
  font-size: 24px;
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
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.38), rgba(255, 255, 255, 0.1)),
    rgba(244, 237, 222, 0.82);
  padding: var(--spacing-md);
  border: 1px solid rgba(145, 120, 91, 0.1);
}

.conflict-meta {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.conflict-subtext {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
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

  .card,
  .statistics-tabs {
    padding: var(--spacing-md);
  }
}
</style>
