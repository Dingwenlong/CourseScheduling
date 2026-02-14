<template>
  <div class="page page-with-tabbar task-page">
    <van-nav-bar title="教学任务" class="custom-nav">
      <template #right>
        <van-button icon="plus" size="small" type="primary" @click="showAdd = true" class="add-btn">新增</van-button>
      </template>
    </van-nav-bar>

    <div class="search-wrapper">
      <van-search
        v-model="searchText"
        placeholder="搜索课程名称"
        @search="onSearch"
        shape="round"
        class="search-input"
      />
    </div>

    <van-dropdown-menu class="dropdown-menu">
      <van-dropdown-item v-model="filterSemester" :options="semesterOptions" @change="onFilterChange" />
      <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="onFilterChange" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="pull-refresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
        class="task-list"
      >
        <van-swipe-cell v-for="(item, index) in list" :key="item.id" class="swipe-cell">
          <div class="card task-card" @click="showTaskDetail(item)">
            <div class="flex-between">
              <div class="flex-1">
                <div class="task-title">{{ item.courseName }}</div>
                <div class="task-info mt-8">
                  <span class="info-item"><van-icon name="clock-o" /> 周学时: {{ item.weeklyHours || '-' }}</span>
                  <span class="info-item"><van-icon name="friends-o" /> 学生: {{ item.studentCount || '-' }}</span>
                  <span class="info-item"><van-icon name="flag-o" /> 优先级: {{ item.priorityLevel || '-' }}</span>
                </div>
              </div>
              <van-tag :type="getStatusType(item.status)" class="status-tag">
                {{ getStatusText(item.status) }}
              </van-tag>
            </div>
          </div>
          <template #right>
            <van-button square type="primary" text="编辑" class="swipe-btn edit-btn" @click="editTask(item)" />
            <van-button square type="danger" text="删除" class="swipe-btn delete-btn" @click="deleteTaskConfirm(item)" />
          </template>
        </van-swipe-cell>
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showAdd" position="bottom" round style="height: 85%;" class="task-popup">
      <div class="popup-header">
        <div class="popup-title">{{ editingTask ? '编辑任务' : '新增任务' }}</div>
        <van-icon name="cross" size="20" @click="showAdd = false" class="close-icon" />
      </div>
      <div class="popup-content">
        <van-form @submit="handleSubmit">
          <van-cell-group inset class="form-group">
            <van-field
              v-model="form.semester"
              is-link
              readonly
              name="semester"
              label="学期"
              placeholder="请选择学期"
              required
              @click="showSemesterPicker = true"
              class="form-field"
            />
            <van-field
              v-model="form.courseId"
              name="courseId"
              label="课程ID"
              placeholder="请输入课程ID"
              required
              type="number"
              class="form-field"
            />
            <van-field
              v-model="form.teacherId"
              name="teacherId"
              label="教师ID"
              placeholder="请输入教师ID"
              required
              type="number"
              class="form-field"
            />
            <van-field
              v-model="form.classId"
              name="classId"
              label="班级ID"
              placeholder="请输入班级ID"
              required
              type="number"
              class="form-field"
            />
            <van-field
              v-model="form.studentCount"
              name="studentCount"
              label="学生人数"
              placeholder="请输入学生人数"
              type="number"
              class="form-field"
            />
            <van-field
              v-model="form.weeklyHours"
              name="weeklyHours"
              label="周学时"
              placeholder="请输入周学时"
              type="number"
              class="form-field"
            />
            <van-field
              v-model="form.priorityLevel"
              name="priorityLevel"
              label="优先级"
              placeholder="1-10，数字越小优先级越高"
              type="number"
              class="form-field"
            />
          </van-cell-group>
          <div class="form-btn">
            <van-button round block type="primary" native-type="submit" :loading="submitting" class="submit-btn">
              {{ editingTask ? '保存' : '创建' }}
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
        :columns="semesterOptions.slice(1)"
        v-model="selectedSemester"
        @confirm="onSemesterConfirm"
      />
    </van-popup>

    <van-popup v-model:show="showDetail" position="bottom" round style="height: 55%;" class="detail-popup">
      <div class="popup-header">
        <div class="popup-title">{{ currentTask?.courseName || '任务详情' }}</div>
        <van-icon name="cross" size="20" @click="showDetail = false" class="close-icon" />
      </div>
      <div class="popup-content" v-if="currentTask">
        <van-cell-group inset class="detail-group">
          <van-cell title="学期" :value="currentTask.semester" />
          <van-cell title="课程ID" :value="currentTask.courseId" />
          <van-cell title="教师ID" :value="currentTask.teacherId" />
          <van-cell title="班级ID" :value="currentTask.classId" />
          <van-cell title="学生人数" :value="currentTask.studentCount" />
          <van-cell title="周学时" :value="currentTask.weeklyHours" />
          <van-cell title="优先级" :value="currentTask.priorityLevel" />
          <van-cell title="状态">
            <template #value>
              <van-tag :type="getStatusType(currentTask.status)">{{ getStatusText(currentTask.status) }}</van-tag>
            </template>
          </van-cell>
        </van-cell-group>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import dayjs from 'dayjs'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/task'

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const list = ref([])
const page = ref(1)
const pageSize = 10

const searchText = ref('')
const filterSemester = ref('')
const filterStatus = ref('')
const selectedSemester = ref([])

const showAdd = ref(false)
const showDetail = ref(false)
const showSemesterPicker = ref(false)
const submitting = ref(false)
const editingTask = ref(null)
const currentTask = ref(null)

const form = ref({
  semester: '',
  courseId: '',
  teacherId: '',
  classId: '',
  studentCount: '',
  weeklyHours: '',
  priorityLevel: ''
})

const semesterOptions = ref([
  { text: '全部学期', value: '' }
])

const statusOptions = [
  { text: '全部状态', value: '' },
  { text: '待排课', value: 'PENDING' },
  { text: '已排课', value: 'SCHEDULED' },
  { text: '调整中', value: 'ADJUSTING' },
  { text: '已完成', value: 'COMPLETED' }
]

const getStatusType = (status) => {
  const map = { 'PENDING': 'warning', 'SCHEDULED': 'success', 'ADJUSTING': 'primary', 'COMPLETED': 'default' }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = { 'PENDING': '待排课', 'SCHEDULED': '已排课', 'ADJUSTING': '调整中', 'COMPLETED': '已完成' }
  return map[status] || status
}

const onLoad = async () => {
  try {
    const res = await getTaskList({
      current: page.value,
      size: pageSize,
      semester: filterSemester.value,
      status: filterStatus.value
    })
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

const onSearch = () => {
  page.value = 1
  list.value = []
  finished.value = false
  onLoad()
}

const onFilterChange = () => {
  onRefresh()
}

const confirmSemester = () => {
  if (selectedSemester.value.length > 0) {
    form.value.semester = selectedSemester.value[0].value
  }
  showSemesterPicker.value = false
}

const onSemesterConfirm = ({ selectedOptions }) => {
  form.value.semester = selectedOptions[0].value
  showSemesterPicker.value = false
}

const showTaskDetail = (item) => {
  currentTask.value = item
  showDetail.value = true
}

const editTask = (item) => {
  editingTask.value = item
  form.value = { ...item }
  showAdd.value = true
}

const deleteTaskConfirm = async (item) => {
  await showConfirmDialog({ title: '确认删除', message: '删除后数据将无法恢复，确定删除吗？' })
  await deleteTask(item.id)
  showToast('删除成功')
  onRefresh()
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (editingTask.value) {
      await updateTask({ ...form.value, id: editingTask.value.id })
      showToast('更新成功')
    } else {
      await createTask(form.value)
      showToast('创建成功')
    }
    showAdd.value = false
    editingTask.value = null
    form.value = { semester: '', courseId: '', teacherId: '', classId: '', studentCount: '', weeklyHours: '', priorityLevel: '' }
    onRefresh()
  } catch (e) {
    showToast('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const year = dayjs().year()
  for (let i = 0; i < 3; i++) {
    semesterOptions.value.push({ text: `${year - i}-${year - i + 1}学年第一学期`, value: `${year - i}-1` })
    semesterOptions.value.push({ text: `${year - i}-${year - i + 1}学年第二学期`, value: `${year - i}-2` })
  }
})
</script>

<style scoped>
.task-page {
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

.search-wrapper {
  padding: var(--spacing-md);
  padding-bottom: 0;
}

.search-input {
  border-radius: var(--radius-lg);
}

.dropdown-menu {
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

.pull-refresh {
  min-height: calc(100vh - 200px);
}

.task-list {
  padding: 4px 0;
}

.swipe-cell {
  margin: var(--spacing-md);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.task-card {
  margin: 0;
  cursor: pointer;
  animation: slideUp 0.3s ease-out backwards;
}

.task-card:nth-child(1) { animation-delay: 0.05s; }
.task-card:nth-child(2) { animation-delay: 0.1s; }
.task-card:nth-child(3) { animation-delay: 0.15s; }

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

.task-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.task-info {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: var(--text-muted);
}

.status-tag {
  flex-shrink: 0;
}

.swipe-btn {
  height: 100%;
  border: none;
}

.edit-btn {
  background: var(--primary-color);
}

.delete-btn {
  background: var(--danger-color);
}

.task-popup,
.detail-popup {
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

.form-group,
.detail-group {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.form-field {
  background: var(--bg-primary);
}

.form-btn {
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
  .task-page {
    max-width: 800px;
    margin: 0 auto;
  }
  
  .task-popup,
  .detail-popup {
    max-width: 500px;
    left: 50% !important;
    transform: translateX(-50%) !important;
    border-radius: var(--radius-xl) !important;
    margin-bottom: 20px;
  }
}
</style>
