import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const login = async (username, password) => {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      realName: res.data.realName,
      role: res.data.role
    }
    localStorage.setItem('token', res.data.token)
    return res
  }

  const logout = async () => {
    try {
      await logoutApi()
    } catch (e) {
      console.error(e)
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  return {
    token,
    userInfo,
    login,
    logout,
    setUserInfo
  }
})
