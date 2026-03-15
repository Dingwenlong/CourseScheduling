<template>
  <div class="main-layout" :class="{ 'dark-mode': isDarkMode }">
    <div class="main-content" role="main">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    <div 
      class="custom-tabbar"
      role="navigation"
      aria-label="底部导航"
    >
      <router-link
        v-for="(item, index) in tabItems" 
        :key="item.path"
        :to="item.path" 
        class="tabbar-item touch-target"
        :class="{ active: isActive(item.path) }"
        :aria-label="item.text"
        :aria-current="isActive(item.path) ? 'page' : null"
        tabindex="0"
        @keydown.enter="handleTabKeydown($event, item.path)"
      >
        <n-icon size="22">
          <component :is="item.icon" />
        </n-icon>
        <span class="tabbar-text">{{ item.text }}</span>
      </router-link>
      <button 
        class="tabbar-theme-toggle"
        @click="toggleTheme"
        :aria-label="isDarkMode ? '切换到亮色模式' : '切换到暗色模式'"
        type="button"
      >
        <n-icon size="20">
          <component :is="isDarkMode ? SunnyOutline : MoonOutline" />
        </n-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { NIcon } from 'naive-ui'
import {
  HomeOutline,
  CalendarOutline,
  ClipboardOutline,
  SearchOutline,
  BarChartOutline,
  PersonOutline,
  MoonOutline,
  SunnyOutline
} from '@vicons/ionicons5'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const active = ref(0)

const isDarkMode = computed(() => themeStore.isDark())

const tabItems = [
  { path: '/home', icon: HomeOutline, text: '首页' },
  { path: '/timetable', icon: CalendarOutline, text: '课表管理' },
  { path: '/task', icon: ClipboardOutline, text: '教学任务' },
  { path: '/schedule', icon: SearchOutline, text: '排课查询' },
  { path: '/statistics', icon: BarChartOutline, text: '统计分析' },
  { path: '/profile', icon: PersonOutline, text: '个人中心' }
]

const tabMap = {
  '/home': 0,
  '/timetable': 1,
  '/task': 2,
  '/schedule': 3,
  '/statistics': 4,
  '/profile': 5
}

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

watch(() => route.path, (path) => {
  active.value = tabMap[path] || 0
}, { immediate: true })

const toggleTheme = () => {
  themeStore.toggleTheme()
}

const handleTabKeydown = (event, path) => {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    router.push(path)
  }
}

const handleGlobalKeydown = (event) => {
  if (event.altKey && event.key === 'd') {
    event.preventDefault()
    toggleTheme()
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleGlobalKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<style scoped>
.main-layout {
  height: 100%;
  overflow-y: hidden;
  background-color: var(--bg-secondary);
  transition: background-color var(--transition-base);
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 60px;
}

.custom-tabbar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  box-sizing: border-box;
  padding-right: calc(52px + env(safe-area-inset-right, 0px));
  padding-left: 4px;
  padding-right: calc(56px + env(safe-area-inset-right, 0px));
  padding-top: 6px;
  padding-bottom: calc(6px + env(safe-area-inset-bottom, 0px));
  transition: background-color var(--transition-base), box-shadow var(--transition-base);
  position: relative;
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 2px;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s ease;
  min-height: 44px;
  min-width: 44px;
}

.tabbar-item:hover {
  color: var(--text-primary);
}

.tabbar-item.active {
  color: var(--primary-color);
  transform: scale(1.05);
}

.tabbar-text {
  font-size: 11px;
}

.tabbar-item:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
  border-radius: var(--radius-md);
}

.tabbar-theme-toggle {
  position: absolute;
  right: calc(8px + env(safe-area-inset-right, 0px));
  top: 50%;
  transform: translateY(-50%);
  background: var(--bg-secondary);
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
  z-index: 2;
}

.tabbar-theme-toggle:hover {
  background: var(--bg-tertiary);
  color: var(--primary-color);
}

.tabbar-theme-toggle:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all var(--transition-base);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (min-width: 1024px) {
  .main-layout {
    display: none;
  }
}

[data-theme="dark"] .custom-tabbar {
  background: rgba(31, 41, 55, 0.95);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.3);
}

[data-theme="dark"] .tabbar-theme-toggle {
  background: var(--bg-tertiary);
}
</style>
