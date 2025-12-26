FROM openjdk:8-jre-slim

# 其他保持不变
WORKDIR /app
ADD target/notification-service-starter*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]