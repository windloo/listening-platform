<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <h2>windloo 登录</h2>
      <div class="login-tabs">
        <div class="login-tab" :class="{ active: form.mode === 'userName' }" @click="form.mode = 'userName'">用户名登录</div>
        <div class="login-tab" :class="{ active: form.mode === 'phone' }" @click="form.mode = 'phone'">手机号登录</div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" @submit.prevent="submit">
        <el-form-item v-if="form.mode === 'userName'" label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-else label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" native-type="submit" style="width:100%">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ mode: 'userName' as 'userName' | 'phone', userName: '', phone: '', password: '' })
const rules: FormRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      if (form.mode === 'userName') await auth.loginByUserNameAction(form.userName, form.password)
      else await auth.loginByPhoneAction(form.phone, form.password)
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } catch (e: any) { ElMessage.error(e?.message || '登录失败') }
    finally { loading.value = false }
  })
}
</script>

<style scoped>
.login-wrap { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f0f2f5 }
.login-card { width: 380px }
.login-tabs { display: flex; margin-bottom: 24px }
.login-tab { flex: 1; text-align: center; padding: 10px 0; cursor: pointer; font-size: 15px; color: #999; border-bottom: 2px solid #e4e7ed; transition: color 0.3s, border-color 0.3s }
.login-tab:hover { color: #666 }
.login-tab.active { color: #409eff; border-bottom: 2px solid #409eff }
</style>