<template>
  <div class="page page-with-tabbar">
    <van-nav-bar title="调课管理" />

    <div class="card">
      <div class="page-title">申请调课</div>
      <van-form @submit="handleSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.timetableId"
            name="timetableId"
            label="课表ID"
            placeholder="请输入课表ID"
            required
            type="number"
          />
          <van-field
            v-model="form.detailId"
            name="detailId"
            label="课程明细ID"
            placeholder="请输入课程明细ID"
            required
            type="number"
          />
          <van-field
            v-model="form.newDayOfWeek"
            name="newDayOfWeek"
            label="新星期"
            placeholder="1-5"
            required
            type="number"
          />
          <van-field
            v-model="form.newSlotNo"
            name="newSlotNo"
            label="新节次"
            placeholder="1-10"
            required
            type="number"
          />
          <van-field
            v-model="form.newClassroomId"
            name="newClassroomId"
            label="新教室ID"
            placeholder="可选"
            type="number"
          />
          <van-field
            v-model="form.reason"
            name="reason"
            label="调课原因"
            placeholder="请输入调课原因"
            type="textarea"
            rows="2"
          />
        </van-cell-group>
        <div class="form-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            检测冲突
          </van-button>
        </div>
      </van-form>
    </div>

    <div v-if="checkResult" class="card">
      <div class="page-title">检测结果</div>
      <van-cell-group inset>
        <van-cell title="检测结果" :value="checkResult.success ? '通过' : '存在冲突'" :value-class="checkResult.success ? 'text-success' : 'text-danger'" />
        <van-cell title="消息" :value="checkResult.message" />
      </van-cell-group>
      <div v-if="checkResult.conflicts && checkResult.conflicts.length > 0" class="mt-16">
        <div class="text-danger" style="font-size: 14px; margin-bottom: 8px;">冲突详情：</div>
        <van-cell-group inset>
          <van-cell v-for="(conflict, index) in checkResult.conflicts" :key="index" :title="conflict" />
        </van-cell-group>
      </div>
      <div class="form-btn mt-16">
        <van-button round block type="primary" :loading="executing" @click="executeAdjustment">
          确认调课
        </van-button>
      </div>
    </div>

    <div class="card">
      <div class="page-title">课程交换</div>
      <van-cell-group inset>
        <van-field
          v-model="swapForm.timetableId"
          name="timetableId"
          label="课表ID"
          placeholder="请输入课表ID"
          required
          type="number"
        />
        <van-field
          v-model="swapForm.detailId1"
          name="detailId1"
          label="课程1 ID"
          placeholder="请输入课程明细ID"
          required
          type="number"
        />
        <van-field
          v-model="swapForm.detailId2"
          name="detailId2"
          label="课程2 ID"
          placeholder="请输入课程明细ID"
          required
          type="number"
        />
      </van-cell-group>
      <div class="form-btn">
        <van-button round block type="primary" :loading="swapping" @click="handleSwap">
          交换课程
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { checkAdjustment, executeAdjustment as doExecuteAdjustment, swapCourses } from '@/api/adjustment'

const route = useRoute()

const loading = ref(false)
const executing = ref(false)
const swapping = ref(false)
const checkResult = ref(null)

const form = ref({
  timetableId: '',
  detailId: '',
  newDayOfWeek: '',
  newSlotNo: '',
  newClassroomId: '',
  reason: ''
})

const swapForm = ref({
  timetableId: '',
  detailId1: '',
  detailId2: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    const res = await checkAdjustment(form.value)
    checkResult.value = res.data
    if (res.data.success) {
      showToast('检测通过')
    }
  } catch (e) {
    showToast('检测失败')
  } finally {
    loading.value = false
  }
}

const executeAdjustment = async () => {
  await showConfirmDialog({ title: '确认调课', message: '确定执行调课操作吗？' })
  executing.value = true
  try {
    await doExecuteAdjustment(form.value)
    showToast('调课成功')
    checkResult.value = null
    form.value = { timetableId: '', detailId: '', newDayOfWeek: '', newSlotNo: '', newClassroomId: '', reason: '' }
  } catch (e) {
    showToast('调课失败')
  } finally {
    executing.value = false
  }
}

const handleSwap = async () => {
  await showConfirmDialog({ title: '确认交换', message: '确定交换这两门课程吗？' })
  swapping.value = true
  try {
    await swapCourses(swapForm.value.timetableId, swapForm.value.detailId1, swapForm.value.detailId2)
    showToast('交换成功')
    swapForm.value = { timetableId: '', detailId1: '', detailId2: '' }
  } catch (e) {
    showToast('交换失败')
  } finally {
    swapping.value = false
  }
}

onMounted(() => {
  if (route.query.timetableId) {
    form.value.timetableId = route.query.timetableId
  }
  if (route.query.detailId) {
    form.value.detailId = route.query.detailId
  }
})
</script>

<style scoped>
.form-btn {
  margin-top: 16px;
  padding: 0 8px;
}
</style>
