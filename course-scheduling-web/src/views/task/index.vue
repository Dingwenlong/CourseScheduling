<template>
  <PageContainer with-tabbar>
    <div class="task-page-content">
      <PageHeader title="教学任务">
        <template #actions>
          <n-button quaternary @click="onRefresh">
            <template #icon>
              <n-icon>
                <RefreshOutline />
              </n-icon>
            </template>
            刷新
          </n-button>
          <n-button v-if="isAdmin" type="primary" @click="showAdd = true">
            <template #icon>
              <n-icon>
                <AddOutline />
              </n-icon>
            </template>
            新增
          </n-button>
        </template>
      </PageHeader>

      <n-alert
        v-if="!isAdmin"
        type="info"
        :bordered="false"
        class="readonly-alert"
      >
        当前为教师只读视图，你可以查看自己的教学任务，新增、编辑、删除仅管理员可用。
      </n-alert>

      <div class="table-container animate-fade-in">
        <div class="table-header">
          <div class="table-title desktop-only">任务列表</div>
          <div class="table-filters search-wrapper">
            <n-input
              v-model:value="searchText"
              placeholder="搜索课程名称"
              class="search-input desktop-search"
              @keyup.enter="onSearch"
            >
              <template #prefix>
                <n-icon>
                  <SearchOutline />
                </n-icon>
              </template>
            </n-input>
            <div class="filter-dropdowns">
              <n-select
                v-model:value="filterSemester"
                :options="semesterOptions"
                placeholder="全部学期"
                style="width: 140px"
                @update:value="onFilterChange"
              />
              <n-select
                v-model:value="filterStatus"
                :options="statusOptions"
                placeholder="全部状态"
                style="width: 140px"
                @update:value="onFilterChange"
              />
            </div>
          </div>
        </div>

        <div v-if="isAdmin" class="mobile-actions mobile-only">
          <n-button type="primary" block @click="showAdd = true" class="add-btn-mobile">
            <template #icon>
              <n-icon>
                <AddOutline />
              </n-icon>
            </template>
            新增任务
          </n-button>
        </div>

        <div class="desktop-content">
          <n-spin :show="loading" class="loading-container">
            <div v-if="!loading" class="task-list grid-layout">
              <div v-for="(item, index) in list" :key="item.id" class="card task-card" @click="showTaskDetail(item)">
                <div class="flex-between">
                  <div class="flex-1">
                    <div class="task-title">{{ item.courseName }}</div>
                    <div class="task-info mt-8">
                      <span class="info-item">
                        <n-icon size="12">
                          <TimeOutline />
                        </n-icon>
                        周学时: {{ item.weeklyHours || '-' }}
                      </span>
                      <span class="info-item">
                        <n-icon size="12">
                          <PeopleOutline />
                        </n-icon>
                        学生: {{ item.studentCount || '-' }}
                      </span>
                      <span class="info-item">
                        <n-icon size="12">
                          <FlagOutline />
                        </n-icon>
                        优先级: {{ item.priorityLevel || '-' }}
                      </span>
                    </div>
                  </div>
                  <n-tag :type="getStatusTagType(item.status)" size="small">
                    {{ getStatusText(item.status) }}
                  </n-tag>
                </div>
                <div v-if="isAdmin" class="task-actions desktop-only">
                  <n-button size="small" type="primary" @click.stop="editTask(item)">编辑</n-button>
                  <n-button size="small" type="error" @click.stop="deleteTaskConfirm(item)">删除</n-button>
                </div>
              </div>
            </div>
            <n-empty v-if="list.length === 0 && !loading" description="暂无数据" />
          </n-spin>
        </div>

        <div class="mobile-content">
          <div class="user-list-wrapper">
            <div>
              <n-list v-if="list.length > 0">
                <n-list-item v-for="(item, index) in list" :key="item.id">
                  <template #header>
                    <div class="flex-between">
                      <div class="task-title">{{ item.courseName }}</div>
                      <n-tag :type="getStatusTagType(item.status)" size="small">
                        {{ getStatusText(item.status) }}
                      </n-tag>
                    </div>
                  </template>
                  <div class="task-info mt-8">
                    <span class="info-item">
                      <n-icon size="12">
                        <TimeOutline />
                      </n-icon>
                      周学时: {{ item.weeklyHours || '-' }}
                    </span>
                    <span class="info-item">
                      <n-icon size="12">
                        <PeopleOutline />
                      </n-icon>
                      学生: {{ item.studentCount || '-' }}
                    </span>
                    <span class="info-item">
                      <n-icon size="12">
                        <FlagOutline />
                      </n-icon>
                      优先级: {{ item.priorityLevel || '-' }}
                    </span>
                  </div>
                  <template v-if="isAdmin" #action>
                    <div class="task-actions-mobile">
                      <n-button type="primary" size="small" @click="editTask(item)">编辑</n-button>
                      <n-button type="error" size="small" @click="deleteTaskConfirm(item)">删除</n-button>
                    </div>
                  </template>
                </n-list-item>
              </n-list>
              <n-empty v-else description="暂无数据" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <n-modal v-model:show="showAdd" preset="card" :title="editingTask ? '编辑任务' : '新增任务'" :style="{ width: '500px' }" class="task-dialog">
        <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="100px">
          <n-form-item label="学期" path="semester">
            <n-select v-model:value="form.semester" :options="semesterOptions.slice(1)" placeholder="请选择学期" />
          </n-form-item>
          <n-form-item label="课程" path="courseId">
            <n-select
              v-model:value="form.courseId"
              filterable
              remote
              clearable
              :options="courseOptions"
              :loading="courseLookupLoading"
              placeholder="搜索课程名称或课程编号"
              @search="searchCourseOptions"
              @focus="loadCourseOptions"
            />
          </n-form-item>
          <n-form-item label="教师" path="teacherId">
            <n-select
              v-model:value="form.teacherId"
              filterable
              remote
              clearable
              :options="teacherOptions"
              :loading="teacherLookupLoading"
              placeholder="搜索教师姓名或教师编号"
              @search="searchTeacherOptions"
              @focus="loadTeacherOptions"
            />
          </n-form-item>
          <n-form-item label="班级" path="classId">
            <n-select
              v-model:value="form.classId"
              filterable
              remote
              clearable
              :options="classOptions"
              :loading="classLookupLoading"
              placeholder="搜索班级名称或班级编号"
              @search="searchClassOptions"
              @focus="loadClassOptions"
            />
          </n-form-item>
        <n-form-item label="学生人数">
          <n-input-number v-model:value="form.studentCount" placeholder="请输入学生人数" style="width: 100%" :min="0" />
        </n-form-item>
        <n-form-item label="周学时">
          <n-input-number v-model:value="form.weeklyHours" placeholder="请输入周学时" style="width: 100%" :min="0" />
        </n-form-item>
        <n-form-item label="优先级">
          <n-input-number v-model:value="form.priorityLevel" placeholder="1-10，数字越小优先级越高" style="width: 100%" :min="1" :max="10" />
        </n-form-item>
        <n-form-item label="总周数" path="totalWeeks">
          <n-input-number v-model:value="form.totalWeeks" placeholder="请输入总周数" style="width: 100%" :min="1" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showAdd = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ editingTask ? '保存' : '创建' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showDetail" preset="card" title="任务详情" :style="{ width: '500px' }" class="detail-dialog">
      <div v-if="currentTask" class="detail-content">
        <n-descriptions :column="1" bordered>
          <n-descriptions-item label="学期">{{ currentTask.semester }}</n-descriptions-item>
          <n-descriptions-item label="课程">
            {{ currentTask.courseName || `课程 #${currentTask.courseId}` }}
          </n-descriptions-item>
          <n-descriptions-item label="教师">
            {{ currentTask.teacherName || `教师 #${currentTask.teacherId}` }}
          </n-descriptions-item>
          <n-descriptions-item label="班级">
            {{ currentTask.className || `班级 #${currentTask.classId}` }}
          </n-descriptions-item>
          <n-descriptions-item label="学生人数">{{ currentTask.studentCount }}</n-descriptions-item>
          <n-descriptions-item label="周学时">{{ currentTask.weeklyHours }}</n-descriptions-item>
          <n-descriptions-item label="优先级">{{ currentTask.priorityLevel }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="getStatusTagType(currentTask.status)">{{ getStatusText(currentTask.status) }}</n-tag>
          </n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import dayjs from 'dayjs'
import { getTaskList, createTask, updateTask, deleteTask } from '@/api/task'
import { searchCourses, searchTeachers, searchClasses } from '@/api/lookup'
import { buildSemesterOptions } from '@/utils/semester'
import { useLayoutStore } from '@/stores/layout'
import { useUserStore } from '@/stores/user'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import {
  NAlert,
  NButton,
  NIcon,
  NInput,
  NSelect,
  NSpin,
  NTag,
  NList,
  NListItem,
  NModal,
  NForm,
  NFormItem,
  NInputNumber,
  NEmpty,
  NSpace,
  NDescriptions,
  NDescriptionsItem
} from 'naive-ui'
import {
  RefreshOutline,
  AddOutline,
  SearchOutline,
  TimeOutline,
  PeopleOutline,
  FlagOutline
} from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()
const layoutStore = useLayoutStore()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

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
const submitting = ref(false)
const editingTask = ref(null)
const currentTask = ref(null)
const formRef = ref(null)

const form = reactive({
  semester: '',
  courseId: null,
  teacherId: null,
  classId: null,
  studentCount: null,
  weeklyHours: null,
  priorityLevel: null,
  totalWeeks: null
})
const courseOptions = ref([])
const teacherOptions = ref([])
const classOptions = ref([])
const courseLookupLoading = ref(false)
const teacherLookupLoading = ref(false)
const classLookupLoading = ref(false)

const rules = {
  semester: {
    required: true,
    message: '请选择学期',
    trigger: 'blur'
  },
  courseId: {
    required: true,
    message: '请选择课程',
    trigger: 'blur'
  },
  teacherId: {
    required: true,
    message: '请选择教师',
    trigger: 'blur'
  },
  classId: {
    required: true,
    message: '请选择班级',
    trigger: 'blur'
  },
  totalWeeks: {
    required: true,
    message: '请输入总周数',
    trigger: 'blur'
  }
}

const semesterOptions = ref([
  { label: '全部学期', value: '' }
])

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待排课', value: 'PENDING' },
  { label: '已排课', value: 'SCHEDULED' },
  { label: '调整中', value: 'ADJUSTING' },
  { label: '已完成', value: 'COMPLETED' }
]

const getStatusText = (status) => {
  const map = { 'PENDING': '待排课', 'SCHEDULED': '已排课', 'ADJUSTING': '调整中', 'COMPLETED': '已完成' }
  return map[status] || status
}

const getStatusTagType = (status) => {
  const map = { 'PENDING': 'warning', 'SCHEDULED': 'success', 'ADJUSTING': 'primary', 'COMPLETED': 'default' }
  return map[status] || 'default'
}

const resetForm = () => {
  form.semester = ''
  form.courseId = null
  form.teacherId = null
  form.classId = null
  form.studentCount = null
  form.weeklyHours = null
  form.priorityLevel = null
  form.totalWeeks = null
}

const loadLookupOptions = async (searcher, targetRef, loadingRef, keyword = '') => {
  loadingRef.value = true
  try {
    const res = await searcher({ keyword, limit: 20 })
    targetRef.value = res.data || []
  } catch (error) {
    targetRef.value = []
  } finally {
    loadingRef.value = false
  }
}

const loadCourseOptions = async () => {
  if (!courseOptions.value.length) {
    await loadLookupOptions(searchCourses, courseOptions, courseLookupLoading)
  }
}

const loadTeacherOptions = async () => {
  if (!teacherOptions.value.length) {
    await loadLookupOptions(searchTeachers, teacherOptions, teacherLookupLoading)
  }
}

const loadClassOptions = async () => {
  if (!classOptions.value.length) {
    await loadLookupOptions(searchClasses, classOptions, classLookupLoading)
  }
}

const searchCourseOptions = async (keyword) => {
  await loadLookupOptions(searchCourses, courseOptions, courseLookupLoading, keyword)
}

const searchTeacherOptions = async (keyword) => {
  await loadLookupOptions(searchTeachers, teacherOptions, teacherLookupLoading, keyword)
}

const searchClassOptions = async (keyword) => {
  await loadLookupOptions(searchClasses, classOptions, classLookupLoading, keyword)
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

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: page.value,
      size: 100,
      semester: filterSemester.value,
      status: filterStatus.value,
      keyword: searchText.value?.trim()
    }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    const res = await getTaskList(params)
    list.value = res.data.records || []
  } catch (e) {
    message.error(e.message || '加载失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  loadData()
}

const onSearch = () => {
  loadData()
}

const onFilterChange = () => {
  onRefresh()
}

const showTaskDetail = (item) => {
  currentTask.value = item
  showDetail.value = true
}

const editTask = (item) => {
  if (!isAdmin.value) {
    message.warning('当前账号仅支持查看教学任务')
    return
  }
  editingTask.value = item
  form.semester = item.semester
  form.courseId = item.courseId
  form.teacherId = item.teacherId
  form.classId = item.classId
  form.studentCount = item.studentCount
  form.weeklyHours = item.weeklyHours
  form.priorityLevel = item.priorityLevel
  form.totalWeeks = item.totalWeeks
  ensureOption(courseOptions, item.courseId, item.courseName ? `${item.courseName} (${item.courseId})` : `课程 #${item.courseId}`)
  ensureOption(teacherOptions, item.teacherId, item.teacherName ? `${item.teacherName} (${item.teacherId})` : `教师 #${item.teacherId}`)
  ensureOption(classOptions, item.classId, item.className ? `${item.className} (${item.classId})` : `班级 #${item.classId}`)
  showAdd.value = true
}

const deleteTaskConfirm = async (item) => {
  if (!isAdmin.value) {
    message.warning('当前账号仅支持查看教学任务')
    return
  }
  dialog.warning({
    title: '确认删除',
    content: '删除后数据将无法恢复，确定删除吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteTask(item.id)
        message.success('删除成功')
        onRefresh()
      } catch (e) {
        message.error(e.message || '删除失败')
      }
    }
  })
}

const handleSubmit = async () => {
  if (!isAdmin.value) {
    message.warning('当前账号仅支持查看教学任务')
    return
  }
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    const data = {
      ...form,
      courseId: form.courseId ? Number(form.courseId) : null,
      teacherId: form.teacherId ? Number(form.teacherId) : null,
      classId: form.classId ? Number(form.classId) : null,
      studentCount: form.studentCount ? Number(form.studentCount) : null,
      weeklyHours: form.weeklyHours ? Number(form.weeklyHours) : null,
      priorityLevel: form.priorityLevel ? Number(form.priorityLevel) : null,
      totalWeeks: form.totalWeeks ? Number(form.totalWeeks) : null
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
    resetForm()
    onRefresh()
  } catch (e) {
    message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  semesterOptions.value = buildSemesterOptions(dayjs().year(), 3, true)
  loadData()

  if (isAdmin.value) {
    layoutStore.setHeaderAction({
      icon: 'plus',
      text: '新增',
      onClick: () => { showAdd.value = true }
    })
  } else {
    layoutStore.clearHeaderAction()
  }
})

onUnmounted(() => {
  layoutStore.clearHeaderAction()
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

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

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

.task-page-content {
  position: relative;
  animation: fadeIn 0.3s ease-out;
  padding-bottom: var(--spacing-xl);
}

.task-page-content::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 220px;
  background:
    radial-gradient(circle at top left, rgba(126, 149, 99, 0.14), transparent 40%),
    radial-gradient(circle at top right, rgba(184, 102, 89, 0.12), transparent 32%);
  pointer-events: none;
  opacity: 0.8;
}

.readonly-alert {
  margin-bottom: var(--spacing-md);
  border-radius: var(--radius-lg);
  background: rgba(255, 250, 243, 0.8);
  border: 1px solid rgba(145, 120, 91, 0.14);
  box-shadow: var(--shadow-sm);
}

.table-container {
  position: relative;
  border: 1px solid rgba(145, 120, 91, 0.18);
  border-radius: var(--radius-xl);
  background: var(--fabric-surface), rgba(255, 250, 243, 0.76);
  backdrop-filter: blur(14px);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.table-container::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  border: 1px solid rgba(255, 255, 255, 0.32);
  pointer-events: none;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px dashed var(--border-soft);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.table-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.01em;
}

.table-filters {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex: 1;
  justify-content: flex-end;
  flex-wrap: wrap;
  padding: var(--spacing-sm);
  border-radius: calc(var(--radius-lg) - 6px);
  background: rgba(255, 252, 247, 0.72);
  border: 1px solid rgba(145, 120, 91, 0.1);
}

.search-input {
  flex: 1;
  max-width: 240px;
}

.filter-dropdowns {
  display: flex;
  gap: var(--spacing-sm);
}

.mobile-actions {
  padding: var(--spacing-md);
}

.add-btn-mobile {
  height: 44px;
  border-radius: var(--radius-md);
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--spacing-2xl);
}

.desktop-content {
  display: block;
  padding: var(--spacing-lg);
}

.mobile-content {
  display: none;
}

.desktop-only {
  display: block;
}

.mobile-only {
  display: none;
}

.task-list.grid-layout {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
  padding: 0;
}

.task-card {
  margin: 0;
  cursor: pointer;
  position: relative;
  padding: var(--spacing-xl);
  animation: slideUp 0.3s ease-out backwards;
  transition: all var(--transition-base);
  border: 1px solid rgba(145, 120, 91, 0.14);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.68), rgba(255, 255, 255, 0.2)),
    rgba(255, 250, 243, 0.68);
  border-radius: var(--radius-lg);
}

.task-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-2px);
}

.task-card:nth-child(1) { animation-delay: 0.05s; }
.task-card:nth-child(2) { animation-delay: 0.1s; }
.task-card:nth-child(3) { animation-delay: 0.15s; }

.flex-between {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--spacing-md);
}

.flex-1 {
  flex: 1;
  min-width: 0;
}

.task-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
  margin-bottom: var(--spacing-sm);
}

.task-info {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.info-item {
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-size: 12px;
  color: var(--text-secondary);
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 248, 238, 0.72);
  border: 1px solid rgba(145, 120, 91, 0.1);
}

.task-actions {
  display: flex;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
  border-top: 1px dashed var(--border-light);
}

.user-list-wrapper {
  padding: var(--spacing-lg);
}

.task-actions-mobile {
  display: flex;
  gap: var(--spacing-sm);
}

.task-dialog {
  margin: var(--spacing-md);
}

.detail-dialog {
  margin: var(--spacing-md);
}

.detail-content {
  padding: var(--spacing-md) 0;
}

.mt-8 {
  margin-top: 8px;
}

:deep(.task-page-content .n-list-item) {
  border-radius: var(--radius-lg);
  border: 1px solid rgba(145, 120, 91, 0.1);
  background: rgba(255, 250, 243, 0.74);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

:deep(.task-page-content .n-base-selection),
:deep(.task-page-content .n-input),
:deep(.task-page-content .n-input-number) {
  border-radius: var(--radius-md);
}

@media (min-width: 1600px) {
  .task-list.grid-layout {
    grid-template-columns: repeat(3, 1fr);
    gap: var(--spacing-xl);
    padding: var(--spacing-xl);
  }

  .task-card {
    padding: var(--spacing-xl);
  }

  .task-title {
    font-size: 18px;
  }
}

@media (max-width: 1199px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-filters {
    width: 100%;
    justify-content: flex-start;
    padding: var(--spacing-sm);
  }
}

@media (max-width: 767px) {
  .desktop-only {
    display: none;
  }

  .mobile-only {
    display: block;
  }

  .desktop-content {
    display: none;
  }

  .mobile-content {
    display: block;
  }

  .table-container {
    margin-top: var(--spacing-md);
  }

  .desktop-content,
  .user-list-wrapper {
    padding: var(--spacing-md);
  }

  .task-card {
    padding: var(--spacing-lg);
  }
}
</style>
