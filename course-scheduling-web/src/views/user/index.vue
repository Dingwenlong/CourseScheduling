<template>
  <div class="page page-with-tabbar user-page">
    <van-nav-bar title="用户管理" class="custom-nav" />

    <div class="table-container animate-fade-in">
      <div class="table-header">
        <div class="table-title desktop-only">用户列表</div>
        <div class="table-filters search-wrapper">
          <div class="search-inputs">
            <van-search
              v-model="searchForm.username"
              placeholder="用户名"
              class="search-input"
              @search="handleSearch"
            />
            <van-search
              v-model="searchForm.realName"
              placeholder="真实姓名"
              class="search-input"
              @search="handleSearch"
            />
          </div>
          <van-dropdown-menu class="filter-dropdowns">
            <van-dropdown-item v-model="searchForm.role" :options="roleOptions" @change="handleSearch" />
            <van-dropdown-item v-model="searchForm.status" :options="statusOptions" @change="handleSearch" />
          </van-dropdown-menu>
          <van-button type="success" icon="plus" size="small" @click="handleAdd" class="add-btn-desktop desktop-only">
            新增用户
          </van-button>
        </div>
      </div>

      <div class="mobile-actions mobile-only">
        <van-button type="success" icon="plus" block @click="handleAdd" class="add-btn-mobile">
          新增用户
        </van-button>
      </div>

      <div class="user-list-wrapper">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
            class="user-list"
          >
            <van-swipe-cell v-for="user in userList" :key="user.id" class="user-item">
              <van-cell class="user-cell">
                <template #icon>
                  <van-image round width="48" height="48" :src="user.avatar || defaultAvatar" class="user-avatar" />
                </template>
                <template #title>
                  <div class="user-title">
                    <span class="user-name">{{ user.realName || user.username }}</span>
                    <van-tag :type="getRoleTagType(user.role)" size="small" class="role-tag">
                      {{ getRoleName(user.role) }}
                    </van-tag>
                    <van-tag :type="user.status === 1 ? 'success' : 'danger'" size="small">
                      {{ user.status === 1 ? '启用' : '禁用' }}
                    </van-tag>
                  </div>
                </template>
                <template #label>
                  <div class="user-info">
                    <div class="info-line">
                      <van-icon name="user-o" size="12" />
                      <span>{{ user.username }}</span>
                    </div>
                    <div v-if="user.phone" class="info-line">
                      <van-icon name="phone-o" size="12" />
                      <span>{{ user.phone }}</span>
                    </div>
                    <div v-if="user.email" class="info-line">
                      <van-icon name="envelop-o" size="12" />
                      <span>{{ user.email }}</span>
                    </div>
                  </div>
                </template>
                <template #value>
                  <div class="user-actions">
                    <van-button type="primary" size="mini" @click="handleEdit(user)">编辑</van-button>
                  </div>
                </template>
              </van-cell>
              <template #right>
                <van-button
                  square
                  :type="user.status === 1 ? 'danger' : 'success'"
                  :text="user.status === 1 ? '禁用' : '启用'"
                  class="swipe-btn"
                  @click="handleToggleStatus(user)"
                />
                <van-button
                  square
                  type="warning"
                  text="重置密码"
                  class="swipe-btn"
                  @click="handleResetPassword(user)"
                />
                <van-button
                  square
                  type="danger"
                  text="删除"
                  class="swipe-btn"
                  @click="handleDelete(user)"
                />
              </template>
            </van-swipe-cell>
          </van-list>
        </van-pull-refresh>
      </div>
    </div>

    <van-dialog v-model:show="showForm" :title="isEdit ? '编辑用户' : '新增用户'" show-cancel-button @confirm="handleSubmit" class="user-dialog">
      <van-form @submit.prevent="handleSubmit">
        <van-cell-group inset class="form-group">
          <van-field
            v-model="form.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '用户名不能为空' }]"
            :disabled="isEdit"
            required
            class="form-field"
          />
          <van-field
            v-if="!isEdit"
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: !isEdit, message: '密码不能为空' }]"
            required
            class="form-field"
          />
          <van-field
            v-model="form.realName"
            name="realName"
            label="真实姓名"
            placeholder="请输入真实姓名"
            :rules="[{ required: true, message: '真实姓名不能为空' }]"
            required
            class="form-field"
          />
          <van-field
            v-model="form.role"
            name="role"
            label="角色"
            placeholder="请选择角色"
            :rules="[{ required: true, message: '请选择角色' }]"
            required
            class="form-field"
            readonly
            @click="showRolePicker = true"
          />
          <van-field
            v-model="form.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            class="form-field"
          />
          <van-field
            v-model="form.email"
            name="email"
            label="邮箱"
            placeholder="请输入邮箱"
            class="form-field"
          />
          <van-field name="status" label="状态" class="form-field">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio :name="1">启用</van-radio>
                <van-radio :name="0">禁用</van-radio>
              </van-radio-group>
            </template>
          </van-field>
        </van-cell-group>
      </van-form>
    </van-dialog>

    <van-popup v-model:show="showRolePicker" position="bottom" round>
      <van-picker
        :columns="roleColumns"
        @confirm="onRoleConfirm"
        @cancel="showRolePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast, showConfirmDialog, showLoadingToast, closeToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { getUserList, createUser, updateUser, deleteUser, resetUserPassword, toggleUserStatus } from '@/api/user'

const userStore = useUserStore()
const defaultAvatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const submitting = ref(false)
const showForm = ref(false)
const showRolePicker = ref(false)
const isEdit = ref(false)

const userList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

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

const roleOptions = [
  { text: '全部角色', value: '' },
  { text: '管理员', value: 'ADMIN' },
  { text: '教师', value: 'TEACHER' },
  { text: '学生', value: 'STUDENT' }
]

const statusOptions = [
  { text: '全部状态', value: null },
  { text: '启用', value: 1 },
  { text: '禁用', value: 0 }
]

const roleColumns = [
  { text: '管理员', value: 'ADMIN' },
  { text: '教师', value: 'TEACHER' },
  { text: '学生', value: 'STUDENT' }
]

const getRoleName = (role) => {
  const map = { 'ADMIN': '管理员', 'TEACHER': '教师', 'STUDENT': '学生' }
  return map[role] || role || '未知'
}

const getRoleTagType = (role) => {
  const map = { 'ADMIN': 'danger', 'TEACHER': 'primary', 'STUDENT': 'success' }
  return map[role] || 'default'
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
  if (loading.value) return
  loading.value = true

  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })

    const res = await getUserList(params)
    if (res.data) {
      if (pageNum.value === 1) {
        userList.value = res.data.list || []
      } else {
        userList.value.push(...(res.data.list || []))
      }
      total.value = res.data.total || 0
      finished.value = userList.value.length >= total.value
    }
  } catch (e) {
    showToast(e.message || '加载失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  loadData()
  pageNum.value++
}

const onRefresh = () => {
  pageNum.value = 1
  finished.value = false
  loadData()
}

const handleSearch = () => {
  pageNum.value = 1
  userList.value = []
  finished.value = false
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

const onRoleConfirm = ({ selectedOptions }) => {
  form.role = selectedOptions[0].value
  showRolePicker.value = false
}

const handleSubmit = async () => {
  submitting.value = true
  showLoadingToast({
    message: isEdit.value ? '保存中...' : '创建中...',
    forbidClick: true,
    duration: 0
  })

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
      showToast('修改成功')
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
      showToast('创建成功')
    }
    showForm.value = false
    onRefresh()
  } catch (e) {
    showToast(e.message || (isEdit.value ? '修改失败' : '创建失败'))
  } finally {
    submitting.value = false
    closeToast()
  }
}

const handleToggleStatus = async (user) => {
  try {
    await showConfirmDialog({
      title: '确认操作',
      message: `确定要${user.status === 1 ? '禁用' : '启用'}该用户吗？`
    })
    await toggleUserStatus(user.id)
    showToast(user.status === 1 ? '已禁用' : '已启用')
    onRefresh()
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '操作失败')
    }
  }
}

const handleResetPassword = async (user) => {
  try {
    await showConfirmDialog({
      title: '确认重置',
      message: `确定要重置 ${user.realName || user.username} 的密码吗？`
    })
    const res = await resetUserPassword(user.id)
    showToast(res.data || '密码重置成功')
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '重置失败')
    }
  }
}

const handleDelete = async (user) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: `确定要删除用户 ${user.realName || user.username} 吗？此操作不可恢复！`
    })
    await deleteUser(user.id)
    showToast('删除成功')
    onRefresh()
  } catch (e) {
    if (e !== 'cancel') {
      showToast(e.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-page {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.table-container {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  margin-top: var(--spacing-md);
  margin-left: 10px;
  margin-right: 10px;
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
  flex: 1;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.search-inputs {
  display: flex;
  gap: var(--spacing-sm);
  flex: 1;
  max-width: 500px;
}

.search-input {
  flex: 1;
  padding: 0;
}

.filter-dropdowns {
  display: flex;
  gap: var(--spacing-sm);
}

.filter-dropdowns :deep(.van-dropdown-menu__bar) {
  box-shadow: none;
  height: 36px;
  background: transparent;
}

.add-btn-desktop {
  height: 36px;
}

.mobile-actions {
  padding: var(--spacing-md);
}

.add-btn-mobile {
  height: 44px;
  border-radius: var(--radius-md);
}

.user-list-wrapper {
  padding: var(--spacing-md);
}

.custom-nav {
  background: var(--bg-primary);
  box-shadow: var(--shadow-sm);
}

.desktop-only {
  display: none;
}

.mobile-only {
  display: block;
}

@media (min-width: 768px) {
  .custom-nav {
    display: none;
  }
  
  .desktop-only {
    display: block;
  }
  
  .mobile-only {
    display: none;
  }
  
  .table-container {
    margin-top: 0;
  }
  
  .user-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: var(--spacing-md);
  }
  
  .user-item {
    margin-bottom: 0;
  }
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
  box-shadow: var(--shadow-md);
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

.swipe-btn {
  height: 100%;
  min-width: 60px;
}

.user-dialog .form-group {
  margin: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow: hidden;
  max-height: 60vh;
  overflow-y: auto;
}

.user-dialog .form-field {
  background: var(--bg-primary);
}

@media (min-width: 1440px) {
  .user-list {
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: var(--spacing-lg);
  }
}
</style>
