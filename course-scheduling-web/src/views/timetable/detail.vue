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
        <div class="stats-grid">
          <div class="stat-card-desktop" tabindex="0" role="article">
            <div class="stat-icon" style="background: rgba(114, 137, 103, 0.12);">
              <n-icon size="32" color="var(--primary-color)">
                <ListOutline />
              </n-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ timetable.taskCount }}</div>
              <div class="stat-label">任务数</div>
            </div>
          </div>

          <div class="stat-card-desktop" tabindex="0" role="article">
            <div class="stat-icon" style="background: rgba(125, 149, 99, 0.14);">
              <n-icon size="32" color="var(--success-color)">
                <CheckmarkDoneOutline />
              </n-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value text-success">{{ timetable.scheduledCount }}</div>
              <div class="stat-label">已排课</div>
            </div>
          </div>

          <div class="stat-card-desktop" tabindex="0" role="article">
            <div class="stat-icon" style="background: rgba(184, 102, 89, 0.14);">
              <n-icon size="32" color="var(--danger-color)">
                <WarningOutline />
              </n-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value text-danger">{{ timetable.conflictCount }}</div>
              <div class="stat-label">冲突</div>
            </div>
          </div>

          <div class="stat-card-desktop" tabindex="0" role="article">
            <div class="stat-icon" style="background: rgba(198, 144, 84, 0.14);">
              <n-icon size="32" color="var(--warning-color)">
                <TrendingUpOutline />
              </n-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value text-primary">{{ timetable.utilizationRate ? timetable.utilizationRate.toFixed(1) : 0 }}%</div>
              <div class="stat-label">利用率</div>
            </div>
          </div>
        </div>

        <n-tabs v-model:value="activeTab" class="content-tabs">
          <n-tab-pane name="timetable" tab="课表视图">
            <div class="card timetable-card">
              <div class="timetable-grid">
                <div class="timetable-header"></div>
                <div v-for="day in 5" :key="'h'+day" class="timetable-header">
                  周{{ ['一', '二', '三', '四', '五'][day - 1] }}
                </div>
                <template v-for="slot in 10" :key="'s'+slot">
                  <div class="timetable-header">{{ slot }}</div>
                  <div
                    v-for="day in 5"
                    :key="'c'+day+'-'+slot"
                    class="timetable-cell"
                    :class="{
                      'has-course': getCourse(day, slot),
                      'conflict': getCourse(day, slot)?.isConflict === 1
                    }"
                    @click="showCourseInfo(day, slot)"
                  >
                    <div v-if="getCourse(day, slot)" class="course-block">
                      <div class="course-block-name">{{ getCourse(day, slot).courseName }}</div>
                      <div class="course-block-info">{{ getCourse(day, slot).classroomName }}</div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </n-tab-pane>

          <n-tab-pane name="courses" tab="课程列表">
            <div class="card">
              <div class="table-wrapper">
                <table class="data-table">
                  <thead>
                    <tr>
                      <th>课程名称</th>
                      <th>教师</th>
                      <th>班级</th>
                      <th>教室</th>
                      <th>时间</th>
                      <th>状态</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="detail in details" :key="detail.id">
                      <td class="name-cell">{{ detail.courseName }}</td>
                      <td>{{ detail.teacherName }}</td>
                      <td>{{ detail.className }}</td>
                      <td>{{ detail.classroomName }}</td>
                      <td>周{{ detail.dayOfWeek }} 第{{ detail.slotNo }}节</td>
                      <td>
                        <n-tag v-if="detail.isConflict === 1" type="error">冲突</n-tag>
                        <n-tag v-else type="success">正常</n-tag>
                      </td>
                      <td>
                        <n-button size="small" type="primary" @click="showDetailInfo(detail)">详情</n-button>
                        <n-button size="small" type="default" @click="goAdjustmentFromDetail(detail)">调课</n-button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <n-empty v-if="details.length === 0" description="暂无数据" />
            </div>
          </n-tab-pane>

          <n-tab-pane name="conflicts" :tab="'冲突(' + conflicts.length + ')'">
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
                      <th>教师</th>
                      <th>冲突信息</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="conflict in conflicts" :key="conflict.id">
                      <td class="name-cell">{{ conflict.courseName }}</td>
                      <td>{{ conflict.teacherName }}</td>
                      <td class="text-danger">{{ conflict.conflictInfo }}</td>
                      <td>
                        <n-button size="small" type="error" @click="goAdjustmentFromDetail(conflict)">处理</n-button>
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

    <n-modal v-model:show="showCoursePopup" preset="card" title="课程详情" :style="{ width: '500px' }" class="course-dialog">
      <div v-if="currentCourse" class="course-details">
        <n-descriptions :column="1" bordered>
          <n-descriptions-item label="课程名称">{{ currentCourse.courseName }}</n-descriptions-item>
          <n-descriptions-item label="教师">{{ currentCourse.teacherName }}</n-descriptions-item>
          <n-descriptions-item label="班级">{{ currentCourse.className }}</n-descriptions-item>
          <n-descriptions-item label="教室">{{ currentCourse.classroomName }}</n-descriptions-item>
          <n-descriptions-item label="时间">周{{ currentCourse.dayOfWeek }} 第{{ currentCourse.slotNo }}节</n-descriptions-item>
          <n-descriptions-item v-if="currentCourse.weeks" label="上课周次">{{ currentCourse.weeks }}</n-descriptions-item>
          <n-descriptions-item v-if="currentCourse.isConflict === 1" label="冲突信息">
            <span class="text-danger">{{ currentCourse.conflictInfo }}</span>
          </n-descriptions-item>
        </n-descriptions>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button type="primary" @click="goAdjustment">申请调课</n-button>
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
import { getTimetableById, getTimetableDetails, getConflicts, publishTimetable, archiveTimetable, deleteTimetable } from '@/api/timetable'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { useLayoutStore } from '@/stores/layout'
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
  ListOutline,
  CheckmarkDoneOutline,
  WarningOutline,
  TrendingUpOutline,
  CheckmarkCircleOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const layoutStore = useLayoutStore()

const loading = ref(true)
const timetable = ref(null)
const details = ref([])
const conflicts = ref([])
const activeTab = ref('timetable')
const showCoursePopup = ref(false)
const currentCourse = ref(null)

const dropdownOptions = computed(() => {
  const options = []
  if (timetable.value?.status === 'DRAFT') {
    options.push({ label: '发布课表', value: 'publish' })
  }
  if (timetable.value?.status !== 'ARCHIVED') {
    options.push({ label: '归档课表', value: 'archive' })
  }
  if (timetable.value?.status === 'DRAFT') {
    options.push({ label: '删除课表', value: 'delete' })
  }
  return options.length > 0 ? options : [{ label: '无操作', value: 'none' }]
})

const getStatusText = (status) => {
  const map = { 'DRAFT': '草稿', 'PUBLISHED': '已发布', 'ARCHIVED': '已归档' }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = { 'DRAFT': 'warning', 'PUBLISHED': 'success', 'ARCHIVED': 'default' }
  return map[status] || 'default'
}

const detailMap = computed(() => {
  const map = new Map()
  for (const detail of details.value) {
    map.set(`${detail.dayOfWeek}_${detail.slotNo}`, detail)
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
  } catch (e) {
    console.error(e)
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  layoutStore.clearHeaderAction()
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
}

.course-block-name {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-block-info {
  font-size: 11px;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
  padding: var(--spacing-md) 0;
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
</style>
