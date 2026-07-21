import { describe, it, expect, beforeEach } from 'vitest'
import { routeGuard } from '../router'
import { clearToken, setToken } from '@windloo/shared'

const to = (meta: any) => ({ matched: [{ meta }], name: 'x', fullPath: '/x', path: '/x' } as any)

describe('routeGuard', () => {
  beforeEach(() => clearToken())

  it('public route allowed without token', () => {
    expect(routeGuard(to({ public: true }))).toBe(true)
  })
  it('requiresAuth route redirects to login when no token', () => {
    const r: any = routeGuard(to({ requiresAuth: true }))
    expect(r.name).toBe('login')
    expect(r.query.redirect).toBe('/x')
  })
  it('requiresAuth route allowed when token present', () => {
    setToken('t')
    expect(routeGuard(to({ requiresAuth: true }))).toBe(true)
  })
})