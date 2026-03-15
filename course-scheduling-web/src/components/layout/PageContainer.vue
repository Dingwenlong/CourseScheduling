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
  padding: var(--spacing-lg);
  padding-bottom: var(--spacing-2xl);
  background-color: var(--bg-secondary);
  position: relative;
  overflow-x: hidden;
  transition: background-color var(--transition-base);
}

.page-container.with-tabbar {
  padding-bottom: 70px;
}

@media (min-width: 1024px) {
  .page-container {
    padding: var(--spacing-xl);
    padding-bottom: var(--spacing-2xl);
  }
}

@media (min-width: 1440px) {
  .page-container {
    padding: var(--spacing-2xl);
    padding-bottom: var(--spacing-3xl);
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
    padding: var(--spacing-md);
    padding-bottom: var(--spacing-xl);
  }
}

@media (max-width: 479px) {
  .page-container {
    padding: var(--spacing-sm);
    padding-bottom: var(--spacing-lg);
  }
}
</style>
