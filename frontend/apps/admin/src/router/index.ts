import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { isAuthenticated, hasRole } from '@windloo/shared'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('../views/login/Login.vue'), meta: { public: true } },
  { path: '/403', name: 'forbidden', component: () => import('../views/Forbidden.vue'), meta: { public: true } },
  {
    path: '/admin', component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', name: 'dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'users', name: 'users', component: () => import('../views/identity/Users.vue'), meta: { requiresAdmin: true } },
      { path: 'categories', name: 'categories', component: () => import('../views/listening/Categories.vue') },
      { path: 'albums', name: 'albums', component: () => import('../views/listening/Albums.vue') },
      { path: 'episodes', name: 'episodes', component: () => import('../views/listening/Episodes.vue') },
      { path: 'profile', name: 'profile', component: () => import('../views/profile/Profile.vue') },
    ],
  },
  { path: '/', redirect: '/admin' },
  { path: '/:pathMatch(.*)*', redirect: '/admin' },
]

export const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  if (to.matched.some((r) => r.meta?.public)) {
    if (to.name === 'login' && isAuthenticated()) return { name: 'dashboard' }
    return true
  }
  if (to.matched.some((r) => r.meta?.requiresAuth) && !isAuthenticated())
    return { name: 'login', query: { redirect: to.fullPath } }
  if (to.matched.some((r) => r.meta?.requiresAdmin) && !hasRole('ADMIN'))
    return { name: 'forbidden' }
  return true
})