import { request } from './client'
import type { UploadedItem } from '../types/file'

export async function computeSha256(file: Blob): Promise<string> {
  const buf = await file.arrayBuffer()
  const digest = await crypto.subtle.digest('SHA-256', buf)
  return Array.from(new Uint8Array(digest)).map((b) => b.toString(16).padStart(2, '0')).join('')
}

export function adminCheckExists(fileSize: number, sha256Hash: string): Promise<UploadedItem | null> {
  return request<UploadedItem | null>({ method: 'GET', url: '/file/admin/exists', params: { fileSize, sha256Hash } })
}

export async function adminUpload(file: File): Promise<UploadedItem> {
  const form = new FormData()
  form.append('file', file)
  return request<UploadedItem>({ method: 'POST', url: '/file/admin/upload', data: form })
}

export async function uploadWithDedup(file: File): Promise<UploadedItem> {
  // crypto.subtle 仅在 HTTPS 或 localhost 下可用，HTTP 环境下跳过去重直接上传
  if (crypto?.subtle) {
    const hash = await computeSha256(file)
    const exists = await adminCheckExists(file.size, hash)
    if (exists) return exists
  }
  return adminUpload(file)
}