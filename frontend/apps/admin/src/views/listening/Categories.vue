<template>
  <div class="page">
    <div class="toolbar"><el-button type="primary" @click="openCreate">新建分类</el-button></div>
    <div class="table-wrap">
    <el-table :data="paged" border v-loading="loading">
      <el-table-column type="index" label="序号" :index="globalIndex" width="70" />
      <el-table-column prop="nameChinese" label="中文名" />
      <el-table-column prop="nameEnglish" label="英文名" />
      <el-table-column label="封面" width="90">
        <template #default="{ $index }">
          <el-image v-if="paged[$index].coverUrl" :src="paged[$index].coverUrl" style="width:50px;height:50px" fit="cover" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ $index }">
          <el-button size="small" v-if="isAdmin" :disabled="$index === 0 && currentPage === 1" @click="move($index, -1)">上移</el-button>
          <el-button size="small" v-if="isAdmin" :disabled="isLast($index)" @click="move($index, 1)">下移</el-button>
          <el-button size="small" v-if="isAdmin || paged[$index].createdBy === myId" @click="openEdit($index)">编辑</el-button>
          <el-popconfirm v-if="isAdmin" title="确定删除？" @confirm="remove($index)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    </div>
    <div class="pager">
      <span style="color:#606266">总计 {{ list.length }} 条</span>
      <el-pagination :current-page="currentPage" :page-size="pageSize" :page-sizes="[10,20,30,50]" :total="list.length" layout="sizes, prev, pager, next" @size-change="onSizeChange" @current-change="currentPage = $event" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新建分类'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="中文名" prop="nameChinese"><el-input v-model="form.nameChinese" /></el-form-item>
        <el-form-item label="英文名" prop="nameEnglish"><el-input v-model="form.nameEnglish" /></el-form-item>
        <el-form-item label="封面"><FileUploadButton v-model="form.coverUrl" accept="image/*" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import {
  getCategories, adminCreateCategory, adminUpdateCategory, adminDeleteCategory, adminSortCategories,
  type Category, type CategoryReq,
} from '@windloo/shared'
import FileUploadButton from '../../components/FileUploadButton.vue'
import { notifyError, notifySuccess } from '../../utils/notify'
import { useAuthStore } from '../../stores/auth'
const auth = useAuthStore()
const isAdmin = auth.isAdmin()
const myId = auth.userId

const list = ref<Category[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const paged = computed(() => list.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))
function globalIndex(i: number) { return (currentPage.value - 1) * pageSize.value + i + 1 }
function isLast(i: number) { return (currentPage.value - 1) * pageSize.value + i === list.value.length - 1 }
function onSizeChange(s: number) { pageSize.value = s; currentPage.value = 1 }

async function load() { loading.value = true; try { list.value = await getCategories() } catch (e) { notifyError(e) } finally { loading.value = false } }
onMounted(load)

async function move(index: number, delta: number) {
  const i = (currentPage.value - 1) * pageSize.value + index
  const j = i + delta
  if (j < 0 || j >= list.value.length) return
  const arr = [...list.value]
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
  try { await adminSortCategories(arr.map((c) => c.id)); list.value = arr; notifySuccess('排序已更新') }
  catch (e) { notifyError(e); load() }
}

const dialogVisible = ref(false); const submitting = ref(false); const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<CategoryReq>({ nameChinese: '', nameEnglish: '', coverUrl: null })
const rules: FormRules = {
  nameChinese: [{ required: true, message: '必填', trigger: 'blur' }],
  nameEnglish: [{ required: true, message: '必填', trigger: 'blur' }],
}
function openCreate() { editingId.value = null; form.nameChinese = ''; form.nameEnglish = ''; form.coverUrl = null; dialogVisible.value = true }
function openEdit(idx: number) { const c = paged.value[idx]; editingId.value = c.id; form.nameChinese = c.nameChinese; form.nameEnglish = c.nameEnglish; form.coverUrl = c.coverUrl; dialogVisible.value = true }
async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editingId.value) await adminUpdateCategory(editingId.value, { ...form })
      else await adminCreateCategory({ ...form })
      dialogVisible.value = false; notifySuccess(); load()
    } catch (e) { notifyError(e) } finally { submitting.value = false }
  })
}
async function remove(idx: number) { const c = paged.value[idx]; try { await adminDeleteCategory(c.id); notifySuccess('已删除'); load() } catch (e) { notifyError(e) } }
</script>
<style scoped>
.page { height: 100%; display: flex; flex-direction: column; min-height: 0; }
.toolbar { flex: 0 0 auto; padding: 12px 12px 0; display: flex; gap: 12px; align-items: center; }
.table-wrap { flex: 1 1 auto; overflow: auto; padding: 12px; min-height: 0; }
.pager { flex: 0 0 auto; padding: 8px 12px; border-top: 1px solid #eee; background: #fff; display: flex; align-items: center; gap: 12px; }
</style>