import { request } from './client'
import { getToken } from '../auth/token'
import type { ConversationDTO, MessageDTO, ChatRequest, ChatMeta, ChatToken, ChatError } from '../types'

const baseURL = (import.meta as any).env?.VITE_API_BASE ?? '/api'

export const listConversations = (page = 1, size = 20) =>
  request<{ list: ConversationDTO[]; total: number; page: number; size: number }>({
    method: 'GET', url: '/ai/conversations', params: { page, size },
  })
export const getMessages = (conversationId: string) =>
  request<MessageDTO[]>({ method: 'GET', url: `/ai/conversations/${conversationId}/messages` })
export const createConversation = (title?: string) =>
  request<ConversationDTO>({ method: 'POST', url: '/ai/conversations', data: title ? { title } : {} })
export const renameConversation = (id: string, title: string) =>
  request<ConversationDTO>({ method: 'PUT', url: `/ai/conversations/${id}`, data: { title } })
export const deleteConversation = (id: string) =>
  request<void>({ method: 'DELETE', url: `/ai/conversations/${id}` })

export interface StreamCallbacks {
  onMeta?: (m: ChatMeta) => void
  onToken?: (t: ChatToken) => void
  onDone?: (m: ChatMeta) => void
  onError?: (e: ChatError) => void
}

export async function streamChat(req: ChatRequest, cb: StreamCallbacks): Promise<void> {
  const token = getToken()
  const resp = await fetch(`${baseURL}/ai/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: JSON.stringify(req),
  })
  if (!resp.ok || !resp.body) { cb.onError?.({ code: resp.status, msg: `HTTP ${resp.status}` }); return }
  const reader = resp.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  for (;;) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n\n')
    buffer = parts.pop() ?? ''
    for (const part of parts) {
      const evt = parseSse(part)
      if (!evt) continue
      if (evt.event === 'meta') cb.onMeta?.(evt.data)
      else if (evt.event === 'token') cb.onToken?.(evt.data)
      else if (evt.event === 'done') cb.onDone?.(evt.data)
      else if (evt.event === 'error') cb.onError?.(evt.data)
    }
  }
}

function parseSse(part: string): { event: string; data: any } | null {
  let event = 'message'
  let dataLine = ''
  for (const line of part.split('\n')) {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    else if (line.startsWith('data:')) dataLine += line.slice(5).trim()
  }
  if (!dataLine) return null
  try { return { event, data: JSON.parse(dataLine) } } catch { return null }
}