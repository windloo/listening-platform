import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as Icons from '@element-plus/icons-vue'
import App from './App.vue'
import { router } from './router'
import './styles/global.css'

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
for (const [k, c] of Object.entries(Icons)) app.component(k, c as any)
app.mount('#app')