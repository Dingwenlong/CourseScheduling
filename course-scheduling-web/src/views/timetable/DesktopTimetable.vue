<template>
  <div class="desktop-timetable-page">
    <div class="page-header">
      <h2 class="page-title">课表管理</h2>
      <div class="header-actions">
        <n-button @click="onRefresh">
          <template #icon>
            <n-icon :component="RefreshOutline" />
          </template>
          刷新
        </n-button>
        <n-button type="primary" @click="showGenerate = true">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新建
        </n-button>
      </div>
    </div>

    <div class="table-container">
      <div class="table-header">
        <div class="table-title">课表列表</div>
        <div class="table-filters search-wrapper">
          <n-select v-model:value="filterSemester" :options="semesterOptions" placeholder="选择学期" @update:value="onFilterChange" style="width: 200px;" />
          <n-select v-model:value="filterStatus" :options="statusOptions" placeholder="选择状态" @update:value="onFilterChange" style="width: 150px;" />
        </div>
      </div>

      <n-spin v-if="loading" class="loading-container" />
      <div v-else class="table-wrapper">
        <n-data-table
          :columns="columns"
          :data="list"
          :pagination="false"
          :bordered="false"
          :single-line="false"
          size="medium"
        />
        <n-empty v-if="list.length === 0" description="暂无数据" class="empty-container" />
      </div>
    </div>

    <n-modal v-model:show="showGenerate" preset="card" title="生成新课表" style="width: 500px;">
      <n-form :model="generateForm" label-placement="left" label-width="100px">
        <n-form-item label="学期" path="semester">
          <n-input v-model:value="generateForm.semester" readonly placeholder="请选择学期" @click="showSemesterPicker = true" style="cursor: pointer;" />
        </n-form-item>
        <n-form-item label="算法类型" path="algorithmType">
          <n-input v-model:value="algorithmName" readonly placeholder="请选择算法" @click="showAlgorithmPicker = true" style="cursor: pointer;" />
        </n-form-item>
        <n-form-item label="高级选项" path="showAdvanced">
          <n-switch v-model:value="showAdvanced" />
        </n-form-item>
        <template v-if="showAdvanced">
          <n-form-item label="最大迭代" path="maxGenerations">
            <n-input-number v-model:value="generateForm.maxGenerations" placeholder="默认500" style="width: 100%;" />
          </n-form-item>
          <n-form-item label="目标适应度" path="targetFitness">
            <n-input-number v-model:value="generateForm.targetFitness" :step="0.01" placeholder="默认0.95" style="width: 100%;" />
          </n-form-item>
        </template>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showGenerate = false">取消</n-button>
          <n-button type="primary" :loading="generating" @click="handleGenerate">
            开始生成
          </n-button>
        </div>
      </template>
    </n-modal>

    <n-modal v-model:show="showSemesterPicker" preset="card" title="选择学期" style="width: 400px;">
      <n-select v-model:value="selectedSemester" :options="semesterColumns" placeholder="请选择学期" style="width: 100%;" />
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showSemesterPicker = false">取消</n-button>
          <n-button type="primary" @click="confirmSemester">确定</n-button>
        </div>
      </template>
    </n-modal>

    <n-modal v-model:show="showAlgorithmPicker" preset="card" title="选择算法" style="width: 400px;">
      <n-select v-model:value="selectedAlgorithm" :options="algorithmColumns" placeholder="请选择算法" style="width: 100%;" />
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showAlgorithmPicker = false">取消</n-button>
          <n-button type="primary" @click="confirmAlgorithm">确定</n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import dayjs from 'dayjs'
import { getTimetableList, generateTimetable, getAlgorithms } from '@/api/timetable'
import { AddOutline, RefreshOutline } from '@vicons/ionicons5'

const router = useRouter()
const message = useMessage()

const loading = ref(false)
const list = ref([])
const filterSemester = ref('')
const filterStatus = ref('')
const selectedSemester = ref('')
const selectedAlgorithm = ref('')

const showGenerate = ref(false)
const showAdvanced = ref(false)
const showSemesterPicker = ref(false)
const showAlgorithmPicker = ref(false)
const generating = ref(false)

const generateForm = ref({
  semester: '',
  algorithmType: 'GREEDY',
  daysPerWeek: 5,
  slotsPerDay: 10,
  maxGenerations: null,
  targetFitness: null
})

const algorithmColumns = ref([])

const columns = [
  {
    title: '课表名称',
    key: 'name',
    width: 200,
    render: (row) => h('span', { style: { color: 'var(--primary-color)', fontWeight: '500' } }, row.name)
  },
  {
    title: '学期',
    key: 'semester',
    width: 200
  },
  {
    title: '任务数',
    key: 'taskCount',
    width: 100
  },
  {
    title: '已排课',
    key: 'scheduledCount',
    width: 100,
    render: (row) => h('span', { style: { color: 'var(--text-success)' } }, row.scheduledCount)
  },
  {
    title: '冲突',
    key: 'conflictCount',
    width: 100,
    render: (row) => h('span', { style: { color: 'var(--text-danger)' } }, row.conflictCount)
  },
  {
    title: '利用率',
    key: 'utilizationRate',
    width: 120,
    render: (row) => (row.utilizationRate ? row.utilizationRate.toFixed(1) : 0) + '%'
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row) => h('n-tag', { type: getStatusType(row.status), size: 'small' }, getStatusText(row.status))
  },
  {
    title: '生成时间',
    key: 'generateTime',
    width: 180,
    render: (row) => formatTime(row.generateTime)
  },
  {
    title: '操作',
    key: 'actions',
    width: 120,
    fixed: 'right',
    render: (row) => h('n-button', { 
      size: 'small', 
      type: 'primary', 
      onClick: () => goDetail(row.id) 
    }, { default: () => '查看' })
  }
]

const algorithmName = computed(() => {
  const item = algorithmColumns.value.find(a => a.value === generateForm.value.algorithmType)
  return item ? item.label : ''
})

const loadAlgorithms = async () => {
  try {
    const res = await getAlgorithms()
    algorithmColumns.value = res.data.map(alg => ({
      label: alg.name,
      value: alg.code
    }))
  } catch (e) {
    console.error('加载算法列表失败', e)
    algorithmColumns.value = [
      { label: '贪心算法', value: 'GREEDY' },
      { label: '遗传算法', value: 'GENETIC' }
    ]
  }
}

const semesterColumns = computed(() => {
  const year = dayjs().year()
  const columns = []
  for (let i = 0; i < 5; i++) {
    columns.push({ label: `${year - i}-${year - i + 1}学年第一学期`, value: `${year - i}-1` })
    columns.push({ label: `${year - i}-${year - i + 1}学年第二学期`, value: `${year - i}-2` })
  }
  return columns
})

const semesterOptions = computed(() => {
  return [
    { label: '全部学期', value: '' },
    ...semesterColumns.value
  ]
})

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已归档', value: 'ARCHIVED' }
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
  if (selectedSemester.value) {
    generateForm.value.semester = selectedSemester.value
  }
  showSemesterPicker.value = false
}

const confirmAlgorithm = () => {
  if (selectedAlgorithm.value) {
    generateForm.value.algorithmType = selectedAlgorithm.value
  }
  showAlgorithmPicker.value = false
}

const handleGenerate = async () => {
  if (!generateForm.value.semester) {
    message.error('请选择学期')
    return
  }

  generating.value = true
  try {
    const res = await generateTimetable(generateForm.value)
    message.success('课表生成成功')
    showGenerate.value = false
    loadData()
    router.push(`/timetable/detail/${res.data.id}`)
  } catch (e) {
    message.error('生成失败')
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

  loadAlgorithms()
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
  margin-bottom: 0;
}

.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--spacing-3xl) 0;
}

@media (min-width: 1440px) {
  .page-title {
    font-size: 28px;
  }

  .table-title {
    font-size: 18px;
  }
}

@media (min-width: 1920px) {
  .page-header {
    margin-bottom: var(--spacing-2xl);
  }

  .table-header {
    padding: var(--spacing-xl) var(--spacing-2xl);
  }
}

@media (min-width: 2560px) {
  .page-title {
    font-size: 32px;
  }

  .table-title {
    font-size: 20px;
  }
}

@media (max-width: 1439px) {
}

@media (max-width: 1199px) {
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

@media (max-width: 767px) {
  .desktop-timetable-page {
    display: none;
  }
}
</style>
