import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import 'vant/lib/index.css'
import './styles/index.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

document.documentElement.style.setProperty('--van-primary-color', '#51caba')
document.documentElement.style.setProperty('--van-primary-color-disabled', '#a3e4dc')
document.documentElement.style.setProperty('--van-primary-color-light', '#c9fffc')
