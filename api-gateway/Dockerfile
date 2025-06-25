# 修改基础镜像为Java 8
FROM openjdk:8-jre-slim

# 其他保持不变
WORKDIR /app
ADD target/api-gateway*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]