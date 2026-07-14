<template>
  <div class="page">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">添加用户</el-button>
    </div>
    <div class="table-wrap">
    <el-table :data="list" v-loading="loading" border>
      <el-table-column label="头像" width="70">
        <template #default="{ row }"><el-avatar :src="row.avatar || undefined" :size="32" /></template>
      </el-table-column>
      <el-table-column prop="userName" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="角色">
        <template #default="{ row }">{{ (row.roles || []).join(", ") || "-" }}</template>
      </el-table-column>
      <el-table-column label="创建时间">
        <template #default="{ row }">{{ formatDateTime(row.creationTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" @click="resetPwd(row.id)">重置密码</el-button>
          <el-popconfirm title="确定删除？" @confirm="remove(row.id)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    </div>
    <div class="pager">
      <span style="color:#606266">总计 {{ total }} 条</span>
      <el-pagination :current-page="page" :page-size="size" :page-sizes="[10,20,30,50]" :total="total" layout="sizes, prev, pager, next" @size-change="onSizeChange" @current-change="onPage" />
    </div>

    <el-dialog v-model="createVisible" title="添加用户" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="userName"><el-input v-model="form.userName" /></el-form-item>
        <el-form-item label="手机号" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width:120px">
            <el-option label="管理员" value="ADMIN" /><el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessageBox } from 'element-plus'
import { adminListUsers, adminCreateUser, adminResetPassword, adminDeleteUser, formatDateTime, type UserDTO } from '@windloo/shared'
import { notifyError, notifySuccess } from '../../utils/notify'

const list = ref<UserDTO[]>([])
const loading = ref(false)
const page = ref(1); const size = ref(10); const total = ref(0)

async function load() {
  loading.value = true
  try { const p = await adminListUsers(page.value, size.value); list.value = p.list; total.value = p.total }
  catch (e) { notifyError(e) } finally { loading.value = false }
}
function onPage(p: number) { page.value = p; load() }
function onSizeChange(s: number) { size.value = s; page.value = 1; load() }
onMounted(load)

const createVisible = ref(false); const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({ userName: '', phone: '', role: 'USER' })
const rules: FormRules = {
  userName: [{ required: true, message: '必填', trigger: 'blur' }],
  phone: [{ required: true, message: '必填', trigger: 'blur' }, { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
}
function openCreate() { form.userName = ''; form.phone = ''; form.role = 'USER'; createVisible.value = true }
async function submitCreate() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const pwd = await adminCreateUser(form.userName, form.phone, form.role)
      createVisible.value = false
      await ElMessageBox.alert('随机密码：' + pwd, '创建成功，请妥善保存', { confirmButtonText: '知道了' })
      load()
    } catch (e) { notifyError(e) } finally { submitting.value = false }
  })
}
async function resetPwd(id: string) {
  try {
    const pwd = await adminResetPassword(id)
    await ElMessageBox.alert('新随机密码：' + pwd, '重置成功', { confirmButtonText: '知道了' })
  } catch (e) { notifyError(e) }
}
async function remove(id: string) {
  try { await adminDeleteUser(id); notifySuccess('已删除'); load() } catch (e) { notifyError(e) }
}
</script>
<style scoped>
.page { height: 100%; display: flex; flex-direction: column; min-height: 0; }
.toolbar { flex: 0 0 auto; padding: 12px 12px 0; display: flex; gap: 12px; align-items: center; }
.table-wrap { flex: 1 1 auto; overflow: auto; padding: 12px; min-height: 0; }
.pager { flex: 0 0 auto; padding: 8px 12px; border-top: 1px solid #eee; background: #fff; display: flex; align-items: center; gap: 12px; }
</style>
