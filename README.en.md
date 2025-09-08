# Spring Cloud Microservices Sample Project

This is a microservices architecture sample project based on Spring Cloud, containing a complete set of microservice components and functionalities. The project is built using Spring Boot, Spring Cloud, and related technology stack, demonstrating core concepts and implementation methods of modern distributed systems.

## Project Structure

The project contains multiple modules, each representing an independent microservice or system component:

- **my-eureka-server**: Service registration and discovery center (Eureka Server)
- **config-server**: Configuration center service
- **api-gateway**: API Gateway (using Spring Cloud Gateway)
- **auth-service**: Authentication and authorization service (JWT implementation)
- **product-service**: Product service
- **order-service**: Order service
- **payment-service**: Payment service
- **notification-service**: Notification service (integrated with RabbitMQ)
- **user-service**: User service
- **mall-common**: Common module (contains generic classes and utility classes)

## Key Technology Stack

- Spring Boot 2.x
- Spring Cloud 2020.x
- Spring Cloud Gateway
- Spring Security + JWT (Authentication and Authorization)
- Feign (Inter-service communication)
- RabbitMQ (Message Queue)
- Redis (Caching)
- Redisson (Distributed Lock)
- MyBatis Plus (ORM Framework)
- Eureka (Service Registration and Discovery)
- Config Server (Configuration Center)
- Docker (Containerized Deployment)

## Functional Features

- Service registration and discovery
- Distributed configuration management
- API Gateway routing and filtering
- JWT authentication and authorization mechanism
- Inter-service communication (Feign)
- Distributed transaction processing (Order, Payment, Inventory Management)
- Message queue integration (RabbitMQ)
- Rate limiting and circuit breaker mechanisms
- Unified log handling
- Global exception handling

## Quick Start

1. Ensure the following environments are installed:
   - JDK 1.8+
   - Maven 3.5+
   - Docker (optional)
   - RabbitMQ (optional)
   - Redis (optional)

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Start services in the following order:
   ```bash
   my-eureka-server        # Service registration center
   config-server           # Configuration center
   api-gateway             # API Gateway
   auth-service            # Authentication service
   product-service         # Product service
   user-service            # User service
   order-service           # Order service
   payment-service         # Payment service
   notification-service    # Notification service
   ```

4. Deploy using Docker (optional):
   ```bash
   docker build -t service-name .
   docker run -d -p 8080:8080 service-name
   ```

## Usage Examples

1. Obtain authentication Token:
   ```bash
   POST /auth/login
   {
     "username": "your-username",
     "password": "your-password"
   }
   ```

2. Create an order:
   ```bash
   POST /order/create
   Authorization: Bearer <your-token>
   {
     "userId": 1,
     "productId": 1001,
     "count": 2
   }
   ```

3. Initiate a payment:
   ```bash
   POST /payment/pay
   Authorization: Bearer <your-token>
   {
     "orderId": "20230815001",
     "paymentMethod": "ALIPAY",
     "amount": 99.9
   }
   ```

## Architecture Diagram

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
|    Order Service    |<-->| Product Service  |
+----------+----------+     +------------------+
           |
+----------v----------+     +------------------+
|   Payment Service   |<-->| User Service     |
+----------+----------+     +------------------+
           |
+----------v----------+
| Notification Service|
+---------------------+
```

## Contribution Guide

Contributions to code and documentation improvements are welcome. Please follow these steps:

1. Fork the project
2. Create a new branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## License

This project is licensed under the MIT License. Please refer to the LICENSE file for details.