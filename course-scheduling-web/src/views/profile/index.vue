<template>
  <div class="page page-with-tabbar profile-page">
    <van-nav-bar title="个人中心" class="custom-nav" />

    <div class="profile-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <van-image round width="90" height="90" :src="avatar" class="profile-avatar" />
        <div class="profile-name">{{ userStore.userInfo?.realName || '用户' }}</div>
        <div class="profile-role">
          <van-tag type="primary" plain size="small">{{ getRoleName(userStore.userInfo?.role) }}</van-tag>
        </div>
      </div>
    </div>

    <div class="card info-card">
      <div class="section-title">基本信息</div>
      <van-cell-group inset class="info-group">
        <van-cell title="用户名" :value="userStore.userInfo?.username" />
        <van-cell title="角色">
          <template #value>
            <van-tag :type="getRoleTagType(userStore.userInfo?.role)" size="small">
              {{ getRoleName(userStore.userInfo?.role) }}
            </van-tag>
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <div class="card menu-card">
      <van-cell-group inset class="menu-group">
        <van-cell title="修改密码" is-link @click="showPassword = true" class="menu-item">
          <template #icon>
            <van-icon name="lock" color="#51caba" />
          </template>
        </van-cell>
        <van-cell title="关于系统" is-link @click="showAbout = true" class="menu-item">
          <template #icon>
            <van-icon name="info-o" color="#10b981" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>

    <div class="logout-wrapper">
      <van-button round block type="danger" @click="handleLogout" class="logout-btn">
        退出登录
      </van-button>
    </div>

    <van-popup v-model:show="showPassword" position="bottom" round style="height: 50%;" class="password-popup">
      <div class="popup-header">
        <div class="popup-title">修改密码</div>
        <van-icon name="cross" size="20" @click="showPassword = false" class="close-icon" />
      </div>
      <div class="popup-content">
        <van-form @submit="handleChangePassword">
          <van-cell-group inset class="form-group">
            <van-field
              v-model="passwordForm.oldPassword"
              type="password"
              name="oldPassword"
              label="原密码"
              placeholder="请输入原密码"
              required
              class="form-field"
            />
            <van-field
              v-model="passwordForm.newPassword"
              type="password"
              name="newPassword"
              label="新密码"
              placeholder="请输入新密码"
              required
              class="form-field"
            />
            <van-field
              v-model="passwordForm.confirmPassword"
              type="password"
              name="confirmPassword"
              label="确认密码"
              placeholder="请再次输入新密码"
              required
              class="form-field"
            />
          </van-cell-group>
          <div class="form-btn">
            <van-button round block type="primary" native-type="submit" class="submit-btn">
              确认修改
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showAbout" position="bottom" round style="height: 60%;" class="about-popup">
      <div class="popup-header">
        <div class="popup-title">关于系统</div>
        <van-icon name="cross" size="20" @click="showAbout = false" class="close-icon" />
      </div>
      <div class="popup-content">
        <div class="about-content">
          <div class="about-logo">
            <van-icon name="calendar-o" size="56" color="#51caba" />
          </div>
          <h3 class="about-title">智能排课系统</h3>
          <p class="about-version">版本：1.0.0</p>
          <p class="about-desc mt-16">基于遗传算法和贪心算法的智能排课解决方案，支持多校区、多约束条件下的课程调度优化。</p>
          <van-cell-group inset class="mt-16 about-details">
            <van-cell title="技术栈" value="Vue 3 + Spring Boot" />
            <van-cell title="算法支持" value="贪心算法 / 遗传算法" />
            <van-cell title="开发团队" value="Paike Team" />
          </van-cell-group>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const avatar = 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'
const showPassword = ref(false)
const showAbout = ref(false)

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const getRoleName = (role) => {
  const map = { 'ADMIN': '管理员', 'TEACHER': '教师', 'STUDENT': '学生' }
  return map[role] || role || '未知'
}

const getRoleTagType = (role) => {
  const map = { 'ADMIN': 'danger', 'TEACHER': 'primary', 'STUDENT': 'success' }
  return map[role] || 'default'
}

const handleLogout = async () => {
  await showConfirmDialog({ title: '确认退出', message: '确定要退出登录吗？' })
  await userStore.logout()
  showToast('已退出登录')
  router.push('/login')
}

const handleChangePassword = () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    showToast('两次密码输入不一致')
    return
  }
  showToast('密码修改成功')
  showPassword.value = false
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
}
</script>

<style scoped>
.profile-page {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.custom-nav {
  background: transparent;
  position: relative;
  z-index: 10;
}

.profile-header {
  position: relative;
  padding: var(--spacing-xl) var(--spacing-lg) var(--spacing-2xl);
  overflow: hidden;
  margin-top: -46px;
  padding-top: 60px;
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
  top: -50%;
  right: -20%;
  width: 80%;
  height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 60%);
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.header-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
  animation: slideUp 0.5s ease-out;
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

.profile-avatar {
  border: 4px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.profile-name {
  font-size: 22px;
  font-weight: 700;
  margin-top: var(--spacing-lg);
  letter-spacing: -0.02em;
}

.profile-role {
  margin-top: var(--spacing-sm);
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

.menu-item :deep(.van-cell__icon) {
  margin-right: var(--spacing-md);
}

.logout-wrapper {
  margin: var(--spacing-xl) var(--spacing-md);
  animation: slideUp 0.4s ease-out 0.2s backwards;
}

.logout-btn {
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.password-popup,
.about-popup {
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

.form-group {
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
  box-shadow: 0 4px 12px rgba(81, 202, 186, 0.3);
}

.about-content {
  text-align: center;
  padding: 0 var(--spacing-lg);
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

@media (min-width: 768px) {
  .profile-page {
    max-width: 600px;
    margin: 0 auto;
  }
  
  .password-popup,
  .about-popup {
    max-width: 480px;
    left: 50% !important;
    transform: translateX(-50%) !important;
    border-radius: var(--radius-xl) !important;
    margin-bottom: 20px;
  }
}
</style>
