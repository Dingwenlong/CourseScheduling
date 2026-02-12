<template>
  <div class="page page-with-tabbar">
    <van-nav-bar title="个人中心" />

    <div class="profile-header">
      <van-image round width="80" height="80" :src="avatar" />
      <div class="profile-name">{{ userStore.userInfo?.realName || '用户' }}</div>
      <div class="profile-role">{{ getRoleName(userStore.userInfo?.role) }}</div>
    </div>

    <van-cell-group inset class="mt-16">
      <van-cell title="用户名" :value="userStore.userInfo?.username" />
      <van-cell title="角色" :value="getRoleName(userStore.userInfo?.role)" />
    </van-cell-group>

    <van-cell-group inset class="mt-16">
      <van-cell title="修改密码" is-link @click="showPassword = true" />
      <van-cell title="关于系统" is-link @click="showAbout = true" />
    </van-cell-group>

    <div class="logout-btn">
      <van-button round block type="danger" @click="handleLogout">
        退出登录
      </van-button>
    </div>

    <van-popup v-model:show="showPassword" position="bottom" round style="height: 40%;">
      <div class="popup-content">
        <div class="page-title">修改密码</div>
        <van-form @submit="handleChangePassword">
          <van-cell-group inset>
            <van-field
              v-model="passwordForm.oldPassword"
              type="password"
              name="oldPassword"
              label="原密码"
              placeholder="请输入原密码"
              required
            />
            <van-field
              v-model="passwordForm.newPassword"
              type="password"
              name="newPassword"
              label="新密码"
              placeholder="请输入新密码"
              required
            />
            <van-field
              v-model="passwordForm.confirmPassword"
              type="password"
              name="confirmPassword"
              label="确认密码"
              placeholder="请再次输入新密码"
              required
            />
          </van-cell-group>
          <div class="form-btn">
            <van-button round block type="primary" native-type="submit">
              确认修改
            </van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <van-popup v-model:show="showAbout" position="bottom" round style="height: 50%;">
      <div class="popup-content">
        <div class="page-title">关于系统</div>
        <div class="about-content">
          <div class="about-logo">
            <van-icon name="calendar-o" size="48" color="#1989fa" />
          </div>
          <h3>智能排课系统</h3>
          <p class="text-muted">版本：1.0.0</p>
          <p class="mt-16">基于遗传算法和贪心算法的智能排课解决方案，支持多校区、多约束条件下的课程调度优化。</p>
          <van-cell-group inset class="mt-16">
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
.profile-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
  text-align: center;
  color: #fff;
}

.profile-name {
  font-size: 20px;
  font-weight: 600;
  margin-top: 16px;
}

.profile-role {
  font-size: 14px;
  opacity: 0.8;
  margin-top: 8px;
}

.logout-btn {
  margin: 24px 16px;
}

.popup-content {
  padding: 20px 16px;
}

.form-btn {
  margin-top: 16px;
  padding: 0 8px;
}

.about-content {
  text-align: center;
  padding: 0 16px;
}

.about-logo {
  width: 80px;
  height: 80px;
  background: #f7f8fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}
</style>
