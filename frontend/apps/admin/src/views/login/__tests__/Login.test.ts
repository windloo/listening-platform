import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import ElementPlus from 'element-plus'
import Login from '../Login.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ query: {} }),
}))
vi.mock('@windloo/shared', () => ({
  loginByUserName: vi.fn(), loginByPhone: vi.fn(), me: vi.fn(),
  setToken: vi.fn(), clearToken: vi.fn(), getToken: () => null, getRoles: () => [], getUserId: () => null,
}))
import { loginByUserName } from '@windloo/shared'

beforeEach(() => { setActivePinia(createPinia()); vi.clearAllMocks() })

describe('Login.vue', () => {
  it('renders userName input, password input and a submit button', () => {
    const w = mount(Login, { global: { plugins: [ElementPlus] } })
    const inputs = w.findAll('.el-input__inner')
    expect(inputs.length).toBeGreaterThanOrEqual(2)
    expect(w.find('button[type="submit"]').exists()).toBe(true)
  })

  it('calls loginByUserName with filled values on submit', async () => {
    ;(loginByUserName as any).mockResolvedValue('tok')
    const w = mount(Login, { global: { plugins: [ElementPlus] } })
    const inputs = w.findAll('.el-input__inner')
    await inputs[0].setValue('admin')
    await inputs[1].setValue('123456')
    await w.find('form').trigger('submit')
    await flushPromises()
    expect((loginByUserName as any)).toHaveBeenCalledWith('admin', '123456')
  })
})