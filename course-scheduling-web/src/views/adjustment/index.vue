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
          <n-form-item label="调课原因">
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
        </n-form>
        <div class="form-actions">
          <n-button type="primary" :loading="swapping" @click="handleSwap">
            <template #icon>
              <n-icon>
                <SyncOutline />
              </n-icon>
            </template>
            交换课程
          </n-button>
        </div>
      </div>
    </div>

    <div v-if="checkResult" class="card result-card">
      <div class="card-header">
        <h3 class="card-title">检测结果</h3>
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
        <n-button type="primary" :loading="executing" size="large" block @click="executeAdjustment">
          确认调课
        </n-button>
      </div>
    </div>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import { checkAdjustment, executeAdjustment as doExecuteAdjustment, swapCourses } from '@/api/adjustment'
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
  NListItem
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
const executing = ref(false)
const swapping = ref(false)
const checkResult = ref(null)
const formRef = ref(null)
const swapFormRef = ref(null)

const form = reactive({
  timetableId: null,
  detailId: null,
  newDayOfWeek: null,
  newSlotNo: null,
  newClassroomId: null,
  reason: ''
})

const swapForm = reactive({
  timetableId: null,
  detailId1: null,
  detailId2: null
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
    const res = await checkAdjustment(form)
    checkResult.value = res.data
    if (res.data.success) {
      message.success('检测通过')
    }
  } catch (e) {
    message.error(e.message || '检测失败')
  } finally {
    loading.value = false
  }
}

const executeAdjustment = async () => {
  dialog.warning({
    title: '确认调课',
    content: '确定执行调课操作吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      executing.value = true
      try {
        await doExecuteAdjustment(form)
        message.success('调课成功')
        checkResult.value = null
        onReset()
      } catch (e) {
        message.error(e.message || '调课失败')
      } finally {
        executing.value = false
      }
    }
  })
}

const handleSwap = async () => {
  try {
    await swapFormRef.value?.validate()
  } catch (e) {
    return
  }

  dialog.warning({
    title: '确认交换',
    content: '确定交换这两门课程吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      swapping.value = true
      try {
        await swapCourses(swapForm.timetableId, swapForm.detailId1, swapForm.detailId2)
        message.success('交换成功')
        swapForm.timetableId = null
        swapForm.detailId1 = null
        swapForm.detailId2 = null
      } catch (e) {
        message.error(e.message || '交换失败')
      } finally {
        swapping.value = false
      }
    }
  })
}

const onReset = () => {
  form.timetableId = null
  form.detailId = null
  form.newDayOfWeek = null
  form.newSlotNo = null
  form.newClassroomId = null
  form.reason = ''
  checkResult.value = null
  message.success('已重置')
}

onMounted(() => {
  if (route.query.timetableId) {
    form.timetableId = Number(route.query.timetableId)
  }
  if (route.query.detailId) {
    form.detailId = Number(route.query.detailId)
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
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--border-color);
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

.result-card {
  grid-column: 1 / -1;
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
