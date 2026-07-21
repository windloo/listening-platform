<template>
  <div class="chat-page">
    <aside class="sidebar">
      <el-button type="primary" plain style="width:100%" @click="newChat">+ 新对话</el-button>
      <div class="conv-list">
        <div v-for="c in conversations" :key="c.id" class="conv-item" :class="{ active: c.id === currentId }" @click="select(c.id)">
          <span class="conv-title">{{ c.title }}</span>
          <el-icon class="conv-del" @click.stop="remove(c.id)"><Close /></el-icon>
        </div>
        <el-empty v-if="!conversations.length" description="暂无对话" :image-size="60" />
      </div>
    </aside>
    <section class="main">
      <el-alert v-if="episodeId" type="info" :closable="false" show-icon style="margin-bottom:12px">📌 就本集提问模式:AI 会参考本集字幕回答</el-alert>
      <div class="messages" ref="msgBox">
        <div v-for="m in messages" :key="m.id" class="msg" :class="m.role.toLowerCase()">
          <div class="bubble">
            <MarkdownView v-if="m.role === 'ASSISTANT'" :content="m.content" />
            <template v-else>{{ m.content }}</template>
          </div>
        </div>
        <div v-if="streaming" class="msg assistant"><div class="bubble"><MarkdownView :content="pending" /></div></div>
      </div>
      <div class="composer">
        <el-input v-model="input" type="textarea" :rows="2" placeholder="问个英语问题…" @keydown.enter.exact.prevent="send" />
        <el-button type="primary" :loading="streaming" data-testid="send-btn" @click="send">发送</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { listConversations, getMessages, createConversation, deleteConversation, streamChat, type ConversationDTO, type MessageDTO } from '@windloo/shared'
import MarkdownView from '../components/MarkdownView.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const episodeId = computed(() => (route.query.episodeId as string) || null)
const conversations = ref<ConversationDTO[]>([])
const messages = ref<MessageDTO[]>([])
const currentId = ref<string | null>(null)
const input = ref('')
const streaming = ref(false)
const pending = ref('')
const msgBox = ref<HTMLElement | null>(null)

onMounted(loadConversations)

async function loadConversations() {
  try {
    const r = await listConversations(1, 50)
    conversations.value = r.list
  } catch (e: any) { ElMessage.error(e?.message || '加载会话失败') }
}

async function select(id: string) {
  currentId.value = id
  messages.value = await getMessages(id)
  await scrollBottom()
}

async function newChat() {
  currentId.value = null
  messages.value = []
}

async function remove(id: string) {
  await deleteConversation(id)
  if (currentId.value === id) { currentId.value = null; messages.value = [] }
  await loadConversations()
}

async function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''
  streaming.value = true
  pending.value = ''
  const userMsg: MessageDTO = { id: 'tmp-' + Date.now(), role: 'USER', content: text, createTime: '' }
  messages.value.push(userMsg)
  await scrollBottom()
  try {
    await streamChat({ conversationId: currentId.value ?? undefined, episodeId: episodeId.value ?? undefined, message: text }, {
      onMeta: (m) => { currentId.value = m.conversationId },
      onToken: (t) => { pending.value += t.content; scrollBottom() },
      onDone: async () => {
        pending.value = ''
        if (currentId.value) messages.value = await getMessages(currentId.value)
        streaming.value = false
        await loadConversations()
        await scrollBottom()
      },
      onError: (e) => { ElMessage.error(e.msg || 'AI 调用失败'); streaming.value = false },
    })
  } catch (e: any) { ElMessage.error(e?.message || '发送失败'); streaming.value = false }
}

async function scrollBottom() {
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}
</script>

<style scoped>
.chat-page { display: flex; gap: 16px; height: calc(100vh - 140px) }
.sidebar { width: 240px; display: flex; flex-direction: column; gap: 12px }
.conv-list { flex: 1; overflow-y: auto; background: #fff; border-radius: 8px; padding: 8px }
.conv-item { display: flex; justify-content: space-between; align-items: center; padding: 8px; border-radius: 6px; cursor: pointer }
.conv-item:hover, .conv-item.active { background: #ecf5ff }
.conv-title { font-size: 14px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap }
.conv-del { color: #c0c4cc; flex-shrink: 0 }
.main { flex: 1; display: flex; flex-direction: column; background: #fff; border-radius: 8px; overflow: hidden }
.messages { flex: 1; overflow-y: auto; padding: 16px }
.msg { display: flex; margin-bottom: 12px }
.msg.user { justify-content: flex-end }
.bubble { max-width: 75%; padding: 10px 14px; border-radius: 10px; white-space: pre-wrap; word-break: break-word }
.msg.user .bubble { background: #409eff; color: #fff }
.msg.assistant .bubble { background: #f4f4f5; color: #333 }
.composer { display: flex; gap: 8px; padding: 12px; border-top: 1px solid #eee }
.composer .el-button { align-self: flex-end }
</style>