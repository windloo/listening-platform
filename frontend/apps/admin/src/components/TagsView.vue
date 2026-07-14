<template>
  <div class="tags-view">
    <div
      v-for="v in tags.views"
      :key="v.path"
      class="tag"
      :class="{ active: v.path === route.path }"
      @click="go(v)"
    >
      {{ v.title }}
      <el-icon v-if="!v.affix" class="close" @click.stop="close(v)"><Close /></el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useTagsStore, type TagView } from '../stores/tags'

const router = useRouter()
const route = useRoute()
const tags = useTagsStore()

function go(v: TagView) { if (v.path !== route.path) router.push(v.fullPath) }

function close(v: TagView) {
  const isActive = v.path === route.path
  const n = isActive ? tags.neighbor(v.path) : null
  tags.removeView(v.path)
  if (isActive) {
    const target = n || tags.views[0]
    router.push(target ? target.fullPath : '/admin')
  }
}
</script>

<style scoped>
.tags-view { display: flex; align-items: center; gap: 6px; padding: 6px 12px; background: #fff; border-bottom: 1px solid #eee; overflow-x: auto }
.tag { display: inline-flex; align-items: center; gap: 4px; padding: 3px 10px; border: 1px solid #dcdfe6; border-radius: 3px; font-size: 12px; cursor: pointer; white-space: nowrap; background: #fff }
.tag.active { background: #409eff; color: #fff; border-color: #409eff }
.tag .close { font-size: 12px }
</style>