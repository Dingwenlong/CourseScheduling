<template>
  <div class="main-layout" :class="{ 'dark-mode': isDarkMode }">
    <div class="main-content" role="main">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    <van-tabbar 
      v-model="active" 
      route 
      class="custom-tabbar"
      role="navigation"
      aria-label="底部导航"
    >
      <van-tabbar-item 
        v-for="(item, index) in tabItems" 
        :key="item.path"
        :to="item.path" 
        :icon="item.icon"
        :aria-label="item.text"
        :aria-current="active === index ? 'page' : null"
        tabindex="0"
        @keydown.enter="handleTabKeydown($event, item.path)"
      >
        {{ item.text }}
      </van-tabbar-item>
      <template #after>
        <button 
          class="tabbar-theme-toggle"
          @click="toggleTheme"
          :aria-label="isDarkMode ? '切换到亮色模式' : '切换到暗色模式'"
          type="button"
        >
          <van-icon :name="isDarkMode ? 'sun-o' : 'moon-o'" size="20" />
        </button>
      </template>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()
const active = ref(0)

const isDarkMode = computed(() => themeStore.isDark())

const tabItems = [
  { path: '/home', icon: 'home-o', text: '首页' },
  { path: '/timetable', icon: 'calendar-o', text: '课表管理' },
  { path: '/task', icon: 'todo-list-o', text: '教学任务' },
  { path: '/schedule', icon: 'search', text: '排课查询' },
  { path: '/statistics', icon: 'chart-trending-o', text: '统计分析' },
  { path: '/profile', icon: 'user-o', text: '个人中心' }
]

const tabMap = {
  '/home': 0,
  '/timetable': 1,
  '/task': 2,
  '/schedule': 3,
  '/statistics': 4,
  '/profile': 5
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
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  box-sizing: border-box;
  padding-right: calc(52px + env(safe-area-inset-right, 0px));
  transition: background-color var(--transition-base), box-shadow var(--transition-base);
}

:deep(.van-tabbar-item) {
  transition: all 0.2s ease;
  min-height: 44px;
  min-width: 44px;
}

:deep(.van-tabbar-item--active) {
  color: var(--primary-color);
  transform: scale(1.05);
}

:deep(.van-tabbar-item__icon) {
  margin-bottom: 2px;
  font-size: 22px;
}

:deep(.van-tabbar-item__text) {
  font-size: 11px;
}

:deep(.van-tabbar-item):focus-visible {
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
