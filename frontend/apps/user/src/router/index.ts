import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'
import { isAuthenticated } from '@windloo/shared'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/category/:id', name: 'category', component: () => import('../views/Category.vue') },
  { path: '/album/:id', name: 'album', component: () => import('../views/Album.vue') },
  { path: '/episode/:id', name: 'episode', component: () => import('../views/Episode.vue') },
  { path: '/search', name: 'search', component: () => import('../views/Search.vue') },
  { path: '/login', name: 'login', component: () => import('../views/login/Login.vue'), meta: { public: true } },
  { path: '/chat', name: 'chat', component: () => import('../views/Chat.vue'), meta: { requiresAuth: true } },
]

export const router = createRouter({ history: createWebHistory(), routes })

export function routeGuard(to: RouteLocationNormalized): boolean | { name: string; query: { redirect: string } } {
  if (to.matched.some((r) => r.meta?.public)) return true
  if (to.matched.some((r) => r.meta?.requiresAuth) && !isAuthenticated())
    return { name: 'login', query: { redirect: to.fullPath } }
  return true
}
router.beforeEach(routeGuard)