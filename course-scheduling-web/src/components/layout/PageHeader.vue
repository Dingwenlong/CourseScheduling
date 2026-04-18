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
  padding: 20px 0 18px;
  margin-bottom: var(--spacing-xl);
}

.page-header::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: calc(100% - 6px);
  border-radius: calc(var(--radius-xl) - 4px);
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.22), rgba(255, 255, 255, 0)),
    radial-gradient(circle at top right, rgba(184, 102, 89, 0.08), transparent 34%),
    radial-gradient(circle at top left, rgba(126, 149, 99, 0.08), transparent 30%);
  pointer-events: none;
  opacity: 0.78;
}

.page-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-xl);
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.page-header-main {
  flex: 1;
  min-width: 0;
  display: grid;
  gap: 6px;
}

.page-header-actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-shrink: 0;
  padding: 8px;
  border-radius: var(--radius-full);
  background: rgba(255, 250, 243, 0.54);
  border: 1px solid rgba(145, 120, 91, 0.12);
  box-shadow: var(--shadow-xs);
}

.page-title {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: 0.01em;
  line-height: 1.18;
  transition: color var(--transition-base);
}

.page-title .subtitle {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-top: 0;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  background: rgba(255, 248, 238, 0.72);
  border: 1px solid rgba(145, 120, 91, 0.12);
  letter-spacing: 0;
}

.page-title .subtitle::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-gradient);
  box-shadow: 0 0 0 4px rgba(118, 140, 106, 0.12);
  flex-shrink: 0;
}

.page-header-description {
  margin-top: var(--spacing-md);
  padding-left: 2px;
  color: var(--text-secondary);
  font-size: 14px;
  max-width: 760px;
  line-height: 1.75;
  position: relative;
  z-index: 1;
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
    padding: 24px 0 20px;
    margin-bottom: var(--spacing-xl);
  }
  
  .page-title {
    font-size: 32px;
  }
}

@media (min-width: 1440px) {
  .page-header {
    padding: 28px 0 24px;
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
    gap: var(--spacing-md);
  }
  
  .page-header-actions {
    width: 100%;
    justify-content: flex-start;
    padding: 6px;
    border-radius: var(--radius-lg);
    flex-wrap: wrap;
  }

  .page-title .subtitle {
    max-width: 100%;
    line-height: 1.5;
  }
}

@media (max-width: 479px) {
  .page-title {
    font-size: 20px;
  }
}
</style>
