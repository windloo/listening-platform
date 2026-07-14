import { request } from './client'
import type { ListeningStats, IdentityStats } from '../types'

export const getListeningStats = () => request<ListeningStats>({ method: 'GET', url: '/listening/admin/stats' })
export const getIdentityStats = () => request<IdentityStats>({ method: 'GET', url: '/identity/admin/stats' })