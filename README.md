<!--
企业级项目 README 模板（已按当前仓库模块与技术栈调整）
维护建议：如有新增服务/中间件/部署方式，请同步更新对应章节。
-->

# QingShu 企业级微服务系统

QingShu 是一个基于 **Spring Boot + Spring Cloud** 的企业级微服务系统，面向内容/互动类业务场景，提供从认证、用户、内容、互动、检索、通知到 AI 能力的完整服务矩阵。项目采用模块化与分层架构，强调 **可扩展性、可维护性、可观测性与高可用性**。

---

## ✨ 核心特性

- **微服务架构**：服务自治、按领域拆分，便于独立扩展与部署。
- **统一网关**：API Gateway 对外提供统一入口与路由治理。
- **集中配置**：Config Server 管理配置，支持多环境隔离。
- **服务注册/发现**：Eureka Server 实现服务治理与健康检测。
- **领域服务丰富**：用户、评论、点赞、收藏、分享、话题、搜索等。
- **AI/对话能力**：ai-service 与 dialog-service 提供智能增强能力。

---

## 🧱 项目结构

```
QingShu
├── api-gateway            # API 网关
├── auth-service           # 认证与授权
├── user-service           # 用户服务
├── quote-service          # 文本/内容引用服务
├── oss-service            # 对象存储服务
├── notification-service   # 通知服务
├── like-service           # 点赞服务
├── favorite-service       # 收藏服务
├── comment-service        # 评论服务
├── view-count-service     # 浏览计数服务
├── share-service          # 分享服务
├── dialog-service         # 对话服务
├── fanout-service         # 扇出/分发服务
├── feed-service           # 信息流服务
├── search-service         # 搜索服务
├── topic-service          # 话题服务
├── ai-service             # AI 服务
├── config-server          # 配置中心
├── my-eureka-server       # 注册中心
├── mall-common            # 公共模块（工具、基础设施）
├── doubao-starter         # 业务基础 starter
└── pom.xml                # Maven 聚合配置
```

---

## 📚 服务帮助文档（按服务划分）

> 说明：以下为各服务的职责与建议依赖，便于理解系统边界与启动/排障。若实际实现中依赖有所差异，请以各服务的配置文件为准。

### 基础设施与平台服务

| 服务 | 目录 | 主要职责 | 依赖/说明 |
| --- | --- | --- | --- |
| 注册中心 | `my-eureka-server` | 统一服务注册与发现 | 需最先启动，所有服务通过它注册 |
| 配置中心 | `config-server` | 统一配置管理、环境隔离 | 依赖注册中心；提供配置给各服务 |
| API 网关 | `api-gateway` | 统一入口、路由、鉴权与限流 | 依赖注册中心与配置中心 |
| 公共模块 | `mall-common` | 通用工具、基础设施封装 | 被多数业务服务依赖 |
| 业务基础 starter | `doubao-starter` | 统一业务启动/配置约定 | 作为各服务的基础依赖 |

### 账户与权限

| 服务 | 目录 | 主要职责 | 依赖/说明 |
| --- | --- | --- | --- |
| 认证与授权 | `auth-service` | 登录、鉴权、令牌管理 | 常与网关配合；依赖用户数据 |
| 用户服务 | `user-service` | 用户资料、账户管理 | 依赖数据源与缓存等基础设施 |

### 内容与互动

| 服务 | 目录 | 主要职责 | 依赖/说明 |
| --- | --- | --- | --- |
| 内容引用 | `quote-service` | 内容引用/转发/引用链路 | 与用户、内容、分享相关 |
| 评论 | `comment-service` | 评论与回复体系 | 依赖用户、内容与通知 |
| 点赞 | `like-service` | 点赞与取消点赞 | 常与内容/评论联动 |
| 收藏 | `favorite-service` | 收藏夹与收藏记录 | 依赖用户与内容 |
| 分享 | `share-service` | 分享链接与传播记录 | 与内容/用户相关 |
| 浏览计数 | `view-count-service` | 浏览量统计 | 可与缓存/消息队列配合 |
| 话题 | `topic-service` | 话题/标签体系 | 与内容聚合/搜索联动 |

### 内容分发与推荐

| 服务 | 目录 | 主要职责 | 依赖/说明 |
| --- | --- | --- | --- |
| 扇出/分发 | `fanout-service` | 内容分发到关注者/订阅 | 依赖消息队列或缓存 |
| 信息流 | `feed-service` | 首页/关注流生成 | 依赖扇出、内容、用户 |

### 搜索与存储

| 服务 | 目录 | 主要职责 | 依赖/说明 |
| --- | --- | --- | --- |
| 搜索 | `search-service` | 内容/用户检索 | 依赖搜索引擎（ES 等） |
| 对象存储 | `oss-service` | 图片/附件上传管理 | 依赖对象存储（OSS/S3） |

### 通知与 AI

| 服务 | 目录 | 主要职责 | 依赖/说明 |
| --- | --- | --- | --- |
| 通知 | `notification-service` | 系统通知/站内信/推送 | 与评论、点赞等事件联动 |
| 对话 | `dialog-service` | 对话能力/多轮会话 | 可接入模型服务 |
| AI 服务 | `ai-service` | AI 能力编排与调用 | 依赖对话/模型/向量等 |

---

## 🧰 技术栈

- **语言/平台**：Java 8
- **框架**：Spring Boot 2.6.x、Spring Cloud 2021.x
- **构建**：Maven (multi-module)
- **服务治理**：Eureka、Spring Cloud Config
- **API 网关**：Spring Cloud Gateway
- **可扩展组件**：对象存储、搜索、通知、AI 能力等（按模块接入）

---

## 🚀 快速开始

### 1. 环境准备

确保本地已安装：

- JDK 1.8+
- Maven 3.6+
- Git

如涉及外部中间件（数据库、缓存、搜索等），请按具体服务配置启动。

### 2. 克隆项目

```bash
git clone <your-repo-url>
cd QingShu
```

### 3. 构建

```bash
mvn clean install -DskipTests
```

### 4. 启动顺序（建议）

1. **my-eureka-server**（注册中心）
2. **config-server**（配置中心）
3. **api-gateway**（对外入口）
4. 业务服务（按需启动）

示例：

```bash
cd my-eureka-server
mvn spring-boot:run
```

```bash
cd ../config-server
mvn spring-boot:run
```

```bash
cd ../api-gateway
mvn spring-boot:run
```

---

## ⚙️ 配置说明

系统使用 **Spring Cloud Config** 进行集中配置，建议按如下方式管理：

- `application.yml`：基础配置
- `application-{profile}.yml`：环境配置（dev/test/prod）

请根据实际环境补充数据库、缓存、消息队列、搜索引擎等配置。

---

## 📦 交付与部署建议

### 容器化部署（推荐）

- 每个服务独立镜像
- 配合 Kubernetes / Docker Compose 编排
- 建议接入统一日志与监控平台

### CI/CD 建议流程

1. 代码检查（Checkstyle/SpotBugs 等）
2. 单元测试/集成测试
3. 构建镜像与推送镜像仓库
4. 自动化部署（K8s/GitOps）

---

## 🔍 运维与观测

建议引入以下能力以完善企业级稳定性：

- **日志系统**：ELK / Loki
- **监控系统**：Prometheus + Grafana
- **链路追踪**：SkyWalking / Zipkin
- **告警**：Alertmanager / 企业 IM 通知

---

## ✅ 测试

```bash
mvn test
```

建议为每个服务配置单元测试与集成测试。

---

## 🤝 贡献指南

1. Fork 本仓库并创建分支
2. 提交修改并编写清晰的 commit message
3. 发起 Pull Request

---

## 📄 License

本项目遵循企业内部约定或自定义许可证。如需开源，请补充具体 License 文本。
