<template>
  <div class="login-page">
    <div class="login-header">
      <div class="login-logo">
        <van-icon name="calendar-o" size="48" color="#1989fa" />
      </div>
      <h1 class="login-title">智能排课系统</h1>
      <p class="login-subtitle">高效便捷的课程调度平台</p>
    </div>

    <div class="login-form">
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="username"
            name="username"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
          >
            <template #left-icon>
              <van-icon name="user-o" />
            </template>
          </van-field>
          <van-field
            v-model="password"
            type="password"
            name="password"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          >
            <template #left-icon>
              <van-icon name="lock" />
            </template>
          </van-field>
        </van-cell-group>

        <div class="login-btn-wrapper">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
          >
            登录
          </van-button>
        </div>
      </van-form>
    </div>

    <div class="login-footer">
      <p>测试账号：admin / 123456</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)

const handleLogin = async () => {
  loading.value = true
  try {
    await userStore.login(username.value, password.value)
    showToast('登录成功')
    router.push('/home')
  } catch (error) {
    showToast('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-logo {
  width: 80px;
  height: 80px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.login-title {
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.login-subtitle {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.login-form {
  background: #fff;
  border-radius: 16px;
  padding: 24px 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.login-btn-wrapper {
  margin-top: 24px;
  padding: 0 8px;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}
</style>
