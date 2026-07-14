import { ref } from 'vue'
import type { Sentence } from '@windloo/shared'

export function useHardSentences(episodeId: string) {
  const key = `hard_sentences_${episodeId}`
  const list = ref<Sentence[]>([])
  try { const s = localStorage.getItem(key); if (s) list.value = JSON.parse(s) } catch {}
  function save() { try { localStorage.setItem(key, JSON.stringify(list.value)) } catch {} }
  function add(s: Sentence) {
    if (!list.value.some(x => x.startMs === s.startMs && x.text === s.text)) { list.value.push(s); save() }
  }
  function remove(i: number) { list.value.splice(i, 1); save() }
  return { list, add, remove }
}