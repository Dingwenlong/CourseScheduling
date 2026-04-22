<template>
  <component :is="currentLayout">
    <router-view />
  </component>
</template>

<script setup>
import { markRaw, onMounted, onUnmounted, ref, shallowRef } from 'vue'
import DesktopLayout from './DesktopLayout.vue'
import MainLayout from './MainLayout.vue'

const desktopLayout = markRaw(DesktopLayout)
const mobileLayout = markRaw(MainLayout)
const currentLayout = shallowRef(desktopLayout)
const resizeTimeout = ref(null)

const checkScreenSize = () => {
  if (resizeTimeout.value) {
    clearTimeout(resizeTimeout.value)
  }
  
  resizeTimeout.value = setTimeout(() => {
    currentLayout.value = window.innerWidth >= 1024 ? desktopLayout : mobileLayout
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
