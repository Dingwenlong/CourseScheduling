<template>
  <div class="desktop-layout" :class="{ 'dark-mode': isDarkMode }">
    <aside 
      class="sidebar" 
      :class="{ 
        collapsed: isCollapsed, 
        mobile: isMobile,
        'sidebar-expanded': isMobile && !isCollapsed 
      }"
      role="navigation"
      :aria-label="isCollapsed ? '折叠的导航菜单' : '主导航菜单'"
      :aria-expanded="!isCollapsed"
    >
      <div class="sidebar-header">
        <div class="logo">
          <van-icon name="calendar-o" size="28" color="var(--primary-color)" aria-hidden="true" />
          <span v-show="!isCollapsed" class="logo-text">排课系统</span>
        </div>
        <button 
          v-if="!isCollapsed && !isMobile" 
          class="collapse-icon touch-target icon-button"
          @click="toggleSidebar"
          :aria-label="'收起侧边栏'"
          aria-expanded="true"
          type="button"
        >
          <van-icon name="arrow-left" size="20" aria-hidden="true" />
        </button>
      </div>
      
      <nav class="sidebar-nav" role="menubar" aria-label="主导航">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path" 
          :to="item.path" 
          class="nav-item touch-target"
          :class="{ active: isActive(item.path) }"
          @click="handleNavClick"
          role="menuitem"
          :aria-current="isActive(item.path) ? 'page' : null"
          :aria-label="item.text"
        >
          <van-icon :name="item.icon" size="20" aria-hidden="true" />
          <span v-show="!isCollapsed" class="nav-text">{{ item.text }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer" :class="{ 'footer-with-user': !isCollapsed }">
        <button 
          v-show="!isCollapsed"
          class="theme-toggle touch-target icon-button"
          @click="toggleTheme"
          :aria-label="isDarkMode ? '切换到亮色模式' : '切换到暗色模式'"
          type="button"
        >
          <van-icon :name="isDarkMode ? 'sun-o' : 'moon-o'" size="18" />
          <span class="theme-text">{{ isDarkMode ? '亮色' : '暗色' }}</span>
        </button>
        <button 
          v-if="isCollapsed && !isMobile"
          class="theme-toggle-collapsed touch-target icon-button"
          @click="toggleTheme"
          :aria-label="isDarkMode ? '切换到亮色模式' : '切换到暗色模式'"
          type="button"
        >
          <van-icon :name="isDarkMode ? 'sun-o' : 'moon-o'" size="20" aria-hidden="true" />
        </button>
        <div v-show="!isCollapsed" class="user-info">
          <van-icon name="user-o" size="20" aria-hidden="true" />
          <span class="user-name">{{ userStore.userInfo?.realName || '用户' }}</span>
        </div>
        <button 
          v-if="isCollapsed && !isMobile" 
          class="expand-icon touch-target icon-button"
          @click="toggleSidebar"
          :aria-label="'展开侧边栏'"
          aria-expanded="false"
          type="button"
        >
          <van-icon name="bars" size="20" aria-hidden="true" />
        </button>
      </div>
    </aside>

    <div 
      v-if="isMobile && showOverlay" 
      class="sidebar-overlay" 
      @click="closeSidebar"
      role="presentation"
      aria-hidden="true"
    ></div>

    <main class="main-content" role="main">
      <header class="top-header" role="banner">
        <div class="header-left">
          <button 
            v-if="isMobile"
            class="menu-toggle touch-target icon-button"
            @click="toggleSidebar"
            :aria-label="'打开导航菜单'"
            :aria-expanded="showOverlay"
            type="button"
          >
            <van-icon name="bars" size="24" aria-hidden="true" />
          </button>
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        <div class="header-right">
          <button 
            v-if="isMobile"
            class="theme-toggle-mobile touch-target icon-button"
            @click="toggleTheme"
            :aria-label="isDarkMode ? '切换到亮色模式' : '切换到暗色模式'"
            type="button"
          >
            <van-icon :name="isDarkMode ? 'sun-o' : 'moon-o'" size="22" />
          </button>
          <van-button 
            v-if="layoutStore.headerAction.visible"
            :icon="layoutStore.headerAction.icon" 
            type="primary" 
            size="small"
            class="touch-target"
            @click="layoutStore.headerAction.onClick"
            :aria-label="layoutStore.headerAction.text"
          >
            {{ layoutStore.headerAction.text }}
          </van-button>
        </div>
      </header>

      <div class="content-wrapper" role="region" aria-label="主要内容">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { useLayoutStore } from '@/stores/layout'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const themeStore = useThemeStore()
const layoutStore = useLayoutStore()
const userStore = useUserStore()
const isCollapsed = ref(false)
const isMobile = ref(false)
const showOverlay = ref(false)
const resizeTimeout = ref(null)

const isDarkMode = computed(() => themeStore.isDark())
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

const menuItems = computed(() => {
  const items = [
    { path: '/home', icon: 'home-o', text: '首页' },
    { path: '/timetable', icon: 'calendar-o', text: '课表管理' },
    { path: '/task', icon: 'todo-list-o', text: '教学任务' },
    { path: '/schedule', icon: 'search', text: '排课查询' },
    { path: '/statistics', icon: 'chart-trending-o', text: '统计分析' }
  ]

  if (isAdmin.value) {
    items.push({ path: '/users', icon: 'friends-o', text: '用户管理' })
  }

  items.push({ path: '/profile', icon: 'user-o', text: '个人中心' })

  return items
})

const pageTitle = computed(() => {
  const item = menuItems.value.find(m => route.path.startsWith(m.path))
  return item ? item.text : '排课系统'
})

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}

const checkMobile = () => {
  if (resizeTimeout.value) {
    clearTimeout(resizeTimeout.value)
  }
  
  resizeTimeout.value = setTimeout(() => {
    const wasMobile = isMobile.value
    isMobile.value = window.innerWidth < 1024
    
    if (isMobile.value && !wasMobile) {
      isCollapsed.value = true
      showOverlay.value = false
    } else if (!isMobile.value && wasMobile) {
      isCollapsed.value = false
      showOverlay.value = false
    }
  }, 100)
}

const toggleSidebar = () => {
  if (isMobile.value) {
    showOverlay.value = !showOverlay.value
    if (showOverlay.value) {
      isCollapsed.value = false
    }
  } else {
    isCollapsed.value = !isCollapsed.value
  }
}

const closeSidebar = () => {
  showOverlay.value = false
  if (isMobile.value) {
    isCollapsed.value = true
  }
}

const handleNavClick = () => {
  if (isMobile.value) {
    closeSidebar()
  }
}

const toggleTheme = () => {
  themeStore.toggleTheme()
}

const handleKeydown = (event) => {
  if (event.key === 'Escape' && showOverlay.value) {
    closeSidebar()
  }
  
  if (event.altKey && event.key === 'b') {
    event.preventDefault()
    toggleSidebar()
  }
  
  if (event.altKey && event.key === 'd') {
    event.preventDefault()
    toggleTheme()
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('orientationchange', checkMobile)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('orientationchange', checkMobile)
  window.removeEventListener('keydown', handleKeydown)
  if (resizeTimeout.value) {
    clearTimeout(resizeTimeout.value)
  }
})
</script>

<style scoped>
.desktop-layout {
  display: flex;
  height: 100%;
  background: var(--bg-secondary);
  transition: background-color var(--transition-base);
}

.icon-button {
  background: transparent;
  border: none;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.icon-button:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.icon-button:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
}

.sidebar {
  width: 240px;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-base), transform var(--transition-base), background-color var(--transition-base), border-color var(--transition-base);
  flex-shrink: 0;
  position: relative;
  z-index: 100;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar.mobile {
  position: fixed;
  top: 0;
  left: 0;
  height: 100%;
  transform: translateX(-100%);
  z-index: 1000;
  width: 280px;
}

.sidebar.mobile.sidebar-expanded {
  transform: translateX(0);
  box-shadow: var(--shadow-xl);
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
  transition: border-color var(--transition-base);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color var(--transition-base);
}

.collapse-icon,
.expand-icon {
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.collapse-icon:hover,
.expand-icon:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.sidebar-nav {
  flex: 1;
  padding: var(--spacing-lg) 0;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  margin: 0 var(--spacing-md);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  transition: all var(--transition-fast);
  white-space: nowrap;
  overflow: hidden;
  position: relative;
}

.nav-item:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
}

.nav-item:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
  transform: translateX(4px);
}

.nav-item.active {
  background: rgba(81, 202, 186, 0.14);
  color: var(--text-primary);
  border: 1px solid rgba(81, 202, 186, 0.25);
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: var(--primary-color);
  border-radius: 0 4px 4px 0;
  transition: height var(--transition-fast);
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: var(--spacing-md);
  margin: 0 var(--spacing-sm);
}

.sidebar.collapsed .nav-item:hover {
  transform: none;
}

.sidebar.collapsed:not(.mobile) .nav-text {
  display: none;
}


.sidebar-footer {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: border-color var(--transition-base);
}

.sidebar.collapsed .sidebar-footer {
  padding: var(--spacing-md);
}

.sidebar-footer.footer-with-user {
  align-items: flex-start;
}

.theme-toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  width: 86%;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-secondary);
}

.theme-toggle-collapsed {
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 9999px;
  background: var(--primary-gradient);
  color: #ffffff;
  border: 1px solid rgba(81, 202, 186, 0.35);
  box-shadow: var(--shadow-sm);
}

.theme-toggle-collapsed:hover {
  filter: brightness(1.05);
  box-shadow: var(--shadow-md);
}

.sidebar-footer .expand-icon {
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 9999px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.sidebar-footer .expand-icon:hover {
  background: var(--primary-gradient);
  color: #ffffff;
  border-color: transparent;
  box-shadow: var(--shadow-md);
}

.theme-toggle:hover {
  background: var(--bg-tertiary);
  color: var(--primary-color);
}

.theme-text {
  font-weight: 500;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
  width: 100%;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.top-header {
  height: 64px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-xl);
  flex-shrink: 0;
  transition: background-color var(--transition-base), border-color var(--transition-base);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  min-width: 0;
}

.menu-toggle {
  color: var(--text-secondary);
  cursor: pointer;
  padding: 8px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.menu-toggle:hover {
  background: var(--bg-secondary);
  color: var(--primary-color);
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--transition-base);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-shrink: 0;
}

.theme-toggle-mobile {
  color: var(--text-secondary);
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--spacing-xl);
  background: var(--bg-secondary);
  height: 100%;
}

.content-wrapper::-webkit-scrollbar {
  width: 6px;
}

.content-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.content-wrapper::-webkit-scrollbar-thumb {
  background: transparent;
  border-radius: 3px;
  transition: background 0.2s ease;
}

.content-wrapper:hover::-webkit-scrollbar-thumb {
  background: var(--border-color);
}

.content-wrapper:hover::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
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

@media (max-width: 1023px) {
  .desktop-layout {
    display: none;
  }
}

@media (min-width: 1024px) and (max-width: 1439px) {
  .sidebar {
    width: 200px;
  }
  
  .sidebar.collapsed {
    width: 64px;
  }
  
  .content-wrapper {
    padding: var(--spacing-lg);
  }
}

@media (min-width: 1440px) {
  .sidebar {
    width: 260px;
  }
  
  .sidebar.collapsed {
    width: 72px;
  }
  
  .content-wrapper {
    padding: var(--spacing-2xl);
    max-width: 95%; /* Use percentage for better adaptation */
    width: var(--content-max-width-wide);
    margin: 0 auto;
  }
}

@media (min-width: 1920px) {
  .sidebar {
    width: 300px;
  }
  
  .sidebar.collapsed {
    width: 80px;
  }
  
  .content-wrapper {
    padding: var(--spacing-3xl);
    max-width: 92%;
    width: var(--content-max-width-ultra);
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .nav-text {
    font-size: 16px;
  }
}

@media (min-width: 2560px) {
  .sidebar {
    width: 360px;
  }
  
  .sidebar.collapsed {
    width: 90px;
  }
  
  .content-wrapper {
    padding: var(--spacing-4xl);
    max-width: 90%;
    width: var(--content-max-width-super);
  }
  
  .page-title {
    font-size: 26px;
  }
  
  .nav-text {
    font-size: 18px;
  }

  .logo-text {
    font-size: 20px;
  }

  .top-header {
    height: 80px;
  }
}

@media (min-width: 1024px) {
  .sidebar-overlay {
    display: none;
  }
  
  .theme-toggle-mobile {
    display: none;
  }
}
</style>
