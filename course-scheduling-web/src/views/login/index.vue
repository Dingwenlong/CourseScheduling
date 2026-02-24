<template>
  <div class="login-page">
    <div class="login-bg" aria-hidden="true">
      <div class="bg-blob blob-1"></div>
      <div class="bg-blob blob-2"></div>
      <div class="bg-blob blob-3"></div>
    </div>

    <main class="login-shell">
      <section class="brand-panel" aria-label="品牌信息">
        <div class="brand-logo" aria-hidden="true">
          <van-icon name="calendar-o" size="40" color="#fff" />
        </div>
        <h1 class="brand-title">智能排课系统</h1>
        <p class="brand-subtitle">高效便捷的课程调度平台</p>
        <div class="brand-caption">
          <div class="caption-line">更快创建课表</div>
          <div class="caption-line">更少冲突与调整</div>
          <div class="caption-line">更清晰的数据统计</div>
        </div>
      </section>

      <section class="auth-panel" aria-label="登录表单">
        <div class="login-card">
          <header class="card-header">
            <h2 class="card-title">登录</h2>
            <p class="card-subtitle">请输入账号与密码继续</p>
          </header>

          <div
            v-if="errorMessage"
            ref="errorAlertRef"
            class="alert alert-error"
            role="alert"
            aria-live="polite"
            tabindex="-1"
          >
            <van-icon name="warning-o" class="alert-icon" />
            <div class="alert-content">{{ errorMessage }}</div>
          </div>

          <van-form @submit="handleLogin" class="form">
            <div class="field-wrap">
              <label class="field-label" for="login-username">用户名</label>
              <van-field
                v-model="username"
                id="login-username"
                name="username"
                autocomplete="username"
                placeholder="请输入用户名"
                :disabled="loading"
                :rules="[{ required: true, message: '请输入用户名' }]"
                class="form-field"
                @update:model-value="errorMessage = ''"
              >
                <template #left-icon>
                  <van-icon name="user-o" class="field-icon" />
                </template>
              </van-field>
            </div>

            <div class="field-wrap">
              <label class="field-label" for="login-password">密码</label>
              <van-field
                v-model="password"
                type="password"
                id="login-password"
                name="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                :disabled="loading"
                :rules="[{ required: true, message: '请输入密码' }]"
                class="form-field"
                @update:model-value="errorMessage = ''"
              >
                <template #left-icon>
                  <van-icon name="lock" class="field-icon" />
                </template>
              </van-field>
            </div>

            <div class="actions">
              <van-button
                block
                type="primary"
                native-type="submit"
                :loading="loading"
                loading-text="登录中..."
                :disabled="loading"
                class="primary-btn"
              >
                登录
              </van-button>
            </div>
          </van-form>

          <footer class="card-footer">
            <div class="test-account">测试账号：admin / 123456</div>
          </footer>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')
const errorAlertRef = ref(null)

const handleLogin = async () => {
  errorMessage.value = ''
  loading.value = true
  try {
    await userStore.login(username.value, password.value)
    showToast('登录成功')
    router.push('/home')
  } catch (error) {
    errorMessage.value = '登录失败，请检查用户名和密码'
    await nextTick()
    if (errorAlertRef.value && typeof errorAlertRef.value.focus === 'function') {
      errorAlertRef.value.focus()
    }
    showToast('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  padding: 24px;

  --bg: #0b1220;
  --card: rgba(255, 255, 255, 0.92);
  --card-solid: #ffffff;
  --text: #0f172a;
  --muted: rgba(255, 255, 255, 0.75);
  --muted-dark: #64748b;
  --primary: #2563eb;
  --primary-2: #38bdf8;
  --danger: #dc2626;
  --border: rgba(15, 23, 42, 0.12);
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
  background:
    radial-gradient(1200px 600px at 10% 10%, rgba(56, 189, 248, 0.35), transparent 55%),
    radial-gradient(900px 520px at 90% 20%, rgba(37, 99, 235, 0.35), transparent 55%),
    radial-gradient(1000px 560px at 50% 100%, rgba(16, 185, 129, 0.25), transparent 60%),
    linear-gradient(180deg, #070b14 0%, var(--bg) 100%);
}

.bg-blob {
  position: absolute;
  border-radius: 999px;
  filter: blur(36px);
  opacity: 0.35;
}

.blob-1 {
  width: 520px;
  height: 520px;
  top: -180px;
  left: -220px;
  background: rgba(56, 189, 248, 0.9);
  animation: float 10s ease-in-out infinite;
}

.blob-2 {
  width: 420px;
  height: 420px;
  bottom: -180px;
  right: -160px;
  background: rgba(37, 99, 235, 0.9);
  animation: float 12s ease-in-out infinite reverse;
}

.blob-3 {
  width: 320px;
  height: 320px;
  top: 45%;
  left: 60%;
  background: rgba(16, 185, 129, 0.85);
  animation: pulse 14s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) translateX(0); }
  50% { transform: translateY(-22px) translateX(18px); }
}

@keyframes pulse {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(-16px, -10px) scale(1.08); }
}

.login-shell {
  position: relative;
  z-index: 1;
  max-width: 1120px;
  margin: 0 auto;
  width: 100%;
  min-height: calc(100vh - 48px);
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  align-items: center;
}

.brand-panel {
  color: #fff;
  text-align: center;
  padding: 10px 6px;
}

.brand-logo {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(14px);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.22);
}

.brand-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin: 0;
}

.brand-subtitle {
  margin: 10px 0 0;
  font-size: 14px;
  color: var(--muted);
}

.brand-caption {
  margin: 18px auto 0;
  display: grid;
  gap: 8px;
  max-width: 420px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(14px);
}

.caption-line {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
}

.auth-panel {
  display: flex;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 20px;
  background: var(--card);
  border: 1px solid rgba(255, 255, 255, 0.38);
  backdrop-filter: blur(22px);
  box-shadow:
    0 24px 70px rgba(0, 0, 0, 0.32),
    0 1px 0 rgba(255, 255, 255, 0.25) inset;
  padding: 22px;
}

.card-header {
  text-align: left;
  margin-bottom: 14px;
}

.card-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: -0.01em;
}

.card-subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--muted-dark);
}

.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  border-radius: 14px;
  padding: 12px 12px;
  margin: 12px 0 14px;
  outline: none;
}

.alert-error {
  background: rgba(220, 38, 38, 0.08);
  border: 1px solid rgba(220, 38, 38, 0.22);
  color: #991b1b;
}

.alert-icon {
  margin-top: 2px;
  font-size: 16px;
}

.alert-content {
  font-size: 13px;
  line-height: 1.5;
}

.form {
  display: grid;
  gap: 14px;
}

.field-wrap {
  display: grid;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  color: rgba(15, 23, 42, 0.84);
  font-weight: 600;
}

.field-wrap :deep(.van-cell) {
  padding: 0;
  background: transparent;
}

.field-wrap :deep(.van-field__left-icon) {
  margin-right: 10px;
}

.field-icon {
  color: rgba(15, 23, 42, 0.45);
}

.field-wrap :deep(.van-field__body) {
  min-height: 44px;
  border-radius: 12px;
  border: 1px solid var(--border);
  background: var(--card-solid);
  padding: 0 12px;
  transition: box-shadow 160ms ease, border-color 160ms ease, transform 160ms ease;
}

@media (hover: hover) {
  .field-wrap :deep(.van-field__body):hover {
    border-color: rgba(15, 23, 42, 0.18);
  }
}

.field-wrap:focus-within :deep(.van-field__body) {
  border-color: rgba(37, 99, 235, 0.55);
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.14);
}

.field-wrap :deep(.van-field__control) {
  font-size: 15px;
  color: var(--text);
}

.field-wrap :deep(.van-field__error-message) {
  margin-top: 8px;
  font-size: 12px;
}

.field-wrap :deep(.van-field--disabled .van-field__body) {
  background: rgba(15, 23, 42, 0.03);
}

.actions {
  margin-top: 4px;
}

.primary-btn {
  height: 46px;
  border-radius: 12px;
  font-weight: 700;
  letter-spacing: 0.01em;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-2) 100%);
  border: none;
  box-shadow: 0 10px 26px rgba(37, 99, 235, 0.28);
  transition: transform 160ms ease, box-shadow 160ms ease, filter 160ms ease;
}

.primary-btn:hover {
  filter: brightness(1.02);
  transform: translateY(-1px);
  box-shadow: 0 14px 32px rgba(37, 99, 235, 0.34);
}

.primary-btn:active {
  transform: translateY(0);
}

.primary-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(56, 189, 248, 0.22), 0 14px 32px rgba(37, 99, 235, 0.34);
}

.card-footer {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid rgba(15, 23, 42, 0.06);
}

.test-account {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.58);
}

@media (min-width: 768px) {
  .login-page {
    padding: 32px;
  }

  .login-card {
    padding: 26px;
  }
}

@media (min-width: 1024px) {
  .login-shell {
    grid-template-columns: 1.15fr 0.85fr;
    gap: 28px;
  }

  .brand-panel {
    text-align: left;
    padding: 0;
  }

  .brand-logo {
    margin: 0 0 18px;
    width: 64px;
    height: 64px;
    border-radius: 18px;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-subtitle {
    font-size: 15px;
    margin-top: 12px;
  }

  .brand-caption {
    margin: 22px 0 0;
    max-width: 520px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .bg-blob {
    animation: none;
  }

  .primary-btn {
    transition: none;
  }
}
</style>
