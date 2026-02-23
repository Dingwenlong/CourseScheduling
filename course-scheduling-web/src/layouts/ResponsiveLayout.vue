<template>
  <component :is="currentLayout">
    <router-view />
  </component>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import DesktopLayout from './DesktopLayout.vue'
import MainLayout from './MainLayout.vue'

const currentLayout = ref(null)
const resizeTimeout = ref(null)

const checkScreenSize = () => {
  if (resizeTimeout.value) {
    clearTimeout(resizeTimeout.value)
  }
  
  resizeTimeout.value = setTimeout(() => {
    currentLayout.value = window.innerWidth >= 1024 ? DesktopLayout : MainLayout
  }, 100)
}

onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
  window.addEventListener('orientationchange', checkScreenSize)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
  window.removeEventListener('orientationchange', checkScreenSize)
  if (resizeTimeout.value) {
    clearTimeout(resizeTimeout.value)
  }
})
</script>
