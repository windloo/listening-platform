import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  base: '/admin/',
  plugins: [vue()],
  server: { port: 5173, proxy: { '/api': { target: 'http://localhost:80', changeOrigin: true } } },
  test: { environment: 'jsdom', globals: true, include: ['src/**/__tests__/**/*.test.ts'] },
})
