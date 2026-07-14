<template>
  <div class="page">
    <div class="toolbar">
      <el-select v-model="categoryId" placeholder="选择分类" style="width:180px" @change="onCategoryChange">
        <el-option v-for="c in categories" :key="c.id" :label="c.nameChinese" :value="c.id" />
      </el-select>
      <el-select v-model="albumId" placeholder="选择专辑" style="width:200px" :disabled="!categoryId" @change="onAlbumChange">
        <el-option v-for="a in albums" :key="a.id" :label="a.nameChinese" :value="a.id" />
      </el-select>
      <el-button type="primary" :disabled="!albumId" @click="openCreate">新建单集</el-button>
    </div>
    <div class="table-wrap">
    <el-table :data="paged" border v-loading="loading">
      <el-table-column type="index" label="序号" :index="globalIndex" width="70" />
      <el-table-column prop="nameChinese" label="中文名" />
      <el-table-column prop="nameEnglish" label="英文名" />
      <el-table-column label="时长" width="90">
        <template #default="{ $index }">{{ formatDuration(paged[$index].durationInSecond) }}</template>
      </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑单集' : '新建单集'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="所属专辑"><el-input :model-value="albumName(form.albumId)" disabled /></el-form-item>
        <el-form-item label="中文名" prop="nameChinese"><el-input v-model="form.nameChinese" /></el-form-item>
        <el-form-item label="英文名" prop="nameEnglish"><el-input v-model="form.nameEnglish" /></el-form-item>
        <el-form-item label="音频">
          <el-upload :show-file-list="false" :before-upload="onAudioPick" accept="audio/*">
            <el-button :loading="uploading">{{ form.audioUrl ? '已上传·点击替换' : '选择音频上传' }}</el-button>
          </el-upload>
          <span v-if="form.audioUrl" style="margin-left:8px;color:#999;font-size:12px">{{ form.audioUrl }}</span>
        </el-form-item>
        <el-form-item label="时长(秒)"><el-input-number v-model="form.durationInSecond" :min="0" :precision="1" /></el-form-item>
        <el-form-item label="字幕" prop="subtitle"><el-input v-model="form.subtitle" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="字幕类型" prop="subtitleType">
          <el-select v-model="form.subtitleType" style="width:160px">
            <el-option value="SRT" /><el-option value="LRC" /><el-option value="JSON" />
          </el-select>
        </el-form-item>
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
import { ElMessage } from 'element-plus'
import {
  getCategories, adminListAlbums, adminListEpisodes, adminCreateEpisode, adminUpdateEpisode, adminDeleteEpisode,
  adminSortEpisodes, adminShowEpisode, adminHideEpisode, uploadWithDedup, formatDuration,
  type Category, type Album, type Episode, type EpisodeReq,
} from '@windloo/shared'
import { notifyError, notifySuccess } from '../../utils/notify'
import { useAuthStore } from '../../stores/auth'
const auth = useAuthStore()
const isAdmin = auth.isAdmin()
const myId = auth.userId

const categories = ref<Category[]>([])
const albums = ref<Album[]>([])
const categoryId = ref<string | null>(null)
const albumId = ref<string | null>(null)
const list = ref<Episode[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const paged = computed(() => list.value.slice((currentPage.value - 1) * pageSize.value, currentPage.value * pageSize.value))
function globalIndex(i: number) { return (currentPage.value - 1) * pageSize.value + i + 1 }
function isLast(i: number) { return (currentPage.value - 1) * pageSize.value + i === list.value.length - 1 }
function onSizeChange(s: number) { pageSize.value = s; currentPage.value = 1 }
function albumName(id: string) { return albums.value.find((a) => a.id === id)?.nameChinese ?? '' }

async function loadCats() {
  try { categories.value = await getCategories() } catch (e) { notifyError(e) }
  if (categories.value.length > 0) {
    categoryId.value = categories.value[0].id
    await loadAlbums()
    if (albums.value.length > 0) { albumId.value = albums.value[0].id; await load() }
  }
}
async function loadAlbums() {
  albumId.value = null; list.value = []
  if (!categoryId.value) { albums.value = []; return }
  try { albums.value = await adminListAlbums(categoryId.value) } catch (e) { notifyError(e); albums.value = [] }
}
async function onCategoryChange() {
  currentPage.value = 1
  await loadAlbums()
  if (albums.value.length > 0) { albumId.value = albums.value[0].id; await load() }
}
async function onAlbumChange() { currentPage.value = 1; await load() }
async function load() {
  if (!albumId.value) return
  loading.value = true
  try { list.value = await adminListEpisodes(albumId.value); currentPage.value = 1 } catch (e) { notifyError(e) } finally { loading.value = false }
}
onMounted(loadCats)

async function toggleVisible(ep: Episode, v: boolean) {
  try { v ? await adminShowEpisode(ep.id) : await adminHideEpisode(ep.id); ep.isVisible = v } catch (e) { notifyError(e) }
}
async function move(index: number, delta: number) {
  const i = (currentPage.value - 1) * pageSize.value + index
  const j = i + delta
  if (j < 0 || j >= list.value.length) return
  const arr = [...list.value]
  ;[arr[i], arr[j]] = [arr[j], arr[i]]
  try { await adminSortEpisodes(arr.map((e) => e.id)); list.value = arr; notifySuccess('排序已更新') }
  catch (e) { notifyError(e); load() }
}

const dialogVisible = ref(false); const submitting = ref(false); const uploading = ref(false); const editingId = ref<string | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<EpisodeReq>({ nameChinese: '', nameEnglish: '', albumId: '', audioUrl: '', durationInSecond: 0, subtitle: '', subtitleType: 'SRT' })
const rules: FormRules = {
  nameChinese: [{ required: true, message: '必填', trigger: 'blur' }],
  nameEnglish: [{ required: true, message: '必填', trigger: 'blur' }],
  audioUrl: [{ required: true, message: '请上传音频', trigger: 'change' }],
  subtitle: [{ required: true, message: '必填', trigger: 'blur' }],
  subtitleType: [{ required: true, message: '必填', trigger: 'change' }],
}
function openCreate() { if (!albumId.value) return; editingId.value = null; Object.assign(form, { nameChinese: '', nameEnglish: '', albumId: albumId.value, audioUrl: '', durationInSecond: 0, subtitle: '', subtitleType: 'SRT' }); dialogVisible.value = true }
function openEdit(idx: number) {
  const ep = paged.value[idx]
  editingId.value = ep.id
  Object.assign(form, { nameChinese: ep.nameChinese, nameEnglish: ep.nameEnglish, albumId: ep.albumId, audioUrl: ep.audioUrl, durationInSecond: ep.durationInSecond, subtitle: ep.subtitle ?? '', subtitleType: ep.subtitleType })
  dialogVisible.value = true
}
async function onAudioPick(file: File): Promise<boolean> {
  uploading.value = true
  try {
    const item = await uploadWithDedup(file)
    form.audioUrl = item.remoteUrl
    const url = URL.createObjectURL(file)
    const audio = new Audio(url)
    audio.onloadedmetadata = () => { form.durationInSecond = Math.round(audio.duration * 10) / 10; URL.revokeObjectURL(url) }
    ElMessage.success('音频上传成功')
  } catch (e: any) { ElMessage.error(e?.message || '上传失败') }
  finally { uploading.value = false }
  return false
}
async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (editingId.value) await adminUpdateEpisode(editingId.value, { ...form })
      else await adminCreateEpisode({ ...form })
      dialogVisible.value = false; notifySuccess(); load()
    } catch (e) { notifyError(e) } finally { submitting.value = false }
  })
}
async function remove(idx: number) {
  const ep = paged.value[idx]
  try { await adminDeleteEpisode(ep.id); notifySuccess('已删除'); load() } catch (e) { notifyError(e) }
}
</script>
<style scoped>
.page { height: 100%; display: flex; flex-direction: column; min-height: 0; }
.toolbar { flex: 0 0 auto; padding: 12px 12px 0; display: flex; gap: 12px; align-items: center; }
.table-wrap { flex: 1 1 auto; overflow: auto; padding: 12px; min-height: 0; }
.pager { flex: 0 0 auto; padding: 8px 12px; border-top: 1px solid #eee; background: #fff; display: flex; align-items: center; gap: 12px; }
</style>