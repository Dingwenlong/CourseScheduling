<template>
  <div class="main-layout">
    <div class="main-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" route class="custom-tabbar">
      <van-tabbar-item to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/timetable" icon="calendar-o">课表</van-tabbar-item>
      <van-tabbar-item to="/schedule" icon="search">查询</van-tabbar-item>
      <van-tabbar-item to="/statistics" icon="chart-trending-o">统计</van-tabbar-item>
      <van-tabbar-item to="/profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = ref(0)

const tabMap = {
  '/home': 0,
  '/timetable': 1,
  '/schedule': 2,
  '/statistics': 3,
  '/profile': 4
}

watch(() => route.path, (path) => {
  active.value = tabMap[path] || 0
}, { immediate: true })
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background-color: var(--bg-secondary);
}

.main-content {
  padding-bottom: 60px;
}

.custom-tabbar {
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

:deep(.van-tabbar-item) {
  transition: all 0.2s ease;
}

:deep(.van-tabbar-item--active) {
  color: var(--primary-color);
  transform: scale(1.05);
}

:deep(.van-tabbar-item__icon) {
  margin-bottom: 2px;
}
</style>
