FROM openjdk:8-jre-slim

# 其他保持不变
WORKDIR /app
ADD target/product-service*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]