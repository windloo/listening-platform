<template>
  <div class="page" style="padding:16px;overflow:auto;height:100%">
    <el-card>
      <el-row :gutter="24">
        <el-col :span="8" style="text-align:center">
          <el-avatar :src="auth.user?.avatar || undefined" :size="120" />
          <div style="margin-top:12px"><el-button @click="pickAvatar">更换头像</el-button></div>
          <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="onAvatarFile" />
        </el-col>
        <el-col :span="16">
          <el-form ref="profileRef" :model="profile" :rules="profileRules" label-width="80px">
            <el-form-item label="用户名"><el-input :model-value="auth.user?.userName" disabled /></el-form-item>
            <el-form-item label="昵称" prop="nickname"><el-input v-model="profile.nickname" /></el-form-item>
            <el-form-item label="手机号" prop="phone"><el-input v-model="profile.phone" /></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存</el-button>
              <el-button @click="pwdVisible = true">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>

    <AvatarCropper v-model="cropVisible" :src="avatarSrc" @confirm="onCropConfirm" />

    <el-dialog v-model="pwdVisible" title="修改密码" width="420px">
      <el-form ref="pwdRef" :model="pwd" :rules="pwdRules" label-width="90px">
        <el-form-item label="旧密码" prop="oldPassword"><el-input v-model="pwd.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwd.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="confirm"><el-input v-model="pwd.confirm" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingPwd" @click="savePwd">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { updateProfile, updateAvatar, changePassword, adminUpload } from '@windloo/shared'
import { notifyError, notifySuccess } from '../../utils/notify'
import AvatarCropper from '../../components/AvatarCropper.vue'

const auth = useAuthStore()
const profileRef = ref<FormInstance>(); const pwdRef = ref<FormInstance>()
const savingProfile = ref(false); const savingPwd = ref(false)
const profile = reactive({ nickname: '', phone: '' })
const pwd = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const cropVisible = ref(false); const avatarSrc = ref(''); const avatarInput = ref<HTMLInputElement>()
const pwdVisible = ref(false)

const profileRules: FormRules = {
  nickname: [{ required: true, message: '必填', trigger: 'blur' }],
  phone: [{ required: true, message: '必填', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
}
const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '必填', trigger: 'blur' }],
  newPassword: [{ required: true, message: '必填', trigger: 'blur' }, { min: 6, message: '至少6位', trigger: 'blur' }],
  confirm: [{ required: true, message: '必填', trigger: 'blur' }, { validator: (_r: any, v: string, cb: any) => v === pwd.newPassword ? cb() : cb(new Error('两次不一致')), trigger: 'blur' }],
}
onMounted(async () => {
  if (!auth.user) await auth.loadUser().catch(() => {})
  profile.nickname = auth.user?.nickname ?? auth.user?.userName ?? ''
  profile.phone = auth.user?.phone ?? ''
})
async function saveProfile() {
  if (!profileRef.value) return
  await profileRef.value.validate(async (valid) => {
    if (!valid) return
    savingProfile.value = true
    try { await updateProfile(profile.nickname, profile.phone); await auth.loadUser(); notifySuccess('资料已更新') }
    catch (e) { notifyError(e) } finally { savingProfile.value = false }
  })
}
function pickAvatar() { avatarInput.value?.click() }
function onAvatarFile(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]
  if (!f) return
  const reader = new FileReader()
  reader.onload = () => { avatarSrc.value = reader.result as string; cropVisible.value = true }
  reader.readAsDataURL(f)
  ;(e.target as HTMLInputElement).value = ''
}
async function onCropConfirm(blob: Blob) {
  try {
    const file = new File([blob], 'avatar.png', { type: 'image/png' })
    const item = await adminUpload(file)
    await updateAvatar(item.remoteUrl)
    await auth.loadUser()
    notifySuccess('头像已更新')
  } catch (e) { notifyError(e) }
}
async function savePwd() {
  if (!pwdRef.value) return
  await pwdRef.value.validate(async (valid) => {
    if (!valid) return
    savingPwd.value = true
    try { await changePassword(pwd.oldPassword, pwd.newPassword); notifySuccess('密码已修改'); pwdVisible.value = false; pwd.oldPassword = ''; pwd.newPassword = ''; pwd.confirm = '' }
    catch (e) { notifyError(e) } finally { savingPwd.value = false }
  })
}
</script>