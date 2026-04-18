<template>
  <PageContainer with-tabbar>
    <div class="timetable-page-content">
      <PageHeader title="课表管理">
        <template #actions>
          <n-button quaternary @click="onRefresh">
            <template #icon>
              <n-icon>
                <RefreshOutline />
              </n-icon>
            </template>
            刷新
          </n-button>
        </template>
      </PageHeader>

      <div v-if="generationJob" class="job-card">
        <div class="job-card-header">
          <div>
            <div class="job-card-title">{{ getGenerationTitle(generationJob.status) }}</div>
            <div class="job-card-desc">{{ getGenerationMessage(generationJob) }}</div>
          </div>
          <div class="job-card-actions">
            <n-tag :type="getGenerationTagType(generationJob.status)" :class="['semantic-tag', getGenerationTagClass(generationJob.status)]">
              {{ generationJob.status }}
            </n-tag>
            <n-button
              v-if="generationJob.status === 'FAILED'"
              quaternary
              size="small"
              @click="clearGenerationJob"
            >
              关闭
            </n-button>
          </div>
        </div>
      </div>

      <div class="table-container animate-fade-in">
        <div class="table-header">
          <div class="table-title desktop-only">课表列表</div>
        </div>

        <div class="mobile-actions mobile-only">
          <n-button type="primary" block @click="showGenerate = true" class="add-btn-mobile">
            <template #icon>
              <n-icon>
                <AddOutline />
              </n-icon>
            </template>
            新建课表
          </n-button>
        </div>

        <div class="desktop-content">
          <n-spin :show="loading" class="loading-container">
            <div v-if="!loading" class="timetable-list grid-layout">
              <div v-for="item in list" :key="item.id" class="card timetable-item" @click="goDetail(item.id)">
                <div class="flex-between">
                  <div class="flex-1">
                    <div class="timetable-title">{{ item.name }}</div>
                    <div class="text-muted mt-8">{{ item.semester }}</div>
                  </div>
                  <n-tag :type="getStatusTagType(item.status)" size="small" :class="['semantic-tag', getStatusTagClass(item.status)]">
                    {{ getStatusText(item.status) }}
                  </n-tag>
                </div>
                <n-grid :x-gap="16" :y-gap="16" :cols="4" class="mt-16 stat-grid">
                  <n-grid-item>
                    <div class="stat-item">
                      <div class="stat-value">{{ item.taskCount }}</div>
                      <div class="stat-label">任务数</div>
                    </div>
                  </n-grid-item>
                  <n-grid-item>
                    <div class="stat-item">
                      <div class="stat-value semantic-number semantic-number--success">{{ item.scheduledCount }}</div>
                      <div class="stat-label">已排课</div>
                    </div>
                  </n-grid-item>
                  <n-grid-item>
                    <div class="stat-item">
                      <div class="stat-value semantic-number semantic-number--danger">{{ item.conflictCount }}</div>
                      <div class="stat-label">冲突</div>
                    </div>
                  </n-grid-item>
                  <n-grid-item>
                    <div class="stat-item">
                      <div class="stat-value semantic-number semantic-number--info">{{ item.utilizationRate ? item.utilizationRate.toFixed(1) : 0 }}%</div>
                      <div class="stat-label">利用率</div>
                    </div>
                  </n-grid-item>
                </n-grid>
                <div class="text-muted mt-8" style="font-size: 12px;">
                  <n-icon size="12">
                    <TimeOutline />
                  </n-icon>
                  生成时间：{{ formatTime(item.generateTime) }}
                </div>
              </div>
            </div>
            <n-empty v-if="list.length === 0 && !loading" description="暂无课表数据" />
          </n-spin>
        </div>

        <div class="mobile-content">
          <div class="user-list-wrapper">
            <div>
              <n-list v-if="list.length > 0">
                <n-list-item v-for="item in list" :key="item.id" @click="goDetail(item.id)">
                  <template #header>
                    <div class="flex-between">
                      <div class="timetable-title">{{ item.name }}</div>
                      <n-tag :type="getStatusTagType(item.status)" size="small" :class="['semantic-tag', getStatusTagClass(item.status)]">
                        {{ getStatusText(item.status) }}
                      </n-tag>
                    </div>
                  </template>
                  <div class="text-muted mt-8">{{ item.semester }}</div>
                  <n-grid :x-gap="8" :y-gap="8" :cols="4" class="mt-16 stat-grid">
                    <n-grid-item>
                      <div class="stat-item">
                        <div class="stat-value">{{ item.taskCount }}</div>
                        <div class="stat-label">任务数</div>
                      </div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-item">
                        <div class="stat-value semantic-number semantic-number--success">{{ item.scheduledCount }}</div>
                        <div class="stat-label">已排课</div>
                      </div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-item">
                        <div class="stat-value semantic-number semantic-number--danger">{{ item.conflictCount }}</div>
                        <div class="stat-label">冲突</div>
                      </div>
                    </n-grid-item>
                    <n-grid-item>
                      <div class="stat-item">
                        <div class="stat-value semantic-number semantic-number--info">{{ item.utilizationRate ? item.utilizationRate.toFixed(1) : 0 }}%</div>
                        <div class="stat-label">利用率</div>
                      </div>
                    </n-grid-item>
                  </n-grid>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无课表数据" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <n-modal v-model:show="showGenerate" preset="card" title="生成新课表" :style="{ width: '500px' }" class="generate-dialog">
      <n-form ref="formRef" :model="generateForm" label-placement="left" label-width="100px">
        <n-form-item label="学期" path="semester">
          <n-select v-model:value="generateForm.semester" :options="semesterOptions" placeholder="请选择学期" />
        </n-form-item>
        <n-form-item label="算法类型">
          <n-select v-model:value="generateForm.algorithmType" :options="algorithmOptions" placeholder="请选择算法" />
        </n-form-item>
        <n-form-item label="高级选项">
          <n-switch v-model:value="showAdvanced" />
        </n-form-item>
        <template v-if="showAdvanced">
          <n-form-item label="最大迭代">
            <n-input-number v-model:value="generateForm.maxGenerations" placeholder="默认500" style="width: 100%" :min="1" />
          </n-form-item>
          <n-form-item label="目标适应度">
            <n-input-number v-model:value="generateForm.targetFitness" placeholder="默认0.95" style="width: 100%" :min="0" :max="1" :step="0.01" />
          </n-form-item>
        </template>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showGenerate = false">取消</n-button>
          <n-button type="primary" :loading="generating" @click="handleGenerate">
            开始生成
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </PageContainer>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { getTimetableList, generateTimetableAsync, getTimetableGenerationJob, getAlgorithms } from '@/api/timetable'
import { buildSemesterOptions, getCurrentSemester } from '@/utils/semester'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { useLayoutStore } from '@/stores/layout'
import {
  NButton,
  NIcon,
  NSpin,
  NTag,
  NList,
  NListItem,
  NModal,
  NForm,
  NFormItem,
  NSelect,
  NSwitch,
  NInputNumber,
  NEmpty,
  NSpace,
  NGrid,
  NGridItem
} from 'naive-ui'
import {
  RefreshOutline,
  AddOutline,
  TimeOutline
} from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()
const layoutStore = useLayoutStore()

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const list = ref([])
const page = ref(1)
const pageSize = 10

const showGenerate = ref(false)
const generating = ref(false)
const showAdvanced = ref(false)
const generationJob = ref(null)
let generationJobTimer = null

const generateForm = ref({
  semester: '',
  algorithmType: 'GREEDY',
  daysPerWeek: 5,
  slotsPerDay: 10,
  maxGenerations: null,
  targetFitness: null
})

const algorithmOptions = ref([])

const semesterOptions = computed(() => buildSemesterOptions(dayjs().year(), 5))

const loadAlgorithms = async () => {
  try {
    const res = await getAlgorithms()
    algorithmOptions.value = res.data.map(alg => ({
      label: alg.name,
      value: alg.code
    }))
  } catch (e) {
    console.error('加载算法列表失败', e)
    algorithmOptions.value = [
      { label: '贪心算法', value: 'GREEDY' },
      { label: '遗传算法', value: 'GENETIC' }
    ]
  }
}

const getStatusText = (status) => {
  const map = { 'DRAFT': '草稿', 'PUBLISHED': '已发布', 'ARCHIVED': '已归档' }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = { 'DRAFT': 'warning', 'PUBLISHED': 'success', 'ARCHIVED': 'default' }
  return map[status] || 'default'
}

const getStatusTagClass = (status) => {
  const map = {
    'DRAFT': 'semantic-tag--warning',
    'PUBLISHED': 'semantic-tag--success',
    'ARCHIVED': 'semantic-tag--info'
  }
  return map[status] || ''
}

const formatTime = (time) => time ? dayjs(time).format('MM-DD HH:mm') : '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTimetableList({ current: 1, size: 100 })
    list.value = res.data.records || []
  } catch (e) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  loadData()
}

const stopGenerationPolling = () => {
  if (generationJobTimer) {
    window.clearTimeout(generationJobTimer)
    generationJobTimer = null
  }
}

const clearGenerationJob = () => {
  stopGenerationPolling()
  generationJob.value = null
}

const getGenerationTagType = (status) => {
  const map = {
    'SUBMITTED': 'info',
    'RUNNING': 'warning',
    'SUCCESS': 'success',
    'FAILED': 'error'
  }
  return map[status] || 'default'
}

const getGenerationTagClass = (status) => {
  const map = {
    'SUBMITTED': 'semantic-tag--info',
    'RUNNING': 'semantic-tag--warning',
    'SUCCESS': 'semantic-tag--success',
    'FAILED': 'semantic-tag--danger'
  }
  return map[status] || ''
}

const getGenerationTitle = (status) => {
  const map = {
    'SUBMITTED': '课表生成任务已提交',
    'RUNNING': '课表生成中',
    'SUCCESS': '课表生成完成',
    'FAILED': '课表生成失败'
  }
  return map[status] || '课表生成任务'
}

const getGenerationMessage = (job) => {
  if (!job) return ''
  if (job.status === 'SUCCESS' && job.timetableName) {
    return `${job.message}：${job.timetableName}`
  }
  return job.message || '正在处理请求'
}

const pollGenerationJob = async (jobId) => {
  if (!jobId) return

  stopGenerationPolling()

  const run = async () => {
    try {
      const res = await getTimetableGenerationJob(jobId)
      generationJob.value = res.data

      if (res.data.status === 'SUCCESS') {
        await loadData()
        stopGenerationPolling()
        message.success(res.data.message || '课表生成成功')
        router.push(`/timetable/detail/${res.data.timetableId}`)
        return
      }

      if (res.data.status === 'FAILED') {
        stopGenerationPolling()
        message.error(res.data.message || '课表生成失败')
        return
      }
    } catch (e) {
      console.error('查询课表生成任务失败', e)
    }

    generationJobTimer = window.setTimeout(run, 2000)
  }

  await run()
}

const handleGenerate = async () => {
  if (!generateForm.value.semester) {
    message.warning('请选择学期')
    return
  }

  generating.value = true
  try {
    const res = await generateTimetableAsync(generateForm.value)
    generationJob.value = res.data
    showGenerate.value = false
    message.success('排课任务已提交')
    pollGenerationJob(res.data.jobId)
  } catch (e) {
    message.error(e.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const goDetail = (id) => {
  router.push(`/timetable/detail/${id}`)
}

onMounted(() => {
  generateForm.value.semester = getCurrentSemester()

  loadAlgorithms()
  loadData()

  layoutStore.setHeaderAction({
    icon: 'plus',
    text: '新建',
    onClick: () => { showGenerate.value = true }
  })
})

onUnmounted(() => {
  stopGenerationPolling()
  layoutStore.clearHeaderAction()
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
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

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

.timetable-page-content {
  animation: fadeIn 0.3s ease-out;
}

.job-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.job-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--spacing-md);
}

.job-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.job-card-desc {
  margin-top: 6px;
  color: var(--text-muted);
  font-size: 13px;
}

.job-card-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.table-container {
  position: relative;
  border: 1px solid rgba(145, 120, 91, 0.18);
  border-radius: var(--radius-xl);
  background: var(--fabric-surface), rgba(255, 250, 243, 0.76);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px dashed var(--border-soft);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.table-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.01em;
}

.mobile-actions {
  padding: var(--spacing-md);
}

.add-btn-mobile {
  height: 44px;
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--spacing-2xl);
}

.desktop-content {
  display: block;
  padding: var(--spacing-lg);
}

.mobile-content {
  display: none;
}

.desktop-only {
  display: block;
}

.mobile-only {
  display: none;
}

.timetable-list.grid-layout {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
  padding: 0;
}

.timetable-item {
  cursor: pointer;
  padding: var(--spacing-xl);
  animation: slideUp 0.3s ease-out backwards;
  transition: all var(--transition-base);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.62), rgba(255, 255, 255, 0.16)),
    rgba(255, 250, 243, 0.7);
  border: 1px solid rgba(145, 120, 91, 0.12);
  border-radius: var(--radius-lg);
}

.timetable-item:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.timetable-item:nth-child(1) { animation-delay: 0.05s; }
.timetable-item:nth-child(2) { animation-delay: 0.1s; }
.timetable-item:nth-child(3) { animation-delay: 0.15s; }
.timetable-item:nth-child(4) { animation-delay: 0.2s; }

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.flex-1 {
  flex: 1;
  min-width: 0;
}

.timetable-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.text-muted {
  color: var(--text-muted);
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

.stat-grid {
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0.08)),
    rgba(244, 237, 222, 0.82);
  border-radius: var(--radius-lg);
  padding: var(--spacing-md);
  border: 1px solid rgba(145, 120, 91, 0.1);
}

.stat-item {
  padding: var(--spacing-sm);
  text-align: center;
  border-radius: var(--radius-md);
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: var(--spacing-xs);
}

.user-list-wrapper {
  padding: var(--spacing-lg);
}

.generate-dialog {
  margin: var(--spacing-md);
}

.mt-8 {
  margin-top: 8px;
}

.mt-16 {
  margin-top: 16px;
}

@media (min-width: 1440px) {
  .timetable-list.grid-layout {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--spacing-xl);
  }

  .timetable-item {
    padding: var(--spacing-xl);
  }

  .timetable-title {
    font-size: 18px;
  }
}

@media (max-width: 1199px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 767px) {
  .desktop-only {
    display: none;
  }

  .mobile-only {
    display: block;
  }

  .desktop-content {
    display: none;
  }

  .mobile-content {
    display: block;
  }

  .table-container {
    background: transparent;
    box-shadow: none;
  }

  .table-header {
    display: none;
  }

  .stat-value {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .stat-label {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 4px;
  }

  .add-btn-mobile {
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    background: var(--primary-gradient);
    border: none;
    box-shadow: 0 4px 12px rgba(114, 137, 103, 0.35);
  }

  .add-btn-mobile .n-icon {
    color: #fff;
  }

  .desktop-content,
  .user-list-wrapper {
    padding: var(--spacing-md);
  }
}
</style>
