<template>
  <div class="login-page">
    <div class="login-background">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    
    <div class="login-content">
      <div class="login-header">
        <div class="login-logo">
          <van-icon name="calendar-o" size="56" color="#fff" />
        </div>
        <h1 class="login-title">智能排课系统</h1>
        <p class="login-subtitle">高效便捷的课程调度平台</p>
      </div>

      <div class="login-form">
        <van-form @submit="handleLogin">
          <van-cell-group inset class="form-group">
            <van-field
              v-model="username"
              name="username"
              placeholder="请输入用户名"
              :rules="[{ required: true, message: '请输入用户名' }]"
              class="form-field"
            >
              <template #left-icon>
                <van-icon name="user-o" color="#9ca3af" />
              </template>
            </van-field>
            <van-field
              v-model="password"
              type="password"
              name="password"
              placeholder="请输入密码"
              :rules="[{ required: true, message: '请输入密码' }]"
              class="form-field"
            >
              <template #left-icon>
                <van-icon name="lock" color="#9ca3af" />
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
              class="login-btn"
            >
              <span v-if="!loading">登录</span>
            </van-button>
          </div>
        </van-form>
      </div>

      <div class="login-footer">
        <p>测试账号：admin / 123456</p>
      </div>
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
  padding: 24px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #66fff3 0%, #51caba 100%);
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
  background: white;
}

.shape-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  right: -100px;
  animation: float 6s ease-in-out infinite;
}

.shape-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  left: -50px;
  animation: float 8s ease-in-out infinite reverse;
}

.shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse 10s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.1); }
}

.login-content {
  position: relative;
  z-index: 1;
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 48px;
}

.login-logo {
  width: 96px;
  height: 96px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  animation: scaleIn 0.5s ease-out 0.2s backwards;
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.login-title {
  color: #fff;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -0.02em;
  animation: fadeIn 0.5s ease-out 0.3s backwards;
}

.login-subtitle {
  color: rgba(255, 255, 255, 0.85);
  font-size: 15px;
  animation: fadeIn 0.5s ease-out 0.4s backwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.login-form {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 32px 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  animation: fadeIn 0.5s ease-out 0.5s backwards;
}

.form-group {
  border-radius: 16px;
  overflow: hidden;
}

.form-field :deep(.van-field__control) {
  font-size: 16px;
}

.form-field :deep(.van-field__left-icon) {
  margin-right: 12px;
}

.login-btn-wrapper {
  margin-top: 32px;
  padding: 0 8px;
}

.login-btn {
  height: 52px;
  font-size: 17px;
  font-weight: 600;
  background: linear-gradient(135deg, #66fff3 0%, #51caba 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(81, 202, 186, 0.4);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(81, 202, 186, 0.5);
}

.login-btn:active {
  transform: translateY(0);
}

.login-footer {
  text-align: center;
  margin-top: 32px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  animation: fadeIn 0.5s ease-out 0.6s backwards;
}

.login-footer p {
  background: rgba(255, 255, 255, 0.15);
  padding: 8px 16px;
  border-radius: 20px;
  display: inline-block;
}

@media (min-width: 768px) {
  .login-content {
    max-width: 440px;
    margin: 0 auto;
  }
  
  .login-form {
    padding: 40px 32px;
  }
}
</style>
