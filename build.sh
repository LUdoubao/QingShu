#!/bin/bash

# doubao-starter 项目构建脚本
# 作者: Lingma
# 日期: 2025-12-25

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 检查Maven是否安装
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装或不在PATH中"
        exit 1
    fi
    
    MVN_VERSION=$(mvn -version 2>&1 | grep "Apache Maven" | cut -d' ' -f3)
    log_success "Maven 已安装: $MVN_VERSION"
}

# 检查Java是否安装
check_java() {
    if ! command -v java &> /dev/null; then
        log_error "Java 未安装或不在PATH中"
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    log_success "Java 已安装: $JAVA_VERSION"
}

# 检查项目根目录
check_project_root() {
    if [ ! -f "pom.xml" ] || [ ! -d "doubao-starter" ]; then
        log_error "当前目录不是项目根目录"
        exit 1
    fi
    
    log_success "在项目根目录中"
}

# 清理项目
clean_project() {
    log_info "清理项目..."
    mvn clean
    if [ $? -ne 0 ]; then
        log_error "清理项目失败"
        exit 1
    fi
    log_success "项目清理完成"
}

# 构建所有模块
build_all() {
    log_info "开始构建所有模块..."
    
    # 构建整个项目
    mvn install -DskipTests
    if [ $? -ne 0 ]; then
        log_error "构建所有模块失败"
        exit 1
    fi
    
    log_success "所有模块构建完成"
}

# 构建doubao-starter模块
build_starter() {
    log_info "构建doubao-starter模块..."
    
    mvn package -pl doubao-starter -DskipTests
    if [ $? -ne 0 ]; then
        log_error "构建doubao-starter模块失败"
        exit 1
    fi
    
    # 检查JAR文件是否生成
    if [ ! -f "doubao-starter/target/doubao-starter.jar" ]; then
        log_error "doubao-starter JAR文件未生成"
        exit 1
    fi
    
    JAR_SIZE=$(du -h doubao-starter/target/doubao-starter.jar | cut -f1)
    log_success "doubao-starter JAR文件已生成，大小: $JAR_SIZE"
}

# 复制JAR文件到指定目录
copy_jar() {
    local target_dir=${1:-"/opt/doubao"}
    
    log_info "复制JAR文件到 $target_dir..."
    
    mkdir -p $target_dir
    cp doubao-starter/target/doubao-starter.jar $target_dir/
    
    log_success "JAR文件已复制到 $target_dir"
}

# 构建Docker镜像
build_docker() {
    log_info "构建Docker镜像..."
    
    if ! command -v docker &> /dev/null; then
        log_warn "Docker 未安装，跳过Docker镜像构建"
        return
    fi
    
    # 检查Docker服务是否运行
    if ! docker info &> /dev/null; then
        log_warn "Docker服务未运行，跳过Docker镜像构建"
        return
    fi
    
    # 构建JAR文件
    build_starter
    
    # 构建Docker镜像
    docker build -t doubao-starter:latest .
    if [ $? -ne 0 ]; then
        log_error "Docker镜像构建失败"
        exit 1
    fi
    
    log_success "Docker镜像构建完成"
}

# 打包发布
package_release() {
    local package_name=${1:-"doubao-starter-release-$(date +%Y%m%d_%H%M%S)"}
    
    log_info "打包发布版本: $package_name.tar.gz"
    
    # 构建JAR文件
    build_starter
    
    # 创建发布目录
    mkdir -p $package_name
    cp doubao-starter/target/doubao-starter.jar $package_name/
    cp doubao-starter/application.yml $package_name/ 2>/dev/null || log_warn "配置文件不存在，跳过复制"
    cp doubao-starter/start.sh $package_name/ 2>/dev/null || log_warn "启动脚本不存在，跳过复制"
    cp doubao-starter/deploy.sh $package_name/ 2>/dev/null || log_warn "部署脚本不存在，跳过复制"
    cp doubao-starter/README.md $package_name/ 2>/dev/null || log_warn "README文件不存在，跳过复制"
    
    # 创建tar包
    tar -czf ${package_name}.tar.gz $package_name
    rm -rf $package_name
    
    log_success "发布包已创建: ${package_name}.tar.gz"
}

# 完整构建流程
full_build() {
    log_info "开始完整构建流程..."
    
    check_java
    check_maven
    check_project_root
    clean_project
    build_all
    build_starter
    
    log_success "完整构建流程完成"
}

# 显示帮助信息
show_help() {
    echo -e "${BLUE}doubao-starter 项目构建脚本${NC}"
    echo "用法: $0 [选项]"
    echo ""
    echo "可用选项:"
    echo "  check       - 检查环境"
    echo "  clean       - 清理项目"
    echo "  all         - 构建所有模块"
    echo "  starter     - 构建doubao-starter模块"
    echo "  copy [dir]  - 复制JAR文件到指定目录 (默认: /opt/doubao)"
    echo "  docker      - 构建Docker镜像"
    echo "  package [name] - 打包发布版本 (默认: doubao-starter-release-YYYYMMDD_HHMMSS)"
    echo "  full        - 完整构建流程"
    echo "  help        - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 check        - 检查环境"
    echo "  $0 full         - 完整构建"
    echo "  $0 docker       - 构建Docker镜像"
    echo "  $0 package      - 打包发布版本"
    echo "  $0 copy /opt/myapp - 复制JAR到指定目录"
}

# 主函数
case "$1" in
    check)
        check_java
        check_maven
        check_project_root
        ;;
    clean)
        clean_project
        ;;
    all)
        build_all
        ;;
    starter)
        build_starter
        ;;
    copy)
        copy_jar ${2:-"/opt/doubao"}
        ;;
    docker)
        build_docker
        ;;
    package)
        package_release ${2:-"doubao-starter-release-$(date +%Y%m%d_%H%M%S)"}
        ;;
    full)
        full_build
        ;;
    help|*)
        show_help
        ;;
esac