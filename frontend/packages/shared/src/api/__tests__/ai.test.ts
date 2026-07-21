import { describe, it, expect, vi, beforeEach } from 'vitest'

const store: Record<string, string> = {}
;(globalThis as any).localStorage = {
  getItem: (k: string) => store[k] ?? null,
  setItem: (k: string, v: string) => { store[k] = v },
  removeItem: (k: string) => { delete store[k] },
}

import { streamChat } from '../ai'
import { setToken } from '../../auth/token'

describe('streamChat', () => {
  beforeEach(() => { setToken('t'); (globalThis as any).fetch = vi.fn() })

  it('parses meta/token/done events', async () => {
    const sse =
      'event:meta\ndata:{"conversationId":"1","messageId":"2"}\n\n' +
      'event:token\ndata:{"content":"Hel"}\n\n' +
      'event:token\ndata:{"content":"lo"}\n\n' +
      'event:done\ndata:{"conversationId":"1","messageId":"3"}\n\n'
    const enc = new TextEncoder()
    const body = new ReadableStream({ start(c) { c.enqueue(enc.encode(sse)); c.close() } })
    ;(globalThis as any).fetch = vi.fn().mockResolvedValue({ ok: true, body })
    const meta: any[] = [], tokens: any[] = [], done: any[] = []
    await streamChat({ message: 'hi' }, {
      onMeta: (m: any) => meta.push(m),
      onToken: (t: any) => tokens.push(t),
      onDone: (m: any) => done.push(m),
    })
    expect(meta).toHaveLength(1)
    expect(tokens.map((t) => t.content).join('')).toBe('Hello')
    expect(done).toHaveLength(1)
  })

  it('calls onError on http failure', async () => {
    ;(globalThis as any).fetch = vi.fn().mockResolvedValue({ ok: false, status: 429, body: null })
    let err: any
    await streamChat({ message: 'hi' }, { onError: (e: any) => { err = e } })
    expect(err.code).toBe(429)
  })

  it('uses backend error body when present', async () => {
    ;(globalThis as any).fetch = vi.fn().mockResolvedValue({
      ok: false, status: 400,
      json: async () => ({ code: 42901, msg: '今日提问额度已用完' }),
    })
    let err: any
    await streamChat({ message: 'hi' }, { onError: (e: any) => { err = e } })
    expect(err.code).toBe(42901)
    expect(err.msg).toBe('今日提问额度已用完')
  })
})