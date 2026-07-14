import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/category/:id', name: 'category', component: () => import('../views/Category.vue') },
  { path: '/album/:id', name: 'album', component: () => import('../views/Album.vue') },
  { path: '/episode/:id', name: 'episode', component: () => import('../views/Episode.vue') },
  { path: '/search', name: 'search', component: () => import('../views/Search.vue') },
]

export const router = createRouter({ history: createWebHistory(), routes })