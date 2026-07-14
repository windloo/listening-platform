<template>
  <div class="app">
    <header class="nav">
      <div class="nav-inner">
        <router-link to="/" class="logo">🎵 windloo</router-link>
        <div class="search-box">
          <el-input v-model="keyword" placeholder="搜索单集..." @keyup.enter="doSearch" clearable>
            <template #append><el-button @click="doSearch"><el-icon><Search /></el-icon></el-button></template>
          </el-input>
        </div>
      </div>
    </header>
    <main class="content"><router-view /></main>
    <footer class="footer">windloo 听力平台 · 英语听力练习</footer>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const keyword = ref('')
function doSearch() { if (keyword.value.trim()) router.push({ name: 'search', query: { keyword: keyword.value.trim() } }) }
</script>

<style scoped>
.app { min-height: 100vh; display: flex; flex-direction: column }
.nav { position: sticky; top: 0; z-index: 100; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.08) }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; gap: 24px }
.logo { font-size: 20px; font-weight: 700; color: #409eff; text-decoration: none; white-space: nowrap }
.search-box { flex: 1; max-width: 400px }
.content { flex: 1; max-width: 1200px; margin: 0 auto; width: 100%; padding: 24px 20px }
.footer { text-align: center; padding: 16px; color: #999; font-size: 13px }
@media (max-width: 768px) {
  .nav-inner { flex-direction: column; gap: 8px }
  .search-box { max-width: 100%; width: 100% }
}
</style>