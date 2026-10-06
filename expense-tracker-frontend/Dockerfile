# ---- Build stage ----
    FROM node:18-alpine AS build
    WORKDIR /app
    COPY package*.json ./
    RUN npm install --include=dev
    COPY . .
    RUN npm run build
    
    # ---- Runtime stage ----
    FROM nginx:alpine
    COPY --from=build /app/dist/expense-tracker-frontend/browser /usr/share/nginx/html
    COPY nginx.conf /etc/nginx/conf.d/default.conf
    EXPOSE 80