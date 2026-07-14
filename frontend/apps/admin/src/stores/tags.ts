import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { routeTitles } from '../router/titles'

export interface TagView { path: string; fullPath: string; title: string; name: string; affix?: boolean }

export const useTagsStore = defineStore('tags', () => {
  const views = ref<TagView[]>([
    { path: '/admin', fullPath: '/admin', title: '首页', name: 'dashboard', affix: true },
  ])

  function addView(route: RouteLocationNormalizedLoaded) {
    if (!route.name || route.matched.some((r) => r.meta?.public)) return
    const info = routeTitles[route.name as string]
    const title = info?.title || (route.name as string)
    const exist = views.value.find((v) => v.path === route.path)
    if (exist) { exist.fullPath = route.fullPath; exist.title = title; return }
    views.value.push({ path: route.path, fullPath: route.fullPath, title, name: route.name as string })
  }

  function removeView(path: string) {
    const idx = views.value.findIndex((v) => v.path === path)
    if (idx > -1 && !views.value[idx].affix) views.value.splice(idx, 1)
  }

  function neighbor(path: string): TagView | null {
    const idx = views.value.findIndex((v) => v.path === path)
    if (idx === -1) return null
    return views.value[idx - 1] || views.value[idx + 1] || null
  }

  return { views, addView, removeView, neighbor }
})