import axios from 'axios'
import { createDiscreteApi } from 'naive-ui'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const { message } = createDiscreteApi(['message'])
let authFailureInProgress = false

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    const token = userStore.token || localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    } else {
      return handleApiFailure(res.code, res.message)
    }
  },
  async error => {
    const status = error.response?.status
    const data = error.response?.data || {}
    const code = data.code
    const fallbackMessage = data.message || error.message || '网络错误'

    if (status === 401 || code === 1003 || code === 1005 || code === 1006 || code === 1007) {
      await handleAuthFailure(code, fallbackMessage)
      return Promise.reject(new Error(fallbackMessage))
    }

    if (status === 403 || code === 403) {
      message.warning(fallbackMessage || '当前账号无权执行该操作')
      return Promise.reject(new Error(fallbackMessage || '当前账号无权执行该操作'))
    }

    message.error(fallbackMessage)
    return Promise.reject(new Error(fallbackMessage))
  }
)

const handleApiFailure = async (code, rawMessage) => {
  const messageText = rawMessage || '请求失败'

  if (code === 401 || code === 1003 || code === 1005 || code === 1006 || code === 1007) {
    await handleAuthFailure(code, messageText)
    return Promise.reject(new Error(messageText))
  }

  if (code === 403) {
    message.warning(messageText || '当前账号无权执行该操作')
    return Promise.reject(new Error(messageText || '当前账号无权执行该操作'))
  }

  message.error(messageText)
  return Promise.reject(new Error(messageText))
}

const handleAuthFailure = async (code, rawMessage) => {
  if (authFailureInProgress) {
    return
  }
  authFailureInProgress = true
  const userStore = useUserStore()
  const messageText = resolveAuthMessage(code, rawMessage)
  try {
    message.error(messageText)
    await userStore.logout({ remote: false })
    if (router.currentRoute.value.path !== '/login') {
      await router.push('/login')
    }
  } finally {
    authFailureInProgress = false
  }
}

const resolveAuthMessage = (code, rawMessage) => {
  if (code === 1003) {
    return '账号已被禁用，请联系管理员'
  }
  if (code === 1005 || code === 1006 || code === 1007 || code === 401) {
    return '登录状态已失效，请重新登录'
  }
  return rawMessage || '请求失败'
}

export default request
