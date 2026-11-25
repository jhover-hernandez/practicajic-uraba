import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// En desarrollo, /api se redirige al backend Spring Boot: así la cookie de renovación es del mismo origen.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    // Si el puerto está ocupado (por ejemplo, por otra copia del proyecto) falla en vez de usar otro en silencio.
    strictPort: true,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
