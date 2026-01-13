#!/bin/bash

# doubao-starter 自动化运维脚本
# 作者: Lingma
# 日期: 2025-12-25

# 应用配置
APP_NAME="doubao-starter"
JAR_FILE="target/${APP_NAME}.jar"
PID_FILE="/tmp/${APP_NAME}.pid"
LOG_FILE="logs/${APP_NAME}.log"
CONFIG_FILE="application.yml"

# JVM参数配置
JVM_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:logs/gc.log"
SPRING_OPTS="--spring.config.location=classpath:/,file:./config/,file:./ --server.port=1017"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 创建日志目录
mkdir -p logs

# 检查应用是否运行
is_running() {
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if kill -0 $PID 2>/dev/null; then
            return 0
        else
            rm -f $PID_FILE
            return 1
        fi
    else
        return 1
    fi
}

# 启动应用
start() {
    echo -e "${GREEN}正在启动 ${APP_NAME}...${NC}"
    
    if is_running; then
        echo -e "${YELLOW}${APP_NAME} 已经在运行中 (PID: $(cat $PID_FILE))${NC}"
        exit 1
    fi

    # 检查JAR文件是否存在
    if [ ! -f $JAR_FILE ]; then
        echo -e "${RED}错误: JAR文件不存在: $JAR_FILE${NC}"
        echo -e "${YELLOW}请先构建项目: mvn clean package -pl doubao-starter -DskipTests${NC}"
        exit 1
    fi

    # 启动应用
    nohup java $JVM_OPTS -jar $JAR_FILE $SPRING_OPTS > $LOG_FILE 2>&1 &
    PID=$!
    
    # 保存PID到文件
    echo $PID > $PID_FILE
    
    # 等待应用启动
    sleep 5
    
    if kill -0 $PID 2>/dev/null; then
        echo -e "${GREEN}${APP_NAME} 启动成功 (PID: $PID)${NC}"
        echo "应用启动日志请查看: $LOG_FILE"
    else
        echo -e "${RED}${APP_NAME} 启动失败${NC}"
        rm -f $PID_FILE
        exit 1
    fi
}

# 停止应用
stop() {
    echo -e "${YELLOW}正在停止 ${APP_NAME}...${NC}"
    
    if [ ! -f $PID_FILE ]; then
        echo -e "${YELLOW}${APP_NAME} 未运行${NC}"
        exit 0
    fi
    
    PID=$(cat $PID_FILE)
    
    if kill -0 $PID 2>/dev/null; then
        # 先尝试优雅关闭
        kill -15 $PID
        
        # 等待进程结束
        COUNT=0
        while kill -0 $PID 2>/dev/null; do
            if [ $COUNT -lt 30 ]; then
                sleep 1
                ((COUNT++))
            else
                echo -e "${YELLOW}等待超时，强制停止...${NC}"
                kill -9 $PID
                break
            fi
        done
        
        rm -f $PID_FILE
        echo -e "${GREEN}${APP_NAME} 已停止${NC}"
    else
        rm -f $PID_FILE
        echo -e "${YELLOW}${APP_NAME} 未运行${NC}"
    fi
}

# 重启应用
restart() {
    echo -e "${YELLOW}正在重启 ${APP_NAME}...${NC}"
    stop
    sleep 2
    start
}

# 查看应用状态
status() {
    if is_running; then
        PID=$(cat $PID_FILE)
        echo -e "${GREEN}${APP_NAME} 正在运行 (PID: $PID)${NC}"
        
        # 显示应用信息
        MEM_INFO=$(ps -p $PID -o pid,ppid,cmd,%mem,%cpu,etime | tail -1)
        echo "进程信息: $MEM_INFO"
        
        # 检查端口占用
        PORT=$(netstat -tuln 2>/dev/null | grep -E ':(1017|8080)' | head -1 | awk '{print $4}' | sed 's/.*://')
        if [ -n "$PORT" ]; then
            echo "监听端口: $PORT"
        fi
    else
        echo -e "${RED}${APP_NAME} 未运行${NC}"
    fi
}

# 查看应用日志
tail_log() {
    if [ -f $LOG_FILE ]; then
        tail -f $LOG_FILE
    else
        echo -e "${YELLOW}日志文件不存在: $LOG_FILE${NC}"
        echo -e "${GREEN}尝试启动应用以创建日志文件...${NC}"
        start
    fi
}

# 实时查看日志
view_log() {
    if [ -f $LOG_FILE ]; then
        echo -e "${GREEN}实时查看日志 ($LOG_FILE)，按 Ctrl+C 退出${NC}"
        tail -f $LOG_FILE
    else
        echo -e "${YELLOW}日志文件不存在: $LOG_FILE${NC}"
    fi
}

# 清理日志
clean_log() {
    if [ -f $LOG_FILE ]; then
        echo -e "${YELLOW}清理日志文件...${NC}"
        > $LOG_FILE
        echo -e "${GREEN}日志文件已清理${NC}"
    else
        echo -e "${YELLOW}日志文件不存在: $LOG_FILE${NC}"
    fi
    
    # 清理GC日志
    if [ -f "logs/gc.log" ]; then
        > logs/gc.log
        echo -e "${GREEN}GC日志已清理${NC}"
    fi
}

# 构建项目
build() {
    echo -e "${GREEN}开始构建项目...${NC}"
    
    # 检查Maven是否存在
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}错误: Maven 未安装或未在PATH中${NC}"
        exit 1
    fi
    
    # 构建整个项目
    mvn clean package -DskipTests
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}项目构建成功${NC}"
    else
        echo -e "${RED}项目构建失败${NC}"
        exit 1
    fi
}

# 打包doubao-starter
build_starter() {
    echo -e "${GREEN}开始构建 ${APP_NAME}...${NC}"
    
    # 检查Maven是否存在
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}错误: Maven 未安装或未在PATH中${NC}"
        exit 1
    fi
    
    # 构建doubao-starter模块
    mvn clean package -pl doubao-starter -DskipTests
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}${APP_NAME} 构建成功${NC}"
        ls -la target/${APP_NAME}*.jar
    else
        echo -e "${RED}${APP_NAME} 构建失败${NC}"
        exit 1
    fi
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo "可用选项:"
    echo "  start       - 启动应用"
    echo "  stop        - 停止应用"
    echo "  restart     - 重启应用"
    echo "  status      - 查看应用状态"
    echo "  log         - 查看应用日志"
    echo "  tail        - 实时查看日志"
    echo "  clean       - 清理日志"
    echo "  build       - 构建整个项目"
    echo "  build-starter - 构建doubao-starter模块"
    echo "  help        - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start    - 启动doubao-starter"
    echo "  $0 status   - 查看运行状态"
    echo "  $0 tail     - 实时查看日志"
}

# 主函数
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    log)
        tail_log
        ;;
    tail)
        view_log
        ;;
    clean)
        clean_log
        ;;
    build)
        build
        ;;
    build-starter)
        build_starter
        ;;
    help|*)
        show_help
        ;;
esac