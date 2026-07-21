import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  loginByUserName, loginByPhone, me,
  setToken, clearToken, getToken, getRoles, getUserId,
} from '@windloo/shared'
import type { UserDTO } from '@windloo/shared'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(getToken())
  const roles = ref<string[]>(getRoles())
  const userId = ref<string | null>(getUserId())
  const user = ref<UserDTO | null>(null)

  async function loginByUserNameAction(userName: string, password: string) {
    const t = await loginByUserName(userName, password)
    setToken(t); token.value = t; roles.value = getRoles(); userId.value = getUserId()
  }
  async function loginByPhoneAction(phone: string, password: string) {
    const t = await loginByPhone(phone, password)
    setToken(t); token.value = t; roles.value = getRoles(); userId.value = getUserId()
  }
  async function loadUser() { user.value = await me() }
  function logout() { clearToken(); token.value = null; roles.value = []; userId.value = null; user.value = null }

  return { token, roles, userId, user, loginByUserNameAction, loginByPhoneAction, loadUser, logout }
})