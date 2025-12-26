#!/bin/bash

# doubao-starter 一键部署脚本
# 作者: Lingma
# 日期: 2025-12-25

# 应用配置
APP_NAME="doubao-starter"
JAR_FILE="target/${APP_NAME}.jar"
PID_FILE="/tmp/${APP_NAME}.pid"
LOG_FILE="logs/${APP_NAME}.log"
CONFIG_FILE="application.yml"
DEPLOY_DIR="/opt/doubao"
BACKUP_DIR="/opt/doubao/backup"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

log_warn() {
    echo -e "${YELLOW}[WARN]$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

log_error() {
    echo -e "${RED}[ERROR]$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]$(date '+%Y-%m-%d %H:%M:%S')] $1${NC}"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_warn "建议使用root权限运行此脚本"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "操作已取消"
            exit 1
        fi
    fi
}

# 检查依赖
check_dependencies() {
    log_info "检查依赖..."
    
    local deps=("java" "mvn" "git")
    local missing_deps=()
    
    for dep in "${deps[@]}"; do
        if ! command -v $dep &> /dev/null; then
            missing_deps+=($dep)
        fi
    done
    
    if [ ${#missing_deps[@]} -gt 0 ]; then
        log_error "缺少依赖: ${missing_deps[*]}"
        log_error "请先安装缺失的依赖"
        exit 1
    fi
    
    log_success "依赖检查完成"
}

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

# 停止应用
stop_app() {
    log_info "停止 ${APP_NAME}..."
    
    if [ ! -f $PID_FILE ]; then
        log_warn "${APP_NAME} 未运行"
        return 0
    fi
    
    PID=$(cat $PID_FILE)
    
    if kill -0 $PID 2>/dev/null; then
        kill -15 $PID
        
        # 等待进程结束
        COUNT=0
        while kill -0 $PID 2>/dev/null; do
            if [ $COUNT -lt 30 ]; then
                sleep 1
                ((COUNT++))
            else
                log_warn "等待超时，强制停止..."
                kill -9 $PID
                break
            fi
        done
        
        rm -f $PID_FILE
        log_success "${APP_NAME} 已停止"
    else
        rm -f $PID_FILE
        log_warn "${APP_NAME} 未运行"
    fi
}

# 构建项目
build_project() {
    log_info "开始构建项目..."
    
    # 清理旧构建
    mvn clean
    
    # 构建整个项目
    if ! mvn install -DskipTests; then
        log_error "项目构建失败"
        exit 1
    fi
    
    # 构建doubao-starter模块
    if ! mvn package -pl doubao-starter -DskipTests; then
        log_error "doubao-starter 模块构建失败"
        exit 1
    fi
    
    if [ ! -f $JAR_FILE ]; then
        log_error "JAR文件不存在: $JAR_FILE"
        exit 1
    fi
    
    log_success "项目构建完成: $JAR_FILE"
}

# 部署应用
deploy_app() {
    log_info "部署应用..."
    
    # 创建部署目录
    mkdir -p $DEPLOY_DIR
    mkdir -p logs
    
    # 复制JAR文件到部署目录
    cp $JAR_FILE $DEPLOY_DIR/
    log_success "JAR文件已复制到: $DEPLOY_DIR"
    
    # 复制配置文件
    if [ -f $CONFIG_FILE ]; then
        cp $CONFIG_FILE $DEPLOY_DIR/
        log_success "配置文件已复制到: $DEPLOY_DIR"
    fi
    
    # 创建启动脚本
    cat > $DEPLOY_DIR/start.sh << 'EOF'
#!/bin/bash
APP_NAME="doubao-starter"
JAR_FILE="/opt/doubao/${APP_NAME}.jar"
PID_FILE="/tmp/${APP_NAME}.pid"
LOG_FILE="/opt/doubao/logs/${APP_NAME}.log"

JVM_OPTS="-Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
SPRING_OPTS="--spring.config.location=file:/opt/doubao/ --server.port=1017"

mkdir -p /opt/doubao/logs

if [ -f $PID_FILE ]; then
    PID=$(cat $PID_FILE)
    if kill -0 $PID 2>/dev/null; then
        echo "${APP_NAME} 已在运行 (PID: $PID)"
        exit 1
    else
        rm -f $PID_FILE
    fi
fi

nohup java $JVM_OPTS -jar $JAR_FILE $SPRING_OPTS > $LOG_FILE 2>&1 &
PID=$!
echo $PID > $PID_FILE
echo "${APP_NAME} 启动成功 (PID: $PID)"
EOF
    
    chmod +x $DEPLOY_DIR/start.sh
    log_success "启动脚本已创建: $DEPLOY_DIR/start.sh"
}

# 启动应用
start_app() {
    log_info "启动 ${APP_NAME}..."
    
    if is_running; then
        log_warn "${APP_NAME} 已经在运行中"
        return 0
    fi
    
    # 检查JAR文件
    if [ ! -f $JAR_FILE ]; then
        log_error "JAR文件不存在: $JAR_FILE"
        exit 1
    fi
    
    # 启动应用
    mkdir -p logs
    nohup java -Xms512m -Xmx2048m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar $JAR_FILE --spring.config.location=classpath:/,file:./config/,file:./ --server.port=1017 > $LOG_FILE 2>&1 &
    PID=$!
    
    # 保存PID
    echo $PID > $PID_FILE
    
    # 等待应用启动
    sleep 10
    
    if kill -0 $PID 2>/dev/null; then
        log_success "${APP_NAME} 启动成功 (PID: $PID)"
        
        # 检查端口是否监听
        sleep 5
        if netstat -tuln 2>/dev/null | grep -q ":1017 "; then
            log_success "应用已在端口 1017 监听"
        else
            log_warn "端口 1017 未监听，请检查应用状态"
        fi
    else
        log_error "${APP_NAME} 启动失败"
        rm -f $PID_FILE
        exit 1
    fi
}

# 检查应用健康状态
check_health() {
    log_info "检查应用健康状态..."
    
    # 检查进程
    if is_running; then
        PID=$(cat $PID_FILE)
        log_success "进程运行正常 (PID: $PID)"
        
        # 检查端口
        if netstat -tuln 2>/dev/null | grep -q ":1017 "; then
            log_success "端口监听正常"
        else
            log_warn "端口 1017 未监听"
        fi
        
        # 检查日志
        if [ -f $LOG_FILE ]; then
            ERROR_COUNT=$(grep -i "error\|exception\|fatal" $LOG_FILE | wc -l)
            if [ $ERROR_COUNT -gt 0 ]; then
                log_warn "发现 $ERROR_COUNT 个错误/异常信息"
            else
                log_success "日志中未发现错误信息"
            fi
        fi
    else
        log_error "${APP_NAME} 未运行"
        return 1
    fi
}

# 创建系统服务
create_systemd_service() {
    log_info "创建 systemd 服务..."
    
    SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
    
    cat > $SERVICE_FILE << EOF
[Unit]
Description=Doubao Starter Service
After=network.target

[Service]
Type=forking
User=root
WorkingDirectory=${DEPLOY_DIR}
ExecStart=/opt/doubao/start.sh
ExecStop=kill \$(cat /tmp/${APP_NAME}.pid)
PIDFile=/tmp/${APP_NAME}.pid
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF
    
    # 重新加载systemd配置
    systemctl daemon-reload
    
    log_success "systemd 服务已创建: $SERVICE_FILE"
    log_info "可以使用以下命令管理服务:"
    log_info "  systemctl start $APP_NAME"
    log_info "  systemctl stop $APP_NAME"
    log_info "  systemctl restart $APP_NAME"
    log_info "  systemctl status $APP_NAME"
    log_info "  systemctl enable $APP_NAME  # 开机自启"
}

# 显示部署状态
show_status() {
    echo -e "${CYAN}=== ${APP_NAME} 部署状态 ===${NC}"
    
    # 检查JAR文件
    if [ -f $JAR_FILE ]; then
        JAR_SIZE=$(du -h $JAR_FILE 2>/dev/null | cut -f1)
        echo -e "${GREEN}JAR文件: 存在 ($JAR_SIZE)${NC}"
    else
        echo -e "${RED}JAR文件: 不存在${NC}"
    fi
    
    # 检查配置文件
    if [ -f $CONFIG_FILE ]; then
        echo -e "${GREEN}配置文件: 存在${NC}"
    else
        echo -e "${YELLOW}配置文件: 不存在${NC}"
    fi
    
    # 检查运行状态
    if is_running; then
        PID=$(cat $PID_FILE)
        echo -e "${GREEN}运行状态: 运行中 (PID: $PID)${NC}"
    else
        echo -e "${RED}运行状态: 未运行${NC}"
    fi
    
    # 检查端口
    if netstat -tuln 2>/dev/null | grep -q ":1017 "; then
        echo -e "${GREEN}端口状态: 1017 端口已监听${NC}"
    else
        echo -e "${RED}端口状态: 1017 端口未监听${NC}"
    fi
    
    # 检查日志
    if [ -f $LOG_FILE ]; then
        LOG_SIZE=$(du -h $LOG_FILE 2>/dev/null | cut -f1)
        LATEST_ERROR=$(grep -i "error\|exception\|fatal" $LOG_FILE | tail -1)
        echo -e "${GREEN}日志文件: 存在 ($LOG_SIZE)${NC}"
        if [ -n "$LATEST_ERROR" ]; then
            echo -e "${YELLOW}最新错误: $LATEST_ERROR${NC}"
        fi
    else
        echo -e "${YELLOW}日志文件: 不存在${NC}"
    fi
}

# 完整部署流程
full_deploy() {
    log_info "开始完整部署流程..."
    
    check_dependencies
    stop_app
    build_project
    deploy_app
    start_app
    check_health
    
    log_success "完整部署流程完成"
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}doubao-starter 一键部署脚本${NC}"
    echo "用法: $0 [选项]"
    echo ""
    echo "可用选项:"
    echo "  build       - 构建项目"
    echo "  deploy      - 部署应用"
    echo "  start       - 启动应用"
    echo "  stop        - 停止应用"
    echo "  restart     - 重启应用"
    echo "  status      - 显示部署状态"
    echo "  health      - 检查应用健康状态"
    echo "  service     - 创建系统服务"
    echo "  full        - 完整部署流程 (构建+部署+启动)"
    echo "  check       - 检查依赖和环境"
    echo "  help        - 显示此帮助信息"
    echo ""
    echo -e "${YELLOW}注意: 此脚本需要在项目根目录下运行${NC}"
    echo ""
    echo "示例:"
    echo "  $0 check     - 检查环境依赖"
    echo "  $0 full      - 完整部署"
    echo "  $0 status    - 查看部署状态"
    echo "  $0 service   - 创建系统服务"
}

# 主函数
case "$1" in
    build)
        check_dependencies
        build_project
        ;;
    deploy)
        deploy_app
        ;;
    start)
        start_app
        ;;
    stop)
        stop_app
        ;;
    restart)
        stop_app
        start_app
        ;;
    status)
        show_status
        ;;
    health)
        check_health
        ;;
    service)
        create_systemd_service
        ;;
    full)
        full_deploy
        ;;
    check)
        check_dependencies
        ;;
    help|*)
        show_help
        ;;
esac