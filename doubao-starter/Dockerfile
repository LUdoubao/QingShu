# 使用官方OpenJDK 8作为基础镜像
FROM openjdk:8-jre

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apt-get update && \
    apt-get install -y net-tools && \
    rm -rf /var/lib/apt/lists/*

# 复制JAR文件到容器中
COPY target/doubao-starter.jar app.jar

# 复制配置文件（如果存在）
COPY application.yml application.yml

# 暴露应用端口
EXPOSE 1017

# 创建日志目录
RUN mkdir -p logs

# 启动应用
ENTRYPOINT ["java", "-Xms512m", "-Xmx2048m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", \
            "-Dfile.encoding=UTF-8", "-Duser.timezone=Asia/Shanghai", \
            "-jar", "app.jar", \
            "--spring.config.location=classpath:/,file:./config/,file:./", \
            "--server.port=1017"]

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:1017/actuator/health || exit 1