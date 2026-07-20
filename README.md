# windloo · 英语听力练习平台

> 前端演示地址：[windloo 听力平台](https://windloo.top/)
>
> 管理端演示地址：[https://windloo.top/admin/](http://82.156.126.145/admin/)

## 项目演示

## 前端

### 首页

![](https://github-production-user-asset-6210df.s3.amazonaws.com/101187942/621473496-f1fbd86c-c40c-42a1-b847-dd1ac701df6a.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20260714%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260714T124651Z&X-Amz-Expires=300&X-Amz-Signature=a1f08bf8313fcd576c70315a7a2e2b6c087f845e9773ae79f322d8775433b562&X-Amz-SignedHeaders=host&response-content-type=image%2Fpng)

### elasticsearch搜索页

![](https://github-production-user-asset-6210df.s3.amazonaws.com/101187942/621473546-1b289792-efa5-43e1-9224-be7344005da5.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20260714%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260714T124637Z&X-Amz-Expires=300&X-Amz-Signature=cbf1817a587e1968cfa1b46e49b7550819952780abc33ac53f906535127f9adf&X-Amz-SignedHeaders=host&response-content-type=image%2Fpng)

### 单集

![](https://github-production-user-asset-6210df.s3.amazonaws.com/101187942/621473560-873c83f1-1767-4fbf-91c0-dd3219de998c.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20260714%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260714T124459Z&X-Amz-Expires=300&X-Amz-Signature=0c076f99f59cfe7a6d63b96bff8f6f420c684db32311301c726d36d0e01039e4&X-Amz-SignedHeaders=host&response-content-type=image%2Fpng)

## 后端

![](https://github-production-user-asset-6210df.s3.amazonaws.com/101187942/621473117-47e51386-fd43-4b94-83ee-c1c156dac843.png?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAVCODYLSA53PQK4ZA%2F20260714%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20260714T124438Z&X-Amz-Expires=300&X-Amz-Signature=1c7435090e3a673fbe12e5d53413f4a31bba9924c2daf4136adc01fd1abfa7ba&X-Amz-SignedHeaders=host&response-content-type=image%2Fpng)

## 为了系统学习 Spring Cloud Alibaba 微服务体系，本项目围绕一个结构相对简单的英语听力平台业务，引入 Gateway、Nacos、Redis、Seata、Sentinel 等组件，重点实践现代 Java 微服务开发流程，而非追求复杂业务本身。

平台以「分类 → 专辑 → 单集」三级结构组织听力内容，单集页面提供音频与字幕逐句同步播放，支持句子收藏、全文搜索跳转定位等练习功能。

---

## 技术选型与设计思路

### 为什么用 Gateway 而不是 Nginx 做统一鉴权？

所有请求都经过 Gateway，JWT 在网关统一校验，校验成功后通过 Header 透传用户身份（`X-User-Id`、`X-User-Roles`），业务服务无需再次解析 JWT，也无需引入 JWT 依赖。如果用 Nginx 做鉴权，需要写 Lua 脚本或额外搭建鉴权服务，不如 Spring Cloud Gateway 与 Java 生态融合自然。

### 为什么用 Elasticsearch 而不是 MySQL LIKE？

字幕全文搜索需要支持「输入一句话 → 找到对应字幕 → 跳转播放位置」。MySQL LIKE 虽然可以完成匹配，但无法支持分词、相关度排序，且数据量增长后性能下降明显。采用 ES 的倒排索引完成全文检索，搜索结果直接返回带时间戳的句子，前端通过 `?t=startMs` 参数精确定位播放位置。

### 为什么用 Seata AT 模式？

创建单集时需要同时写 listening 库（episode 表）和 encoder 库（encoding_item 表），这是跨库操作。如果转码任务提交失败，已创建的单集应该回滚。Seata AT 模式通过 `@GlobalTransactional` 注解实现自动回滚，无需手动编写补偿逻辑。之所以用 AT 而非 TCC/SAGA，是因为 AT 模式对业务代码侵入最小，只需加注解。

> Seata 在 Spring Bean 创建阶段就检查 `undo_log` 表是否存在，而 Flyway 要等 DataSource 就绪后才执行迁移。两者存在启动顺序冲突，应在服务启动前由初始化脚本预建 `undo_log` 表。

### 为什么用 Flyway 管理数据库版本？

每个微服务有独立的数据库，数据库变更需要可追溯、可回滚、可重复执行。Flyway 的 SQL 脚本随代码一起版本管理，部署时自动执行迁移，避免手动执行 SQL 的不确定性。配合 `baseline-on-migrate` 和 `baseline-version: 0`，即使数据库中已有非 Flyway 管理的表（如 Seata 的 `undo_log`），也不会跳过业务表的迁移。

### 为什么用 Feign + LoadBalancer 而不是 RestTemplate？

服务间调用（如 listening-service 调 search-service 索引数据）使用 OpenFeign 声明式接口，代码可读性好，且自带 Fallback 降级。例如搜索服务不可用时，Feign Fallback 返回空结果而不抛异常，保证主业务（单集创建）不受搜索索引失败影响。

### 为什么前端用 pnpm Monorepo？

管理后台和用户端共享 API 客户端、类型定义、工具函数。Monorepo 下提取 `@windloo/shared` 包，两个应用直接 import，避免代码复制。pnpm 的符号链接机制比 npm/yarn 更节省磁盘空间。

---

## 核心功能实现

### 字幕同步播放

根据 HTML5 Audio `currentTime` 事件，与字幕句子的 `startMs`/`endMs` 时间戳比对，高亮当前播放句子。用户点击「这句听不懂」可收藏当前句子到本地，点击收藏的句子直接跳转到对应播放位置。

### 文件秒传

上传文件时先在前端计算 SHA-256（利用 Web Crypto API），调用服务端查重接口判断是否已存在相同文件。如果命中则直接返回已存储的 URL，跳过上传过程，实现秒传。服务端在上传时也会计算 SHA-256 作为二次校验。

> HTTP 环境下 `crypto.subtle` 不可用（需要安全上下文），代码中通过 `window.isSecureContext` 判断，HTTP 下跳过去重直接上传。

### 网关鉴权链路

```
用户请求 → Gateway 拦截 → 校验 JWT
  ├─ 白名单路径（/api/identity/login 等）→ 直接放行
  └─ 需认证路径 → 解析 JWT → 注入 X-User-Id / X-User-Roles Header → 转发
       └─ 业务服务 → HeaderAuthenticationFilter 读取 Header → 设置 SecurityContext
            └─ @PreAuthorize("hasRole('ADMIN')") → 方法级权限校验
```

### 搜索索引同步

单集创建/更新时，listening-service 发布 `EpisodeUpsertedEvent`，`@Async` 事件监听器通过 Feign 调用 search-service 的索引接口，将字幕句子（带时间戳）写入 ES 的 Nested 类型字段。删除单集时同步删除索引。

---

## 系统架构

```
                         ┌─────────────────────────────────────────┐
                         │           前端 (Vue 3 Monorepo)          │
                         │  apps/admin (管理端)   apps/user (用户端) │
                         │           packages/shared (共享包)        │
                         └───────────────────┬─────────────────────┘
                                             │  HTTP / JWT
                         ┌───────────────────▼─────────────────────┐
                         │       Gateway (Spring Cloud Gateway)     │
                         │       网关 :8080  ·  集中 JWT 鉴权        │
                         └─┬────────┬────────┬────────┬────────┬───┘
                           │        │        │        │        │
               ┌───────────▼──┐ ┌───▼────┐ ┌─▼──────┐ ┌▼────────┐ ┌▼──────────┐
               │identity-svc  │ │listening│ │file-svc│ │encoder  │ │search-svc │
               │  :10000      │ │  -svc   │ │:12000  │ │  -svc   │ │  :14000   │
               │ 用户/角色/登录│ │:11000   │ │文件上传│ │:13000   │ │ ES 全文检索│
               └──────┬───────┘ └────┬────┘ └───┬────┘ └────┬────┘ └─────┬─────┘
                      │   Feign + LoadBalancer (服务间调用)          │
                      └───────────────┬───────────────────────────────┘
                                      │
       ┌──────────────────┬───────────┼────────────┬─────────────────┐
       ▼                  ▼           ▼            ▼                 ▼
  ┌─────────┐       ┌─────────┐ ┌──────────┐ ┌───────────┐   ┌─────────────┐
  │ MySQL 8 │       │ Redis 7 │ │  Nacos   │ │ Sentinel  │   │Seata/Zipkin │
  │ +Flyway │       │ 重置码  │ │注册/配置 │ │ 限流降级  │   │ 分布式事务/ │
  └─────────┘       └─────────┘ └──────────┘ └───────────┘   │ 链路追踪    │
                                                                  └─────────────┘
```

---

## 项目结构

```
englishStudy/
├── README.md                     # 本文件
├── DEPLOY.md                     # 部署指南
├── docs/                         # 设计文档与实施计划
├── frontend/                     # 前端 Monorepo (pnpm)
│   ├── apps/
│   │   ├── admin/                # 管理后台（Vue 3 + Element Plus）
│   │   └── user/                 # 用户端（Vue 3 + Element Plus）
│   └── packages/
│       └── shared/               # @windloo/shared 共享包（API/类型/鉴权）
└── windloo-cloud/                # 后端微服务 (Maven 多模块)
    ├── docker-compose.full.yml   # 全量 Docker Compose（一键部署）
    ├── Dockerfile                # 多阶段构建（Maven 编译 + JRE 运行）
    ├── docker/init-databases.sql # MySQL 初始化脚本（建库 + undo_log）
    ├── common/                   # 跨切面公共模块（JsonResponse/异常/JWT/Redis）
    ├── model/                    # 服务间契约（DTO + Feign 客户端 + Fallback）
    ├── gateway/                  # API 网关（JWT 集中校验 + 路由）
    └── services/
        ├── identity-service/     # 用户/角色/登录/JWT 签发
        ├── listening-service/    # 听力核心（分类/专辑/单集/字幕解析/Seata）
        ├── file-service/         # 文件上传（SHA-256 去重）
        ├── media-encoder-service/# 媒体转码（异步任务 + Feign 回调）
        └── search-service/       # ES 全文检索（Nested 类型 + 句子级搜索）
```

---

## 技术栈

**后端**：Spring Boot 3.3 · Spring Cloud 2023 · Spring Cloud Alibaba 2023 · JDK 17 · MyBatis-Plus · Flyway · Spring Cloud Gateway · Nacos · OpenFeign · Sentinel · Seata AT · Redis · Elasticsearch 8 · Zipkin · JWT

**前端**：Vue 3 · TypeScript · Vite · Pinia · Vue Router · Element Plus · ECharts · Axios · pnpm Monorepo

**部署**：Docker Compose · 多阶段 Dockerfile · Nginx 反向代理

---

## 鸣谢

本项目借鉴自**杨中科**老师的英语听力项目，在此表示衷心感谢。

> 项目地址：[https://github.com/yangzhongke/NETBookMaterials](https://github.com/yangzhongke/NETBookMaterials)
