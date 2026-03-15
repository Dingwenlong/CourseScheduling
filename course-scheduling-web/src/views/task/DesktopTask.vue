<template>
  <div class="desktop-task-page">
    <div class="page-header">
      <h2 class="page-title">教学任务</h2>
      <div class="header-actions">
        <n-button @click="onRefresh">
          <template #icon>
            <n-icon :component="RefreshOutline" />
          </template>
          刷新
        </n-button>
        <n-button type="primary" @click="showAdd = true">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新增
        </n-button>
      </div>
    </div>

    <div class="table-container">
      <div class="table-header">
        <div class="table-title">任务列表</div>
        <div class="table-filters search-wrapper">
          <n-select v-model:value="filterSemester" :options="semesterOptions" placeholder="选择学期" @update:value="onFilterChange" style="width: 180px;" />
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

    <n-modal v-model:show="showAdd" preset="card" :title="editingTask ? '编辑任务' : '新增任务'" style="width: 500px;">
      <n-form :model="form" label-placement="left" label-width="100px">
        <n-form-item label="学期" path="semester">
          <n-input v-model:value="form.semester" readonly placeholder="请选择学期" @click="showSemesterPicker = true" style="cursor: pointer;" />
        </n-form-item>
        <n-form-item label="课程ID" path="courseId">
          <n-input-number v-model:value="form.courseId" placeholder="请输入课程ID" style="width: 100%;" />
        </n-form-item>
        <n-form-item label="教师ID" path="teacherId">
          <n-input-number v-model:value="form.teacherId" placeholder="请输入教师ID" style="width: 100%;" />
        </n-form-item>
        <n-form-item label="班级ID" path="classId">
          <n-input-number v-model:value="form.classId" placeholder="请输入班级ID" style="width: 100%;" />
        </n-form-item>
        <n-form-item label="学生人数" path="studentCount">
          <n-input-number v-model:value="form.studentCount" placeholder="请输入学生人数" style="width: 100%;" />
        </n-form-item>
        <n-form-item label="周学时" path="weeklyHours">
          <n-input-number v-model:value="form.weeklyHours" placeholder="请输入周学时" style="width: 100%;" />
        </n-form-item>
        <n-form-item label="优先级" path="priorityLevel">
          <n-input-number v-model:value="form.priorityLevel" placeholder="1-10，数字越小优先级越高" :min="1" :max="10" style="width: 100%;" />
        </n-form-item>
        <n-form-item label="总周数" path="totalWeeks">
          <n-input-number v-model:value="form.totalWeeks" placeholder="请输入总周数" style="width: 100%;" />
        </n-form-item>
      </n-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 12px;">
          <n-button @click="showAdd = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editingTask ? '保存' : '创建' }}
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import dayjs from 'dayjs'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/task'
import { AddOutline, RefreshOutline, CreateOutline, TrashOutline } from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const list = ref([])
const filterSemester = ref('')
const filterStatus = ref('')
const selectedSemester = ref('')

const showAdd = ref(false)
const showSemesterPicker = ref(false)
const submitting = ref(false)
const editingTask = ref(null)

const form = ref({
  semester: '',
  courseId: null,
  teacherId: null,
  classId: null,
  studentCount: null,
  weeklyHours: null,
  priorityLevel: null,
  totalWeeks: null
})

const columns = [
  {
    title: '课程ID',
    key: 'courseId',
    width: 100
  },
  {
    title: '课程名称',
    key: 'courseName',
    width: 180,
    render: (row) => h('span', { style: { color: 'var(--primary-color)', fontWeight: '500' } }, row.courseName)
  },
  {
    title: '教师ID',
    key: 'teacherId',
    width: 100
  },
  {
    title: '班级ID',
    key: 'classId',
    width: 100
  },
  {
    title: '学生人数',
    key: 'studentCount',
    width: 100,
    render: (row) => row.studentCount || '-'
  },
  {
    title: '周学时',
    key: 'weeklyHours',
    width: 100,
    render: (row) => row.weeklyHours || '-'
  },
  {
    title: '优先级',
    key: 'priorityLevel',
    width: 100,
    render: (row) => row.priorityLevel || '-'
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row) => h('n-tag', { type: getStatusType(row.status), size: 'small' }, getStatusText(row.status))
  },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    fixed: 'right',
    render: (row) => h('div', { style: { display: 'flex', gap: '8px' } }, [
      h('n-button', { 
        size: 'small', 
        type: 'primary', 
        onClick: () => editTask(row) 
      }, { default: () => '编辑' }),
      h('n-button', { 
        size: 'small', 
        type: 'error', 
        onClick: () => deleteTaskConfirm(row) 
      }, { default: () => '删除' })
    ])
  }
]

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
  { label: '待排课', value: 'PENDING' },
  { label: '已排课', value: 'SCHEDULED' },
  { label: '调整中', value: 'ADJUSTING' },
  { label: '已完成', value: 'COMPLETED' }
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

const onFilterChange = () => {
  loadData()
}

const confirmSemester = () => {
  if (selectedSemester.value) {
    form.value.semester = selectedSemester.value
  }
  showSemesterPicker.value = false
}

const editTask = (item) => {
  editingTask.value = item
  form.value = {
    semester: item.semester,
    courseId: item.courseId,
    teacherId: item.teacherId,
    classId: item.classId,
    studentCount: item.studentCount,
    weeklyHours: item.weeklyHours,
    priorityLevel: item.priorityLevel,
    totalWeeks: item.totalWeeks
  }
  showAdd.value = true
}

const deleteTaskConfirm = async (item) => {
  await new Promise((resolve, reject) => {
    dialog.warning({
      title: '确认删除',
      content: '删除后数据将无法恢复，确定删除吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: resolve,
      onNegativeClick: reject
    })
  })
  await deleteTask(item.id)
  message.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const data = {
      ...form.value,
      courseId: form.value.courseId ? Number(form.value.courseId) : null,
      teacherId: form.value.teacherId ? Number(form.value.teacherId) : null,
      classId: form.value.classId ? Number(form.value.classId) : null,
      studentCount: form.value.studentCount ? Number(form.value.studentCount) : null,
      weeklyHours: form.value.weeklyHours ? Number(form.value.weeklyHours) : null,
      priorityLevel: form.value.priorityLevel ? Number(form.value.priorityLevel) : null,
      totalWeeks: form.value.totalWeeks ? Number(form.value.totalWeeks) : null
    }
    if (editingTask.value) {
      await updateTask({ ...data, id: editingTask.value.id })
      message.success('更新成功')
    } else {
      await createTask(data)
      message.success('创建成功')
    }
    showAdd.value = false
    editingTask.value = null
    form.value = { semester: '', courseId: null, teacherId: null, classId: null, studentCount: null, weeklyHours: null, priorityLevel: null, totalWeeks: null }
    loadData()
  } catch (e) {
    message.error('操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  const year = dayjs().year()
  for (let i = 0; i < 3; i++) {
    semesterOptions.value.push({ label: `${year - i}-${year - i + 1}学年第一学期`, value: `${year - i}-1` })
    semesterOptions.value.push({ label: `${year - i}-${year - i + 1}学年第二学期`, value: `${year - i}-2` })
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
  .desktop-task-page {
    display: none;
  }
}
</style>
