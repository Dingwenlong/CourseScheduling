<template>
  <n-config-provider :theme="naiveTheme" :theme-overrides="themeOverrides" :locale="zhCN">
    <n-dialog-provider>
      <n-notification-provider>
        <n-message-provider>
          <router-view />
        </n-message-provider>
      </n-notification-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import {
  NConfigProvider,
  NDialogProvider,
  NMessageProvider,
  NNotificationProvider,
  zhCN,
  darkTheme,
  lightTheme
} from 'naive-ui'
import { useThemeStore } from './stores/theme'
import { createNaiveThemeOverrides } from './styles/naive-theme'

const themeStore = useThemeStore()

const naiveTheme = computed(() => {
  return themeStore.isDark() ? darkTheme : lightTheme
})

const themeOverrides = computed(() => createNaiveThemeOverrides(themeStore.isDark()))
</script>

<style>
#app {
  min-height: 100%;
  width: 100%;
  color: var(--text-primary);
  background: transparent;
}
</style>
