# Build stage
FROM node:18-alpine AS builder

WORKDIR /app

ARG VITE_API_IDENTITY_URL=http://localhost:8081
ARG VITE_API_USUARIO_URL=http://localhost:8082
ARG VITE_API_ENCOMENDA_URL=http://localhost:8083
ARG VITE_API_NOTIFICACAO_URL=http://localhost:8084

ENV VITE_API_IDENTITY_URL=$VITE_API_IDENTITY_URL
ENV VITE_API_USUARIO_URL=$VITE_API_USUARIO_URL
ENV VITE_API_ENCOMENDA_URL=$VITE_API_ENCOMENDA_URL
ENV VITE_API_NOTIFICACAO_URL=$VITE_API_NOTIFICACAO_URL

# Copy package files
COPY package.json package-lock.json* ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built assets from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Ajusta nginx para rodar como usuario nao-root (PID em diretorio gravavel)
RUN sed -i 's|^user .*;|# user disabled for non-root runtime;|' /etc/nginx/nginx.conf && \
    sed -i 's|pid[[:space:]]*/run/nginx.pid;|pid /tmp/nginx.pid;|' /etc/nginx/nginx.conf

# Create non-root user
RUN addgroup -g 1001 -S nodejs && \
    adduser -S nextjs -u 1001

# Change ownership of the nginx directory
RUN chown -R nextjs:nodejs /usr/share/nginx/html && \
    chown -R nextjs:nodejs /var/cache/nginx && \
    chown -R nextjs:nodejs /var/log/nginx && \
    chown -R nextjs:nodejs /etc/nginx/conf.d

# Switch to non-root user
USER nextjs

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/ || exit 1

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
