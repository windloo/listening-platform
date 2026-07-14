<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="categoryId" placeholder="选择分类" style="width:200px" @change="onCategoryChange">
        <el-option v-for="c in categories" :key="c.id" :label="c.nameChinese" :value="c.id" />
      </el-select>
      <el-button type="primary" :disabled="!categoryId" @click="openCreate">新建专辑</el-button>
    </div>
    <div class="table-wrap">
    <el-table :data="paged" border v-loading="loading">
      <el-table-column type="index" label="序号" :index="globalIndex" width="70" />
      <el-table-column prop="nameChinese" label="中文名" />
      <el-table-column prop="nameEnglish" label="英文名" />
      <el-table-column label="可见" width="90">
        <template #default="{ $index }">
          <el-switch v-if="isAdmin" :model-value="paged[$index].isVisible" @change="(v: string | number | boolean) => toggleVisible(paged[$index], !!v)" />
          <span v-else>{{ paged[$index].isVisible ? '可见' : '隐藏' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑专辑' : '新建专辑'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属分类"><el-input :model-value="categoryName(form.categoryId)" disabled /></el-form-item>
        <el-form-item label="中文名" prop="nameChinese"><el-input v-model="form.nameChinese" /></el-form-item>
        <el-form-item label="英文名" prop="nameEnglish"><el-input v-model="form.nameEnglish" /></el-form-item>
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
  getCategories, adminListAlbums, adminCreateAlbum, adminUpdateAlbum, adminDeleteAlbum, adminSortAlbums, adminShowAlbum, adminHideAlbum,
  type Category, type Album, type AlbumReq,
} from '@windloo/shared'
import { notifyError, notifySuccess } from '../../utils/notify'
import { useAuthStore } from '../../stores/auth'
const auth = useAuthStore()
const isAdmin = auth.isAdmin()
const myId = auth.userId

const categories = ref<Category[]>([])
const categoryId = ref<string | null>(null)
const list = ref<Album[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const paged = computed(() => list.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))
function globalIndex(i: number) { return (currentPage.value - 1) * pageSize.value + i + 1 }
function isLast(i: number) { return (currentPage.value - 1) * pageSize.value + i === list.value.length - 1 }
function onSizeChange(s: number) { pageSize.value = s; currentPage.value = 1 }
function categoryName(id: string) { return categories.value.find((c) => c.id === id)?.nameChinese ?? '' }

async function loadCats() {
  try { categories.value = await getCategories() } catch (e) { notifyError(e) }
  if (categories.value.length > 0) { categoryId.value = categories.value[0].id; await load() }
}
async function load() {
  if (!categoryId.value) return
  loading.value = true
  try { list.value = await adminListAlbums(categoryId.value); currentPage.value = 1 } catch (e) { notifyError(e) } finally { loading.value = false }
}
function onCategoryChange() { load() }
onMounted(loadCats)

async function toggleVisible(a: Album, v: boolean) {
  try { v ? await adminShowAlbum(a.id) : await adminHideAlbum(a.id); a.isVisible = v } catch (e) { notifyError(e) }
}

async function move(index: number, delta: number) {
  const i = (currentPage.value - 1) * pageSize.value + index
  const j = i + delta
  if (j < 0 || j >= list.value.length) return
  const arr = [...list.value]
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
  try { await adminSortAlbums(arr.map((a) => a.id)); list.value = arr; notifySuccess('排序已更新') }
  catch (e) { notifyError(e); load() }
}

const dialogVisible = ref(false); const submitting = ref(false); const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<AlbumReq>({ nameChinese: '', nameEnglish: '', categoryId: '' })
const rules: FormRules = {
  nameChinese: [{ required: true, message: '必填', trigger: 'blur' }],
  nameEnglish: [{ required: true, message: '必填', trigger: 'blur' }],
}
function openCreate() { if (!categoryId.value) return; editingId.value = null; form.nameChinese = ''; form.nameEnglish = ''; form.categoryId = categoryId.value; dialogVisible.value = true }
function openEdit(idx: number) {
  const a = paged.value[idx]
  editingId.value = a.id; form.nameChinese = a.nameChinese; form.nameEnglish = a.nameEnglish; form.categoryId = a.categoryId; dialogVisible.value = true
}
async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editingId.value) await adminUpdateAlbum(editingId.value, { ...form })
      else await adminCreateAlbum({ ...form })
      dialogVisible.value = false; notifySuccess(); load()
    } catch (e) { notifyError(e) } finally { submitting.value = false }
  })
}
async function remove(idx: number) {
  const a = paged.value[idx]
  try { await adminDeleteAlbum(a.id); notifySuccess('已删除'); load() } catch (e) { notifyError(e) }
}
</script>
<style scoped>
.page { height: 100%; display: flex; flex-direction: column; min-height: 0; }
.toolbar { flex: 0 0 auto; padding: 12px 12px 0; display: flex; gap: 12px; align-items: center; }
.table-wrap { flex: 1 1 auto; overflow: auto; padding: 12px; min-height: 0; }
.pager { flex: 0 0 auto; padding: 8px 12px; border-top: 1px solid #eee; background: #fff; display: flex; align-items: center; gap: 12px; }
</style>