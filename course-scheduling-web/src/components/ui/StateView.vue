<template>
  <div class="state-view">
    <div v-if="loading" class="state state-loading" aria-live="polite">
      <van-loading size="24" />
      <div class="state-text">{{ loadingText }}</div>
    </div>
    <div v-else-if="error" class="state">
      <van-empty :description="errorText" />
      <div class="state-actions">
        <van-button v-if="retryable" size="small" type="primary" class="touch-target" @click="$emit('retry')">
          重试
        </van-button>
      </div>
    </div>
    <div v-else-if="empty" class="state">
      <van-empty :description="emptyText" />
    </div>
    <slot v-else />
  </div>
</template>

<script setup>
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
  padding: 28px 12px;
}

.state-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 160px;
}

.state-text {
  font-size: 13px;
  color: var(--text-secondary);
}

.state-actions {
  display: flex;
  justify-content: center;
  padding: 10px 0 4px;
}
</style>

