import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'timetable',
        name: 'Timetable',
        component: () => import('@/views/timetable/index.vue'),
        meta: { title: '课表管理' }
      },
      {
        path: 'timetable/detail/:id',
        name: 'TimetableDetail',
        component: () => import('@/views/timetable/detail.vue'),
        meta: { title: '课表详情' }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/task/index.vue'),
        meta: { title: '教学任务' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/schedule/index.vue'),
        meta: { title: '课表查询' }
      },
      {
        path: 'adjustment',
        name: 'Adjustment',
        component: () => import('@/views/adjustment/index.vue'),
        meta: { title: '调课管理' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '统计分析' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智能排课系统` : '智能排课系统'
  
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')
  
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
