<template>
  <div class="main-layout">
    <div class="main-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" route>
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
  background-color: #f7f8fa;
}

.main-content {
  padding-bottom: 50px;
}
</style>
