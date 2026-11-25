# RNF-13 · Imagen del frontend: compila con Node y sirve los archivos estáticos con Nginx,
# que además redirige /api al backend (mismo origen para la cookie de sesión).
FROM node:22.12.0-alpine AS compilacion
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27.3-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=compilacion /app/dist /usr/share/nginx/html
EXPOSE 80
