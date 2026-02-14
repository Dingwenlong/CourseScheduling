<template>
  <div class="page page-with-tabbar timetable-page">
    <van-nav-bar title="课表管理" class="custom-nav">
      <template #right>
        <van-button icon="plus" size="small" type="primary" @click="showGenerate = true" class="add-btn">新建</van-button>
      </template>
    </van-nav-bar>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="pull-refresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
        class="timetable-list"
      >
        <div v-for="item in list" :key="item.id" class="card timetable-item" @click="goDetail(item.id)">
          <div class="flex-between">
            <div class="flex-1">
              <div class="timetable-title">{{ item.name }}</div>
              <div class="text-muted mt-8">{{ item.semester }}</div>
            </div>
            <van-tag :type="getStatusType(item.status)" class="status-tag">
              {{ getStatusText(item.status) }}
            </van-tag>
          </div>
          <van-grid :column-num="4" :border="false" class="mt-16 stat-grid">
            <van-grid-item class="stat-item">
              <div class="stat-value">{{ item.taskCount }}</div>
              <div class="stat-label">任务数</div>
            </van-grid-item>
            <van-grid-item class="stat-item">
              <div class="stat-value text-success">{{ item.scheduledCount }}</div>
              <div class="stat-label">已排课</div>
            </van-grid-item>
            <van-grid-item class="stat-item">
              <div class="stat-value text-danger">{{ item.conflictCount }}</div>
              <div class="stat-label">冲突</div>
            </van-grid-item>
            <van-grid-item class="stat-item">
              <div class="stat-value text-primary">{{ item.utilizationRate ? item.utilizationRate.toFixed(1) : 0 }}%</div>
              <div class="stat-label">利用率</div>
            </van-grid-item>
          </van-grid>
          <div class="text-muted mt-8" style="font-size: 12px;">
            <van-icon name="clock-o" /> 生成时间：{{ formatTime(item.generateTime) }}
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showGenerate" position="bottom" round style="height: 70%;" class="generate-popup">
      <div class="popup-header">
        <div class="popup-title">生成新课表</div>
        <van-icon name="cross" size="20" @click="showGenerate = false" class="close-icon" />
      </div>
      <div class="popup-content">
        <van-form @submit="handleGenerate">
          <van-cell-group inset class="form-group">
            <van-field
              v-model="generateForm.semester"
              is-link
              readonly
              name="semester"
              label="学期"
              placeholder="请选择学期"
              @click="showSemesterPicker = true"
              class="form-field"
            />
            <van-field
              v-model="algorithmName"
              is-link
              readonly
              name="algorithm"
              label="算法类型"
              placeholder="请选择算法"
              @click="showAlgorithmPicker = true"
              class="form-field"
            />
            <van-field name="switch" label="高级选项" class="form-field">
              <template #input>
                <van-switch v-model="showAdvanced" size="20" />
              </template>
            </van-field>
            <template v-if="showAdvanced">
              <van-field
                v-model="generateForm.maxGenerations"
                type="number"
                name="maxGenerations"
                label="最大迭代"
                placeholder="默认500"
                class="form-field"
              />
              <van-field
                v-model="generateForm.targetFitness"
                type="number"
                name="targetFitness"
                label="目标适应度"
                placeholder="默认0.95"
                class="form-field"
              />
            </template>
          </van-cell-group>
          <div class="generate-btn">
            <van-button round block type="primary" native-type="submit" :loading="generating" class="submit-btn">
              开始生成
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showSemesterPicker" position="bottom" round class="picker-popup">
      <div class="picker-header">
        <span class="picker-cancel" @click="showSemesterPicker = false">取消</span>
        <span class="picker-title">选择学期</span>
        <span class="picker-confirm" @click="confirmSemester">确定</span>
      </div>
      <van-picker
        :columns="semesterColumns"
        v-model="selectedSemester"
        @confirm="onSemesterConfirm"
      />
    </van-popup>

    <van-popup v-model:show="showAlgorithmPicker" position="bottom" round class="picker-popup">
      <div class="picker-header">
        <span class="picker-cancel" @click="showAlgorithmPicker = false">取消</span>
        <span class="picker-title">选择算法</span>
        <span class="picker-confirm" @click="confirmAlgorithm">确定</span>
      </div>
      <van-picker
        :columns="algorithmColumns"
        v-model="selectedAlgorithm"
        @confirm="onAlgorithmConfirm"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import dayjs from 'dayjs'
import { getTimetableList, generateTimetable } from '@/api/timetable'

const router = useRouter()

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const list = ref([])
const page = ref(1)
const pageSize = 10

const showGenerate = ref(false)
const generating = ref(false)
const showAdvanced = ref(false)
const showSemesterPicker = ref(false)
const showAlgorithmPicker = ref(false)
const selectedSemester = ref([])
const selectedAlgorithm = ref([])

const generateForm = ref({
  semester: '',
  algorithmType: 'GREEDY',
  daysPerWeek: 5,
  slotsPerDay: 10,
  maxGenerations: null,
  targetFitness: null
})

const algorithmColumns = [
  { text: '贪心算法', value: 'GREEDY' },
  { text: '遗传算法', value: 'GENETIC' }
]

const algorithmName = computed(() => {
  const item = algorithmColumns.find(a => a.value === generateForm.value.algorithmType)
  return item ? item.text : ''
})

const semesterColumns = computed(() => {
  const year = dayjs().year()
  const columns = []
  for (let i = 0; i < 3; i++) {
    columns.push({ text: `${year - i}-${year - i + 1}学年第一学期`, value: `${year - i}-1` })
    columns.push({ text: `${year - i}-${year - i + 1}学年第二学期`, value: `${year - i}-2` })
  }
  return columns
})

const getStatusType = (status) => {
  const map = { 'DRAFT': 'warning', 'PUBLISHED': 'success', 'ARCHIVED': 'default' }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = { 'DRAFT': '草稿', 'PUBLISHED': '已发布', 'ARCHIVED': '已归档' }
  return map[status] || status
}

const formatTime = (time) => time ? dayjs(time).format('MM-DD HH:mm') : '-'

const onLoad = async () => {
  try {
    const res = await getTimetableList({ current: page.value, size: pageSize })
    list.value.push(...res.data.records)
    if (list.value.length >= res.data.total) {
      finished.value = true
    } else {
      page.value++
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  page.value = 1
  list.value = []
  finished.value = false
  await onLoad()
  refreshing.value = false
}

const confirmSemester = () => {
  if (selectedSemester.value.length > 0) {
    generateForm.value.semester = selectedSemester.value[0].value
  }
  showSemesterPicker.value = false
}

const onSemesterConfirm = ({ selectedOptions }) => {
  generateForm.value.semester = selectedOptions[0].value
  showSemesterPicker.value = false
}

const confirmAlgorithm = () => {
  if (selectedAlgorithm.value.length > 0) {
    generateForm.value.algorithmType = selectedAlgorithm.value[0].value
  }
  showAlgorithmPicker.value = false
}

const onAlgorithmConfirm = ({ selectedOptions }) => {
  generateForm.value.algorithmType = selectedOptions[0].value
  showAlgorithmPicker.value = false
}

const handleGenerate = async () => {
  if (!generateForm.value.semester) {
    showToast('请选择学期')
    return
  }
  
  generating.value = true
  try {
    const res = await generateTimetable(generateForm.value)
    showToast('课表生成成功')
    showGenerate.value = false
    onRefresh()
    router.push(`/timetable/detail/${res.data.id}`)
  } catch (e) {
    showToast('生成失败')
  } finally {
    generating.value = false
  }
}

const goDetail = (id) => {
  router.push(`/timetable/detail/${id}`)
}

onMounted(() => {
  const year = dayjs().year()
  const semester = dayjs().month() < 7 ? `${year - 1}-2` : `${year}-1`
  generateForm.value.semester = semester
})
</script>

<style scoped>
.timetable-page {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.custom-nav {
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

.add-btn {
  border: none;
  background: var(--primary-gradient);
  padding: 0 16px;
}

.pull-refresh {
  min-height: calc(100vh - 100px);
}

.timetable-list {
  padding: 4px 0;
}

.timetable-item {
  cursor: pointer;
  animation: slideUp 0.3s ease-out backwards;
}

.timetable-item:nth-child(1) { animation-delay: 0.05s; }
.timetable-item:nth-child(2) { animation-delay: 0.1s; }
.timetable-item:nth-child(3) { animation-delay: 0.15s; }
.timetable-item:nth-child(4) { animation-delay: 0.2s; }

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

.timetable-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.status-tag {
  flex-shrink: 0;
}

.stat-grid {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: var(--spacing-md) 0;
}

.stat-item {
  padding: var(--spacing-sm) 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.generate-popup {
  border-radius: var(--radius-xl) var(--radius-xl) 0 0 !important;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-light);
}

.popup-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.close-icon {
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
}

.popup-content {
  padding: var(--spacing-lg) 0 var(--spacing-xl);
  height: calc(100% - 60px);
  overflow-y: auto;
}

.form-group {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.form-field {
  background: var(--bg-primary);
}

.generate-btn {
  margin-top: var(--spacing-xl);
  padding: 0 var(--spacing-lg);
}

.submit-btn {
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  background: var(--primary-gradient);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.picker-popup {
  border-radius: var(--radius-xl) var(--radius-xl) 0 0 !important;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-light);
}

.picker-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.picker-cancel,
.picker-confirm {
  font-size: 15px;
  color: var(--primary-color);
  font-weight: 500;
  cursor: pointer;
}

.picker-cancel {
  color: var(--text-secondary);
}

@media (min-width: 768px) {
  .timetable-page {
    max-width: 800px;
    margin: 0 auto;
  }
  
  .generate-popup {
    max-width: 500px;
    left: 50% !important;
    transform: translateX(-50%) !important;
    border-radius: var(--radius-xl) !important;
    margin-bottom: 20px;
  }
}
</style>
