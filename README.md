# Spring Cloud 微服务示例项目

这是一个基于 Spring Cloud 的微服务架构示例项目，包含完整的微服务组件和功能。项目使用 Spring Boot、Spring Cloud 以及相关技术栈构建，展示了现代分布式系统的核心概念和实现方式。

## 项目结构

该项目包含多个模块，每个模块代表一个独立的微服务或系统组件：

- **my-eureka-server**: 服务注册与发现中心 (Eureka Server)
- **config-server**: 配置中心服务
- **api-gateway**: API 网关 (使用 Spring Cloud Gateway)
- **auth-service**: 认证授权服务 (JWT 实现)
- **product-service**: 商品服务
- **order-service**: 订单服务
- **payment-service**: 支付服务
- **notification-service**: 通知服务 (集成 RabbitMQ)
- **user-service**: 用户服务
- **mall-common**: 公共模块 (包含通用类和工具类)

## 主要技术栈

- Spring Boot 2.x
- Spring Cloud 2020.x
- Spring Cloud Gateway
- Spring Security + JWT (认证授权)
- Feign (服务间通信)
- RabbitMQ (消息队列)
- Redis (缓存)
- Redisson (分布式锁)
- MyBatis Plus (ORM 框架)
- Eureka (服务注册与发现)
- Config Server (配置中心)
- Docker (容器化部署)

## 功能特性

- 服务注册与发现
- 分布式配置管理
- API 网关路由与过滤
- JWT 认证授权机制
- 服务间通信 (Feign)
- 分布式事务处理 (订单、支付、库存管理)
- 消息队列集成 (RabbitMQ)
- 限流与熔断机制
- 日志统一处理
- 全局异常处理

## 快速开始

1. 确保已安装以下环境：
   - JDK 1.8+
   - Maven 3.5+
   - Docker (可选)
   - RabbitMQ (可选)
   - Redis (可选)

2. 构建项目：
   ```bash
   mvn clean install
   ```

3. 启动服务顺序：
   ```bash
   my-eureka-server        # 服务注册中心
   config-server           # 配置中心
   api-gateway             # API 网关
   auth-service            # 认证服务
   product-service         # 商品服务
   user-service            # 用户服务
   order-service           # 订单服务
   payment-service         # 支付服务
   notification-service    # 通知服务
   ```

4. 使用 Docker 部署 (可选)：
   ```bash
   docker build -t service-name .
   docker run -d -p 8080:8080 service-name
   ```

## 使用示例

1. 获取认证 Token:
   ```bash
   POST /auth/login
   {
     "username": "your-username",
     "password": "your-password"
   }
   ```

2. 创建订单:
   ```bash
   POST /order/create
   Authorization: Bearer <your-token>
   {
     "userId": 1,
     "productId": 1001,
     "count": 2
   }
   ```

3. 发起支付:
   ```bash
   POST /payment/pay
   Authorization: Bearer <your-token>
   {
     "orderId": "20230815001",
     "paymentMethod": "ALIPAY",
     "amount": 99.9
   }
   ```

## 架构图

```
+---------------------+
|      Gateway        |
+----------+----------+
           |
+----------v----------+
|     Auth Service    |
+----------+----------+
           |
+----------v----------+     +------------------+
|    Order Service    |<--->| Product Service  |
+----------+----------+     +------------------+
           |
+----------v----------+     +------------------+
|   Payment Service   |<--->| User Service     |
+----------+----------+     +------------------+
           |
+----------v----------+
| Notification Service|
+---------------------+
```

## 贡献指南

欢迎贡献代码和改进文档。请遵循以下步骤：

1. Fork 项目
2. 创建新分支 (`git checkout -b feature/new-feature`)
3. 提交更改 (`git commit -am 'Add new feature'`)
4. 推送分支 (`git push origin feature/new-feature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。详情请查看 LICENSE 文件。