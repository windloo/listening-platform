import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../client', () => ({ request: vi.fn() }))
import { request } from '../client'
import { computeSha256, uploadWithDedup } from '../file'

const sampleItem = { id: 1, fileName: 'a.mp3', fileSizeInBytes: 1, fileSha256Hash: 'h', remoteUrl: '/api/file/files/h' }

describe('computeSha256', () => {
  it('computes sha256 of a blob', async () => {
    const hash = await computeSha256(new Blob([new TextEncoder().encode('hello')]))
    expect(hash).toBe('2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824')
  })
})

describe('uploadWithDedup', () => {
  beforeEach(() => vi.clearAllMocks())
  it('skips upload when file exists', async () => {
    ;(request as any).mockResolvedValueOnce(sampleItem)
    const r = await uploadWithDedup(new File(['x'], 'x.mp3'))
    expect(r).toEqual(sampleItem)
    expect(request).toHaveBeenCalledTimes(1)
  })
  it('uploads when file does not exist', async () => {
    ;(request as any).mockResolvedValueOnce(null)
    ;(request as any).mockResolvedValueOnce(sampleItem)
    const r = await uploadWithDedup(new File(['x'], 'x.mp3'))
    expect(r).toEqual(sampleItem)
    expect(request).toHaveBeenCalledTimes(2)
  })
})
