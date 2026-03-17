<template>
  <PageContainer with-tabbar class="profile-page">
    <PageHeader title="个人中心" />

    <div class="profile-layout">
      <div class="profile-sidebar">
        <div class="sidebar-card">
          <div class="profile-avatar-large">
            <n-image round width="120" height="120" :src="avatar" />
          </div>
          <div class="profile-name-large">{{ userStore.userInfo?.realName || '用户' }}</div>
          <div class="profile-role-large">
            <n-tag :class="['status-tag', getRoleClass(userStore.userInfo?.role)]" size="medium">
              {{ getRoleName(userStore.userInfo?.role) }}
            </n-tag>
          </div>
          <div class="profile-username">
            <n-icon :component="PersonOutline" /> {{ userStore.userInfo?.username }}
          </div>
        </div>

        <div class="sidebar-card menu-card">
          <div class="menu-list">
            <div class="menu-item-desktop active">
              <n-icon :component="PersonOutline" size="20" />
              <span>基本信息</span>
            </div>
            <div class="menu-item-desktop" @click="showPassword = true">
              <n-icon :component="LockClosedOutline" size="20" />
              <span>修改密码</span>
            </div>
            <div class="menu-item-desktop" @click="showAbout = true">
              <n-icon :component="InformationCircleOutline" size="20" />
              <span>关于系统</span>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-content">

        <div class="profile-content-grid">
          <div class="card info-card">
            <div class="info-card-header">
              <div class="section-title">基本信息</div>
              <n-button type="primary" size="small" class="edit-btn" @click="showEditProfile = true">
                <template #icon>
                  <n-icon :component="CreateOutline" />
                </template>
                编辑资料
              </n-button>
            </div>

            <div class="info-grid">
              <div class="info-item">
                <div class="info-icon-wrapper" style="background: rgba(114, 137, 103, 0.12);">
                  <n-icon :component="PersonOutline" size="20" color="#728967" />
                </div>
                <div class="info-content">
                  <div class="info-label">用户名</div>
                  <div class="info-value">{{ userStore.userInfo?.username }}</div>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon-wrapper" style="background: rgba(81, 202, 186, 0.12);">
                  <n-icon :component="IdCardOutline" size="20" color="#51caba" />
                </div>
                <div class="info-content">
                  <div class="info-label">真实姓名</div>
                  <div class="info-value">{{ userStore.userInfo?.realName }}</div>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon-wrapper" style="background: rgba(245, 158, 11, 0.12);">
                  <n-icon :component="ShieldCheckmarkOutline" size="20" color="#f59e0b" />
                </div>
                <div class="info-content">
                  <div class="info-label">角色权限</div>
                  <n-tag :class="['status-tag', getRoleClass(userStore.userInfo?.role)]" size="small">
                    {{ getRoleName(userStore.userInfo?.role) }}
                  </n-tag>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon-wrapper" style="background: rgba(111, 137, 163, 0.12);">
                  <n-icon :component="CallOutline" size="20" color="#6f89a3" />
                </div>
                <div class="info-content">
                  <div class="info-label">联系电话</div>
                  <div class="info-value">{{ userStore.userInfo?.phone || '-' }}</div>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon-wrapper" style="background: rgba(184, 102, 89, 0.12);">
                  <n-icon :component="MailOutline" size="20" color="#b86659" />
                </div>
                <div class="info-content">
                  <div class="info-label">电子邮箱</div>
                  <div class="info-value">{{ userStore.userInfo?.email || '-' }}</div>
                </div>
              </div>

              <div class="info-item">
                <div class="info-icon-wrapper" style="background: rgba(139, 92, 246, 0.12);">
                  <n-icon :component="TimeOutline" size="20" color="#8b5cf6" />
                </div>
                <div class="info-content">
                  <div class="info-label">注册时间</div>
                  <div class="info-value">{{ formatDate(userStore.userInfo?.createTime) }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="card menu-card mobile-only">
            <n-list>
              <n-list-item clickable @click="showPassword = true" class="menu-item">
                <template #prefix>
                  <n-icon :component="LockClosedOutline" color="var(--primary-color)" />
                </template>
                修改密码
              </n-list-item>
              <n-list-item clickable @click="showAbout = true" class="menu-item">
                <template #prefix>
                  <n-icon :component="InformationCircleOutline" color="var(--info-color)" />
                </template>
                关于系统
              </n-list-item>
            </n-list>
          </div>
        </div>

        <div class="logout-wrapper mobile-only">
          <n-button round block type="error" @click="handleLogout" class="logout-btn">
            退出登录
          </n-button>
        </div>
      </div>
    </div>

    <n-modal v-model:show="showPassword" preset="dialog" title="修改密码" class="password-dialog" positive-text="确定" negative-text="取消" @positive-click="handleChangePassword">
      <n-form :model="passwordForm" label-placement="left" label-width="80px">
        <n-form-item label="原密码" path="oldPassword">
          <n-input v-model:value="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
        </n-form-item>
        <n-form-item label="新密码" path="newPassword">
          <n-input v-model:value="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </n-form-item>
        <n-form-item label="确认密码" path="confirmPassword">
          <n-input v-model:value="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </n-form-item>
      </n-form>
    </n-modal>

    <n-modal v-model:show="showAbout" preset="card" title="关于系统" class="about-dialog">
        <div class="about-content">
          <div class="about-logo">
          <n-icon :component="CalendarOutline" size="56" color="var(--primary-color)" />
          </div>
        <h3 class="about-title">智能排课系统</h3>
        <p class="about-version">版本：1.0.0</p>
        <p class="about-desc mt-16">基于遗传算法和贪心算法的智能排课解决方案，支持多校区、多约束条件下的课程调度优化。</p>
        <n-descriptions bordered column="1" class="mt-16 about-details">
          <n-descriptions-item label="技术栈">Vue 3 + Spring Boot</n-descriptions-item>
          <n-descriptions-item label="算法支持">贪心算法 / 遗传算法</n-descriptions-item>
          <n-descriptions-item label="开发团队">Paike Team</n-descriptions-item>
        </n-descriptions>
      </div>
    </n-modal>
  </PageContainer>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, useDialog, NButton, NModal, NInput, NForm, NFormItem, NImage, NTag, NIcon, NList, NListItem, NDescriptions, NDescriptionsItem } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import PageContainer from '@/components/layout/PageContainer.vue'
import PageHeader from '@/components/layout/PageHeader.vue'
import { CalendarOutline, PersonOutline, LockClosedOutline, InformationCircleOutline, CallOutline, MailOutline, CreateOutline, IdCardOutline, ShieldCheckmarkOutline, TimeOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const dialog = useDialog()

const avatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const showPassword = ref(false)
const showAbout = ref(false)
const showEditProfile = ref(false)

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loadUserInfo = async () => {
  try {
    await userStore.fetchUserInfo()
  } catch (e) {
    console.error('加载用户信息失败', e)
  }
}

const getRoleName = (role) => {
  const map = { 'ADMIN': '管理员', 'TEACHER': '教师', 'STUDENT': '学生' }
  return map[role] || role || '未知'
}

const getRoleClass = (role) => {
  const map = { 'ADMIN': 'tag-danger', 'TEACHER': 'tag-primary', 'STUDENT': 'tag-success' }
  return map[role] || 'tag-default'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : '-'
}

const handleLogout = async () => {
  await new Promise((resolve, reject) => {
    dialog.warning({
      title: '确认退出',
      content: '确定要退出登录吗？',
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: resolve,
      onNegativeClick: reject
    })
  })
  await userStore.logout()
  message.success('已退出登录')
  router.push('/login')
}

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    message.error('两次密码输入不一致')
    return false
  }

  try {
    await changePassword(passwordForm.value)
    message.success('密码修改成功')
    showPassword.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    message.error(e.message || '密码修改失败')
    return false
  }
}

onMounted(() => {
  if (!userStore.userInfo) {
    loadUserInfo()
  }
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

.profile-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: var(--spacing-xl);
  align-items: start;
}

.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.profile-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.profile-content-grid {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.info-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-lg);
  align-content: start;
}

.sidebar-card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  padding: var(--spacing-xl);
  text-align: center;
}

.profile-avatar-large {
  margin-bottom: var(--spacing-lg);
}

.profile-avatar-large :deep(.n-image) {
  border: 4px solid var(--primary-color);
  box-shadow: 0 8px 24px rgba(81, 202, 186, 0.2);
}

.profile-name-large {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-sm);
}

.profile-role-large {
  margin-bottom: var(--spacing-md);
}

.profile-username {
  font-size: 14px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.menu-card {
  padding: var(--spacing-md) 0;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item-desktop {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-xl);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.menu-item-desktop:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.menu-item-desktop.active {
  color: var(--primary-color);
  background: rgba(81, 202, 186, 0.1);
  border-left: 3px solid var(--primary-color);
}

.profile-content {
  flex: 1;
}

.card {
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  padding: var(--spacing-xl);
}

.info-card {
  margin-bottom: 0;
}

.info-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px dashed var(--border-soft);
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-lg);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  transition: all var(--transition-base);
}

.info-item:hover {
  background: var(--bg-tertiary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.info-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-content {
  flex: 1;
  min-width: 0;
}

.info-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
}

.info-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg);
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: var(--spacing-md);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.info-group,
.menu-group,
.about-details {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.password-dialog .form-group,
.about-dialog .about-details {
  margin: var(--spacing-md);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.password-dialog .form-field {
  background: var(--bg-primary);
}

.about-content {
  text-align: center;
  padding: var(--spacing-md);
  max-height: 50vh;
  overflow-y: auto;
}

.about-logo {
  width: 96px;
  height: 96px;
  background: var(--bg-secondary);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-lg);
  box-shadow: var(--shadow-sm);
}

.about-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--spacing-xs);
}

.about-version {
  font-size: 14px;
  color: var(--text-muted);
}

.about-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.mt-16 {
  margin-top: 16px;
}

.mobile-only {
  display: none;
}

.desktop-only {
  display: block;
}

@media (min-width: 1440px) {
  .profile-layout {
    grid-template-columns: 340px 1fr;
    gap: var(--spacing-2xl);
  }

  .sidebar-card {
    padding: var(--spacing-2xl);
  }

  .card {
    padding: var(--spacing-2xl);
  }
}

@media (min-width: 1920px) {
  .profile-layout {
    grid-template-columns: 380px 1fr;
    gap: var(--spacing-3xl);
  }

  .profile-name-large {
    font-size: 26px;
  }

  .menu-item-desktop {
    padding: var(--spacing-lg) var(--spacing-2xl);
    font-size: 16px;
  }
}

@media (max-width: 1199px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-content {
    height: auto;
  }

  .info-card {
    flex: none;
  }

  .info-grid {
    grid-template-columns: 1fr;
    flex: none;
  }

  .profile-sidebar {
    flex-direction: row;
  }

  .sidebar-card {
    flex: 1;
  }

  .menu-card {
    padding: var(--spacing-md);
  }

}

@media (min-width: 768px) {
  .mobile-only {
    display: none !important;
  }
}

@media (max-width: 768px) {
  .mobile-profile-header {
    position: relative;
    padding: var(--spacing-lg);
    overflow: hidden;
    margin: calc(-1 * var(--spacing-lg));
    margin-bottom: var(--spacing-lg);
    border-radius: 0 0 var(--radius-xl) var(--radius-xl);
    min-height: 120px;
    display: flex;
    align-items: center;
  }

  .mobile-profile-header .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: var(--primary-gradient);
  }

  .mobile-profile-header .header-bg::before {
    content: '';
    position: absolute;
    top: -20%;
    right: -5%;
    width: 50%;
    height: 70%;
    background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 60%);
  }

  .mobile-profile-header .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: var(--spacing-lg);
    color: #fff;
    width: 100%;
  }

  .mobile-profile-header .profile-avatar {
    width: 64px;
    height: 64px;
    border: 3px solid rgba(255, 255, 255, 0.4);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    flex-shrink: 0;
  }

  .mobile-profile-header .header-info {
    flex: 1;
    min-width: 0;
  }

  .mobile-profile-header .profile-name {
    font-size: 18px;
    font-weight: 700;
    margin-bottom: var(--spacing-xs);
    color: #fff;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mobile-profile-header .profile-role {
    display: inline-flex;
  }

  .mobile-profile-header .profile-role .status-tag {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
    color: #fff;
    font-size: 12px;
  }
}

@media (max-width: 767px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    display: none;
  }

  .mobile-only {
    display: block;
  }

  .desktop-only {
    display: none;
  }

  .card {
    padding: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);
  }

  .card:last-child {
    margin-bottom: 0;
  }

  .info-card {
    padding: var(--spacing-lg);
    margin-bottom: 0;
  }

  .info-card-header {
    margin-bottom: var(--spacing-lg);
    padding-bottom: var(--spacing-md);
  }

  .info-grid {
    grid-template-columns: 1fr;
    gap: var(--spacing-md);
  }

  .info-item {
    padding: var(--spacing-md);
  }

  .info-icon-wrapper {
    width: 40px;
    height: 40px;
  }

  .info-value {
    font-size: 14px;
  }

  .mobile-profile-header {
    position: relative;
    padding: var(--spacing-lg) var(--spacing-lg) var(--spacing-xl);
    overflow: hidden;
    margin-top: calc(-1 * var(--spacing-lg));
    margin-left: calc(-1 * var(--spacing-lg));
    margin-right: calc(-1 * var(--spacing-lg));
    margin-bottom: var(--spacing-lg);
    border-radius: 0 0 var(--radius-xl) var(--radius-xl);
  }

  .header-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: var(--primary-gradient);
  }

  .header-bg::before {
    content: '';
    position: absolute;
    top: -30%;
    right: -10%;
    width: 60%;
    height: 80%;
    background: radial-gradient(circle, rgba(255,255,255,0.2) 0%, transparent 60%);
    animation: float 6s ease-in-out infinite;
  }

  .header-bg::after {
    content: '';
    position: absolute;
    bottom: -20%;
    left: -10%;
    width: 50%;
    height: 60%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 50%);
    animation: float 8s ease-in-out infinite reverse;
  }

  @keyframes float {
    0%, 100% { transform: translateY(0) rotate(0deg); }
    50% { transform: translateY(-15px) rotate(3deg); }
  }

  .header-content {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    gap: var(--spacing-md);
    color: #fff;
    animation: slideUp 0.5s ease-out;
    text-align: left;
  }

  .profile-avatar {
    border: 3px solid rgba(255, 255, 255, 0.4);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    flex-shrink: 0;
  }

  .profile-avatar :deep(img) {
    object-fit: cover;
  }

  .header-info {
    flex: 1;
    min-width: 0;
  }

  .profile-name {
    font-size: 20px;
    font-weight: 700;
    margin-bottom: var(--spacing-xs);
    letter-spacing: -0.01em;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .profile-role {
    display: inline-flex;
  }

  .profile-role .status-tag {
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
    color: #fff;
    backdrop-filter: blur(4px);
  }

  .info-card,
  .menu-card {
    animation: slideUp 0.4s ease-out backwards;
  }

  .info-card {
    animation-delay: 0.1s;
  }

  .menu-card {
    animation-delay: 0.15s;
  }

  .logout-wrapper {
    margin-top: var(--spacing-xl);
    padding: 0 var(--spacing-lg);
    animation: slideUp 0.4s ease-out 0.2s backwards;
  }

  .logout-btn {
    height: 52px;
    font-size: 16px;
    font-weight: 600;
    border-radius: var(--radius-lg);
    background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
    border: none;
    box-shadow:
      0 6px 16px rgba(239, 68, 68, 0.35),
      inset 0 1px 0 rgba(255, 255, 255, 0.2);
    transition: all var(--transition-base);
  }

  .logout-btn:active {
    transform: translateY(2px);
    box-shadow:
      0 2px 8px rgba(239, 68, 68, 0.3),
      inset 0 1px 0 rgba(255, 255, 255, 0.1);
  }
}
</style>
