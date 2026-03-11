#!/bin/sh

echo "========================================"
echo "Starting Doubao Starter Application..."
echo "JDK Version: $(java -version 2>&1 | head -n 1)"
echo "Server Port: ${SERVER_PORT}"
echo "Active Profile: ${SPRING_PROFILES_ACTIVE}"
echo "========================================"

# 等待依赖服务（可选，如果需要确保 MySQL/MongoDB 先启动）
# echo "Waiting for dependencies..."
# sleep 10

# 启动应用
exec java ${JAVA_OPTS} \
    -Dserver.port=${SERVER_PORT} \
    -jar /app/app.jar \
    --spring.profiles.active=${SPRING_PROFILES_ACTIVE}