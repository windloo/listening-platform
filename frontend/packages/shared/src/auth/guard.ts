import { getToken } from './token'

export interface JwtPayload { userId: string; roles: string; exp?: number }

export function decodeJwt(token: string): JwtPayload {
  const payload = token.split('.')[1]
  const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'))
  return JSON.parse(json)
}

export function getRoles(): string[] {
  const t = getToken()
  if (!t) return []
  try {
    const raw = decodeJwt(t).roles || '[]'
    return raw.replace(/[\[\]]/g, '').split(',').map((s) => s.trim()).filter(Boolean)
  } catch { return [] }
}

export function getUserId(): string | null {
  const t = getToken()
  if (!t) return null
  try { return decodeJwt(t).userId } catch { return null }
}

export function isAuthenticated(): boolean { return !!getToken() }
export function hasRole(role: string): boolean { return getRoles().includes(role) }
