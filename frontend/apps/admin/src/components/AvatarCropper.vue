<template>
  <el-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" title="剪裁头像" width="420px" @opened="init" @closed="destroy">
    <div style="width:340px;height:340px">
      <img ref="imgRef" :src="src" style="max-width:100%;display:block" />
    </div>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" @click="confirm">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

const props = defineProps<{ modelValue: boolean; src: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: boolean): void; (e: 'confirm', blob: Blob): void }>()
const imgRef = ref<HTMLImageElement>()
let cropper: Cropper | null = null

function init() {
  if (imgRef.value) cropper = new Cropper(imgRef.value, { aspectRatio: 1, viewMode: 1, autoCropArea: 0.9 })
}
function destroy() { if (cropper) { cropper.destroy(); cropper = null } }
function confirm() {
  if (!cropper) return
  cropper.getCroppedCanvas({ width: 200, height: 200 }).toBlob((blob) => {
    if (blob) { emit('confirm', blob); emit('update:modelValue', false) }
  }, 'image/png')
}
onBeforeUnmount(destroy)
</script>