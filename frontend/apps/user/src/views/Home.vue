<template>
  <div>
    <div class="hero"><h1>英语听力练习平台</h1><p>选择分类开始练习</p></div>
    <el-row :gutter="20">
      <el-col v-for="c in displayedCategories" :key="c.id" :xs="24" :sm="12" :md="8" :lg="6">
        <router-link :to="`/category/${c.id}`">
          <div class="card">
            <el-image v-if="c.coverUrl" :src="c.coverUrl" fit="cover" class="card-img" />
            <div v-else class="card-img placeholder"><el-icon size="40"><Headset /></el-icon></div>
            <div class="card-name">{{ c.nameChinese }}</div>
            <div class="card-name-en">{{ c.nameEnglish }}</div>
          </div>
        </router-link>
      </el-col>
      <el-col v-if="showViewAll" :xs="24" :sm="12" :md="8" :lg="6">
        <div class="card" @click="showAll = true" style="cursor:pointer">
          <div class="card-img placeholder"><el-icon size="40"><More /></el-icon></div>
          <div class="card-name">查看全部分类</div>
          <div class="card-name-en">&nbsp;</div>
        </div>
      </el-col>
    </el-row>
    <el-empty v-if="!loading && categories.length === 0" description="暂无分类" />
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getCategories, type Category } from '@windloo/shared'
import { ElMessage } from 'element-plus'
const categories = ref<Category[]>([])
const loading = ref(true)
const showAll = ref(false)
const displayedCategories = computed(() => showAll.value || categories.value.length <= 8 ? categories.value : categories.value.slice(0, 7))
const showViewAll = computed(() => !showAll.value && categories.value.length > 8)
onMounted(async () => {
  try { categories.value = await getCategories() } catch (e: any) { ElMessage.error(e?.message || '加载失败') } finally { loading.value = false }
})
</script>
<style scoped>
.hero { text-align: center; padding: 40px 0 32px }
.hero h1 { font-size: 28px; color: #333 }
.hero p { color: #999; margin-top: 8px }
.card { background: #fff; border-radius: 12px; overflow: hidden; margin-bottom: 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.08); transition: transform 0.2s, box-shadow 0.2s; cursor: pointer }
.card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.12) }
.card-img { width: 100%; height: 160px; display: block }
.card-img.placeholder { display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea, #764ba2); color: #fff }
.card-name { padding: 12px 12px 4px; font-size: 16px; font-weight: 600 }
.card-name-en { padding: 0 12px 12px; font-size: 13px; color: #999 }
.view-all { display: flex; flex-direction: column; justify-content: center; align-items: center }
</style>