import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    createSvgIconsPlugin({
      iconDirs: [path.resolve(process.cwd(), 'src/assets/icons')],
      symbolId: 'icon-[name]'
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    chunkSizeWarningLimit: 700,
    rollupOptions: {
      output: {
        manualChunks(id) {
          const normalizedId = id.replace(/\\/g, '/')
          if (!normalizedId.includes('/node_modules/')) {
            return
          }

          if (
            normalizedId.includes('/naive-ui/') ||
            normalizedId.includes('/vue/') ||
            normalizedId.includes('/@vue/') ||
            normalizedId.includes('/vueuc/') ||
            normalizedId.includes('/vooks/') ||
            normalizedId.includes('/vdirs/') ||
            normalizedId.includes('/seemly/') ||
            normalizedId.includes('/treemate/') ||
            normalizedId.includes('/evtd/') ||
            normalizedId.includes('/css-render/')
          ) {
            return 'vendor-ui'
          }
          if (normalizedId.includes('/@vicons/')) {
            return 'vendor-icons'
          }
          if (
            normalizedId.includes('/echarts/') ||
            normalizedId.includes('/zrender/') ||
            normalizedId.includes('/vue-echarts/')
          ) {
            return 'vendor-echarts'
          }
          if (normalizedId.includes('/vue-router/')) {
            return 'vendor-router'
          }
          if (normalizedId.includes('/pinia/')) {
            return 'vendor-store'
          }
          if (normalizedId.includes('/dayjs/')) {
            return 'vendor-dayjs'
          }
          if (
            normalizedId.includes('/axios/') ||
            normalizedId.includes('/nprogress/') ||
            normalizedId.includes('/js-cookie/')
          ) {
            return 'vendor-utils'
          }
        },
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    }
  }
})
