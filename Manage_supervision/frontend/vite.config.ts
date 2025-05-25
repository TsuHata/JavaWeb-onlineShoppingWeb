import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    allowedHosts: ['frp-oil.com'],
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        configure: (proxy, _options) => {
          proxy.on('error', (err, _req, _res) => {
            console.log('proxy error', err);
          });
          proxy.on('proxyReq', (_proxyReq, req, _res) => {
            console.log('发送请求:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyRes, req, _res) => {
            console.log('收到响应:', proxyRes.statusCode, req.url);
          });
        }
      },
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      },
      '/ws': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        ws: true  // 启用WebSocket代理
      }
    }
  },
  define: {
    global: 'window'
  }
})
