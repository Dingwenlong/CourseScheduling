<template>
  <div class="desktop-timetable-page">
    <div class="page-header">
      <h2 class="page-title">课表管理</h2>
      <div class="header-actions">
        <van-button icon="replay" @click="onRefresh">刷新</van-button>
        <van-button type="primary" icon="plus" @click="showGenerate = true">新建课表</van-button>
      </div>
    </div>

    <div class="table-container">
      <div class="table-header">
        <div class="table-title">课表列表</div>
        <div class="table-filters">
          <van-dropdown-menu>
            <van-dropdown-item v-model="filterSemester" :options="semesterOptions" @change="onFilterChange" />
            <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="onFilterChange" />
          </van-dropdown-menu>
        </div>
      </div>

      <van-loading v-if="loading" class="loading-container" />
      <div v-else class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>课表名称</th>
              <th>学期</th>
              <th>任务数</th>
              <th>已排课</th>
              <th>冲突</th>
              <th>利用率</th>
              <th>状态</th>
              <th>生成时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id">
              <td class="name-cell">{{ item.name }}</td>
              <td>{{ item.semester }}</td>
              <td>{{ item.taskCount }}</td>
              <td class="text-success">{{ item.scheduledCount }}</td>
              <td class="text-danger">{{ item.conflictCount }}</td>
              <td>{{ item.utilizationRate ? item.utilizationRate.toFixed(1) : 0 }}%</td>
              <td>
                <van-tag :type="getStatusType(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </van-tag>
              </td>
              <td>{{ formatTime(item.generateTime) }}</td>
              <td>
                <div class="action-buttons">
                  <van-button size="small" type="primary" @click="goDetail(item.id)">查看</van-button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <van-empty v-if="list.length === 0" description="暂无数据" />
      </div>
    </div>

    <van-dialog v-model:show="showGenerate" title="生成新课表" show-cancel-button @confirm="handleGenerate">
      <van-form @submit.prevent="handleGenerate">
        <van-cell-group inset class="form-group">
          <van-field
            v-model="generateForm.semester"
            is-link
            readonly
            name="semester"
            label="学期"
            placeholder="请选择学期"
            @click="showSemesterPicker = true"
          />
          <van-field
            v-model="algorithmName"
            is-link
            readonly
            name="algorithm"
            label="算法类型"
            placeholder="请选择算法"
            @click="showAlgorithmPicker = true"
          />
          <van-field name="switch" label="高级选项">
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
            />
            <van-field
              v-model="generateForm.targetFitness"
              type="number"
              name="targetFitness"
              label="目标适应度"
              placeholder="默认0.95"
            />
          </template>
        </van-cell-group>
      </van-form>
    </van-dialog>

    <van-popup v-model:show="showSemesterPicker" position="bottom" round>
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

    <van-popup v-model:show="showAlgorithmPicker" position="bottom" round>
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
import { showToast } from 'vant'
import dayjs from 'dayjs'
import { getTimetableList, generateTimetable } from '@/api/timetable'

const router = useRouter()

const loading = ref(false)
const list = ref([])
const filterSemester = ref('')
const filterStatus = ref('')
const selectedSemester = ref([])
const selectedAlgorithm = ref([])

const showGenerate = ref(false)
const showAdvanced = ref(false)
const showSemesterPicker = ref(false)
const showAlgorithmPicker = ref(false)

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

const semesterOptions = computed(() => {
  return [
    { text: '全部学期', value: '' },
    ...semesterColumns.value
  ]
})

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '草稿', value: 'DRAFT' },
  { text: '已发布', value: 'PUBLISHED' },
  { text: '已归档', value: 'ARCHIVED' }
]

const getStatusType = (status) => {
  const map = { 'DRAFT': 'warning', 'PUBLISHED': 'success', 'ARCHIVED': 'default' }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = { 'DRAFT': '草稿', 'PUBLISHED': '已发布', 'ARCHIVED': '已归档' }
  return map[status] || status
}

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTimetableList({
      current: 1,
      size: 100,
      semester: filterSemester.value,
      status: filterStatus.value
    })
    list.value = res.data.records || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const onRefresh = () => {
  loadData()
}

const onFilterChange = () => {
  loadData()
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
  
  try {
    const res = await generateTimetable(generateForm.value)
    showToast('课表生成成功')
    showGenerate.value = false
    loadData()
    router.push(`/timetable/detail/${res.data.id}`)
  } catch (e) {
    showToast('生成失败')
  }
}

const goDetail = (id) => {
  router.push(`/timetable/detail/${id}`)
}

onMounted(() => {
  const year = dayjs().year()
  const semester = dayjs().month() < 7 ? `${year - 1}-2` : `${year}-1`
  generateForm.value.semester = semester
  loadData()
})
</script>

<style scoped>
.desktop-timetable-page {
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
  gap: var(--spacing-md);
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: var(--spacing-md);
}

.table-container {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-color);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.table-filters {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex-wrap: wrap;
}

.table-wrapper {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
  min-width: 900px;
}

.data-table:thead {
  background: var(--bg-secondary);
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
  border-bottom: 1px solid var(--border-light);
  color: var(--text-primary);
}

.data-table tbody tr:hover {
  background: var(--bg-secondary);
}

.name-cell {
  font-weight: 500;
  color: var(--primary-color);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-sm);
}

.form-group {
  border-radius: var(--radius-md);
  overflow: hidden;
  margin: var(--spacing-lg);
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

@media (max-width: 1400px) {
  .data-table {
    min-width: 800px;
  }
  
  .data-table th,
  .data-table td {
    padding: var(--spacing-sm) var(--spacing-md);
  }
}

@media (max-width: 1200px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .table-filters {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .desktop-timetable-page {
    display: none;
  }
}
</style>
