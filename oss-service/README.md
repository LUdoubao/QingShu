# OSS Service

对象存储服务，用于处理文件上传、下载和管理。

## 模块结构

- `oss-service-api`: API接口模块，包含服务定义、DTO等公共接口
- `oss-service-server`: 服务实现模块，包含具体的业务逻辑实现

## 功能特性

- 支持本地存储和阿里云OSS存储
- 文件上传/下载
- 文件路径加密
- 支持多种文件类型验证

## 配置

配置文件位于 `oss-service-server/src/main/resources/` 目录下：
- `application.yml`: 应用配置
- `bootstrap.yml`: 配置中心相关配置

## 存储策略

- 本地存储: 存储到指定目录
- 阿里云OSS: 上传到阿里云对象存储服务