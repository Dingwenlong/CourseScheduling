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
          <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100px">
            <n-form-item label="课表ID" path="timetableId">
              <n-input-number v-model:value="form.timetableId" placeholder="请输入课表ID" style="width: 100%" :min="1" />
            </n-form-item>
            <n-form-item label="课程明细ID" path="detailId">
              <n-input-number v-model:value="form.detailId" placeholder="请输入课程明细ID" style="width: 100%" :min="1" />
            </n-form-item>
            <n-form-item label="新星期" path="newDayOfWeek">
              <n-input-number v-model:value="form.newDayOfWeek" placeholder="1-5" style="width: 100%" :min="1" :max="5" />
            </n-form-item>
            <n-form-item label="新节次" path="newSlotNo">
              <n-input-number v-model:value="form.newSlotNo" placeholder="1-10" style="width: 100%" :min="1" :max="10" />
            </n-form-item>
            <n-form-item label="新教室ID">
              <n-input-number v-model:value="form.newClassroomId" placeholder="可选" style="width: 100%" :min="1" />
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
            <n-form-item label="课表ID" path="timetableId">
              <n-input-number v-model:value="swapForm.timetableId" placeholder="请输入课表ID" style="width: 100%" :min="1" />
            </n-form-item>
            <n-form-item label="课程1 ID" path="detailId1">
              <n-input-number v-model:value="swapForm.detailId1" placeholder="请输入课程明细ID" style="width: 100%" :min="1" />
            </n-form-item>
            <n-form-item label="课程2 ID" path="detailId2">
              <n-input-number v-model:value="swapForm.detailId2" placeholder="请输入课程明细ID" style="width: 100%" :min="1" />
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
import { ref, reactive, onMounted, onUnmounted } from 'vue'
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
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { useLayoutStore } from '@/stores/layout'
import {
  NButton,
  NIcon,
  NForm,
  NFormItem,
  NInputNumber,
  NInput,
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

const rules = {
  timetableId: {
    required: true,
    message: '请输入课表ID',
    trigger: 'blur'
  },
  detailId: {
    required: true,
    message: '请输入课程明细ID',
    trigger: 'blur'
  },
  newDayOfWeek: {
    required: true,
    message: '请输入新星期',
    trigger: 'blur'
  },
  newSlotNo: {
    required: true,
    message: '请输入新节次',
    trigger: 'blur'
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
    message: '请输入课表ID',
    trigger: 'blur'
  },
  detailId1: {
    required: true,
    message: '请输入课程1 ID',
    trigger: 'blur'
  },
  detailId2: {
    required: true,
    message: '请输入课程2 ID',
    trigger: 'blur'
  },
  reason: {
    required: true,
    message: '请输入交换原因',
    trigger: 'blur'
  }
}

const resetForm = () => {
  form.applicationId = null
  form.timetableId = null
  form.detailId = null
  form.newDayOfWeek = null
  form.newSlotNo = null
  form.newClassroomId = null
  form.reason = ''
}

const resetSwapForm = () => {
  swapForm.applicationId = null
  swapForm.timetableId = null
  swapForm.detailId1 = null
  swapForm.detailId2 = null
  swapForm.reason = ''
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
        resetForm()
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
        resetSwapForm()
      } catch (e) {
        message.error(e.message || '课程交换失败')
      } finally {
        executingSwap.value = false
      }
    }
  })
}

const resetState = () => {
  pendingApplication.value = null
  pendingSwapApplication.value = null
  checkResult.value = null
  swapCheckResult.value = null
  resetForm()
  resetSwapForm()
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

.adjustment-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
}

.card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
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

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.form-actions {
  padding: var(--spacing-lg) var(--spacing-xl);
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-md);
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
}

.application-summary {
  padding: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  border-radius: 18px;
  background: rgba(255, 250, 243, 0.54);
  border: 1px solid var(--border-light);
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
}
</style>
