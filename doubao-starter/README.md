# doubao-starter 运维脚本说明

## 项目概述

doubao-starter 是一个集成了多个微服务的单体应用，包括用户服务、认证服务、AI服务、语录服务、点赞服务、评论服务、收藏服务、对话服务、分享服务、观看计数服务、消息推送服务、搜索服务、信息流服务、扇出服务和OSS服务等。

## 运维脚本说明

本项目提供了三个主要的运维脚本，用于在Linux环境下自动化管理doubao-starter应用：

### 1. deploy.sh - 基础运维脚本

基础运维脚本，提供应用的启动、停止、重启、状态检查等基本功能。

#### 功能列表
- `start` - 启动应用
- `stop` - 停止应用
- `restart` - 重启应用
- `status` - 查看应用状态
- `log` - 查看应用日志
- `tail` - 实时查看日志
- `clean` - 清理日志
- `build` - 构建整个项目
- `build-starter` - 构建doubao-starter模块
- `help` - 显示帮助信息

#### 使用示例
```bash
# 启动应用
./deploy.sh start

# 查看应用状态
./deploy.sh status

# 实时查看日志
./deploy.sh tail

# 构建doubao-starter模块
./deploy.sh build-starter
```

### 2. config-manager.sh - 配置管理脚本

配置管理脚本，提供配置文件的备份、恢复、环境检查等功能。

#### 功能列表
- `show` - 显示当前配置
- `backup` - 备份配置和日志
- `restore [timestamp]` - 从备份恢复配置
- `list` - 列出所有备份
- `env` - 检查运行环境
- `monitor` - 性能监控
- `security` - 安全检查
- `help` - 显示帮助信息

#### 使用示例
```bash
# 显示当前配置
./config-manager.sh show

# 备份配置
./config-manager.sh backup

# 列出所有备份
./config-manager.sh list

# 检查运行环境
./config-manager.sh env
```

### 3. deploy-full.sh - 一键部署脚本

完整部署脚本，提供从构建到部署的一站式解决方案。

#### 功能列表
- `build` - 构建项目
- `deploy` - 部署应用
- `start` - 启动应用
- `stop` - 停止应用
- `restart` - 重启应用
- `status` - 显示部署状态
- `health` - 检查应用健康状态
- `service` - 创建系统服务
- `full` - 完整部署流程 (构建+部署+启动)
- `check` - 检查依赖和环境
- `help` - 显示帮助信息

#### 使用示例
```bash
# 完整部署流程
./deploy-full.sh full

# 检查环境依赖
./deploy-full.sh check

# 查看部署状态
./deploy-full.sh status

# 创建系统服务
./deploy-full.sh service
```

## 部署前准备

### 环境要求
- Java 8 或更高版本
- Maven 3.6.0 或更高版本
- Linux 系统 (推荐 Ubuntu 18.04+ 或 CentOS 7+)

### 依赖服务
在运行doubao-starter之前，请确保以下服务已启动：
- MySQL (端口 3306)
- Redis (端口 6379)
- MongoDB (端口 27017)
- RabbitMQ (端口 5672)

### 配置文件
应用使用 [application.yml](file:///D:/workspace/doubao/SpringCloudDemo/doubao-starter/src/main/resources/application.yml) 进行配置，主要配置项包括：

```yaml
server:
  port: 1017  # 应用端口

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/doubao_db
    username: root
    password: LG101799
  redis:
    host: 123.56.13.199
    port: 6379
  data:
    mongodb:
      host: 123.56.13.199
      port: 27017
      database: doubao_db
      username: root
      password: Lg101799.
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: doubao
    password: 123456
```

## 部署步骤

### 1. 构建项目
```bash
# 方法一：使用基础脚本构建
./deploy.sh build-starter

# 方法二：使用完整部署脚本
./deploy-full.sh build
```

### 2. 启动应用
```bash
# 方法一：使用基础脚本
./deploy.sh start

# 方法二：使用完整部署脚本
./deploy-full.sh start

# 方法三：使用完整部署流程
./deploy-full.sh full
```

### 3. 检查应用状态
```bash
# 查看应用状态
./deploy.sh status

# 查看实时日志
./deploy.sh tail
```

## 系统服务配置

可以使用以下命令创建systemd服务，实现开机自启：

```bash
# 创建系统服务
./deploy-full.sh service

# 启动服务
systemctl start doubao-starter

# 设置开机自启
systemctl enable doubao-starter

# 查看服务状态
systemctl status doubao-starter
```

## 故障排除

### 常见问题

1. **端口被占用**
   ```bash
   # 检查端口占用情况
   netstat -tuln | grep 1017
   ```

2. **依赖服务未启动**
   ```bash
   # 检查MySQL
   systemctl status mysql

   # 检查Redis
   systemctl status redis

   # 检查MongoDB
   systemctl status mongod
   ```

3. **内存不足**
   ```bash
   # 检查系统内存
   free -h

   # 检查应用内存使用
   ./config-manager.sh monitor
   ```

### 日志查看
```bash
# 查看应用日志
tail -f logs/doubao-starter.log

# 查看GC日志
tail -f logs/gc.log
```

## 维护操作

### 备份配置
```bash
# 备份当前配置
./config-manager.sh backup
```

### 恢复配置
```bash
# 列出可用备份
./config-manager.sh list

# 恢复指定备份
./config-manager.sh restore 20251225_173000
```

### 清理日志
```bash
# 清理日志文件
./deploy.sh clean
```

## 安全建议

1. 修改默认的数据库密码和Redis密码
2. 定期备份配置文件
3. 监控应用性能和安全日志
4. 定期更新依赖库版本