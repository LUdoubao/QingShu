#!/bin/bash

# doubao-starter 环境配置脚本
# 作者: Lingma
# 日期: 2025-12-25

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

# 检查操作系统
check_os() {
    log_info "检查操作系统..."
    
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS=$NAME
        VER=$VERSION_ID
    else
        log_error "无法确定操作系统"
        exit 1
    fi
    
    log_success "操作系统: $OS $VER"
    
    # 检查是否为支持的系统
    if [[ "$OS" =~ "Ubuntu" ]] || [[ "$OS" =~ "CentOS" ]] || [[ "$OS" =~ "Red Hat" ]] || [[ "$OS" =~ "Debian" ]]; then
        log_success "操作系统受支持"
    else
        log_warn "操作系统可能不受支持: $OS"
    fi
}

# 检查并安装Java
install_java() {
    log_info "检查Java..."
    
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
        log_success "Java已安装: $JAVA_VERSION"
        
        # 检查Java版本是否满足要求 (至少Java 8)
        if [[ $JAVA_VERSION =~ ^1\.8 ]] || [[ $JAVA_VERSION =~ ^[9-9] ]] || [[ $JAVA_VERSION =~ ^[1-9][0-9] ]]; then
            log_success "Java版本满足要求"
        else
            log_error "Java版本过低，需要Java 8或更高版本"
            exit 1
        fi
    else
        log_warn "Java未安装，正在安装..."
        
        if command -v apt-get &> /dev/null; then
            # Ubuntu/Debian
            apt-get update
            apt-get install -y openjdk-8-jdk
        elif command -v yum &> /dev/null; then
            # CentOS/RHEL
            yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel
        else
            log_error "不支持的包管理器"
            exit 1
        fi
        
        log_success "Java安装完成"
    fi
}

# 检查并安装Maven
install_maven() {
    log_info "检查Maven..."
    
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version 2>&1 | grep "Apache Maven" | cut -d' ' -f3)
        log_success "Maven已安装: $MVN_VERSION"
    else
        log_warn "Maven未安装，正在安装..."
        
        if command -v apt-get &> /dev/null; then
            # Ubuntu/Debian
            apt-get install -y maven
        elif command -v yum &> /dev/null; then
            # CentOS/RHEL
            yum install -y maven
        else
            log_error "不支持的包管理器，手动安装Maven..."
            # 手动安装Maven
            wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz
            tar -xzf apache-maven-3.8.6-bin.tar.gz
            mv apache-maven-3.8.6 /opt/maven
            echo 'export PATH=/opt/maven/bin:$PATH' >> /etc/profile
            source /etc/profile
        fi
        
        log_success "Maven安装完成"
    fi
}

# 检查并安装Git
install_git() {
    log_info "检查Git..."
    
    if command -v git &> /dev/null; then
        GIT_VERSION=$(git --version | cut -d' ' -f3)
        log_success "Git已安装: $GIT_VERSION"
    else
        log_warn "Git未安装，正在安装..."
        
        if command -v apt-get &> /dev/null; then
            # Ubuntu/Debian
            apt-get install -y git
        elif command -v yum &> /dev/null; then
            # CentOS/RHEL
            yum install -y git
        else
            log_error "不支持的包管理器"
            exit 1
        fi
        
        log_success "Git安装完成"
    fi
}

# 检查并安装MySQL
install_mysql() {
    log_info "检查MySQL..."
    
    if command -v mysql &> /dev/null; then
        MYSQL_VERSION=$(mysql --version | cut -d' ' -f6 | cut -d',' -f1)
        log_success "MySQL客户端已安装: $MYSQL_VERSION"
    else
        log_warn "MySQL未安装，正在安装..."
        
        if command -v apt-get &> /dev/null; then
            # Ubuntu/Debian
            apt-get install -y mysql-server mysql-client
        elif command -v yum &> /dev/null; then
            # CentOS/RHEL
            yum install -y mysql-server mysql mysql-devel
        else
            log_error "不支持的包管理器"
            exit 1
        fi
        
        log_success "MySQL安装完成"
    fi
}

# 检查并安装Redis
install_redis() {
    log_info "检查Redis..."
    
    if command -v redis-server &> /dev/null; then
        REDIS_VERSION=$(redis-server --version 2>&1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1)
        log_success "Redis已安装: $REDIS_VERSION"
    else
        log_warn "Redis未安装，正在安装..."
        
        if command -v apt-get &> /dev/null; then
            # Ubuntu/Debian
            apt-get install -y redis-server
        elif command -v yum &> /dev/null; then
            # CentOS/RHEL
            yum install -y redis
        else
            log_error "不支持的包管理器"
            exit 1
        fi
        
        # 启动Redis服务
        systemctl start redis
        systemctl enable redis
        
        log_success "Redis安装并启动完成"
    fi
}

# 检查并安装MongoDB
install_mongodb() {
    log_info "检查MongoDB..."
    
    if command -v mongod &> /dev/null; then
        MONGO_VERSION=$(mongod --version | grep "db version" | cut -d' ' -f3)
        log_success "MongoDB已安装: $MONGO_VERSION"
    else
        log_warn "MongoDB未安装，正在安装..."
        
        if [[ "$OS" =~ "Ubuntu" ]]; then
            # Ubuntu
            wget -qO - https://www.mongodb.org/static/pgp/server-4.4.asc | apt-key add -
            echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu $(lsb_release -cs)/mongodb-org/4.4 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-4.4.list
            apt-get update
            apt-get install -y mongodb-org
        elif [[ "$OS" =~ "CentOS" ]]; then
            # CentOS
            cat > /etc/yum.repos.d/mongodb-org-4.4.repo << EOF
[mongodb-org-4.4]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/redhat/\$releasever/mongodb-org/4.4/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-4.4.asc
EOF
            yum install -y mongodb-org
        else
            log_error "不支持的操作系统安装MongoDB"
            exit 1
        fi
        
        # 启动MongoDB服务
        systemctl start mongod
        systemctl enable mongod
        
        log_success "MongoDB安装并启动完成"
    fi
}

# 检查并安装RabbitMQ
install_rabbitmq() {
    log_info "检查RabbitMQ..."
    
    if command -v rabbitmq-server &> /dev/null; then
        log_success "RabbitMQ已安装"
    else
        log_warn "RabbitMQ未安装，正在安装..."
        
        if command -v apt-get &> /dev/null; then
            # Ubuntu/Debian
            apt-get install -y rabbitmq-server
        elif command -v yum &> /dev/null; then
            # CentOS/RHEL
            yum install -y rabbitmq-server
        else
            log_error "不支持的包管理器"
            exit 1
        fi
        
        # 启动RabbitMQ服务
        systemctl start rabbitmq-server
        systemctl enable rabbitmq-server
        
        log_success "RabbitMQ安装并启动完成"
    fi
}

# 检查系统资源
check_resources() {
    log_info "检查系统资源..."
    
    # 检查内存
    TOTAL_MEM=$(free -g | awk 'NR==2{print $2}')
    log_info "总内存: ${TOTAL_MEM}GB"
    
    if [ $TOTAL_MEM -lt 4 ]; then
        log_warn "内存可能不足，建议至少4GB内存"
    else
        log_success "内存满足要求"
    fi
    
    # 检查磁盘空间
    DISK_SPACE=$(df -h / | awk 'NR==2{print $4}' | sed 's/G//')
    log_info "可用磁盘空间: ${DISK_SPACE}GB"
    
    if [ $DISK_SPACE -lt 5 ]; then
        log_warn "磁盘空间可能不足，建议至少5GB可用空间"
    else
        log_success "磁盘空间满足要求"
    fi
    
    # 检查CPU核心数
    CPU_CORES=$(nproc)
    log_info "CPU核心数: $CPU_CORES"
    
    if [ $CPU_CORES -lt 2 ]; then
        log_warn "CPU核心数较少，可能影响性能"
    else
        log_success "CPU核心数满足要求"
    fi
}

# 创建应用目录
create_app_dirs() {
    log_info "创建应用目录..."
    
    # 创建部署目录
    mkdir -p /opt/doubao
    mkdir -p /opt/doubao/logs
    mkdir -p /opt/doubao/config
    mkdir -p /opt/doubao/backup
    mkdir -p /opt/doubao/backup/logs
    mkdir -p /opt/doubao/backup/config
    
    # 设置目录权限
    chown -R $USER:$USER /opt/doubao
    
    log_success "应用目录创建完成"
}

# 检查防火墙设置
check_firewall() {
    log_info "检查防火墙设置..."
    
    if command -v ufw &> /dev/null; then
        # Ubuntu/Debian
        if ufw status | grep -q "active"; then
            log_info "UFW防火墙已启用"
            # 检查端口是否开放
            if ufw status | grep -q "1017"; then
                log_success "端口1017已开放"
            else
                log_warn "端口1017未开放，建议开放此端口"
                read -p "是否自动开放端口1017? (y/N): " -n 1 -r
                echo
                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    ufw allow 1017
                    log_success "端口1017已开放"
                fi
            fi
        else
            log_info "UFW防火墙未启用"
        fi
    elif command -v firewall-cmd &> /dev/null; then
        # CentOS/RHEL
        if systemctl is-active --quiet firewalld; then
            log_info "Firewalld已启用"
            # 检查端口是否开放
            if firewall-cmd --list-ports | grep -q "1017"; then
                log_success "端口1017已开放"
            else
                log_warn "端口1017未开放，建议开放此端口"
                read -p "是否自动开放端口1017? (y/N): " -n 1 -r
                echo
                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    firewall-cmd --permanent --add-port=1017/tcp
                    firewall-cmd --reload
                    log_success "端口1017已开放"
                fi
            fi
        else
            log_info "Firewalld未启用"
        fi
    else
        log_info "未检测到常用防火墙"
    fi
}

# 配置系统参数
configure_system() {
    log_info "配置系统参数..."
    
    # 检查并配置文件描述符限制
    if [ -f /etc/security/limits.conf ]; then
        if ! grep -q "doubao-starter" /etc/security/limits.conf; then
            cat >> /etc/security/limits.conf << EOF
# doubao-starter configurations
* soft nofile 65536
* hard nofile 65536
* soft nproc 65536
* hard nproc 65536
EOF
            log_success "文件描述符限制已配置"
        fi
    fi
    
    # 检查并配置系统ctl参数
    if [ -f /etc/sysctl.conf ]; then
        if ! grep -q "vm.max_map_count" /etc/sysctl.conf; then
            echo "vm.max_map_count=262144" >> /etc/sysctl.conf
            sysctl -p
            log_success "系统参数vm.max_map_count已配置"
        fi
    fi
}

# 完整环境检查
full_check() {
    log_info "开始完整环境检查..."
    
    check_os
    check_resources
    install_java
    install_maven
    install_git
    install_mysql
    install_redis
    install_mongodb
    install_rabbitmq
    create_app_dirs
    check_firewall
    configure_system
    
    log_success "环境检查和配置完成"
    
    echo -e "\n${CYAN}=== 环境检查总结 ===${NC}"
    echo -e "${GREEN}Java: $(java -version 2>&1 | head -1 | cut -d'"' -f2)${NC}"
    echo -e "${GREEN}Maven: $(mvn -version 2>&1 | grep "Apache Maven" | cut -d' ' -f3)${NC}"
    echo -e "${GREEN}Git: $(git --version | cut -d' ' -f3)${NC}"
    echo -e "${GREEN}MySQL: $(mysql --version | cut -d' ' -f6 | cut -d',' -f1)${NC}"
    echo -e "${GREEN}Redis: $(redis-server --version 2>&1 | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1)${NC}"
    echo -e "${GREEN}MongoDB: $(mongod --version | grep "db version" | cut -d' ' -f3)${NC}"
    echo -e "${GREEN}RabbitMQ: 已安装${NC}"
    echo -e "${GREEN}可用内存: $(free -g | awk 'NR==2{print $4}')GB${NC}"
    echo -e "${GREEN}可用磁盘: $(df -h / | awk 'NR==2{print $4}')${NC}"
}

# 显示帮助信息
show_help() {
    echo -e "${CYAN}doubao-starter 环境配置脚本${NC}"
    echo "用法: $0 [选项]"
    echo ""
    echo "可用选项:"
    echo "  check       - 检查当前环境"
    echo "  install     - 安装所有依赖"
    echo "  java        - 检查/安装Java"
    echo "  maven       - 检查/安装Maven"
    echo "  git         - 检查/安装Git"
    echo "  mysql       - 检查/安装MySQL"
    echo "  redis       - 检查/安装Redis"
    echo "  mongodb     - 检查/安装MongoDB"
    echo "  rabbitmq    - 检查/安装RabbitMQ"
    echo "  resources   - 检查系统资源"
    echo "  full        - 完整环境检查和配置"
    echo "  help        - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 check     - 检查当前环境"
    echo "  $0 full      - 完整环境配置"
    echo "  $0 resources - 检查系统资源"
}

# 主函数
case "$1" in
    check)
        check_os
        check_resources
        ;;
    install)
        install_java
        install_maven
        install_git
        ;;
    java)
        install_java
        ;;
    maven)
        install_maven
        ;;
    git)
        install_git
        ;;
    mysql)
        install_mysql
        ;;
    redis)
        install_redis
        ;;
    mongodb)
        install_mongodb
        ;;
    rabbitmq)
        install_rabbitmq
        ;;
    resources)
        check_resources
        ;;
    full)
        full_check
        ;;
    help|*)
        show_help
        ;;
esac