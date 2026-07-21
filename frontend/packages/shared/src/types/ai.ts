export type MessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM'

export interface ConversationDTO {
  id: string
  title: string
  createTime: string
  updateTime: string
}

export interface MessageDTO {
  id: string
  role: MessageRole
  content: string
  createTime: string
}

export interface ChatRequest {
  conversationId?: string
  episodeId?: string
  message: string
}

export interface ChatMeta { conversationId: string; messageId: string }
export interface ChatToken { content: string }
export interface ChatError { code: number; msg: string }