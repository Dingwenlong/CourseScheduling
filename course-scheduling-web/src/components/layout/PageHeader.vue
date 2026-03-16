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
  position: relative;
  padding: 18px 0 14px;
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
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: 0.01em;
  line-height: 1.25;
  transition: color var(--transition-base);
}

.page-title .subtitle {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-top: 8px;
  letter-spacing: 0;
}

.page-header-description {
  margin-top: var(--spacing-md);
  color: var(--text-secondary);
  font-size: 14px;
  max-width: 720px;
  line-height: 1.75;
}

.page-header::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background:
    linear-gradient(90deg, transparent, rgba(145, 120, 91, 0.26), transparent);
}

@media (min-width: 1024px) {
  .page-header {
    padding: 22px 0 18px;
    margin-bottom: var(--spacing-xl);
  }
  
  .page-title {
    font-size: 30px;
  }
}

@media (min-width: 1440px) {
  .page-header {
    padding: 26px 0 22px;
    margin-bottom: var(--spacing-2xl);
  }
  
  .page-title {
    font-size: 34px;
  }
}

@media (min-width: 2560px) {
  .page-title {
    font-size: 36px;
  }
}

@media (max-width: 767px) {
  .page-header {
    padding: 14px 0 12px;
    margin-bottom: var(--spacing-lg);
  }
  
  .page-title {
    font-size: 22px;
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
    font-size: 20px;
  }
}
</style>
