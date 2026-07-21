import { describe, it, expect, vi } from 'vitest'

const store: Record<string, string> = {}
;(globalThis as any).localStorage = {
  getItem: (k: string) => store[k] ?? null,
  setItem: (k: string, v: string) => { store[k] = v },
  removeItem: (k: string) => { delete store[k] },
}

import { unwrap, handleHttpError, setOnUnauthorized } from '../client'
import { BizError } from '../../errors'

describe('unwrap', () => {
  it('returns data when code 0', () => { expect(unwrap({ code: 0, msg: 'success', data: 42 })).toBe(42) })
  it('throws BizError when code != 0', () => {
    try { unwrap({ code: 40001, msg: 'bad', data: null }); throw new Error('should throw') }
    catch (e) { expect(e).toBeInstanceOf(BizError); expect((e as BizError).code).toBe(40001); expect((e as BizError).message).toBe('bad') }
  })
})

describe('handleHttpError', () => {
  it('401 clears token and calls onUnauthorized', () => {
    const fn = vi.fn(); setOnUnauthorized(fn)
    store['yz_token'] = 't'
    try { handleHttpError({ response: { status: 401 } }); throw new Error('should throw') }
    catch (e) { expect((e as BizError).code).toBe(40100) }
    expect(fn).toHaveBeenCalled(); expect(store['yz_token']).toBeUndefined()
  })
  it('403 throws 40300', () => {
    setOnUnauthorized(() => {})
    try { handleHttpError({ response: { status: 403 } }); throw new Error('should throw') }
    catch (e) { expect((e as BizError).code).toBe(40300) }
  })
  it('500 throws 50000', () => {
    try { handleHttpError({ response: { status: 500 } }); throw new Error('should throw') }
    catch (e) { expect((e as BizError).code).toBe(50000) }
  })

  it('uses backend body code/msg when present (e.g. wrong password)', () => {
    try { handleHttpError({ response: { status: 400, data: { code: 40001, msg: '登录失败' } } }); throw new Error('should throw') }
    catch (e) { expect((e as BizError).code).toBe(40001); expect((e as BizError).message).toBe('登录失败') }
  })

  it('uses backend body for 429', () => {
    try { handleHttpError({ response: { status: 429, data: { code: 42901, msg: '额度已用完' } } }); throw new Error('should throw') }
    catch (e) { expect((e as BizError).code).toBe(42901); expect((e as BizError).message).toBe('额度已用完') }
  })

  it('network error (no response) falls back to 50000', () => {
    try { handleHttpError({}); throw new Error('should throw') }
    catch (e) { expect((e as BizError).code).toBe(50000) }
  })
})
