# Build stage
FROM node:19-alpine as build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL=http://localhost:8080/api
ARG VITE_GOOGLE_CLIENT_ID=916866870969-dslc13bd9itnhmjkl54usgskb1gsflvl.apps.googleusercontent.com
ARG VITE_ENCRYPTION_SECRET_KEY=LruA6WtZhsQJlqWdcwo6D6/JCz9fa0I5XzGYwcV7Pv0=
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

