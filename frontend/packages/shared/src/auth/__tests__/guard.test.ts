import { describe, it, expect, beforeEach } from 'vitest'
import { setToken, clearToken } from '../token'
import { decodeJwt, getRoles, getUserId, isAuthenticated, hasRole } from '../guard'

const store: Record<string, string> = {}
;(globalThis as any).localStorage = {
  getItem: (k: string) => store[k] ?? null,
  setItem: (k: string, v: string) => { store[k] = v },
  removeItem: (k: string) => { delete store[k] },
}

function makeToken(payload: object): string {
  const enc = (o: object) => btoa(JSON.stringify(o)).replace(/=/g, '')
  return `${enc({ alg: 'none', typ: 'JWT' })}.${enc(payload)}.sig`
}

describe('guard', () => {
  beforeEach(() => clearToken())
  it('decodeJwt reads claims', () => {
    expect(decodeJwt(makeToken({ userId: '1', roles: '[ADMIN]' })).userId).toBe('1')
  })
  it('getRoles parses "[ADMIN, USER]"', () => {
    setToken(makeToken({ userId: '1', roles: '[ADMIN, USER]' }))
    expect(getRoles()).toEqual(['ADMIN', 'USER'])
  })
  it('hasRole / isAuthenticated', () => {
    setToken(makeToken({ userId: '1', roles: '[ADMIN]' }))
    expect(isAuthenticated()).toBe(true)
    expect(hasRole('ADMIN')).toBe(true)
    expect(hasRole('USER')).toBe(false)
  })
  it('no token -> empty roles, not authenticated', () => {
    expect(isAuthenticated()).toBe(false)
    expect(getRoles()).toEqual([])
    expect(getUserId()).toBeNull()
  })
})
