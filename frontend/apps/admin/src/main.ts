import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import * as Icons from '@element-plus/icons-vue'
import App from './App.vue'
import { router } from './router'
import { setOnUnauthorized } from '@windloo/shared'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
for (const [k, c] of Object.entries(Icons)) app.component(k, c as any)

setOnUnauthorized(() => {
  const cur = router.currentRoute.value.fullPath
  router.push('/login?redirect=' + encodeURIComponent(cur))
})

app.mount('#app')
