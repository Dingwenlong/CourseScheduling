import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const THEME_KEY = 'theme-preference'
  
  const theme = ref(localStorage.getItem(THEME_KEY) || 'system')
  
  const applyTheme = (newTheme) => {
    const root = document.documentElement
    
    if (newTheme === 'dark') {
      root.setAttribute('data-theme', 'dark')
    } else if (newTheme === 'light') {
      root.setAttribute('data-theme', 'light')
    } else {
      root.removeAttribute('data-theme')
    }
  }
  
  const getSystemTheme = () => {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  
  const setTheme = (newTheme) => {
    theme.value = newTheme
    localStorage.setItem(THEME_KEY, newTheme)
    applyTheme(newTheme)
  }
  
  const toggleTheme = () => {
    const currentTheme = theme.value === 'system' ? getSystemTheme() : theme.value
    setTheme(currentTheme === 'dark' ? 'light' : 'dark')
  }
  
  const isDark = () => {
    if (theme.value === 'system') {
      return getSystemTheme() === 'dark'
    }
    return theme.value === 'dark'
  }
  
  const initTheme = () => {
    applyTheme(theme.value)
    
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', (e) => {
      if (theme.value === 'system') {
        applyTheme('system')
      }
    })
  }
  
  watch(theme, (newTheme) => {
    applyTheme(newTheme)
  })
  
  return {
    theme,
    setTheme,
    toggleTheme,
    isDark,
    initTheme
  }
})
