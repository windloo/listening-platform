<template>
  <div>
    <div style="margin-bottom:12px"><el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button></div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索单集..." @keyup.enter="doSearch">
        <template #append><el-button @click="doSearch"><el-icon><Search /></el-icon></el-button></template>
      </el-input>
    </div>
    <div v-for="(r, i) in flatResults" :key="i" class="result-item">
      <router-link :to="episodeLink(r)">
        <div class="result-sub" v-if="r.sentence" v-html="highlight(r.sentence.text)"></div>
        <div class="result-sub" v-else>{{ r.episodeName }}</div>
        <div class="result-ep">-- {{ r.episodeName }}</div>
      </router-link>
    </div>
    <el-empty v-if="searched && flatResults.length === 0" description="无搜索结果" />
    <el-pagination v-if="total > 0" style="margin-top:16px" :current-page="page + 1" :page-size="10" :total="total" layout="prev, pager, next" @current-change="onPage" />
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchEpisodes, type EpisodeIndexDTO, type Sentence } from '@windloo/shared'
import { ElMessage } from 'element-plus'
const route = useRoute()
const router = useRouter()
const keyword = ref((route.query.keyword as string) || '')
const results = ref<EpisodeIndexDTO[]>([])
const total = ref(0)
const page = ref(0)
const searched = ref(false)
onMounted(() => { if (keyword.value) doSearch() })
async function doSearch() {
  if (!keyword.value.trim()) return
  router.replace({ query: { keyword: keyword.value.trim() } })
  try { const res = await searchEpisodes(keyword.value.trim(), page.value, 10); results.value = res.list; total.value = res.total; searched.value = true }
  catch (e: any) { ElMessage.error(e?.message || '搜索失败') }
}
function onPage(p: number) { page.value = p - 1; doSearch() }

interface FlatResult { episodeId: string; episodeName: string; sentence: Sentence | null }

const flatResults = computed<FlatResult[]>(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return []
  const out: FlatResult[] = []
  for (const e of results.value) {
    const matched = (e.sentences || []).filter(s => s.text.toLowerCase().includes(kw)).slice(0, 3)
    if (matched.length > 0) {
      for (const s of matched) out.push({ episodeId: e.id, episodeName: e.nameChinese, sentence: s })
    } else if (e.nameChinese.toLowerCase().includes(kw) || e.nameEnglish.toLowerCase().includes(kw)) {
      // 仅命中名称、无句子命中时，把单集本身作为一条结果展示
      out.push({ episodeId: e.id, episodeName: e.nameChinese, sentence: null })
    }
  }
  return out
})

// 命中具体句子时带时间戳精确定位跳播；仅命中名称时直接进入单集
function episodeLink(r: FlatResult): string {
  return r.sentence
    ? `/episode/${r.episodeId}?t=${r.sentence.startMs}`
    : `/episode/${r.episodeId}`
}

function highlight(text: string): string {
  const escaped = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  if (!keyword.value.trim()) return escaped
  return escaped.replace(new RegExp(keyword.value.trim(), 'gi'), '<em>$&</em>')
}
</script>
<style scoped>
.search-bar { margin-bottom: 20px }
.result-item { background: #fff; border-radius: 8px; padding: 16px; margin-bottom: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06); transition: box-shadow 0.2s }
.result-item:hover { box-shadow: 0 2px 12px rgba(0,0,0,0.1) }
.result-sub { font-size: 15px; color: #333; line-height: 1.6 }
.result-ep { font-size: 13px; color: #999; margin-top: 4px }
:deep(em) { color: #f56c6c; font-style: normal }
</style>
