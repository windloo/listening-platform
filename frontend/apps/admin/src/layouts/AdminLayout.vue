<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '210px'" class="aside">
      <div class="logo">{{ collapsed ? 'Y' : 'windloo 管理端' }}</div>
      <el-menu
        :default-active="$route.path" :collapse="collapsed" :collapse-transition="false" router
        background-color="#001529" text-color="#bfcbd9" active-text-color="#ffd04b"
      >
        <el-menu-item index="/admin"><el-icon><HomeFilled /></el-icon><template #title>首页</template></el-menu-item>
        <el-menu-item index="/admin/users" v-if="auth.isAdmin()"><el-icon><User /></el-icon><template #title>用户管理</template></el-menu-item>
        <el-sub-menu index="content">
          <template #title><el-icon><Menu /></el-icon><span>内容管理</span></template>
          <el-menu-item index="/admin/categories"><el-icon><Collection /></el-icon><template #title>分类</template></el-menu-item>
          <el-menu-item index="/admin/albums"><el-icon><FolderOpened /></el-icon><template #title>专辑</template></el-menu-item>
          <el-menu-item index="/admin/episodes"><el-icon><Headset /></el-icon><template #title>单集</template></el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/profile"><el-icon><UserFilled /></el-icon><template #title>个人信息</template></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" /><Expand v-else />
          </el-icon>
          <Breadcrumb />
        </div>
        <el-dropdown @command="onCommand">
          <span class="user">
            <el-avatar :src="auth.user?.avatar || undefined" :size="28" />{{ auth.user?.nickname || auth.user?.userName || '管理员' }}<el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <TagsView />
      <el-main class="main"><router-view /></el-main>
      <el-footer class="footer" height="auto">
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener">京ICP备2026044089号</a>
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useTagsStore } from '../stores/tags'
import Breadcrumb from '../components/Breadcrumb.vue'
import TagsView from '../components/TagsView.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const tags = useTagsStore()
const collapsed = ref(false)

onMounted(() => { if (!auth.user) auth.loadUser().catch(() => {}) })
watch(() => route.path, () => { tags.addView(route) }, { immediate: true })

function onCommand(cmd: string) { if (cmd === 'logout') { auth.logout(); router.push('/login') } }
</script>

<style scoped>
.layout { height: 100vh }
.main { padding: 0; overflow: hidden; }
.aside { background: #001529; transition: width 0.2s; overflow-x: hidden }
.logo { height: 56px; color: #fff; font-size: 16px; line-height: 56px; text-align: center; white-space: nowrap; overflow: hidden }
.header { display: flex; align-items: center; justify-content: space-between; background: #fff; border-bottom: 1px solid #eee }
.header-left { display: flex; align-items: center; gap: 16px }
.collapse-btn { font-size: 20px; cursor: pointer }
.user { display: inline-flex; align-items: center; gap: 8px; cursor: pointer; outline: none }
.footer { text-align: center; padding: 8px 0; color: #999; font-size: 12px }
.footer a { color: #999; text-decoration: none }
</style>