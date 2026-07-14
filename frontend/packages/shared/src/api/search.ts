import { request } from './client'
import type { EpisodeIndexDTO, PageResult } from '../types'
export const searchEpisodes = (keyword: string, page: number, size: number) =>
  request<PageResult<EpisodeIndexDTO>>({ method: 'GET', url: '/search/episodes', params: { keyword, page, size } })
