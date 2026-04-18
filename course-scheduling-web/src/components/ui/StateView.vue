<template>
  <div class="state-view">
    <div v-if="loading" class="state state-loading" aria-live="polite">
      <n-spin size="medium" />
      <div class="state-text">{{ loadingText }}</div>
    </div>
    <div v-else-if="error" class="state">
      <n-empty :description="errorText">
        <template #extra>
          <n-button v-if="retryable" size="small" type="primary" @click="$emit('retry')">
            重试
          </n-button>
        </template>
      </n-empty>
    </div>
    <div v-else-if="empty" class="state">
      <n-empty :description="emptyText" />
    </div>
    <slot v-else />
  </div>
</template>

<script setup>
import { NSpin, NEmpty, NButton } from 'naive-ui'

defineProps({
  loading: { type: Boolean, default: false },
  empty: { type: Boolean, default: false },
  error: { type: Boolean, default: false },
  loadingText: { type: String, default: '加载中...' },
  emptyText: { type: String, default: '暂无数据' },
  errorText: { type: String, default: '加载失败，请稍后重试' },
  retryable: { type: Boolean, default: true }
})

defineEmits(['retry'])
</script>

<style scoped>
.state-view {
  width: 100%;
}

.state {
  padding: 32px 16px;
  border-radius: var(--radius-xl);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0.08)),
    rgba(255, 250, 243, 0.42);
  border: 1px dashed rgba(145, 120, 91, 0.14);
}

.state-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 200px;
}

.state-text {
  font-size: 13px;
  color: var(--text-secondary);
  letter-spacing: 0.02em;
}

:deep(.state .n-empty) {
  padding: var(--spacing-lg) 0;
}

:deep(.state .n-empty__description) {
  color: var(--text-secondary);
  font-size: 14px;
}

:deep(.state .n-spin-body) {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
