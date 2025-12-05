import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8123,
    open: true,
  },
  build: {
    outDir: 'dist',
  },
  css: {
    preprocessorOptions: {
      css: {
        additionalData: '@import "./src/assets/base.css";',
      },
    },
  },
});