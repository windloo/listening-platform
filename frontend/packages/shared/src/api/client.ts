import axios, { type AxiosRequestConfig } from 'axios'
import JSONBig from 'json-bigint'
import type { JsonResponse } from '../types/response'
import { BizError } from '../errors'
import { getToken, clearToken } from '../auth/token'

const baseURL = (import.meta as any).env?.VITE_API_BASE ?? '/api'
const JSONBigString = JSONBig({ storeAsString: true })

export function unwrap<T>(body: JsonResponse<T>): T {
  if (body.code !== 0) throw new BizError(body.code, body.msg)
  return body.data
}

let onUnauthorized: () => void = () => {}
export function setOnUnauthorized(fn: () => void): void { onUnauthorized = fn }

export function handleHttpError(err: unknown): never {
  const status = (err as any)?.response?.status
  if (status === 401) { clearToken(); onUnauthorized(); throw new BizError(40100, '未登录或登录已过期') }
  if (status === 403) throw new BizError(40300, '无权限')
  throw new BizError(50000, '服务异常，请稍后重试')
}

export const apiClient = axios.create({ baseURL, timeout: 30000 })
apiClient.defaults.transformResponse = [
  (data: any) => {
    if (typeof data !== 'string') return data
    try { return JSONBigString.parse(data) } catch { return data }
  },
]

apiClient.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.set('Authorization', `Bearer ${token}`)
  return config
})

apiClient.interceptors.response.use(
  (res) => { unwrap(res.data as JsonResponse<unknown>); return res },
  (err) => { try { handleHttpError(err) } catch (e) { return Promise.reject(e) } }
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const res = await apiClient.request<JsonResponse<T>>(config)
  return (res.data as JsonResponse<T>).data
}