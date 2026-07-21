import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import Chat from '../Chat.vue'

vi.mock('@windloo/shared', () => ({
  listConversations: vi.fn().mockResolvedValue({ list: [], total: 0, page: 1, size: 20 }),
  getMessages: vi.fn().mockResolvedValue([]),
  createConversation: vi.fn(),
  deleteConversation: vi.fn(),
  renameConversation: vi.fn(),
  streamChat: vi.fn(),
  setToken: vi.fn(), getToken: vi.fn(() => 't'), clearToken: vi.fn(),
  isAuthenticated: vi.fn(() => true), hasRole: vi.fn(() => false),
  getRoles: vi.fn(() => []), getUserId: vi.fn(() => '1'),
}))

describe('Chat.vue', () => {
  beforeEach(() => { setActivePinia(createPinia()) })

  it('renders input and send button', async () => {
    const w = mount(Chat, { global: { plugins: [ElementPlus] } })
    await flushPromises()
    expect(w.find('.el-textarea__inner').exists()).toBe(true)
    expect(w.text()).toContain('发送')
  })

  it('calls streamChat on send', async () => {
    const { streamChat } = await import('@windloo/shared')
    const w = mount(Chat, { global: { plugins: [ElementPlus] } })
    await flushPromises()
    const input = w.find('.el-textarea__inner')
    await input.setValue('what is a noun?')
    await w.find('[data-testid="send-btn"]').trigger('click')
    await flushPromises()
    expect(streamChat).toHaveBeenCalled()
  })
})