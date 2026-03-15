<template>
  <n-config-provider :theme="naiveTheme" :locale="zhCN">
    <router-view />
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { zhCN, darkTheme, lightTheme } from 'naive-ui'
import { useThemeStore } from './stores/theme'

const themeStore = useThemeStore()

const naiveTheme = computed(() => {
  return themeStore.isDark() ? darkTheme : lightTheme
})
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  height: 100%;
  width: 100%;
  background-color: var(--bg-secondary);
}
</style>
