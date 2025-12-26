#!/bin/bash

# doubao-starter 配置管理脚本
# 作者: Lingma
# 日期: 2025-12-25

# 应用配置
APP_NAME="doubao-starter"
CONFIG_DIR="config"
BACKUP_DIR="backup"
DEFAULT_CONFIG="application.yml"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 创建必要的目录
mkdir -p $CONFIG_DIR
mkdir -p $BACKUP_DIR/logs
mkdir -p $BACKUP_DIR/config

# 显示配置信息
show_config() {
    echo -e "${BLUE}=== ${APP_NAME} 配置信息 ===${NC}"
    
    if [ -f $DEFAULT_CONFIG ]; then
        echo -e "${GREEN}主配置文件: $DEFAULT_CONFIG${NC}"
        echo "服务器端口: $(grep -E '^ *port:' $DEFAULT_CONFIG | head -1 | awk '{print $2}' 2>/dev/null || echo '未找到')"
        echo "应用名称: $(grep -E '^ *name:' $DEFAULT_CONFIG | head -1 | awk '{print $2}' 2>/dev/null || echo '未找到')"
    else
        echo -e "${RED}主配置文件不存在: $DEFAULT_CONFIG${NC}"
    fi
    
    if [ -f "logs/${APP_NAME}.log" ]; then
        LOG_SIZE=$(du -h "logs/${APP_NAME}.log" 2>/dev/null | cut -f1)
        echo "日志文件大小: $LOG_SIZE"
    fi
}

# 备份配置
backup_config() {
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    BACKUP_NAME="${BACKUP_DIR}/config_${TIMESTAMP}"
    
    echo -e "${YELLOW}正在备份配置...${NC}"
    
    # 备份配置文件
    if [ -f $DEFAULT_CONFIG ]; then
        cp $DEFAULT_CONFIG ${BACKUP_NAME}_${DEFAULT_CONFIG}
        echo -e "${GREEN}配置文件已备份到: ${BACKUP_NAME}_${DEFAULT_CONFIG}${NC}"
    fi
    
    # 备份日志文件
    if [ -f "logs/${APP_NAME}.log" ]; then
        cp "logs/${APP_NAME}.log" "${BACKUP_DIR}/logs/${APP_NAME}_${TIMESTAMP}.log"
        echo -e "${GREEN}日志文件已备份到: ${BACKUP_DIR}/logs/${APP_NAME}_${TIMESTAMP}.log${NC}"
    fi
    
    # 备份JVM参数配置（如果存在）
    if [ -f "jvm.conf" ]; then
        cp "jvm.conf" "${BACKUP_NAME}_jvm.conf"
        echo -e "${GREEN}JVM配置已备份到: ${BACKUP_NAME}_jvm.conf${NC}"
    fi
}

# 恢复配置
restore_config() {
    if [ -z "$2" ]; then
        echo -e "${RED}错误: 请指定备份时间戳，例如: $0 restore 20251225_173000${NC}"
        return 1
    fi
    
    TIMESTAMP=$2
    BACKUP_NAME="${BACKUP_DIR}/config_${TIMESTAMP}"
    
    echo -e "${YELLOW}正在从备份恢复配置...${NC}"
    
    # 恢复主配置文件
    if [ -f "${BACKUP_NAME}_${DEFAULT_CONFIG}" ]; then
        cp "${BACKUP_NAME}_${DEFAULT_CONFIG}" $DEFAULT_CONFIG
        echo -e "${GREEN}主配置文件已恢复${NC}"
    else
        echo -e "${RED}错误: 找不到备份文件: ${BACKUP_NAME}_${DEFAULT_CONFIG}${NC}"
        return 1
    fi
    
    # 恢复JVM配置（如果存在）
    if [ -f "${BACKUP_NAME}_jvm.conf" ]; then
        cp "${BACKUP_NAME}_jvm.conf" "jvm.conf"
        echo -e "${GREEN}JVM配置已恢复${NC}"
    fi
    
    echo -e "${GREEN}配置恢复完成${NC}"
}

# 列出所有备份
list_backups() {
    echo -e "${BLUE}=== 配置备份列表 ===${NC}"
    
    if [ -d $BACKUP_DIR ]; then
        BACKUPS=$(ls -la $BACKUP_DIR/config_* 2>/dev/null | grep -E "config_.*\.yml" | sed 's/.*config_\(.*\)_'${DEFAULT_CONFIG}'/\1/' | sort -r)
        
        if [ -n "$BACKUPS" ]; then
            for BACKUP in $BACKUPS; do
                echo -e "${GREEN}$BACKUP${NC}"
            done
        else
            echo -e "${YELLOW}暂无配置备份${NC}"
        fi
    else
        echo -e "${YELLOW}备份目录不存在${NC}"
    fi
    
    # 显示日志备份
    echo -e "\n${BLUE}=== 日志备份列表 ===${NC}"
    if [ -d "${BACKUP_DIR}/logs" ]; then
        LOG_BACKUPS=$(ls -la ${BACKUP_DIR}/logs/ 2>/dev/null | grep -E "${APP_NAME}_.*\.log" | awk '{print $9}' | sort -r)
        
        if [ -n "$LOG_BACKUPS" ]; then
            for LOG in $LOG_BACKUPS; do
                echo -e "${GREEN}$LOG${NC}"
            done
        else
            echo -e "${YELLOW}暂无日志备份${NC}"
        fi
    else
        echo -e "${YELLOW}日志备份目录不存在${NC}"
    fi
}

# 检查环境
check_env() {
    echo -e "${BLUE}=== 环境检查 ===${NC}"
    
    # 检查Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
        echo -e "${GREEN}Java 版本: $JAVA_VERSION${NC}"
    else
        echo -e "${RED}Java 未安装${NC}"
    fi
    
    # 检查Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version 2>&1 | grep "Apache Maven" | cut -d' ' -f3)
        echo -e "${GREEN}Maven 版本: $MVN_VERSION${NC}"
    else
        echo -e "${YELLOW}Maven 未安装${NC}"
    fi
    
    # 检查端口占用
    if command -v netstat &> /dev/null; then
        PORTS=$(netstat -tuln 2>/dev/null | grep -E ':(1017|8080)' | awk '{print $4}' | sed 's/.*://')
        if [ -n "$PORTS" ]; then
            echo -e "${RED}警告: 端口被占用: $PORTS${NC}"
        else
            echo -e "${GREEN}端口检查: 1017/8080 未被占用${NC}"
        fi
    fi
}

# 性能监控
monitor() {
    echo -e "${BLUE}=== ${APP_NAME} 性能监控 ===${NC}"
    
    # 查找应用进程
    PID=$(pgrep -f "${APP_NAME}" | head -1)
    
    if [ -n "$PID" ]; then
        echo -e "${GREEN}应用PID: $PID${NC}"
        
        # 获取进程信息
        PS_INFO=$(ps -p $PID -o pid,ppid,cmd,%mem,%cpu,etime 2>/dev/null)
        echo "$PS_INFO"
        
        # 获取内存使用情况
        MEM_INFO=$(pmap -x $PID 2>/dev/null | tail -1 | awk '{print "RSS: "$2"KB, Dirty: "$4"KB"}')
        if [ -n "$MEM_INFO" ]; then
            echo "内存信息: $MEM_INFO"
        fi
        
        # 检查打开的文件数
        OPEN_FILES=$(lsof -p $PID 2>/dev/null | wc -l)
        echo "打开的文件数: $OPEN_FILES"
    else
        echo -e "${RED}${APP_NAME} 未运行${NC}"
    fi
}

# 安全检查
security_check() {
    echo -e "${BLUE}=== 安全检查 ===${NC}"
    
    # 检查配置文件中的敏感信息
    if [ -f $DEFAULT_CONFIG ]; then
        SENSITIVE_KEYS=("password" "secret" "key" "token")
        
        echo "检查配置文件中的敏感信息..."
        for KEY in "${SENSITIVE_KEYS[@]}"; do
            RESULTS=$(grep -i "$KEY" $DEFAULT_CONFIG 2>/dev/null | grep -v "^#" | grep -v "^$")
            if [ -n "$RESULTS" ]; then
                echo -e "${YELLOW}发现可能的敏感信息 (${KEY}):${NC}"
                echo "$RESULTS" | sed 's/^/  /'
            fi
        done
    fi
    
    # 检查文件权限
    if [ -f $DEFAULT_CONFIG ]; then
        PERMS=$(stat -c "%a %n" $DEFAULT_CONFIG)
        echo "配置文件权限: $PERMS"
    fi
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo "配置管理选项:"
    echo "  show        - 显示当前配置"
    echo "  backup      - 备份配置和日志"
    echo "  restore [timestamp] - 从备份恢复配置"
    echo "  list        - 列出所有备份"
    echo "  env         - 检查运行环境"
    echo "  monitor     - 性能监控"
    echo "  security    - 安全检查"
    echo "  help        - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 show     - 显示当前配置"
    echo "  $0 backup   - 备份当前配置"
    echo "  $0 list     - 列出所有备份"
    echo "  $0 restore 20251225_173000 - 恢复指定备份"
}

# 主函数
case "$1" in
    show)
        show_config
        ;;
    backup)
        backup_config
        ;;
    restore)
        restore_config $1 $2
        ;;
    list)
        list_backups
        ;;
    env)
        check_env
        ;;
    monitor)
        monitor
        ;;
    security)
        security_check
        ;;
    help|*)
        show_help
        ;;
esac