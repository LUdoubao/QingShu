# Spring Cloud 微服务示例项目

这是一个基于 Spring Cloud 的微服务架构示例项目，包含了多个服务模块和完整的微服务基础设施。

## 项目概述

本项目演示了一个完整的电商系统微服务架构实现，包含以下组件：

- 注册中心 (Eureka Server)
- 配置中心 (Spring Cloud Config)
- API 网关 (Spring Cloud Gateway)
- 认证服务 (JWT 认证)
- 商品服务
- 用户服务
- 订单服务
- 支付服务
- 通知服务

## 架构特点

- 使用 Spring Cloud Netflix 套件实现微服务架构
- 基于 JWT 的认证授权机制
- 服务间通信使用 Feign 和 WebClient
- 使用 RabbitMQ 实现事件驱动架构
- 包含熔断降级和限流策略
- 提供分布式事务处理示例

## 主要模块

### my-eureka-server
服务注册与发现中心，基于 Eureka 实现

### config-server
配置中心，集中管理所有服务的配置文件

### api-gateway
API 网关，实现路由、鉴权、限流等功能

### auth-service
认证服务，提供登录认证和 JWT 签发功能

### product-service
商品服务，管理商品信息和库存

### user-service
用户服务，管理用户基本信息

### order-service
订单服务，处理订单创建和状态管理

### payment-service
支付服务，处理支付流程和状态同步

### notification-service
通知服务，处理订单和支付相关的邮件通知

## 技术栈

- Spring Boot 2.x
- Spring Cloud 2020.x
- Spring Cloud Gateway
- Spring Security + JWT
- Feign Client
- RabbitMQ
- MyBatis Plus
- Redis
- MySQL

## 快速启动

1. 安装并启动 MySQL 数据库，创建相应数据库和表
2. 安装并启动 RabbitMQ
3. 启动 my-eureka-server
4. 启动 config-server
5. 启动其他服务模块
6. 启动 api-gateway

## 使用说明

1. 通过 auth-service 获取 JWT token
2. 使用 token 访问受保护的 API 接口
3. 通过 api-gateway 访问各个微服务功能
4. 查看日志确认服务间通信和事件处理

## 配置说明

所有服务的基础配置通过 config-server 统一管理，各环境配置文件存放在 config-repo 目录下

## 扩展建议

- 增加服务监控和链路追踪 (Sleuth/Zipkin)
- 添加分布式事务处理 (Seata)
- 实现灰度发布和蓝绿部署
- 增加 API 文档 (Swagger)

## 贡献指南

欢迎贡献代码，请遵循以下步骤：
1. Fork 项目
2. 创建新分支
3. 提交代码修改
4. 创建 Pull Request

## 许可证

本项目采用 Apache 2.0 许可证，请查看具体模块的 LICENSE 文件获取详细信息。