<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
    <el-breadcrumb-item v-if="info?.parent">{{ info.parent }}</el-breadcrumb-item>
    <el-breadcrumb-item v-if="info?.title">{{ info.title }}</el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { routeTitles } from '../router/titles'
const route = useRoute()
const info = computed(() => routeTitles[route.name as string])
</script>