<template>
  <div class="markdown-body" v-html="html"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'

const props = defineProps<{ content: string }>()

const md = new MarkdownIt({ html: false, linkify: true, breaks: true, typographer: true })

DOMPurify.addHook('afterSanitizeAttributes', (node) => {
  if (node.tagName === 'A') {
    node.setAttribute('target', '_blank')
    node.setAttribute('rel', 'noopener noreferrer')
  }
})

const html = computed(() => DOMPurify.sanitize(md.render(props.content ?? '')))
</script>

<style>
.markdown-body { font-size: 14px; line-height: 1.7; word-break: break-word; }
.markdown-body h1,.markdown-body h2,.markdown-body h3,.markdown-body h4 { margin: 0.6em 0 0.3em; line-height: 1.3; }
.markdown-body h1 { font-size: 1.3em }
.markdown-body h2 { font-size: 1.15em }
.markdown-body h3 { font-size: 1.05em }
.markdown-body p { margin: 0.4em 0 }
.markdown-body ul,.markdown-body ol { padding-left: 1.4em; margin: 0.4em 0 }
.markdown-body code { background: rgba(0,0,0,.06); padding: 1px 5px; border-radius: 4px; font-family: Consolas,monospace; font-size: .9em }
.markdown-body pre { background: rgba(0,0,0,.06); padding: 10px; border-radius: 6px; overflow-x: auto; margin: 0.5em 0 }
.markdown-body pre code { background: none; padding: 0 }
.markdown-body table { border-collapse: collapse; margin: 0.5em 0 }
.markdown-body th,.markdown-body td { border: 1px solid #ddd; padding: 4px 8px }
.markdown-body blockquote { border-left: 3px solid #409eff; margin: 0.5em 0; padding-left: 12px; color: #666 }
.markdown-body a { color: #409eff }
.markdown-body hr { border: none; border-top: 1px solid #eee; margin: 0.8em 0 }
</style>