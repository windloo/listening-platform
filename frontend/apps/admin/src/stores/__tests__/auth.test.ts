import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

vi.mock('@windloo/shared', () => ({
  loginByUserName: vi.fn(),
  loginByPhone: vi.fn(),
  me: vi.fn(),
  setToken: vi.fn(),
  clearToken: vi.fn(),
  getToken: () => null,
  getRoles: () => ['ADMIN'],
  getUserId: () => '1',
}))
import { loginByUserName } from '@windloo/shared'
import { useAuthStore } from '../auth'

beforeEach(() => { setActivePinia(createPinia()); vi.clearAllMocks() })

describe('auth store', () => {
  it('loginByUserName stores token and roles', async () => {
    ;(loginByUserName as any).mockResolvedValue('tok')
    const s = useAuthStore()
    await s.loginByUserNameAction('u', 'p')
    expect(s.token).toBe('tok')
    expect(s.isAdmin()).toBe(true)
  })
  it('logout clears state', () => {
    const s = useAuthStore()
    s.token = 'x'; s.roles = ['ADMIN']
    s.logout()
    expect(s.token).toBeNull()
    expect(s.isAdmin()).toBe(false)
  })
})
