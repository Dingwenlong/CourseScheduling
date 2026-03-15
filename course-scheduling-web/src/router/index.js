import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import ResponsiveLayout from '@/layouts/ResponsiveLayout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: ResponsiveLayout,
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
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 智能排课系统` : '智能排课系统'
  
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')
  
  if (to.path === '/login') {
    if (token) {
      localStorage.removeItem('token')
      userStore.token = ''
      userStore.userInfo = null
    }
    next()
  } else if (!token) {
    next('/login')
  } else {
    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (e) {
        console.error('获取用户信息失败', e)
      }
    }
    next()
  }
})

export default router
