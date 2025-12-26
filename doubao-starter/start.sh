#!/bin/bash

# doubao-starter 启动脚本
# 作者: Lingma
# 日期: 2025-12-25

# 应用配置
APP_NAME="doubao-starter"
JAR_FILE="doubao-starter.jar"
MAIN_CLASS="org.doubao.starter.DoubaoStarterApplication"
PID_FILE="/tmp/${APP_NAME}.pid"
LOG_FILE="logs/${APP_NAME}.log"
CONFIG_FILE="application.yml"

# JVM参数配置
JVM_OPTS="-Xms512m -Xmx2048m"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
JVM_OPTS="$JVM_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=logs/"
JVM_OPTS="$JVM_OPTS -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:logs/gc.log"
JVM_OPTS="$JVM_OPTS -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# Spring参数配置
SPRING_OPTS="--spring.config.location=classpath:/,file:./config/,file:./ --server.port=1017"

# 创建日志目录
mkdir -p logs

# 启动应用
start() {
    echo "正在启动 ${APP_NAME}..."
    
    # 检查PID文件是否存在
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if kill -0 $PID 2>/dev/null; then
            echo "${APP_NAME} 已经在运行 (PID: $PID)"
            exit 1
        else
            rm -f $PID_FILE
        fi
    fi
    
    # 检查JAR文件是否存在
    if [ ! -f $JAR_FILE ]; then
        echo "错误: JAR文件不存在: $JAR_FILE"
        echo "请先构建项目或复制JAR文件到当前目录"
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
        echo "${APP_NAME} 启动成功 (PID: $PID)"
        echo "应用启动日志请查看: $LOG_FILE"
        
        # 检查端口是否监听
        sleep 3
        if netstat -tuln 2>/dev/null | grep -q ":1017 "; then
            echo "应用已在端口 1017 监听"
        else
            echo "警告: 端口 1017 未监听，请检查应用状态"
        fi
    else
        echo "${APP_NAME} 启动失败"
        rm -f $PID_FILE
        exit 1
    fi
}

# 停止应用
stop() {
    echo "正在停止 ${APP_NAME}..."
    
    if [ ! -f $PID_FILE ]; then
        echo "${APP_NAME} 未运行"
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
                echo "等待超时，强制停止..."
                kill -9 $PID
                break
            fi
        done
        
        rm -f $PID_FILE
        echo "${APP_NAME} 已停止"
    else
        rm -f $PID_FILE
        echo "${APP_NAME} 未运行"
    fi
}

# 重启应用
restart() {
    echo "正在重启 ${APP_NAME}..."
    stop
    sleep 2
    start
}

# 查看应用状态
status() {
    if [ -f $PID_FILE ]; then
        PID=$(cat $PID_FILE)
        if kill -0 $PID 2>/dev/null; then
            echo "${APP_NAME} 正在运行 (PID: $PID)"
            
            # 显示应用信息
            PS_INFO=$(ps -p $PID -o pid,ppid,cmd,%mem,%cpu,etime | tail -1)
            echo "进程信息: $PS_INFO"
            
            # 检查端口占用
            if netstat -tuln 2>/dev/null | grep -qE ':(1017|8080) '; then
                PORT=$(netstat -tuln 2>/dev/null | grep -E ':(1017|8080) ' | head -1 | awk '{print $4}' | sed 's/.*://')
                echo "监听端口: $PORT"
            fi
        else
            rm -f $PID_FILE
            echo "${APP_NAME} 未运行"
        fi
    else
        echo "${APP_NAME} 未运行"
    fi
}

# 查看应用日志
tail_log() {
    if [ -f $LOG_FILE ]; then
        tail -f $LOG_FILE
    else
        echo "日志文件不存在: $LOG_FILE"
        echo "尝试启动应用以创建日志文件..."
        start
    fi
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo "可用选项:"
    echo "  start  - 启动应用"
    echo "  stop   - 停止应用"
    echo "  restart - 重启应用"
    echo "  status - 查看应用状态"
    echo "  log    - 查看应用日志"
    echo "  help   - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start  - 启动doubao-starter"
    echo "  $0 status - 查看运行状态"
    echo "  $0 log    - 查看日志"
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
    help|*)
        show_help
        ;;
esac