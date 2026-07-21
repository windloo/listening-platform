<template>
  <div v-if="episode">
    <div style="margin-bottom:12px"><el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon> 返回</el-button></div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="'/'">首页</el-breadcrumb-item>
      <el-breadcrumb-item>单集</el-breadcrumb-item>
      <el-breadcrumb-item>{{ episode.nameChinese }}</el-breadcrumb-item>
    </el-breadcrumb>
    <div class="player-area">
      <h2 class="ep-title">{{ episode.nameChinese }} <span class="ep-title-en">{{ episode.nameEnglish }}</span></h2>
      <audio ref="audioRef" :src="episode.audioUrl" controls autoplay style="width:100%;margin-top:16px" @timeupdate="onTimeUpdate" @loadedmetadata="onLoadedMetadata" />
      <div class="current-sentence" v-if="currentSentence">{{ currentSentence.text }}</div>
      <div class="current-sentence empty" v-else>--</div>
      <el-button type="success" @click="router.push({ name: 'chat', query: { episodeId: String(route.params.id) } })" style="margin-top:12px">就本集提问</el-button>
      <el-button type="primary" @click="addHard" :disabled="!currentSentence" style="margin-top:12px">这句听不懂</el-button>
    </div>
    <div class="hard-list" v-if="hardList.length > 0">
      <h3>听不懂的句子（{{ hardList.length }}）</h3>
      <div v-for="(s, i) in hardList" :key="i" class="hard-item">
        <span class="hard-text" @click="playSentence(s)">{{ s.text }}</span>
        <el-button size="small" text @click="hardRemove(i)">删除</el-button>
      </div>
    </div>
  </div>
  <el-empty v-else-if="!loading" description="单集不存在">
    <el-button @click="$router.push('/')">返回首页</el-button>
  </el-empty>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getEpisode, type Episode, type Sentence } from '@windloo/shared'
import { useHardSentences } from '../composables/useHardSentences'
import { ElMessage } from 'element-plus'
const route = useRoute()
const router = useRouter()
const episode = ref<Episode | null>(null)
const loading = ref(true)
const audioRef = ref<HTMLAudioElement>()
const currentSentence = ref<Sentence | null>(null)
const { list: hardList, add: hardAdd, remove: hardRemove } = useHardSentences(route.params.id as string)
const routeKeyword = (route.query.keyword as string) || ''
onMounted(async () => {
  try { episode.value = await getEpisode(route.params.id as string) } catch (e: any) { ElMessage.error(e?.message || '加载失败') } finally { loading.value = false }
})
function onTimeUpdate() {
  if (!audioRef.value || !episode.value?.sentences) return
  const timeMs = audioRef.value.currentTime * 1000
  const found = episode.value.sentences.find(s => timeMs >= s.startMs && timeMs <= s.endMs)
  if (found && (!currentSentence.value || found.startMs !== currentSentence.value.startMs)) { currentSentence.value = found }
}
function onLoadedMetadata() {
  if (!audioRef.value || !episode.value) return
  // 优先按时间戳精确定位（来自搜索结果点击）
  const t = route.query.t as string
  if (t) { audioRef.value.currentTime = Number(t) / 1000; return }
  // 兼容旧的 keyword 方式：跳到第一个包含关键字的句子
  if (routeKeyword && episode.value.sentences) {
    const kw = routeKeyword.toLowerCase()
    const match = episode.value.sentences.find(s => s.text.toLowerCase().includes(kw))
    if (match) { audioRef.value.currentTime = match.startMs / 1000 }
  }
}
function addHard() { if (currentSentence.value) { hardAdd(currentSentence.value); ElMessage.success('已加入收藏') } }
function playSentence(s: Sentence) { if (audioRef.value) { audioRef.value.currentTime = s.startMs / 1000; audioRef.value.play() } }
</script>
<style scoped>
.player-area { background: #fff; border-radius: 12px; padding: 24px; margin-top: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.08) }
.ep-title { font-size: 22px }
.ep-title-en { font-size: 14px; color: #999; font-weight: 400 }
.current-sentence { font-size: 24px; text-align: center; padding: 32px 16px; margin-top: 16px; background: #f0f7ff; border-radius: 8px; min-height: 80px; display: flex; align-items: center; justify-content: center }
.current-sentence.empty { color: #ccc; font-size: 18px }
.hard-list { margin-top: 24px }
.hard-list h3 { font-size: 16px; margin-bottom: 12px }
.hard-item { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: #fff; border-radius: 8px; margin-bottom: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.06) }
.hard-text { flex: 1; cursor: pointer; font-size: 15px }
.hard-text:hover { color: #409eff }
</style>
