<template>
  <div>
    <div style="margin-bottom:12px"><el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button></div>
    <el-breadcrumb separator="/"><el-breadcrumb-item :to="'/'">首页</el-breadcrumb-item><el-breadcrumb-item>专辑</el-breadcrumb-item></el-breadcrumb>
    <div style="margin-top:20px">
      <router-link v-for="e in episodes" :key="e.id" :to="`/episode/${e.id}`" class="episode-item">
        <span class="ep-seq">{{ e.sequenceNumber }}</span>
        <div class="ep-info"><div class="ep-name">{{ e.nameChinese }}</div><div class="ep-name-en">{{ e.nameEnglish }}</div></div>
        <span class="ep-dur">{{ formatDuration(e.durationInSecond) }}</span>
      </router-link>
    </div>
    <el-empty v-if="!loading && episodes.length === 0" description="该专辑下暂无单集" />
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getEpisodes, formatDuration, type Episode } from '@windloo/shared'
import { ElMessage } from 'element-plus'
const route = useRoute()
const episodes = ref<Episode[]>([])
const loading = ref(true)
onMounted(async () => {
  try { episodes.value = await getEpisodes(route.params.id as string) } catch (e: any) { ElMessage.error(e?.message || '加载失败') } finally { loading.value = false }
})
</script>
<style scoped>
.episode-item { display: flex; align-items: center; gap: 16px; padding: 16px; background: #fff; border-radius: 8px; margin-bottom: 8px; transition: box-shadow 0.2s }
.episode-item:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1) }
.ep-seq { width: 32px; height: 32px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; flex-shrink: 0 }
.ep-info { flex: 1 }
.ep-name { font-size: 16px; font-weight: 500 }
.ep-name-en { font-size: 13px; color: #999 }
.ep-dur { color: #999; font-size: 13px; flex-shrink: 0 }
</style>