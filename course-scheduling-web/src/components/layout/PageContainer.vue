<template>
  <div class="page-container" :class="{ 'with-tabbar': withTabbar }">
    <slot />
  </div>
</template>

<script setup>
defineProps({
  withTabbar: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.page-container {
  min-height: 100%;
  padding: clamp(14px, 2vw, 24px);
  padding-bottom: var(--spacing-3xl);
  background: transparent;
  position: relative;
  overflow-x: hidden;
  transition: background-color var(--transition-base);
}

.page-container.with-tabbar {
  padding-bottom: calc(70px + env(safe-area-inset-bottom, 0px) + 20px);
}

.page-container::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 32px;
  background:
    radial-gradient(circle at top left, rgba(255, 255, 255, 0.35), transparent 24%),
    radial-gradient(circle at bottom right, rgba(190, 168, 134, 0.12), transparent 24%);
  pointer-events: none;
  opacity: 0.7;
}

@media (min-width: 1024px) {
  .page-container {
    padding: clamp(20px, 2vw, 30px);
    padding-bottom: var(--spacing-3xl);
  }
}

@media (min-width: 1440px) {
  .page-container {
    padding: clamp(24px, 2.2vw, 38px);
    padding-bottom: var(--spacing-4xl);
    max-width: var(--content-max-width-wide);
    margin: 0 auto;
  }
}

@media (min-width: 1920px) {
  .page-container {
    max-width: var(--content-max-width-ultra);
    padding: var(--spacing-3xl);
  }
}

@media (min-width: 2560px) {
  .page-container {
    max-width: var(--content-max-width-super);
    padding: var(--spacing-4xl);
  }
}

@media (max-width: 767px) {
  .page-container {
    padding: 14px;
    padding-bottom: var(--spacing-2xl);
  }
}

@media (max-width: 479px) {
  .page-container {
    padding: 12px;
    padding-bottom: var(--spacing-xl);
  }
}
</style>
