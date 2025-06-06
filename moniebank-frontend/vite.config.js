import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        // Ensure cookies are forwarded
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            // Forward cookies
            if (req.headers.cookie) {
              proxyReq.setHeader('cookie', req.headers.cookie);
            }
          });

          proxy.on('proxyRes', (proxyRes, req, res) => {
            // Handle set-cookie headers from backend
            if (proxyRes.headers['set-cookie']) {
              // Ensure cookies are properly set in the browser
              res.setHeader('set-cookie', proxyRes.headers['set-cookie']);
            }
          });
        }
      }
    }
  },
  define: {
    // Make environment variables available
    'process.env': process.env
  }
})