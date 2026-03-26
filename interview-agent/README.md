# interview-agent 模块说明

## 1. 模块定位

`interview-agent` 是项目中的“代码生成实验与落地区”。

主要用途：

1. 承接后续通过 Agent 自动生成的业务代码。
2. 作为模板化开发的标准样板，统一依赖、分层、日志、注释与可运行规范。
3. 确保每次新增代码都能在本模块内独立打通最小流程（Controller -> Service -> DTO）。

当前模块已包含最小可运行链路，接口如下：

- `GET /interview-agent/health`：健康检查
- `POST /interview-agent/generate`：代码生成占位入口

## 2. 模块结构

本模块遵循项目统一的三层子模块结构：

```text
interview-agent
├─ interview-agent-api
│  └─ src/main/java/org/doubao/interview/agent/api
│     ├─ dto
│     └─ service
├─ interview-agent-server
│  └─ src/main/java/org/doubao/interview/agent/server
│     ├─ controller
│     └─ service/impl
└─ interview-agent-starter
   └─ src/main
      ├─ java/org/doubao/interview/agent/starter
      └─ resources/bootstrap.yml
```

### 分层职责

- `api`：只放可复用协议对象、接口定义（DTO/VO/Service Interface/Feign Interface）。
- `server`：放具体业务实现（Controller、ServiceImpl、Repository/Mapper、Domain）。
- `starter`：只做启动装配（SpringBoot 主类、配置文件、运行入口）。

## 3. 包名规范（后续生成代码必须遵守）

基础包：`org.doubao.interview.agent`

推荐包结构：

- `org.doubao.interview.agent.api.dto`：请求/响应 DTO
- `org.doubao.interview.agent.api.service`：服务接口
- `org.doubao.interview.agent.server.controller`：HTTP 接口层
- `org.doubao.interview.agent.server.service`：服务抽象（如有必要）
- `org.doubao.interview.agent.server.service.impl`：服务实现
- `org.doubao.interview.agent.server.mapper`：MyBatis Mapper
- `org.doubao.interview.agent.server.entity`：数据库实体
- `org.doubao.interview.agent.server.domain`：领域对象
- `org.doubao.interview.agent.server.config`：模块配置类
- `org.doubao.interview.agent.starter`：启动类

## 4. 依赖规范（后续生成代码必须遵守）

### 4.1 必需依赖

- `interview-agent-api` 依赖 `mall-common`
- `interview-agent-server` 依赖 `interview-agent-api`
- `interview-agent-starter` 依赖 `interview-agent-server`

### 4.2 常用增强依赖（按需新增）

1. Web/API 能力：`spring-boot-starter-web`
2. 参数校验：`spring-boot-starter-validation`
3. 数据访问：`mybatis-plus-boot-starter`、数据库驱动
4. 消息队列：`spring-boot-starter-amqp`
5. 缓存：`spring-boot-starter-data-redis`
6. 可观测：`spring-boot-starter-actuator`
7. 测试：`spring-boot-starter-test`

说明：新增依赖时，优先和现有服务保持一致版本与生态，不要在本模块引入“孤岛依赖”。

## 5. Agent 生成代码执行规范

后续 Agent 在本模块生成代码时，必须满足以下要求：

1. 代码完整可运行：
   需要包含请求对象、控制器、服务接口、服务实现、配置与启动路径，不能只给片段。
2. 流程可打通：
   至少保证一个端到端接口可访问，返回明确结果，并有日志可追踪。
3. 注释要“解释原因”而不只是“复述代码”：
   尤其是边界处理、兜底逻辑、并发点、事务点、重试策略。
4. 日志要有上下文：
   至少包含 `traceId(如有)`、关键业务主键、状态、耗时、异常原因。
5. 不破坏现有工程结构：
   目录、命名、依赖方向必须符合本 README 与项目既有规范。
6. 主要业务逻辑和实体类添加完整详细的中文注释

## 6. 注释与日志标准模板

### 6.1 中文注释标准

建议每个核心类至少具备：

1. 类注释：职责、边界、是否线程安全、是否幂等。
2. 方法注释：输入约束、输出语义、异常场景、性能注意点。
3. 关键代码段注释：解释“为什么这样做”。

### 6.2 日志建议

- `info`：主流程关键节点（开始、结束、关键参数摘要）
- `warn`：可恢复异常、降级路径、重试触发
- `error`：不可恢复异常（附带业务主键和上下文）

日志示例：

```java
log.info("开始处理生成任务, title={}, requirementLength={}", title, requirementLength);
log.warn("模板渲染失败，进入兜底流程, title={}, reason={}", title, ex.getMessage());
log.error("生成任务失败, title={}, traceId={}", title, traceId, ex);
```

## 7. 本地运行与验证

### 7.1 编译

在项目根目录执行：

```bash
mvn -pl interview-agent -am clean compile
```

### 7.2 启动

```bash
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 7.3 验证

健康检查：

```bash
curl http://localhost:9510/interview-agent/health
```

代码生成占位接口：

```bash
curl -X POST "http://localhost:9510/interview-agent/generate" \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"生成示例\",\"requirement\":\"请生成一个最小可运行的controller+service\"}"
```

## 8. 与面试文档协同方式

参考文档：`docs/interview-agent/Java后端面试问答归档.md`

建议协同流程：

1. 从面试题中抽取一个可落地主题（如“限流”“幂等”“缓存一致性”）。
2. 在 `interview-agent` 中用独立包实现该主题最小闭环代码。
3. 在代码注释里回写“这个实现对应哪条面试知识点”。
4. 形成“问答 -> 代码”的双向索引，提升记忆和理解深度。

## 9. 后续扩展建议

1. 引入测试基线：单元测试 + 接口测试 + 关键流程回归脚本。
2. 增加统一错误码和响应包装，和现有 `mall-common` 保持一致。
3. 增加模板引擎与 Prompt 管理，把“生成规则”产品化。
4. 增加生成结果评审器（静态检查、依赖检查、命名检查、注释覆盖率检查）。
