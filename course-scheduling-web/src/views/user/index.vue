<template>
  <PageContainer with-tabbar class="user-page">
    <PageHeader title="用户管理">
      <n-button quaternary @click="onRefresh">
        <template #icon>
          <n-icon>
            <RefreshOutline />
          </n-icon>
        </template>
        刷新
      </n-button>
      <n-button type="primary" @click="handleAdd">
        <template #icon>
          <n-icon>
            <AddOutline />
          </n-icon>
        </template>
        新增用户
      </n-button>
    </PageHeader>

    <div class="table-container animate-fade-in">
      <div class="table-header">
        <div class="table-title desktop-only">用户列表</div>
        <div class="table-filters search-wrapper">
          <div class="search-inputs">
            <n-input
              v-model:value="searchForm.username"
              placeholder="用户名"
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon>
                  <SearchOutline />
                </n-icon>
              </template>
            </n-input>
            <n-input
              v-model:value="searchForm.realName"
              placeholder="真实姓名"
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon>
                  <PersonOutline />
                </n-icon>
              </template>
            </n-input>
          </div>
          <div class="filter-dropdowns">
            <n-select
              v-model:value="searchForm.role"
              :options="roleOptions"
              placeholder="全部角色"
              style="width: 120px"
              @update:value="handleSearch"
            />
            <n-select
              v-model:value="searchForm.status"
              :options="statusOptions"
              placeholder="全部状态"
              style="width: 120px"
              @update:value="handleSearch"
            />
          </div>
          <n-button type="success" @click="handleAdd" class="add-btn-desktop desktop-only">
            <template #icon>
              <n-icon>
                <AddOutline />
              </n-icon>
            </template>
            新增用户
          </n-button>
        </div>
      </div>

      <div class="mobile-actions mobile-only">
        <n-button type="primary" block @click="handleAdd" class="add-btn-mobile">
          <template #icon>
            <n-icon>
              <AddOutline />
            </n-icon>
          </template>
          新增用户
        </n-button>
      </div>

      <div class="desktop-content">
        <n-spin :show="loading" class="loading-container">
          <div v-if="!loading" class="table-wrapper">
            <table class="data-table">
              <thead>
                <tr>
                  <th>头像</th>
                  <th>用户名</th>
                  <th>真实姓名</th>
                  <th>角色</th>
                  <th>手机号</th>
                  <th>邮箱</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="user in userList" :key="user.id">
                  <td>
                    <n-avatar round size="small" :src="user.avatar || defaultAvatar" />
                  </td>
                  <td>{{ user.username }}</td>
                  <td class="name-cell">{{ user.realName || '-' }}</td>
                  <td>
                    <n-tag :type="getRoleTagType(user.role)" size="small" :class="['semantic-tag', getRoleTagClass(user.role)]">
                      {{ getRoleName(user.role) }}
                    </n-tag>
                  </td>
                  <td>{{ user.phone || '-' }}</td>
                  <td>{{ user.email || '-' }}</td>
                  <td>
                    <n-tag :type="user.status === 1 ? 'success' : 'error'" size="small" :class="['semantic-tag', getUserStatusTagClass(user.status)]">
                      {{ user.status === 1 ? '启用' : '禁用' }}
                    </n-tag>
                  </td>
                  <td>
                    <div class="action-buttons">
                      <n-button size="small" type="primary" @click="handleEdit(user)">编辑</n-button>
                      <n-button size="small" :type="user.status === 1 ? 'default' : 'success'" @click="handleToggleStatus(user)">
                        {{ user.status === 1 ? '禁用' : '启用' }}
                      </n-button>
                      <n-button size="small" type="warning" @click="handleResetPassword(user)">重置密码</n-button>
                      <n-button size="small" type="error" @click="handleDelete(user)">删除</n-button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
            <n-empty v-if="userList.length === 0" description="暂无数据" />
          </div>
        </n-spin>
      </div>

      <div class="mobile-content">
        <div class="user-list-wrapper">
          <n-pull-refresh v-model:refreshing="refreshing" @refresh="onRefresh">
            <n-list v-if="userList.length > 0">
              <n-list-item v-for="user in userList" :key="user.id">
                <template #prefix>
                  <n-avatar round size="large" :src="user.avatar || defaultAvatar" />
                </template>
                <template #header>
                  <div class="user-title">
                    <span class="user-name">{{ user.realName || user.username }}</span>
                    <n-tag :type="getRoleTagType(user.role)" size="small" :class="['role-tag', 'semantic-tag', getRoleTagClass(user.role)]">
                      {{ getRoleName(user.role) }}
                    </n-tag>
                    <n-tag :type="user.status === 1 ? 'success' : 'error'" size="small" :class="['semantic-tag', getUserStatusTagClass(user.status)]">
                      {{ user.status === 1 ? '启用' : '禁用' }}
                    </n-tag>
                  </div>
                </template>
                <div class="user-info">
                  <div class="info-line">
                    <n-icon size="12">
                      <PersonOutline />
                    </n-icon>
                    <span>{{ user.username }}</span>
                  </div>
                  <div v-if="user.phone" class="info-line">
                    <n-icon size="12">
                      <CallOutline />
                    </n-icon>
                    <span>{{ user.phone }}</span>
                  </div>
                  <div v-if="user.email" class="info-line">
                    <n-icon size="12">
                      <MailOutline />
                    </n-icon>
                    <span>{{ user.email }}</span>
                  </div>
                </div>
                <template #action>
                  <div class="user-actions">
                    <n-button type="primary" size="small" @click="handleEdit(user)">编辑</n-button>
                  </div>
                </template>
              </n-list-item>
            </n-list>
            <n-empty v-else description="暂无数据" />
          </n-pull-refresh>
        </div>
      </div>
    </div>

    <n-modal v-model:show="showForm" preset="card" :title="isEdit ? '编辑用户' : '新增用户'" :style="{ width: '500px' }" class="user-dialog">
      <n-form ref="formRef" :model="form" :rules="rules" label-placement="left" label-width="80px">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </n-form-item>
        <n-form-item v-if="!isEdit" label="密码" path="password">
          <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="请输入密码" />
        </n-form-item>
        <n-form-item label="真实姓名" path="realName">
          <n-input v-model:value="form.realName" placeholder="请输入真实姓名" />
        </n-form-item>
        <n-form-item label="角色" path="role">
          <n-select v-model:value="form.role" :options="roleSelectOptions" placeholder="请选择角色" />
        </n-form-item>
        <n-form-item label="手机号">
          <n-input v-model:value="form.phone" placeholder="请输入手机号" />
        </n-form-item>
        <n-form-item label="邮箱">
          <n-input v-model:value="form.email" placeholder="请输入邮箱" />
        </n-form-item>
        <n-form-item label="状态">
          <n-radio-group v-model:value="form.status">
            <n-radio :value="1">启用</n-radio>
            <n-radio :value="0">禁用</n-radio>
          </n-radio-group>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showForm = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ isEdit ? '保存' : '创建' }}
          </n-button>
        </n-space>
      </template>
    </n-modal>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { getUserList, createUser, updateUser, deleteUser, resetUserPassword, toggleUserStatus } from '@/api/user'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import {
  NButton,
  NIcon,
  NInput,
  NSelect,
  NSpin,
  NTag,
  NAvatar,
  NList,
  NListItem,
  NModal,
  NForm,
  NFormItem,
  NRadioGroup,
  NRadio,
  NEmpty,
  NSpace
} from 'naive-ui'
import {
  RefreshOutline,
  AddOutline,
  SearchOutline,
  PersonOutline,
  CallOutline,
  MailOutline
} from '@vicons/ionicons5'

const message = useMessage()
const dialog = useDialog()
const userStore = useUserStore()
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

const loading = ref(false)
const refreshing = ref(false)
const submitting = ref(false)
const showForm = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const userList = ref([])

const searchForm = reactive({
  username: '',
  realName: '',
  role: '',
  status: null
})

const form = reactive({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: '',
  phone: '',
  email: '',
  status: 1
})

const rules = {
  username: {
    required: true,
    message: '用户名不能为空',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '密码不能为空',
    trigger: 'blur'
  },
  realName: {
    required: true,
    message: '真实姓名不能为空',
    trigger: 'blur'
  },
  role: {
    required: true,
    message: '请选择角色',
    trigger: 'blur'
  }
}

const roleOptions = [
  { label: '全部角色', value: '' },
  { label: '管理员', value: 'ADMIN' },
  { label: '教师', value: 'TEACHER' },
  { label: '学生', value: 'STUDENT' }
]

const roleSelectOptions = [
  { label: '管理员', value: 'ADMIN' },
  { label: '教师', value: 'TEACHER' },
  { label: '学生', value: 'STUDENT' }
]

const statusOptions = [
  { label: '全部状态', value: null },
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const getRoleName = (role) => {
  const map = { 'ADMIN': '管理员', 'TEACHER': '教师', 'STUDENT': '学生' }
  return map[role] || role || '未知'
}

const getRoleTagType = (role) => {
  const map = { 'ADMIN': 'error', 'TEACHER': 'primary', 'STUDENT': 'success' }
  return map[role] || 'default'
}

const getRoleTagClass = (role) => {
  const map = {
    'ADMIN': 'semantic-tag--danger',
    'TEACHER': 'semantic-tag--info',
    'STUDENT': 'semantic-tag--success'
  }
  return map[role] || ''
}

const getUserStatusTagClass = (status) => {
  return status === 1 ? 'semantic-tag--success' : 'semantic-tag--danger'
}

const resetForm = () => {
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = ''
  form.phone = ''
  form.email = ''
  form.status = 1
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: 1,
      size: 100,
      ...searchForm
    }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const res = await getUserList(params)
    userList.value = res.data.records || []
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

const handleSearch = () => {
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  showForm.value = true
}

const handleEdit = (user) => {
  isEdit.value = true
  form.id = user.id
  form.username = user.username
  form.password = ''
  form.realName = user.realName
  form.role = user.role
  form.phone = user.phone || ''
  form.email = user.email || ''
  form.status = user.status
  showForm.value = true
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(form.id, {
        id: form.id,
        realName: form.realName,
        role: form.role,
        phone: form.phone,
        email: form.email,
        status: form.status
      })
      message.success('修改成功')
    } else {
      await createUser({
        username: form.username,
        password: form.password,
        realName: form.realName,
        role: form.role,
        phone: form.phone,
        email: form.email,
        status: form.status
      })
      message.success('创建成功')
    }
    showForm.value = false
    onRefresh()
  } catch (e) {
    message.error(e.message || (isEdit.value ? '修改失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

const handleToggleStatus = async (user) => {
  dialog.warning({
    title: '确认操作',
    content: `确定要${user.status === 1 ? '禁用' : '启用'}该用户吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await toggleUserStatus(user.id)
        message.success(user.status === 1 ? '已禁用' : '已启用')
        onRefresh()
      } catch (e) {
        message.error(e.message || '操作失败')
      }
    }
  })
}

const handleResetPassword = async (user) => {
  dialog.warning({
    title: '确认重置',
    content: `确定要重置 ${user.realName || user.username} 的密码吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const res = await resetUserPassword(user.id)
        message.success(res.data || '密码重置成功')
      } catch (e) {
        message.error(e.message || '重置失败')
      }
    }
  })
}

const handleDelete = async (user) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除用户 ${user.realName || user.username} 吗？此操作不可恢复！`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser(user.id)
        message.success('删除成功')
        onRefresh()
      } catch (e) {
        message.error(e.message || '删除失败')
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

.table-container {
  position: relative;
  border: 1px solid rgba(145, 120, 91, 0.18);
  border-radius: var(--radius-xl);
  background: var(--fabric-surface), rgba(255, 250, 243, 0.76);
  box-shadow: var(--shadow-card);
  overflow: hidden;
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
  flex-wrap: wrap;
  margin-bottom: 0;
  width: 100%;
  padding: var(--spacing-sm);
  border-radius: calc(var(--radius-lg) - 4px);
  background: rgba(255, 252, 247, 0.72);
  border: 1px solid rgba(145, 120, 91, 0.1);
}

.search-inputs {
  display: flex;
  gap: var(--spacing-sm);
  flex: 1;
  max-width: 520px;
}

.search-input {
  flex: 1;
  padding: 0;
}

.filter-dropdowns {
  display: flex;
  gap: var(--spacing-sm);
}

.add-btn-desktop {
  height: 40px;
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

.table-wrapper {
  overflow-x: auto;
  padding: var(--spacing-lg);
}

.data-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
  min-width: 900px;
}

.data-table th {
  padding: var(--spacing-md) var(--spacing-lg);
  text-align: left;
  font-weight: 600;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  white-space: nowrap;
  background: var(--bg-secondary);
  background-image: var(--fabric-inset);
}

.data-table td {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  color: var(--text-primary);
  background: rgba(255, 251, 245, 0.34);
}

.data-table tbody tr:hover {
  background: transparent;
}

.data-table tbody tr:hover td {
  background: rgba(255, 251, 245, 0.68);
}

.name-cell {
  font-weight: 500;
  color: var(--primary-color);
}

.action-buttons {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.desktop-only {
  display: block;
}

.mobile-only {
  display: none;
}

.desktop-content {
  display: block;
}

.mobile-content {
  display: none;
}

.user-list-wrapper {
  padding: var(--spacing-lg);
}

.user-item {
  margin-bottom: var(--spacing-sm);
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-light);
  transition: all 0.3s ease;
}

.user-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-card-hover);
}

.user-cell {
  background: var(--bg-primary);
  padding: var(--spacing-md);
}

.user-avatar {
  margin-right: var(--spacing-md);
  border: 2px solid var(--border-light);
}

.user-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.role-tag {
  flex-shrink: 0;
}

.user-info {
  margin-top: 8px;
  color: var(--text-secondary);
  font-size: 13px;
}

.info-line {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
}

.user-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-dialog {
  margin: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow: hidden;
  max-height: 60vh;
  overflow-y: auto;
}

@media (min-width: 1440px) {
  .table-header {
    padding: var(--spacing-xl) var(--spacing-2xl);
  }

  .data-table {
    font-size: 15px;
  }
}

@media (min-width: 1920px) {
  .data-table th,
  .data-table td {
    padding: var(--spacing-lg) var(--spacing-xl);
  }
}

@media (max-width: 1439px) {
  .data-table {
    min-width: 800px;
  }
  
  .data-table th,
  .data-table td {
    padding: var(--spacing-sm) var(--spacing-md);
  }
}

@media (max-width: 1199px) {
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .table-filters {
    width: 100%;
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

  .table-filters,
  .user-list-wrapper,
  .table-wrapper {
    padding: var(--spacing-md);
  }

  .search-inputs {
    max-width: none;
    width: 100%;
    flex-direction: column;
  }
}
</style>
