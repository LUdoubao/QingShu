# 使用官方 OpenJDK 8 镜像
FROM openjdk:8-jdk-alpine

# 设置维护者信息
LABEL maintainer="3411426617@qq.com"

# 设置时区为上海
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装必要工具（字体、curl 等）
RUN apk add --no-cache curl fontconfig ttf-dejavu

# 创建应用目录
WORKDIR /app

# 创建日志目录
RUN mkdir -p /app/logs

# 复制启动脚本
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# 复制 jar 包（假设打包后的名字）
COPY target/*.jar app.jar

# JVM 参数优化（根据容器内存调整）
ENV JAVA_OPTS="-server \
    -Xms512m \
    -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Duser.timezone=Asia/Shanghai"

# 应用配置
ENV SPRING_PROFILES_ACTIVE=dev
ENV SERVER_PORT=1017

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:1017/actuator/health || exit 1

# 暴露端口
EXPOSE 1017

# 使用启动脚本
ENTRYPOINT ["/app/entrypoint.sh"]