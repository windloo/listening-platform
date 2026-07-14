<template>
  <span>
    <el-upload :show-file-list="false" :before-upload="handle" :accept="accept">
      <el-button :loading="loading">{{ modelValue ? '已上传·点击替换' : '上传文件' }}</el-button>
    </el-upload>
    <span v-if="modelValue" style="margin-left:8px;color:#999;font-size:12px">{{ modelValue }}</span>
  </span>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadWithDedup } from '@windloo/shared'

defineProps<{ modelValue: string | null | undefined; accept?: string }>()
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()
const loading = ref(false)

async function handle(file: File): Promise<boolean> {
  loading.value = true
  try {
    const item = await uploadWithDedup(file)
    emit('update:modelValue', item.remoteUrl)
    ElMessage.success('上传成功')
  } catch (e: any) { ElMessage.error(e?.message || '上传失败') }
  finally { loading.value = false }
  return false
}
</script>
