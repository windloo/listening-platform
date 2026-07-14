import { request } from './client'
import type { Category, Album, Episode, CategoryReq, AlbumReq, EpisodeReq } from '../types'

export const getCategories = () => request<Category[]>({ method: 'GET', url: '/listening/categories' })
export const getCategory = (id: string) => request<Category>({ method: 'GET', url: `/listening/categories/${id}` })
export const getAlbums = (categoryId: string) => request<Album[]>({ method: 'GET', url: '/listening/albums', params: { categoryId } })
export const getAlbum = (id: string) => request<Album>({ method: 'GET', url: `/listening/albums/${id}` })
export const getEpisodes = (albumId: string) => request<Episode[]>({ method: 'GET', url: '/listening/episodes', params: { albumId } })
export const getEpisode = (id: string) => request<Episode>({ method: 'GET', url: `/listening/episodes/${id}` })

// admin list (returns all incl. invisible)
export const adminListAlbums = (categoryId: string) => request<Album[]>({ method: 'GET', url: '/listening/admin/albums', params: { categoryId } })
export const adminListEpisodes = (albumId: string) => request<Episode[]>({ method: 'GET', url: '/listening/admin/episodes', params: { albumId } })

export const adminCreateCategory = (req: CategoryReq) => request<Category>({ method: 'POST', url: '/listening/admin/categories', data: req })
export const adminUpdateCategory = (id: string, req: CategoryReq) => request<Category>({ method: 'PUT', url: `/listening/admin/categories/${id}`, data: req })
export const adminDeleteCategory = (id: string) => request<void>({ method: 'DELETE', url: `/listening/admin/categories/${id}` })
export const adminSortCategories = (sortedIds: string[]) => request<void>({ method: 'PUT', url: '/listening/admin/categories/sort', data: sortedIds })

export const adminCreateAlbum = (req: AlbumReq) => request<Album>({ method: 'POST', url: '/listening/admin/albums', data: req })
export const adminUpdateAlbum = (id: string, req: AlbumReq) => request<Album>({ method: 'PUT', url: `/listening/admin/albums/${id}`, data: req })
export const adminDeleteAlbum = (id: string) => request<void>({ method: 'DELETE', url: `/listening/admin/albums/${id}` })
export const adminSortAlbums = (sortedIds: string[]) => request<void>({ method: 'PUT', url: '/listening/admin/albums/sort', data: sortedIds })
export const adminShowAlbum = (id: string) => request<void>({ method: 'PUT', url: `/listening/admin/albums/${id}/show` })
export const adminHideAlbum = (id: string) => request<void>({ method: 'PUT', url: `/listening/admin/albums/${id}/hide` })

export const adminCreateEpisode = (req: EpisodeReq) => request<Episode>({ method: 'POST', url: '/listening/admin/episodes', data: req })
export const adminUpdateEpisode = (id: string, req: EpisodeReq) => request<Episode>({ method: 'PUT', url: `/listening/admin/episodes/${id}`, data: req })
export const adminDeleteEpisode = (id: string) => request<void>({ method: 'DELETE', url: `/listening/admin/episodes/${id}` })
export const adminSortEpisodes = (sortedIds: string[]) => request<void>({ method: 'PUT', url: '/listening/admin/episodes/sort', data: sortedIds })
export const adminShowEpisode = (id: string) => request<void>({ method: 'PUT', url: `/listening/admin/episodes/${id}/show` })
export const adminHideEpisode = (id: string) => request<void>({ method: 'PUT', url: `/listening/admin/episodes/${id}/hide` })