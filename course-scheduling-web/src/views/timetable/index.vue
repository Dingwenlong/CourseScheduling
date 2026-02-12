<template>
  <div class="page page-with-tabbar">
    <van-nav-bar title="课表管理">
      <template #right>
        <van-icon name="plus" size="20" @click="showGenerate = true" />
      </template>
    </van-nav-bar>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-for="item in list" :key="item.id" class="card" @click="goDetail(item.id)">
          <div class="flex-between">
            <div class="flex-1">
              <div class="timetable-title">{{ item.name }}</div>
              <div class="text-muted mt-8">{{ item.semester }}</div>
            </div>
            <van-tag :type="getStatusType(item.status)">
              {{ getStatusText(item.status) }}
            </van-tag>
          </div>
          <van-grid :column-num="4" :border="false" class="mt-16">
            <van-grid-item>
              <div class="stat-value">{{ item.taskCount }}</div>
              <div class="stat-label">任务数</div>
            </van-grid-item>
            <van-grid-item>
              <div class="stat-value text-success">{{ item.scheduledCount }}</div>
              <div class="stat-label">已排课</div>
            </van-grid-item>
            <van-grid-item>
              <div class="stat-value text-danger">{{ item.conflictCount }}</div>
              <div class="stat-label">冲突</div>
            </van-grid-item>
            <van-grid-item>
              <div class="stat-value">{{ item.utilizationRate ? item.utilizationRate.toFixed(1) : 0 }}%</div>
              <div class="stat-label">利用率</div>
            </van-grid-item>
          </van-grid>
          <div class="text-muted mt-8" style="font-size: 12px;">
            生成时间：{{ formatTime(item.generateTime) }}
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showGenerate" position="bottom" round style="height: 60%;">
      <div class="generate-popup">
        <div class="page-title">生成新课表</div>
        <van-form @submit="handleGenerate">
          <van-cell-group inset>
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
                <van-switch v-model="showAdvanced" />
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
          <div class="generate-btn">
            <van-button round block type="primary" native-type="submit" :loading="generating">
              开始生成
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showSemesterPicker" position="bottom" round>
      <van-picker
        :columns="semesterColumns"
        @confirm="onSemesterConfirm"
        @cancel="showSemesterPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showAlgorithmPicker" position="bottom" round>
      <van-picker
        :columns="algorithmColumns"
        @confirm="onAlgorithmConfirm"
        @cancel="showAlgorithmPicker = false"
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

const onSemesterConfirm = ({ selectedOptions }) => {
  generateForm.value.semester = selectedOptions[0].value
  showSemesterPicker.value = false
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
.timetable-title {
  font-size: 16px;
  font-weight: 500;
  color: #323233;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #323233;
}

.stat-label {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.generate-popup {
  padding: 20px 16px;
}

.generate-btn {
  margin-top: 24px;
  padding: 0 8px;
}
</style>
