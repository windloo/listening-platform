import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: { port: 5174, proxy: { '/api': { target: 'http://localhost:80', changeOrigin: true } } },
  test: { environment: 'jsdom', globals: true, include: ['src/**/__tests__/**/*.test.ts'] },
})