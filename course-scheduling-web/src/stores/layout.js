
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useLayoutStore = defineStore('layout', () => {
  const headerAction = ref({
    visible: false,
    icon: '',
    text: '',
    onClick: () => {}
  })

  const setHeaderAction = (action) => {
    headerAction.value = {
      visible: true,
      ...action
    }
  }

  const clearHeaderAction = () => {
    headerAction.value = {
      visible: false,
      icon: '',
      text: '',
      onClick: () => {}
    }
  }

  return {
    headerAction,
    setHeaderAction,
    clearHeaderAction
  }
})
