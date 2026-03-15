import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/auth'

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
    userInfo.value = {
      userId: info.id,
      username: info.username,
      realName: info.realName,
      role: info.role,
      phone: info.phone,
      email: info.email,
      avatar: info.avatar,
      status: info.status
    }
  }

  const fetchUserInfo = async () => {
    try {
      const res = await getUserInfoApi()
      if (res.data) {
        setUserInfo(res.data)
      }
      return res
    } catch (e) {
      console.error('获取用户信息失败', e)
      throw e
    }
  }

  return {
    token,
    userInfo,
    login,
    logout,
    setUserInfo,
    fetchUserInfo
  }
})
