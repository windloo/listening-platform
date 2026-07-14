import { request } from './client'
import type { UserDTO, PageResult } from '../types'

export const loginByUserName = (userName: string, password: string) =>
  request<string>({ method: 'POST', url: '/identity/login/byUserName', data: { userName, password } })
export const loginByPhone = (phone: string, password: string) =>
  request<string>({ method: 'POST', url: '/identity/login/byPhone', data: { phone, password } })
export const me = () => request<UserDTO>({ method: 'GET', url: '/identity/me' })
export const updateProfile = (nickname: string, phone: string) =>
  request<UserDTO>({ method: 'PUT', url: '/identity/profile', data: { nickname, phone } })
export const updateAvatar = (avatar: string) =>
  request<UserDTO>({ method: 'PUT', url: '/identity/profile/avatar', data: { avatar } })
export const changePassword = (oldPassword: string, newPassword: string) =>
  request<void>({ method: 'POST', url: '/identity/profile/password', data: { oldPassword, newPassword } })
export const adminListUsers = (page: number, size: number) =>
  request<PageResult<UserDTO>>({ method: 'GET', url: '/identity/admin/users', params: { page, size } })
export const adminCreateUser = (userName: string, phone: string, role: string) =>
  request<string>({ method: 'POST', url: '/identity/admin/user', data: { userName, phone, role } })
export const adminResetPassword = (id: string) =>
  request<string>({ method: 'POST', url: `/identity/admin/user/${id}/resetPassword` })
export const adminDeleteUser = (id: string) =>
  request<void>({ method: 'DELETE', url: `/identity/admin/user/${id}` })