<template>
  <div class="page" style="padding:16px;overflow:auto;height:100%">
    <el-card style="margin-bottom:16px">当前用户：{{ auth.user?.nickname || auth.user?.userName || '-' }}（{{ auth.user?.userName }}）</el-card>
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="isAdmin ? 6 : 8"><el-card><div>分类数</div><h2>{{ lStats?.categoryCount ?? '-' }}</h2></el-card></el-col>
      <el-col :span="isAdmin ? 6 : 8"><el-card><div>专辑数</div><h2>{{ lStats?.albumCount ?? '-' }}</h2></el-card></el-col>
      <el-col :span="isAdmin ? 6 : 8"><el-card><div>单集数</div><h2>{{ lStats?.episodeCount ?? '-' }}</h2></el-card></el-col>
      <el-col :span="6" v-if="isAdmin"><el-card><div>用户数</div><h2>{{ iStats?.total ?? '-' }}</h2></el-card></el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="12"><el-card><template #header>各分类专辑数</template><v-chart :option="albumBar" autoresize style="height:300px" /></el-card></el-col>
      <el-col :span="12"><el-card><template #header>各分类单集数</template><v-chart :option="episodeBar" autoresize style="height:300px" /></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, DataZoomComponent } from 'echarts/components'
import { useAuthStore } from '../stores/auth'
import { getListeningStats, getIdentityStats, type ListeningStats, type IdentityStats } from '@windloo/shared'
import { notifyError } from '../utils/notify'

use([CanvasRenderer, BarChart, GridComponent, TooltipComponent, DataZoomComponent])

const auth = useAuthStore()
const isAdmin = auth.isAdmin()
const lStats = ref<ListeningStats>()
const iStats = ref<IdentityStats>()

const cats = computed(() => lStats.value?.categories ?? [])
const albumBar = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: cats.value.map((c) => c.name) },
  yAxis: { type: 'value' },
  dataZoom: [{ type: 'slider', xAxisIndex: 0, height: 12 }, { type: 'inside', xAxisIndex: 0 }],
  series: [{ type: 'bar', data: cats.value.map((c) => c.albumCount) }],
}))
const episodeBar = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: cats.value.map((c) => c.name) },
  yAxis: { type: 'value' },
  dataZoom: [{ type: 'slider', xAxisIndex: 0, height: 12 }, { type: 'inside', xAxisIndex: 0 }],
  series: [{ type: 'bar', data: cats.value.map((c) => c.episodeCount) }],
}))

onMounted(async () => {
  try { lStats.value = await getListeningStats() } catch (e) { notifyError(e) }
  if (isAdmin) { try { iStats.value = await getIdentityStats() } catch (e) { notifyError(e) } }
})
</script>