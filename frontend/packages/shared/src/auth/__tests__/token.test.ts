import { describe, it, expect, beforeEach } from 'vitest'
import { getToken, setToken, clearToken } from '../token'

const store: Record<string, string> = {}
;(globalThis as any).localStorage = {
  getItem: (k: string) => store[k] ?? null,
  setItem: (k: string, v: string) => { store[k] = v },
  removeItem: (k: string) => { delete store[k] },
}

describe('token storage', () => {
  beforeEach(() => clearToken())
  it('returns null when empty', () => { expect(getToken()).toBeNull() })
  it('set then get', () => { setToken('abc'); expect(getToken()).toBe('abc') })
  it('clear removes token', () => { setToken('abc'); clearToken(); expect(getToken()).toBeNull() })
})
