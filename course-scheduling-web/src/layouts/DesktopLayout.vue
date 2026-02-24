<template>
  <div class="desktop-layout">
    <aside 
      class="sidebar" 
      :class="{ 
        collapsed: isCollapsed, 
        mobile: isMobile,
        'sidebar-expanded': isMobile && !isCollapsed 
      }"
    >
      <div class="sidebar-header">
        <div class="logo">
          <van-icon name="calendar-o" size="28" color="var(--primary-color)" />
          <span v-show="!isCollapsed" class="logo-text">排课系统</span>
        </div>
        <van-icon 
          v-if="!isCollapsed && !isMobile" 
          name="arrow-left" 
          size="20" 
          class="collapse-icon touch-target" 
          @click="toggleSidebar"
        />
      </div>
      
      <nav class="sidebar-nav">
        <router-link 
          v-for="item in menuItems" 
          :key="item.path" 
          :to="item.path" 
          class="nav-item touch-target"
          :class="{ active: isActive(item.path) }"
          @click="handleNavClick"
        >
          <van-icon :name="item.icon" size="20" />
          <span v-show="!isCollapsed" class="nav-text">{{ item.text }}</span>
          <van-icon 
            v-if="isCollapsed && !isMobile" 
            :name="item.icon" 
            size="20" 
            class="nav-icon-only"
          />
        </router-link>
      </nav>

      <div class="sidebar-footer" :class="{ 'footer-with-user': !isCollapsed }">
        <div v-show="!isCollapsed" class="user-info">
          <van-icon name="user-o" size="20" />
          <span class="user-name">管理员</span>
        </div>
        <van-icon 
          v-if="isCollapsed && !isMobile" 
          name="arrow-right" 
          size="20" 
          class="expand-icon touch-target" 
          @click="toggleSidebar"
        />
      </div>
    </aside>

    <div v-if="isMobile && showOverlay" class="sidebar-overlay" @click="closeSidebar"></div>

    <main class="main-content">
      <header class="top-header">
        <div class="header-left">
          <van-icon 
            v-if="isMobile"
            name="bars" 
            size="24" 
            class="menu-toggle touch-target" 
            @click="toggleSidebar"
          />
          <h1 class="page-title">{{ pageTitle }}</h1>
        </div>
        <div class="header-right">
          <van-button 
            icon="setting-o" 
            type="primary" 
            size="small"
            class="touch-target"
          >
            设置
          </van-button>
        </div>
      </header>

      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isCollapsed = ref(false)
const isMobile = ref(false)
const showOverlay = ref(false)
const resizeTimeout = ref(null)

const menuItems = [
  { path: '/home', icon: 'home-o', text: '首页' },
  { path: '/timetable', icon: 'calendar-o', text: '课表管理' },
  { path: '/task', icon: 'todo-list-o', text: '教学任务' },
  { path: '/schedule', icon: 'search', text: '排课查询' },
  { path: '/statistics', icon: 'chart-trending-o', text: '统计分析' },
  { path: '/profile', icon: 'user-o', text: '个人中心' }
]

const pageTitle = computed(() => {
  const item = menuItems.find(m => route.path.startsWith(m.path))
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

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('orientationchange', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('orientationchange', checkMobile)
  if (resizeTimeout.value) {
    clearTimeout(resizeTimeout.value)
  }
})
</script>

<style scoped>
.desktop-layout {
  display: flex;
  height: 100vh;
  background: var(--bg-secondary);
}

.sidebar {
  width: 240px;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-base), transform var(--transition-base);
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
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-icon-only {
  display: none;
}

.sidebar.collapsed .nav-item {
  justify-content: center;
  padding: var(--spacing-md);
}

.sidebar.collapsed:not(.mobile) .nav-text {
  display: none;
}

.sidebar.collapsed:not(.mobile) .nav-icon-only {
  display: block;
}

.sidebar-footer {
  padding: var(--spacing-lg);
  border-top: 1px solid var(--border-light);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar-footer.footer-with-user {
  justify-content: flex-start;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
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
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-shrink: 0;
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: var(--spacing-xl);
}

@media (max-width: 1023px) {
  .desktop-layout {
    display: none;
  }
}

@media (min-width: 1024px) and (max-width: 1400px) {
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

@media (min-width: 1400px) {
  .sidebar {
    width: 260px;
  }
  
  .sidebar.collapsed {
    width: 72px;
  }
  
  .content-wrapper {
    padding: var(--spacing-2xl);
  }
}

@media (min-width: 1024px) {
  .sidebar-overlay {
    display: none;
  }
}
</style>
