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
          <n-icon size="40" color="var(--primary-color)">
            <CalendarOutline />
          </n-icon>
        </div>
        <h1 class="brand-title">智能排课系统</h1>
        <p class="brand-subtitle">面向高校教学管理的温暖排课工作台</p>
        <div class="brand-caption">
          <div class="caption-line">课程编排更有条理</div>
          <div class="caption-line">调课申请更易追踪</div>
          <div class="caption-line">统计信息更适合教学阅读</div>
        </div>
      </section>

      <section class="auth-panel" aria-label="登录表单">
        <div class="login-card">
          <header class="card-header">
            <h2 class="card-title">登录</h2>
            <p class="card-subtitle">请输入校园账号与密码继续</p>
          </header>

          <div class="form-toolbar">
            <span class="toolbar-text">{{ showDemoAccess ? '演示环境快捷登录' : '校园账号登录' }}</span>
            <n-button
              v-if="showDemoAccess"
              quaternary
              size="small"
              class="demo-btn"
              :disabled="loading"
              @click="fillDemoCredentials"
            >
              填入测试账号
            </n-button>
          </div>

          <n-alert
            v-if="errorMessage"
            ref="errorAlertRef"
            type="error"
            :bordered="false"
            closable
            @close="errorMessage = ''"
            class="alert alert-error"
            role="alert"
            aria-live="polite"
            tabindex="-1"
          >
            {{ errorMessage }}
          </n-alert>

          <n-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="form"
            @submit.prevent="handleLogin"
          >
            <n-form-item path="username" class="field-wrap">
              <label class="field-label" for="login-username">用户名</label>
              <n-input
                v-model:value="form.username"
                id="login-username"
                name="username"
                autocomplete="username"
                placeholder="请输入用户名"
                :disabled="loading"
                size="large"
                class="form-field"
                clearable
                @update:value="errorMessage = ''"
                @blur="normalizeUsername"
                @keydown.enter.prevent="handleLogin"
              >
                <template #prefix>
                  <n-icon class="field-icon">
                    <PersonOutline />
                  </n-icon>
                </template>
              </n-input>
            </n-form-item>

            <n-form-item path="password" class="field-wrap">
              <label class="field-label" for="login-password">密码</label>
              <n-input
                v-model:value="form.password"
                type="password"
                id="login-password"
                name="password"
                autocomplete="current-password"
                placeholder="请输入密码"
                :disabled="loading"
                show-password-on="click"
                size="large"
                class="form-field"
                @update:value="errorMessage = ''"
                @keydown.enter.prevent="handleLogin"
              >
                <template #prefix>
                  <n-icon class="field-icon">
                    <LockClosedOutline />
                  </n-icon>
                </template>
              </n-input>
            </n-form-item>

            <div class="actions">
              <n-button
                block
                type="primary"
                attr-type="button"
                :loading="loading"
                :disabled="loading"
                size="large"
                class="primary-btn"
                @click="handleLogin"
              >
                <template #loading>
                  登录中...
                </template>
                登录
              </n-button>
            </div>

            <div v-if="showDemoAccess" class="login-note">演示环境可使用管理员测试账号快速进入系统。</div>
          </n-form>

          <footer v-if="showDemoAccess" class="card-footer">
            <div class="test-account">测试账号：admin / admin123</div>
          </footer>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { nextTick, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import { NIcon, NAlert, NForm, NFormItem, NInput, NButton } from 'naive-ui'
import { CalendarOutline, PersonOutline, LockClosedOutline } from '@vicons/ionicons5'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()
const formRef = ref(null)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: {
    required: true,
    message: '请输入用户名',
    trigger: 'blur'
  },
  password: {
    required: true,
    message: '请输入密码',
    trigger: 'blur'
  }
}

const loading = ref(false)
const errorMessage = ref('')
const errorAlertRef = ref(null)
const showDemoAccess = import.meta.env.DEV
const demoCredentials = {
  username: 'admin',
  password: 'admin123'
}

const normalizeUsername = () => {
  form.username = form.username.trim()
}

const fillDemoCredentials = () => {
  form.username = demoCredentials.username
  form.password = demoCredentials.password
  errorMessage.value = ''
}

const resolveLoginMessage = (error) => {
  return error?.message || '登录失败，请检查用户名和密码'
}

const handleLogin = async () => {
  if (loading.value) {
    return
  }

  normalizeUsername()

  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }

  errorMessage.value = ''
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    message.success('登录成功')
    await router.push('/home')
  } catch (error) {
    errorMessage.value = resolveLoginMessage(error)
    await nextTick()
    if (errorAlertRef.value && typeof errorAlertRef.value.focus === 'function') {
      errorAlertRef.value.focus()
    }
    message.error(errorMessage.value)
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
  padding: 20px;

  --card: rgba(252, 248, 241, 0.92);
  --card-solid: rgba(255, 251, 245, 0.9);
  --text: #3d352e;
  --muted: rgba(78, 66, 57, 0.72);
  --muted-dark: #7d7064;
  --primary: #768c6a;
  --primary-2: #6f89a3;
  --border: rgba(145, 120, 91, 0.18);
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
    radial-gradient(900px 500px at 12% 12%, rgba(255, 255, 255, 0.54), transparent 52%),
    radial-gradient(1000px 560px at 90% 18%, rgba(198, 144, 84, 0.16), transparent 56%),
    radial-gradient(720px 460px at 55% 92%, rgba(111, 137, 163, 0.12), transparent 60%),
    repeating-linear-gradient(0deg, rgba(121, 98, 72, 0.045) 0 1px, transparent 1px 12px),
    repeating-linear-gradient(90deg, rgba(121, 98, 72, 0.035) 0 1px, transparent 1px 14px),
    linear-gradient(180deg, #f6efe3 0%, #ecdfca 100%);
}

.bg-blob {
  position: absolute;
  border-radius: 999px;
  filter: blur(42px);
  opacity: 0.26;
}

.blob-1 {
  width: 420px;
  height: 420px;
  top: -140px;
  left: -140px;
  background: rgba(233, 211, 180, 0.92);
  animation: float 10s ease-in-out infinite;
}

.blob-2 {
  width: 360px;
  height: 360px;
  bottom: -120px;
  right: -100px;
  background: rgba(111, 137, 163, 0.5);
  animation: float 12s ease-in-out infinite reverse;
}

.blob-3 {
  width: 260px;
  height: 260px;
  top: 45%;
  left: 58%;
  background: rgba(118, 140, 106, 0.44);
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
  max-width: 1180px;
  margin: 0 auto;
  width: 100%;
  min-height: calc(100vh - 40px);
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  align-items: center;
}

.brand-panel {
  color: var(--text);
  text-align: center;
  padding: 12px 10px;
}

.brand-logo {
  width: 72px;
  height: 72px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 18px;
  color: #f7f2e9;
}

.brand-title {
  font-size: clamp(26px, 5vw, 38px);
  font-weight: 700;
  letter-spacing: 0.04em;
  margin: 0;
  white-space: nowrap;
  text-wrap: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.brand-subtitle {
  margin: 12px 0 0;
  font-size: 15px;
  color: var(--muted);
}

.brand-caption {
  margin: 22px auto 0;
  display: grid;
  gap: 10px;
  max-width: 440px;
  padding: 18px 20px;
  text-align: left;
}

.caption-line {
  font-size: 14px;
  color: var(--muted-dark);
  position: relative;
  padding-left: 18px;
}

.caption-line::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.76), var(--primary));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.auth-panel {
  display: flex;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 436px;
  padding: 24px;
}

.card-header {
  text-align: left;
  margin-bottom: 18px;
  min-width: 0;
}

.card-title {
  margin: 0;
  font-size: clamp(22px, 4vw, 24px);
  font-weight: 700;
  color: var(--text);
  letter-spacing: 0.02em;
  white-space: nowrap;
  text-wrap: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--muted-dark);
}

.alert {
  margin: 12px 0 14px;
}

.form-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 2px 10px;
}

.toolbar-text {
  font-size: 12px;
  color: var(--muted-dark);
  letter-spacing: 0.04em;
}

.demo-btn {
  min-width: 118px;
}

.form {
  display: grid;
  gap: 16px;
  width: 100%;
}

.field-wrap {
  display: grid;
  gap: 8px;
  width: 100%;
}

.field-label {
  font-size: 13px;
  color: var(--text);
  font-weight: 600;
  letter-spacing: 0.02em;
}

.field-icon {
  color: rgba(109, 97, 85, 0.74);
}

.field-wrap :deep(.n-input) {
  width: 100%;
  min-height: 46px;
  border-radius: 18px;
  border: 1px solid var(--border);
  background: var(--card-solid);
  padding: 0 12px;
  transition: box-shadow 220ms ease, border-color 220ms ease, transform 220ms ease;
}

.field-wrap :deep(.n-form-item-blank),
.field-wrap :deep(.n-input-wrapper),
.field-wrap :deep(.n-input__input-el) {
  width: 100%;
}

.field-wrap :deep(.n-input-wrapper) {
  min-height: 46px;
  padding-left: 14px;
  padding-right: 14px;
}

@media (hover: hover) {
  .field-wrap :deep(.n-input:hover) {
    border-color: rgba(118, 140, 106, 0.34);
  }
}

.field-wrap:focus-within :deep(.n-input) {
  border-color: rgba(118, 140, 106, 0.54);
  box-shadow: 0 0 0 4px rgba(118, 140, 106, 0.14);
}

.actions {
  margin-top: 4px;
}

.login-note {
  margin-top: -2px;
  font-size: 12px;
  color: var(--muted-dark);
  text-align: center;
}

.primary-btn {
  height: 48px;
  border-radius: 18px;
  font-weight: 700;
  letter-spacing: 0.06em;
  background: linear-gradient(135deg, var(--primary) 0%, #667d5d 100%);
  border: none;
  box-shadow: 0 10px 22px rgba(94, 115, 85, 0.24);
  transition: transform 220ms ease, box-shadow 220ms ease, filter 220ms ease;
}

.primary-btn:hover {
  filter: brightness(1.03);
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(94, 115, 85, 0.3);
}

.primary-btn:active {
  transform: translateY(0);
}

.primary-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(111, 137, 163, 0.16), 0 14px 28px rgba(94, 115, 85, 0.3);
}

.card-footer {
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed rgba(145, 120, 91, 0.2);
}

.test-account {
  font-size: 12px;
  color: rgba(109, 97, 85, 0.82);
  letter-spacing: 0.02em;
}

@media (min-width: 768px) {
  .login-page {
    padding: 28px;
  }

  .login-card {
    padding: 28px;
  }
}

@media (min-width: 1024px) {
  .login-shell {
    grid-template-columns: 1.1fr 0.9fr;
    gap: 42px;
  }

  .brand-panel {
    text-align: left;
    padding: 0;
  }

  .brand-logo {
    margin: 0 0 20px;
  }

  .brand-title {
    font-size: 38px;
  }

  .brand-subtitle {
    font-size: 16px;
    margin-top: 14px;
  }

  .brand-caption {
    margin: 28px 0 0;
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
