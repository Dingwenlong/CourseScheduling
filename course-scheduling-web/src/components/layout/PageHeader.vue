<template>
  <div class="page-header">
    <div class="page-header-content">
      <div class="page-header-main">
        <h1 class="page-title">
          {{ title }}
          <span v-if="subtitle" class="subtitle">{{ subtitle }}</span>
        </h1>
      </div>
      <div v-if="$slots.actions || $slots.default" class="page-header-actions">
        <slot name="actions">
          <slot />
        </slot>
      </div>
    </div>
    <div v-if="$slots.description" class="page-header-description">
      <slot name="description" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.page-header {
  padding: var(--spacing-lg) 0;
  margin-bottom: var(--spacing-lg);
}

.page-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-lg);
  flex-wrap: wrap;
}

.page-header-main {
  flex: 1;
  min-width: 0;
}

.page-header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-shrink: 0;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.02em;
  line-height: 1.3;
  transition: color var(--transition-base);
}

.page-title .subtitle {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-top: var(--spacing-xs);
  letter-spacing: 0;
}

.page-header-description {
  margin-top: var(--spacing-md);
  color: var(--text-secondary);
  font-size: 14px;
}

@media (min-width: 1024px) {
  .page-header {
    padding: var(--spacing-xl) 0;
    margin-bottom: var(--spacing-xl);
  }
  
  .page-title {
    font-size: 28px;
  }
}

@media (min-width: 1440px) {
  .page-header {
    padding: var(--spacing-2xl) 0;
    margin-bottom: var(--spacing-2xl);
  }
  
  .page-title {
    font-size: 32px;
  }
}

@media (min-width: 2560px) {
  .page-header {
    margin-bottom: var(--spacing-2xl);
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: var(--spacing-2xl);
  }
}

@media (max-width: 767px) {
  .page-header {
    padding: var(--spacing-md) 0;
    margin-bottom: var(--spacing-lg);
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .page-header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 479px) {
  .page-title {
    font-size: 18px;
  }
}
</style>
