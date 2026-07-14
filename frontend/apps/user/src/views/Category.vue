<template>
  <div>
    <div style="margin-bottom:12px"><el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button></div>
    <el-breadcrumb separator="/"><el-breadcrumb-item :to="'/'">首页</el-breadcrumb-item><el-breadcrumb-item>分类</el-breadcrumb-item></el-breadcrumb>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col v-for="a in albums" :key="a.id" :xs="24" :sm="12" :md="8" :lg="6">
        <router-link :to="`/album/${a.id}`">
          <div class="album-card">
            <div class="album-icon"><el-icon size="32"><VideoPlay /></el-icon></div>
            <div class="album-name">{{ a.nameChinese }}</div>
            <div class="album-name-en">{{ a.nameEnglish }}</div>
          </div>
        </router-link>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && albums.length === 0" description="该分类下暂无专辑" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getAlbums, type Album } from '@windloo/shared'
import { ElMessage } from 'element-plus'
const route = useRoute()
const albums = ref<Album[]>([])
const loading = ref(true)
onMounted(async () => {
  try { albums.value = await getAlbums(route.params.id as string) } catch (e: any) { ElMessage.error(e?.message || '加载失败') } finally { loading.value = false }
})
</script>
<style scoped>
.album-card { background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 12px; padding: 24px; margin-bottom: 20px; color: #fff; transition: transform 0.2s; cursor: pointer }
.album-card:hover { transform: translateY(-4px) }
.album-icon { margin-bottom: 12px }
.album-name { font-size: 18px; font-weight: 600 }
.album-name-en { font-size: 13px; opacity: 0.8; margin-top: 4px }
</style>