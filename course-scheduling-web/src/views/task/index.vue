<template>
  <div class="page page-with-tabbar">
    <van-nav-bar title="教学任务">
      <template #right>
        <van-icon name="plus" size="20" @click="showAdd = true" />
      </template>
    </van-nav-bar>

    <van-search
      v-model="searchText"
      placeholder="搜索课程名称"
      @search="onSearch"
    />

    <van-dropdown-menu>
      <van-dropdown-item v-model="filterSemester" :options="semesterOptions" @change="onFilterChange" />
      <van-dropdown-item v-model="filterStatus" :options="statusOptions" @change="onFilterChange" />
    </van-dropdown-menu>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-swipe-cell v-for="item in list" :key="item.id">
          <van-cell :title="item.courseName" :label="getTaskLabel(item)" is-link @click="showTaskDetail(item)">
            <template #value>
              <van-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</van-tag>
            </template>
          </van-cell>
          <template #right>
            <van-button square type="primary" text="编辑" class="swipe-btn" @click="editTask(item)" />
            <van-button square type="danger" text="删除" class="swipe-btn" @click="deleteTaskConfirm(item)" />
          </template>
        </van-swipe-cell>
      </van-list>
    </van-pull-refresh>

    <van-popup v-model:show="showAdd" position="bottom" round style="height: 80%;">
      <div class="task-popup">
        <div class="page-title">{{ editingTask ? '编辑任务' : '新增任务' }}</div>
        <van-form @submit="handleSubmit">
          <van-cell-group inset>
            <van-field
              v-model="form.semester"
              is-link
              readonly
              name="semester"
              label="学期"
              placeholder="请选择学期"
              required
              @click="showSemesterPicker = true"
            />
            <van-field
              v-model="form.courseId"
              name="courseId"
              label="课程ID"
              placeholder="请输入课程ID"
              required
              type="number"
            />
            <van-field
              v-model="form.teacherId"
              name="teacherId"
              label="教师ID"
              placeholder="请输入教师ID"
              required
              type="number"
            />
            <van-field
              v-model="form.classId"
              name="classId"
              label="班级ID"
              placeholder="请输入班级ID"
              required
              type="number"
            />
            <van-field
              v-model="form.studentCount"
              name="studentCount"
              label="学生人数"
              placeholder="请输入学生人数"
              type="number"
            />
            <van-field
              v-model="form.weeklyHours"
              name="weeklyHours"
              label="周学时"
              placeholder="请输入周学时"
              type="number"
            />
            <van-field
              v-model="form.priorityLevel"
              name="priorityLevel"
              label="优先级"
              placeholder="1-10，数字越小优先级越高"
              type="number"
            />
          </van-cell-group>
          <div class="form-btn">
            <van-button round block type="primary" native-type="submit" :loading="submitting">
              {{ editingTask ? '保存' : '创建' }}
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showSemesterPicker" position="bottom" round>
      <van-picker
        :columns="semesterOptions.slice(1)"
        @confirm="onSemesterConfirm"
        @cancel="showSemesterPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showDetail" position="bottom" round style="height: 50%;">
      <div class="task-popup" v-if="currentTask">
        <div class="page-title">{{ currentTask.courseName || '任务详情' }}</div>
        <van-cell-group inset>
          <van-cell title="学期" :value="currentTask.semester" />
          <van-cell title="课程ID" :value="currentTask.courseId" />
          <van-cell title="教师ID" :value="currentTask.teacherId" />
          <van-cell title="班级ID" :value="currentTask.classId" />
          <van-cell title="学生人数" :value="currentTask.studentCount" />
          <van-cell title="周学时" :value="currentTask.weeklyHours" />
          <van-cell title="优先级" :value="currentTask.priorityLevel" />
          <van-cell title="状态" :value="getStatusText(currentTask.status)" />
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

const getTaskLabel = (item) => {
  return `周学时: ${item.weeklyHours || '-'} | 学生: ${item.studentCount || '-'} | 优先级: ${item.priorityLevel || '-'}`
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
.swipe-btn {
  height: 100%;
}

.task-popup {
  padding: 20px 16px;
}

.form-btn {
  margin-top: 24px;
  padding: 0 8px;
}
</style>
