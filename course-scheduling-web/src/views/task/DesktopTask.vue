<template>
  <div class="desktop-task-page">
    <div class="page-header">
      <h2 class="page-title">教学任务</h2>
      <div class="header-actions">
        <van-button icon="replay" @click="onRefresh">刷新</van-button>
        <van-button type="primary" icon="plus" @click="showAdd = true">新增任务</van-button>
      </div>
    </div>

    <div class="table-container">
      <div class="table-header">
        <div class="table-title">任务列表</div>
        <div class="table-filters">
          <van-search
            v-model="searchText"
            placeholder="搜索课程名称"
            @search="onSearch"
            style="width: 200px;"
          />
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
              <th>课程ID</th>
              <th>课程名称</th>
              <th>教师ID</th>
              <th>班级ID</th>
              <th>学生人数</th>
              <th>周学时</th>
              <th>优先级</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list" :key="item.id">
              <td>{{ item.courseId }}</td>
              <td class="name-cell">{{ item.courseName }}</td>
              <td>{{ item.teacherId }}</td>
              <td>{{ item.classId }}</td>
              <td>{{ item.studentCount || '-' }}</td>
              <td>{{ item.weeklyHours || '-' }}</td>
              <td>{{ item.priorityLevel || '-' }}</td>
              <td>
                <van-tag :type="getStatusType(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </van-tag>
              </td>
              <td>
                <div class="action-buttons">
                  <van-button size="small" type="primary" @click="editTask(item)">编辑</van-button>
                  <van-button size="small" type="danger" @click="deleteTaskConfirm(item)">删除</van-button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <van-empty v-if="list.length === 0" description="暂无数据" />
      </div>
    </div>

    <van-dialog v-model:show="showAdd" :title="editingTask ? '编辑任务' : '新增任务'" show-cancel-button @confirm="handleSubmit">
      <van-form>
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
      </van-form>
    </van-dialog>

    <van-popup v-model:show="showSemesterPicker" position="bottom" round>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import dayjs from 'dayjs'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/task'

const loading = ref(false)
const list = ref([])
const searchText = ref('')
const filterSemester = ref('')
const filterStatus = ref('')
const selectedSemester = ref([])

const showAdd = ref(false)
const showSemesterPicker = ref(false)
const editingTask = ref(null)

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

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTaskList({
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

const onSearch = () => {
  loadData()
}

const onFilterChange = () => {
  loadData()
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

const editTask = (item) => {
  editingTask.value = item
  form.value = { ...item }
  showAdd.value = true
}

const deleteTaskConfirm = async (item) => {
  await showConfirmDialog({ title: '确认删除', message: '删除后数据将无法恢复，确定删除吗？' })
  await deleteTask(item.id)
  showToast('删除成功')
  loadData()
}

const handleSubmit = async () => {
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
    loadData()
  } catch (e) {
    showToast('操作失败')
  }
}

onMounted(() => {
  const year = dayjs().year()
  for (let i = 0; i < 3; i++) {
    semesterOptions.value.push({ text: `${year - i}-${year - i + 1}学年第一学期`, value: `${year - i}-1` })
    semesterOptions.value.push({ text: `${year - i}-${year - i + 1}学年第二学期`, value: `${year - i}-2` })
  }
  loadData()
})
</script>

<style scoped>
.desktop-task-page {
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
  min-width: 1000px;
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
    min-width: 900px;
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
  .desktop-task-page {
    display: none;
  }
}
</style>
