# ===== 构建阶段：pnpm 编译前端 =====
FROM node:18-alpine AS builder
WORKDIR /build
RUN npm install -g pnpm
COPY pnpm-workspace.yaml package.json .npmrc ./
COPY apps/admin/package.json apps/admin/package.json
COPY apps/user/package.json apps/user/package.json
COPY packages/shared/package.json packages/shared/package.json
RUN pnpm install --no-frozen-lockfile
COPY . .
RUN pnpm -F admin build && pnpm -F user build

# ===== 运行阶段：Nginx =====
FROM nginx:alpine
COPY --from=builder /build/apps/admin/dist /usr/share/nginx/html/admin
COPY --from=builder /build/apps/user/dist /usr/share/nginx/html/user
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制 SSL 证书到镜像中
COPY ssl/windloo.top_bundle.crt /etc/nginx/ssl/windloo.top_bundle.crt
COPY ssl/windloo.top.key /etc/nginx/ssl/windloo.top.key