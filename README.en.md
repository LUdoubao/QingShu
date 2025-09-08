# Spring Cloud Microservices Sample Project

This is a microservices architecture sample project based on Spring Cloud, including multiple service modules and a complete microservices infrastructure.

## Project Overview

This project demonstrates a complete microservices architecture implementation for an e-commerce system, containing the following components:

- Registration Center (Eureka Server)
- Configuration Center (Spring Cloud Config)
- API Gateway (Spring Cloud Gateway)
- Authentication Service (JWT Authentication)
- Product Service
- User Service
- Order Service
- Payment Service
- Notification Service

## Architectural Features

- Implementation of microservices architecture using the Spring Cloud Netflix suite
- JWT-based authentication and authorization mechanism
- Inter-service communication using Feign and WebClient
- Event-driven architecture implemented with RabbitMQ
- Includes circuit breaker, fallback, and rate limiting strategies
- Provides examples of distributed transaction handling

## Main Modules

### my-eureka-server
Service registration and discovery center, implemented using Eureka

### config-server
Configuration center for centralized management of configuration files for all services

### api-gateway
API gateway implementing routing, authentication, rate limiting, and other functions

### auth-service
Authentication service providing login authentication and JWT issuance functionality

### product-service
Product service for managing product information and inventory

### user-service
User service for managing basic user information

### order-service
Order service for handling order creation and status management

### payment-service
Payment service for managing payment processes and status synchronization

### notification-service
Notification service for handling email notifications related to orders and payments

## Technology Stack

- Spring Boot 2.x
- Spring Cloud 2020.x
- Spring Cloud Gateway
- Spring Security + JWT
- Feign Client
- RabbitMQ
- MyBatis Plus
- Redis
- MySQL

## Quick Start

1. Install and start the MySQL database, create the corresponding databases and tables
2. Install and start RabbitMQ
3. Start my-eureka-server
4. Start config-server
5. Start other service modules
6. Start api-gateway

## Usage Instructions

1. Obtain a JWT token via auth-service
2. Use the token to access protected API endpoints
3. Access microservices functionality through the api-gateway
4. Check logs to confirm inter-service communication and event handling

## Configuration Instructions

Basic configurations for all services are managed uniformly through config-server. Environment-specific configuration files are stored in the config-repo directory.

## Suggestions for Expansion

- Add service monitoring and distributed tracing (Sleuth/Zipkin)
- Add distributed transaction handling (Seata)
- Implement canary releases and blue-green deployments
- Add API documentation (Swagger)

## Contribution Guidelines

Code contributions are welcome. Please follow these steps:
1. Fork the project
2. Create a new branch
3. Submit code changes
4. Create a Pull Request

## License

This project uses the Apache 2.0 license. Please check the LICENSE file in the respective modules for detailed information.