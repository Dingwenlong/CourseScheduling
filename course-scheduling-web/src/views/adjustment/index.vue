<template>
  <PageContainer with-tabbar>
    <div class="desktop-adjustment-page">
      <PageHeader title="调课管理">
        <template #actions>
          <n-button quaternary @click="onReset">
            <template #icon>
              <n-icon>
                <RefreshOutline />
              </n-icon>
            </template>
            重置
          </n-button>
        </template>
      </PageHeader>

      <div class="adjustment-grid">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">申请调课</h3>
            <n-tag v-if="pendingApplication" type="warning">调整中</n-tag>
          </div>
          <div class="form-hint">
            默认优先带出当前学期最新课表，课程和教室支持直接搜索选择，不需要手动记忆编号。
          </div>
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100px">
            <n-form-item label="当前课表" path="timetableId">
              <n-input
                :value="currentTimetableText"
                placeholder="系统会自动带入当前课表"
                readonly
              />
              <div class="field-hint">
                调课会默认作用于当前打开的课表；如需切换课表，请先从课表页进入对应版本。
              </div>
            </n-form-item>
            <n-form-item label="要调整的课程" path="detailId">
              <n-select
                v-model:value="form.detailId"
                filterable
                clearable
                :options="detailOptions"
                :loading="detailOptionsLoading"
                :disabled="!form.timetableId"
                placeholder="从当前课表中选择要调整的课程"
              />
              <div v-if="detailOptions.length > 0" class="field-hint">
                已加载 {{ detailOptions.length }} 门课程，列表已按星期和节次排序。
              </div>
            </n-form-item>
            <n-form-item label="调整到哪一天" path="newDayOfWeek">
              <n-select
                v-model:value="form.newDayOfWeek"
                :options="weekdayOptions"
                placeholder="选择新的上课日期"
              />
            </n-form-item>
            <n-form-item label="调整到哪个时段" path="newSlotNo">
              <n-select
                v-model:value="form.newSlotNo"
                :options="slotOptions"
                placeholder="选择新的上课时段"
              />
            </n-form-item>
            <n-form-item label="新教室">
              <n-select
                v-model:value="form.newClassroomId"
                filterable
                remote
                clearable
                :options="classroomOptions"
                :loading="classroomLookupLoading"
                placeholder="可选，搜索教室名称或编号"
                @search="searchClassroomOptions"
                @focus="loadClassroomOptions"
              />
            </n-form-item>
            <n-form-item label="调课原因" path="reason">
              <n-input v-model:value="form.reason" type="textarea" placeholder="请输入调课原因" :rows="3" />
            </n-form-item>
          </n-form>
          <div class="form-actions">
            <n-button type="primary" :loading="loading" @click="handleSubmit">
              <template #icon>
                <n-icon>
                  <SearchOutline />
                </n-icon>
              </template>
              检测冲突
            </n-button>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h3 class="card-title">课程交换</h3>
            <n-tag v-if="pendingSwapApplication" type="warning">调整中</n-tag>
          </div>
          <n-form ref="swapFormRef" :model="swapForm" :rules="swapRules" label-placement="left" label-width="100px">
            <n-form-item label="当前课表" path="timetableId">
              <n-input
                :value="currentTimetableText"
                placeholder="系统会自动带入当前课表"
                readonly
              />
            </n-form-item>
            <n-form-item label="第一门课" path="detailId1">
              <n-select
                v-model:value="swapForm.detailId1"
                filterable
                clearable
                :options="swapDetailOptions1"
                :loading="detailOptionsLoading"
                :disabled="!swapForm.timetableId"
                placeholder="选择要交换的第一门课程"
                @update:value="handleSwapDetailChange('detailId1', $event)"
              />
            </n-form-item>
            <n-form-item label="第二门课" path="detailId2">
              <n-select
                v-model:value="swapForm.detailId2"
                filterable
                clearable
                :options="swapDetailOptions2"
                :loading="detailOptionsLoading"
                :disabled="!swapForm.timetableId"
                placeholder="选择要交换的第二门课程"
                @update:value="handleSwapDetailChange('detailId2', $event)"
              />
              <div v-if="swapForm.detailId1 || swapForm.detailId2" class="field-hint">
                课程交换必须选择两门不同课程，另一侧列表会自动排除当前已选项。
              </div>
            </n-form-item>
            <n-form-item label="交换原因" path="reason">
              <n-input v-model:value="swapForm.reason" type="textarea" placeholder="请输入交换原因" :rows="3" />
            </n-form-item>
          </n-form>
          <div class="form-actions">
            <n-button type="primary" :loading="checkingSwap" @click="handleSwapCheck">
              <template #icon>
                <n-icon>
                  <SyncOutline />
                </n-icon>
              </template>
              检测冲突
            </n-button>
          </div>
        </div>
      </div>

      <div v-if="checkResult" class="card result-card">
        <div class="card-header">
          <h3 class="card-title">调课检测结果</h3>
          <n-tag :type="checkResult.success ? 'success' : 'error'" size="large">
            {{ checkResult.success ? '通过' : '存在冲突' }}
          </n-tag>
        </div>
        <n-descriptions :column="1" bordered class="mt-8">
          <n-descriptions-item label="消息">{{ checkResult.message }}</n-descriptions-item>
        </n-descriptions>
        <div v-if="checkResult.conflicts && checkResult.conflicts.length > 0" class="conflicts-section">
          <div class="section-label">
            <n-icon size="16">
              <WarningOutline />
            </n-icon>
            冲突详情
          </div>
          <n-list>
            <n-list-item v-for="(conflict, index) in checkResult.conflicts" :key="index">
              <template #prefix>
                <n-icon color="#ee0a24">
                  <CloseOutline />
                </n-icon>
              </template>
              {{ conflict }}
            </n-list-item>
          </n-list>
        </div>
        <div v-if="checkResult.success" class="confirm-section">
          <div v-if="pendingApplication" class="application-summary">
            <div class="summary-line">
              <span class="summary-label">申请单号</span>
              <span class="summary-value">{{ pendingApplication.applicationNo }}</span>
            </div>
            <div class="summary-line">
              <span class="summary-label">申请状态</span>
              <n-tag type="warning" size="small">待处理</n-tag>
            </div>
          </div>
          <n-space vertical :size="12" style="width: 100%">
            <n-button type="default" :loading="applying" size="large" block @click="submitApplication">
              {{ pendingApplication ? '更新申请' : '提交申请' }}
            </n-button>
            <n-button
              v-if="pendingApplication"
              type="primary"
              :loading="executing"
              size="large"
              block
              @click="executeAdjustment"
            >
              执行调课
            </n-button>
          </n-space>
        </div>
      </div>

      <div v-if="swapCheckResult" class="card result-card">
        <div class="card-header">
          <h3 class="card-title">交换检测结果</h3>
          <n-tag :type="swapCheckResult.success ? 'success' : 'error'" size="large">
            {{ swapCheckResult.success ? '通过' : '存在冲突' }}
          </n-tag>
        </div>
        <n-descriptions :column="1" bordered class="mt-8">
          <n-descriptions-item label="消息">{{ swapCheckResult.message }}</n-descriptions-item>
        </n-descriptions>
        <div v-if="swapCheckResult.conflicts && swapCheckResult.conflicts.length > 0" class="conflicts-section">
          <div class="section-label">
            <n-icon size="16">
              <WarningOutline />
            </n-icon>
            冲突详情
          </div>
          <n-list>
            <n-list-item v-for="(conflict, index) in swapCheckResult.conflicts" :key="index">
              <template #prefix>
                <n-icon color="#ee0a24">
                  <CloseOutline />
                </n-icon>
              </template>
              {{ conflict }}
            </n-list-item>
          </n-list>
        </div>
        <div v-if="swapCheckResult.success" class="confirm-section">
          <div v-if="pendingSwapApplication" class="application-summary">
            <div class="summary-line">
              <span class="summary-label">申请单号</span>
              <span class="summary-value">{{ pendingSwapApplication.applicationNo }}</span>
            </div>
            <div class="summary-line">
              <span class="summary-label">申请状态</span>
              <n-tag type="warning" size="small">待处理</n-tag>
            </div>
          </div>
          <n-space vertical :size="12" style="width: 100%">
            <n-button type="default" :loading="applyingSwap" size="large" block @click="submitSwapApplication">
              {{ pendingSwapApplication ? '更新申请' : '提交申请' }}
            </n-button>
            <n-button
              v-if="pendingSwapApplication"
              type="primary"
              :loading="executingSwap"
              size="large"
              block
              @click="executeSwapCourse"
            >
              执行交换
            </n-button>
          </n-space>
        </div>
      </div>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import {
  checkAdjustment,
  applyAdjustment as doApplyAdjustment,
  getPendingAdjustment,
  cancelAdjustment,
  executeAdjustment as doExecuteAdjustment,
  checkSwapAdjustment,
  applySwapAdjustment as doApplySwapAdjustment,
  getPendingSwapAdjustment,
  cancelSwapAdjustment,
  executeSwapAdjustment as doExecuteSwapAdjustment
} from '@/api/adjustment'
import { getLatestTimetable, getTimetableDetails } from '@/api/timetable'
import { searchClassrooms } from '@/api/lookup'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { useLayoutStore } from '@/stores/layout'
import { getCurrentSemester } from '@/utils/semester'
import {
  NButton,
  NIcon,
  NForm,
  NFormItem,
  NInputNumber,
  NInput,
  NSelect,
  NTag,
  NDescriptions,
  NDescriptionsItem,
  NList,
  NListItem,
  NSpace
} from 'naive-ui'
import {
  RefreshOutline,
  SearchOutline,
  SyncOutline,
  WarningOutline,
  CloseOutline
} from '@vicons/ionicons5'

const route = useRoute()
const message = useMessage()
const dialog = useDialog()
const layoutStore = useLayoutStore()

const loading = ref(false)
const applying = ref(false)
const executing = ref(false)
const checkingSwap = ref(false)
const applyingSwap = ref(false)
const executingSwap = ref(false)
const checkResult = ref(null)
const swapCheckResult = ref(null)
const pendingApplication = ref(null)
const pendingSwapApplication = ref(null)
const formRef = ref(null)
const swapFormRef = ref(null)
const detailOptions = ref([])
const detailOptionsLoading = ref(false)
const classroomOptions = ref([])
const classroomLookupLoading = ref(false)
const weekdayLabels = ['一', '二', '三', '四', '五', '六', '日']
const currentTimetableMeta = ref(null)
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

const form = reactive({
  applicationId: null,
  timetableId: null,
  detailId: null,
  newDayOfWeek: null,
  newSlotNo: null,
  newClassroomId: null,
  reason: ''
})

const swapForm = reactive({
  applicationId: null,
  timetableId: null,
  detailId1: null,
  detailId2: null,
  reason: ''
})

const currentTimetableText = computed(() => {
  if (currentTimetableMeta.value?.name) {
    return `${currentTimetableMeta.value.name}（${currentTimetableMeta.value.semester} · 第${currentTimetableMeta.value.version}版）`
  }
  const currentId = form.timetableId || swapForm.timetableId
  return currentId ? `课表 #${currentId}` : ''
})

const weekdayOptions = computed(() => weekdayLabels.slice(0, 5).map((label, index) => ({
  label: `周${label}`,
  value: index + 1
})))

const slotOptions = computed(() => Object.entries(slotMeta).map(([value, meta]) => ({
  value: Number(value),
  label: `${meta.label} · ${meta.time}`
})))

const slotLabel = (slotNo) => slotMeta[slotNo]?.label || `第${slotNo}节`
const slotTimeRange = (slotNo) => slotMeta[slotNo]?.time || ''
const courseTimeText = (detail) => {
  const weekday = weekdayLabels[(detail.dayOfWeek || 1) - 1] || detail.dayOfWeek
  const slot = slotLabel(detail.slotNo)
  const time = slotTimeRange(detail.slotNo)
  return time ? `周${weekday} ${slot} · ${time}` : `周${weekday} ${slot}`
}

const rules = {
  timetableId: {
    required: true,
    message: '当前课表不能为空',
    trigger: 'blur'
  },
  detailId: {
    required: true,
    message: '请选择要调整的课程',
    trigger: 'change'
  },
  newDayOfWeek: {
    required: true,
    message: '请选择新的上课日期',
    trigger: 'change'
  },
  newSlotNo: {
    required: true,
    message: '请选择新的上课时段',
    trigger: 'change'
  },
  reason: {
    required: true,
    message: '请输入调课原因',
    trigger: 'blur'
  }
}

const swapRules = {
  timetableId: {
    required: true,
    message: '当前课表不能为空',
    trigger: 'blur'
  },
  detailId1: {
    required: true,
    message: '请选择第一门课程',
    trigger: 'change'
  },
  detailId2: {
    required: true,
    message: '请选择第二门课程',
    trigger: 'change'
  },
  reason: {
    required: true,
    message: '请输入交换原因',
    trigger: 'blur'
  }
}

const resetForm = ({ keepTimetableId = false } = {}) => {
  const timetableId = keepTimetableId ? form.timetableId : null
  form.applicationId = null
  form.timetableId = timetableId
  form.detailId = null
  form.newDayOfWeek = null
  form.newSlotNo = null
  form.newClassroomId = null
  form.reason = ''
}

const resetSwapForm = ({ keepTimetableId = false } = {}) => {
  const timetableId = keepTimetableId ? swapForm.timetableId : null
  swapForm.applicationId = null
  swapForm.timetableId = timetableId
  swapForm.detailId1 = null
  swapForm.detailId2 = null
  swapForm.reason = ''
}

const swapDetailOptions1 = computed(() =>
  detailOptions.value.filter(option => option.value !== Number(swapForm.detailId2 || 0))
)

const swapDetailOptions2 = computed(() =>
  detailOptions.value.filter(option => option.value !== Number(swapForm.detailId1 || 0))
)

const buildDetailLabel = (detail) => {
  const segments = [
    courseTimeText(detail),
    detail.courseName || `课程 #${detail.id}`
  ]
  if (detail.className) {
    segments.push(detail.className)
  }
  if (detail.classroomName) {
    segments.push(detail.classroomName)
  }
  return segments.join(' · ')
}

const ensureOption = (targetRef, value, label) => {
  if (!value) {
    return
  }
  const normalizedValue = Number(value)
  if (!targetRef.value.some(option => option.value === normalizedValue)) {
    targetRef.value = [{ value: normalizedValue, label: label || String(value) }, ...targetRef.value]
  }
}

const loadClassroomOptions = async (keyword = '') => {
  classroomLookupLoading.value = true
  try {
    const res = await searchClassrooms({ keyword, limit: 20 })
    classroomOptions.value = res.data || []
  } catch (e) {
    classroomOptions.value = []
  } finally {
    classroomLookupLoading.value = false
  }
}

const searchClassroomOptions = async (keyword) => {
  await loadClassroomOptions(keyword)
}

const loadDetailOptions = async (timetableId) => {
  if (!timetableId) {
    detailOptions.value = []
    return
  }

  detailOptionsLoading.value = true
  try {
    const res = await getTimetableDetails(timetableId)
    detailOptions.value = [...(res.data || [])]
      .sort((a, b) => {
        const dayDiff = (a.dayOfWeek || 0) - (b.dayOfWeek || 0)
        if (dayDiff !== 0) {
          return dayDiff
        }
        const slotDiff = (a.slotNo || 0) - (b.slotNo || 0)
        if (slotDiff !== 0) {
          return slotDiff
        }
        return String(a.courseName || '').localeCompare(String(b.courseName || ''), 'zh-Hans-CN')
      })
      .map(detail => ({
      value: detail.id,
      label: buildDetailLabel(detail)
    }))
    ensureOption(detailOptions, form.detailId, `课程明细 #${form.detailId}`)
    ensureOption(detailOptions, swapForm.detailId1, `课程明细 #${swapForm.detailId1}`)
    ensureOption(detailOptions, swapForm.detailId2, `课程明细 #${swapForm.detailId2}`)
  } catch (e) {
    detailOptions.value = []
  } finally {
    detailOptionsLoading.value = false
  }
}

const handleSwapDetailChange = (field, value) => {
  swapForm[field] = value
  if (swapForm.detailId1 && swapForm.detailId2 && swapForm.detailId1 === swapForm.detailId2) {
    if (field === 'detailId1') {
      swapForm.detailId2 = null
    } else {
      swapForm.detailId1 = null
    }
    message.warning('课程交换需要选择两门不同课程')
  }
}

const handleTimetableChange = async (value) => {
  const normalizedValue = value ? Number(value) : null
  const previousValue = form.timetableId || swapForm.timetableId || null
  form.timetableId = normalizedValue
  swapForm.timetableId = normalizedValue

  if (normalizedValue === previousValue) {
    return
  }

  pendingApplication.value = null
  pendingSwapApplication.value = null
  checkResult.value = null
  swapCheckResult.value = null
  form.applicationId = null
  swapForm.applicationId = null
  form.detailId = null
  swapForm.detailId1 = null
  swapForm.detailId2 = null

  await loadDetailOptions(normalizedValue)
}

const loadPendingApplication = async ({ hydrate = true } = {}) => {
  if (!form.timetableId || !form.detailId) {
    pendingApplication.value = null
    form.applicationId = null
    return null
  }

  try {
    const res = await getPendingAdjustment({
      timetableId: form.timetableId,
      detailId: form.detailId
    })
    pendingApplication.value = res.data || null
    if (!pendingApplication.value) {
      form.applicationId = null
      return null
    }

    form.applicationId = pendingApplication.value.id
    if (hydrate) {
      form.newDayOfWeek = pendingApplication.value.newDay
      form.newSlotNo = pendingApplication.value.newSlot
      form.newClassroomId = pendingApplication.value.newClassroom
      ensureOption(classroomOptions, pendingApplication.value.newClassroom, `教室 #${pendingApplication.value.newClassroom}`)
      form.reason = pendingApplication.value.reason || ''
      checkResult.value = {
        success: true,
        message: '已存在待处理申请，可更新后重新提交或直接执行',
        conflicts: []
      }
    }
    return pendingApplication.value
  } catch (e) {
    console.error(e)
    return null
  }
}

const loadPendingSwapApplication = async ({ hydrate = true } = {}) => {
  if (!swapForm.timetableId || !swapForm.detailId1 || !swapForm.detailId2) {
    pendingSwapApplication.value = null
    swapForm.applicationId = null
    return null
  }

  try {
    const res = await getPendingSwapAdjustment({
      timetableId: swapForm.timetableId,
      detailId1: swapForm.detailId1,
      detailId2: swapForm.detailId2
    })
    pendingSwapApplication.value = res.data || null
    if (!pendingSwapApplication.value) {
      swapForm.applicationId = null
      return null
    }

    swapForm.applicationId = pendingSwapApplication.value.id
    if (hydrate) {
      swapForm.reason = pendingSwapApplication.value.reason || ''
      swapCheckResult.value = {
        success: true,
        message: '已存在待处理交换申请，可更新后重新提交或直接执行',
        conflicts: []
      }
    }
    return pendingSwapApplication.value
  } catch (e) {
    console.error(e)
    return null
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }

  loading.value = true
  try {
    const pending = await loadPendingApplication({ hydrate: false })
    const res = await checkAdjustment(form)
    checkResult.value = res.data
    if (res.data.success) {
      message.success(pending ? '检测通过，已存在待处理申请' : '检测通过')
    }
  } catch (e) {
    message.error(e.message || '检测失败')
  } finally {
    loading.value = false
  }
}

const submitApplication = async () => {
  if (!checkResult.value?.success) {
    message.warning('请先完成冲突检测')
    return
  }

  applying.value = true
  const hadExisting = Boolean(pendingApplication.value?.id)
  try {
    const res = await doApplyAdjustment(form)
    pendingApplication.value = res.data || null
    form.applicationId = pendingApplication.value?.id || null
    message.success(
      hadExisting
        ? `申请已更新：${pendingApplication.value?.applicationNo || ''}`
        : `申请已提交：${pendingApplication.value?.applicationNo || ''}`
    )
  } catch (e) {
    message.error(e.message || '提交申请失败')
  } finally {
    applying.value = false
  }
}

const executeAdjustment = async () => {
  if (!pendingApplication.value?.id) {
    message.warning('请先提交调课申请')
    return
  }

  dialog.warning({
    title: '确认调课',
    content: '确定执行调课操作吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      executing.value = true
      try {
        await doExecuteAdjustment({
          ...form,
          applicationId: pendingApplication.value.id
        })
        message.success('调课成功')
        pendingApplication.value = null
        checkResult.value = null
        resetForm({ keepTimetableId: true })
      } catch (e) {
        message.error(e.message || '调课失败')
      } finally {
        executing.value = false
      }
    }
  })
}

const handleSwapCheck = async () => {
  try {
    await swapFormRef.value?.validate()
  } catch (e) {
    return
  }

  checkingSwap.value = true
  try {
    const pending = await loadPendingSwapApplication({ hydrate: false })
    const res = await checkSwapAdjustment(swapForm)
    swapCheckResult.value = res.data
    if (res.data.success) {
      message.success(pending ? '检测通过，已存在待处理交换申请' : '检测通过')
    }
  } catch (e) {
    message.error(e.message || '交换检测失败')
  } finally {
    checkingSwap.value = false
  }
}

const submitSwapApplication = async () => {
  if (!swapCheckResult.value?.success) {
    message.warning('请先完成交换冲突检测')
    return
  }

  applyingSwap.value = true
  const hadExisting = Boolean(pendingSwapApplication.value?.id)
  try {
    const res = await doApplySwapAdjustment(swapForm)
    pendingSwapApplication.value = res.data || null
    swapForm.applicationId = pendingSwapApplication.value?.id || null
    message.success(
      hadExisting
        ? `交换申请已更新：${pendingSwapApplication.value?.applicationNo || ''}`
        : `交换申请已提交：${pendingSwapApplication.value?.applicationNo || ''}`
    )
  } catch (e) {
    message.error(e.message || '提交交换申请失败')
  } finally {
    applyingSwap.value = false
  }
}

const executeSwapCourse = async () => {
  if (!pendingSwapApplication.value?.id) {
    message.warning('请先提交交换申请')
    return
  }

  dialog.warning({
    title: '确认交换',
    content: '确定执行课程交换吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      executingSwap.value = true
      try {
        await doExecuteSwapAdjustment({
          ...swapForm,
          applicationId: pendingSwapApplication.value.id
        })
        message.success('课程交换成功')
        pendingSwapApplication.value = null
        swapCheckResult.value = null
        resetSwapForm({ keepTimetableId: true })
      } catch (e) {
        message.error(e.message || '课程交换失败')
      } finally {
        executingSwap.value = false
      }
    }
  })
}

const resetState = () => {
  const currentTimetableId = form.timetableId || swapForm.timetableId || null
  pendingApplication.value = null
  pendingSwapApplication.value = null
  checkResult.value = null
  swapCheckResult.value = null
  resetForm({ keepTimetableId: Boolean(currentTimetableId) })
  resetSwapForm({ keepTimetableId: Boolean(currentTimetableId) })
}

const onReset = () => {
  const hasPending = Boolean(pendingApplication.value?.id || pendingSwapApplication.value?.id)

  const doReset = async () => {
    const tasks = []
    if (pendingApplication.value?.id) {
      tasks.push(cancelAdjustment(pendingApplication.value.id))
    }
    if (pendingSwapApplication.value?.id) {
      tasks.push(cancelSwapAdjustment(pendingSwapApplication.value.id))
    }
    if (tasks.length > 0) {
      await Promise.all(tasks)
    }
    resetState()
  }

  if (hasPending) {
    dialog.warning({
      title: '取消申请',
      content: '当前存在待处理调课申请，重置会同时取消这些申请，是否继续？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: async () => {
        try {
          await doReset()
          message.success('已重置并取消申请')
        } catch (e) {
          message.error(e.message || '取消申请失败')
        }
      }
    })
    return
  }

  resetState()
  message.success('已重置')
}

onMounted(async () => {
  if (route.query.timetableId) {
    const timetableId = Number(route.query.timetableId)
    form.timetableId = timetableId
    swapForm.timetableId = timetableId
  } else {
    try {
      const res = await getLatestTimetable(getCurrentSemester())
      if (res.data?.id) {
        form.timetableId = res.data.id
        swapForm.timetableId = res.data.id
        currentTimetableMeta.value = res.data
      }
    } catch (e) {
      console.error(e)
    }
  }
  if (!currentTimetableMeta.value && form.timetableId) {
    currentTimetableMeta.value = {
      id: form.timetableId,
      name: `课表 #${form.timetableId}`,
      semester: '当前学期',
      version: '-'
    }
  }
  if (route.query.detailId) {
    form.detailId = Number(route.query.detailId)
  }
  if (route.query.detailId1) {
    swapForm.detailId1 = Number(route.query.detailId1)
  }
  if (route.query.detailId2) {
    swapForm.detailId2 = Number(route.query.detailId2)
  }

  if (form.timetableId) {
    await loadDetailOptions(form.timetableId)
  }

  if (form.timetableId && form.detailId) {
    await loadPendingApplication()
  }
  if (swapForm.timetableId && swapForm.detailId1 && swapForm.detailId2) {
    await loadPendingSwapApplication()
  }
})

onUnmounted(() => {
  layoutStore.clearHeaderAction()
})
</script>

<style scoped>
.desktop-adjustment-page {
  position: relative;
  animation: fadeIn 0.3s ease-out;
  padding-bottom: var(--spacing-xl);
}

.desktop-adjustment-page::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 240px;
  background:
    radial-gradient(circle at top left, rgba(184, 102, 89, 0.16), transparent 40%),
    radial-gradient(circle at top right, rgba(130, 151, 118, 0.14), transparent 32%);
  opacity: 0.86;
  pointer-events: none;
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

.adjustment-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
}

.card {
  position: relative;
  border: 1px solid rgba(145, 120, 91, 0.18);
  border-radius: var(--radius-xl);
  background: var(--fabric-surface), rgba(255, 250, 243, 0.78);
  backdrop-filter: blur(14px);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  border: 1px solid rgba(255, 255, 255, 0.32);
  pointer-events: none;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px dashed var(--border-soft);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.form-hint {
  margin: var(--spacing-lg) var(--spacing-xl) 0;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  background:
    linear-gradient(135deg, rgba(184, 102, 89, 0.12), rgba(184, 102, 89, 0.04)),
    rgba(255, 250, 243, 0.72);
  border: 1px solid rgba(184, 102, 89, 0.14);
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.field-hint {
  margin-top: var(--spacing-xs);
  font-size: 12px;
  color: var(--text-muted);
  line-height: 1.5;
}

.card-title {
  font-size: 19px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: 0.01em;
}

.form-actions {
  padding: var(--spacing-lg) var(--spacing-xl);
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
  border-top: 1px dashed rgba(145, 120, 91, 0.12);
}

.result-card + .result-card {
  margin-top: var(--spacing-xl);
}

.conflicts-section {
  padding: var(--spacing-md) var(--spacing-xl);
}

.section-label {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-danger);
  margin-bottom: var(--spacing-md);
}

.confirm-section {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-top: 1px dashed rgba(145, 120, 91, 0.12);
}

.application-summary {
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.72), rgba(255, 255, 255, 0.2)),
    rgba(255, 250, 243, 0.62);
  border: 1px solid rgba(145, 120, 91, 0.12);
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
}

.summary-line + .summary-line {
  margin-top: var(--spacing-sm);
}

.summary-label {
  color: var(--text-secondary);
  font-size: 13px;
}

.summary-value {
  color: var(--text-primary);
  font-weight: 600;
}

.mt-8 {
  margin-top: 8px;
}

:deep(.desktop-adjustment-page .n-form) {
  padding: var(--spacing-lg) var(--spacing-xl) 0;
}

:deep(.desktop-adjustment-page .n-form-item) {
  margin-bottom: var(--spacing-lg);
}

:deep(.desktop-adjustment-page .n-base-selection),
:deep(.desktop-adjustment-page .n-input),
:deep(.desktop-adjustment-page .n-input-number),
:deep(.desktop-adjustment-page .n-button) {
  border-radius: var(--radius-md);
}

:deep(.desktop-adjustment-page .n-list-item) {
  padding: var(--spacing-sm) 0;
}

@media (min-width: 1440px) {
  .page-title {
    font-size: 28px;
  }

  .card-title {
    font-size: 20px;
  }
}

@media (min-width: 1920px) {
  .page-header {
    margin-bottom: var(--spacing-2xl);
  }

  .card-header {
    padding: var(--spacing-xl) var(--spacing-2xl);
  }

  .form-actions,
  .confirm-section {
    padding: var(--spacing-xl) var(--spacing-2xl);
  }

  .conflicts-section {
    padding: var(--spacing-md) var(--spacing-2xl);
  }
}

@media (min-width: 2560px) {
  .page-title {
    font-size: 32px;
  }

  .card-title {
    font-size: 22px;
  }
}

@media (max-width: 1199px) {
  .adjustment-grid {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  :deep(.desktop-adjustment-page .n-form) {
    padding: var(--spacing-md) var(--spacing-lg) 0;
  }
}
</style>
