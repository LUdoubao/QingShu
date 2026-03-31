# Spring 框架设计模式完整实现指南

## 📚 目录结构

```
interview-agent/
├── interview-agent-api/
│   └── src/main/java/org/doubao/interview/agent/api/
│       ├── dto/designpattern/          # DTO 对象
│       │   └── BeanConfig.java
│       └── service/designpattern/      # 服务接口
│           ├── BeanFactory.java
│           ├── OrderService.java
│           ├── DataSource.java
│           ├── Advice.java
│           ├── BeforeAdvice.java
│           ├── Interceptor.java
│           ├── PlatformTransactionManager.java
│           ├── TransactionDefinition.java
│           ├── TransactionStatus.java
│           ├── ApplicationEvent.java
│           ├── ApplicationListener.java
│           ├── ApplicationEventPublisher.java
│           └── HandlerInterceptor.java
├── interview-agent-server/
│   └── src/main/java/org/doubao/interview/agent/server/
│       ├── controller/designpattern/   # Controller 层
│       │   └── DesignPatternDemoController.java
│       ├── service/designpattern/      # Service 层实现
│       │   ├── SingletonBeanRegistry.java
│       │   ├── DefaultBeanFactory.java
│       │   ├── OrderServiceImpl.java
│       │   ├── JdkProxyFactory.java
│       │   ├── SimpleDataSource.java
│       │   ├── TransactionAwareDataSource.java
│       │   ├── BeforeAdviceAdapter.java
│       │   ├── DataSourceTransactionManager.java
│       │   ├── DataSourceTransactionStatus.java
│       │   ├── JdbcTemplate.java
│       │   ├── RowMapper.java
│       │   ├── ContextRefreshedEvent.java
│       │   ├── CacheWarmupListener.java
│       │   ├── SimpleApplicationEventMulticaster.java
│       │   ├── InterceptorChain.java
│       │   ├── LoggingInterceptor.java
│       │   ├── AuthInterceptor.java
│       │   ├── DispatcherServlet.java
│       │   └── ... (辅助类)
│       └── example/designpattern/      # 示例 Bean
│           ├── UserService.java
│           ├── RequestHandler.java
│           ├── ApplicationContextBuilder.java
│           └── MockApplicationContext.java
└── scripts/                            # 测试脚本
    ├── test-design-pattern.bat         # Windows 版本
    └── test-design-pattern.sh          # Linux/Mac 版本
```

---

## 🎯 设计模式分类详解

### 一、核心基础模式（IoC 容器核心）

#### 1. 工厂模式（Factory Pattern）

**核心类**：
- `BeanFactory` - 顶级工厂接口
- `DefaultBeanFactory` - 工厂实现类
- `BeanConfig` - Bean 配置信息

**应用场景**：
- Spring IoC 容器创建和管理 Bean
- 隐藏对象创建细节，实现创建与使用解耦

**关键代码**：
```java
// 工厂接口定义
public interface BeanFactory {
    Object getBean(String beanName);
    <T> T getBean(Class<T> requiredType);
}

// 工厂实现
public class DefaultBeanFactory implements BeanFactory {
    private final Map<String, BeanConfig> beanDefinitionMap = new ConcurrentHashMap<>();
    
    @Override
    public Object getBean(String beanName) {
        BeanConfig config = beanDefinitionMap.get(beanName);
        if ("prototype".equals(config.getScope())) {
            return createPrototypeBean(config);
        } else {
            return getOrCreateSingletonBean(beanName, config);
        }
    }
}
```

**测试接口**：`GET /design-pattern/factory-singleton`

---

#### 2. 单例模式（Singleton Pattern）

**核心类**：
- `SingletonBeanRegistry` - 单例注册器
- `UserService` - 单例 Bean 示例

**实现方式**：
- 使用 `ConcurrentHashMap` 缓存单例 Bean
- 通过 `synchronized + DCL`保证线程安全

**关键代码**：
```java
public class SingletonBeanRegistry {
    // 单例缓存池
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    
    public void registerSingleton(String beanName, Object singletonObject) {
        // 第一次检查（无锁）
        Object existingObject = singletonObjects.get(beanName);
        if (existingObject != null) {
            throw new BeanCreationException("Bean already exists");
        }
        
        // 加锁并第二次检查
        synchronized (this) {
            existingObject = singletonObjects.get(beanName);
            if (existingObject != null) {
                throw new BeanCreationException("Concurrent registration detected");
            }
            singletonObjects.put(beanName, singletonObject);
        }
    }
}
```

**验证点**：两次获取的 Bean 实例 hashcode 相同

---

#### 3. 原型模式（Prototype Pattern）

**核心类**：
- `RequestHandler` - 原型 Bean 示例

**应用场景**：
- 有状态对象（如 HttpRequest 相关）
- 避免多线程共享状态导致线程安全问题

**关键代码**：
```java
private Object createPrototypeBean(BeanConfig config) {
    Object prototypeInstance = doCreateBean(config);
    log.info("创建原型 Bean 实例：{}, hashcode={}", 
            config.getBeanName(), prototypeInstance.hashCode());
    return prototypeInstance;
}
```

**验证点**：每次 getBean() 都返回新实例，hashcode 不同

---

#### 4. 建造者模式（Builder Pattern）

**核心类**：
- `ApplicationContextBuilder` - 构建器
- `MockApplicationContext` - 最终产品

**优势**：
- 链式调用，代码简洁
- 分步构建复杂对象
- 支持可选参数和默认值

**关键代码**：
```java
@Accessors(chain = true)
public class ApplicationContextBuilder {
    private String applicationName;
    private String configLocation;
    private boolean lazyInit = false;
    private String basePackage;
    
    public static ApplicationContextBuilder newInstance() {
        return new ApplicationContextBuilder();
    }
    
    public ApplicationContextBuilder withApplicationName(String name) {
        this.applicationName = name;
        return this;
    }
    
    public MockApplicationContext build() {
        if (applicationName == null) {
            throw new IllegalStateException("applicationName is required");
        }
        return new MockApplicationContext(applicationName, configLocation, 
                                        lazyInit, enableAnnotationScan, 
                                        basePackage, activeProfile);
    }
}

// 使用示例
MockApplicationContext context = ApplicationContextBuilder.newInstance()
    .withApplicationName("DesignPatternDemoApp")
    .withConfigLocation("classpath:application.yml")
    .withBasePackage("org.doubao.interview.agent")
    .withActiveProfile("dev")
    .withLazyInit()
    .build();
```

**测试接口**：`GET /design-pattern/builder`

---

### 二、AOP 相关模式

#### 5. 代理模式（Proxy Pattern）

**核心类**：
- `OrderService` - 业务接口
- `OrderServiceImpl` - 目标对象
- `JdkProxyFactory` - 代理工厂

**两种实现方式**：
1. JDK 动态代理：目标类实现接口时使用
2. CGLIB 动态代理：目标类无接口时使用

**关键代码**：
```java
public class JdkProxyFactory {
    public static <T> T createProxy(T target) {
        InvocationHandler handler = new ProxyInvocationHandler(target);
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            handler
        );
    }
    
    private static class ProxyInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 前置通知
            beforeMethod(method, args);
            
            try {
                // 执行目标方法
                Object result = method.invoke(target, args);
                
                // 返回后通知
                afterReturning(method, result);
                return result;
            } catch (Exception e) {
                // 异常通知
                afterThrowing(method, e);
                throw e;
            } finally {
                // 最终通知
                afterFinally(method, duration);
            }
        }
    }
}
```

**测试接口**：`GET /design-pattern/proxy`

---

#### 6. 装饰器模式（Decorator Pattern）

**核心类**：
- `DataSource` - 组件接口
- `SimpleDataSource` - 基础组件
- `TransactionAwareDataSource` - 装饰器

**与代理模式的区别**：
- 装饰器模式强调功能增强
- 代理模式强调控制访问

**关键代码**：
```java
public class TransactionAwareDataSource implements DataSource {
    private final DataSource targetDataSource;
    private boolean transactionActive = false;
    
    @Override
    public Object getConnection() {
        // 1. 检查事务状态
        if (!transactionActive) {
            beginTransaction();
        }
        
        // 2. 获取基础连接
        Object connection = targetDataSource.getConnection();
        
        // 3. 绑定到线程
        bindConnectionToThread(connection);
        
        return connection;
    }
}

// 使用示例
DataSource simpleDS = new SimpleDataSource("SimpleDS", "jdbc:mysql://localhost:3306/test");
DataSource transactionalDS = new TransactionAwareDataSource(simpleDS);
Object connection = transactionalDS.getConnection();
transactionalDS.commit();
```

**测试接口**：`GET /design-pattern/decorator`

---

#### 7. 适配器模式（Adapter Pattern）

**核心类**：
- `BeforeAdvice` - 被适配者接口
- `Interceptor` - 目标接口
- `BeforeAdviceAdapter` - 适配器

**应用场景**：
- Spring AOP 中将 Advice 适配为 Interceptor
- Spring MVC 中 HandlerAdapter 适配不同处理器

**关键代码**：
```java
public class BeforeAdviceAdapter implements Advice {
    private final BeforeAdvice beforeAdvice;
    
    public Interceptor toInterceptor() {
        return new Interceptor() {
            @Override
            public Object intercept(String method, Object[] args, Object target) {
                // 1. 调用 BeforeAdvice 的前置逻辑
                beforeAdvice.before(method, args, target);
                
                // 2. 执行目标方法
                return invokeTargetMethod(method, args, target);
            }
        };
    }
}
```

**测试接口**：包含在代理模式演示中

---

#### 8. 策略模式（Strategy Pattern）

**核心类**：
- `PlatformTransactionManager` - 策略接口
- `DataSourceTransactionManager` - 具体策略实现
- `TransactionDefinition` - 策略参数

**优势**：
- 算法与使用方解耦
- 新增策略无需修改原有代码（符合开闭原则）

**关键代码**：
```java
// 策略接口
public interface PlatformTransactionManager {
    TransactionStatus getTransaction(TransactionDefinition definition);
    void commit(TransactionStatus status);
    void rollback(TransactionStatus status);
}

// 具体策略：数据源事务管理器
public class DataSourceTransactionManager implements PlatformTransactionManager {
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) {
        log.info("隔离级别：{}", getIsolationLevelName(definition.getIsolationLevel()));
        log.info("传播行为：{}", getPropagationBehaviorName(definition.getPropagationBehavior()));
        
        DataSourceTransactionStatus status = new DataSourceTransactionStatus();
        status.setNewTransaction(true);
        return status;
    }
    
    @Override
    public void commit(TransactionStatus status) {
        // JDBC 提交逻辑
    }
    
    @Override
    public void rollback(TransactionStatus status) {
        // JDBC 回滚逻辑
    }
}
```

**测试接口**：`GET /design-pattern/strategy`

---

### 三、资源与扩展模式

#### 9. 模板方法模式（Template Method Pattern）

**核心类**：
- `JdbcTemplate` - 模板类
- `RowMapper` - 回调接口

**核心思想**：
- 固定流程在模板中实现
- 可变步骤延迟到子类或回调实现

**关键代码**：
```java
public class JdbcTemplate {
    // 模板方法：固定流程
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // 固定步骤 1：获取连接
            conn = dataSource.getConnection();
            
            // 固定步骤 2：创建 PreparedStatement
            ps = conn.prepareStatement(sql);
            setParameters(ps, args);
            
            // 固定步骤 3：执行查询
            rs = ps.executeQuery();
            
            // 可变步骤：通过回调处理结果集
            T result = null;
            if (rs.next()) {
                result = rowMapper.mapRow(rs, rs.getRow());
            }
            
            return result;
        } finally {
            // 固定步骤 4：关闭资源
            closeResources(rs, ps, conn);
        }
    }
}

// 用户使用示例
String sql = "SELECT * FROM users WHERE id = ?";
User user = jdbcTemplate.queryForObject(sql, new RowMapper<User>() {
    @Override
    public User mapRow(ResultSet rs, int rowNum) {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        return u;
    }
}, 1L);
```

**测试接口**：`GET /design-pattern/template-method`

---

#### 10. 观察者模式（Observer Pattern）

**核心组件**：
- `ApplicationEvent` - 事件
- `ApplicationListener` - 监听器
- `ApplicationEventPublisher` - 事件发布器

**核心思想**：
- 一对多依赖关系
- 对象状态改变时自动通知所有观察者

**关键代码**：
```java
// 事件发布器
public class SimpleApplicationEventMulticaster implements ApplicationEventPublisher {
    private final List<ApplicationListener<?>> listeners = new ArrayList<>();
    
    public void addListener(ApplicationListener<?> listener) {
        listeners.add(listener);
    }
    
    @Override
    public void publishEvent(ApplicationEvent event) {
        for (ApplicationListener<?> listener : listeners) {
            if (supportsEventType(listener, event)) {
                listener.onApplicationEvent(event);
            }
        }
    }
}

// 监听器实现
public class CacheWarmupListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("接收到容器刷新事件");
        warmupCache();
    }
    
    private void warmupCache() {
        // 缓存预热逻辑
    }
}

// 使用示例
SimpleApplicationEventMulticaster publisher = new SimpleApplicationEventMulticaster();
publisher.addListener(new CacheWarmupListener());
publisher.publishEvent(new ContextRefreshedEvent(this));
```

**测试接口**：`GET /design-pattern/observer`

---

#### 11. 责任链模式（Chain of Responsibility）

**核心类**：
- `HandlerInterceptor` - 拦截器接口
- `InterceptorChain` - 责任链
- `LoggingInterceptor` / `AuthInterceptor` - 具体拦截器

**核心思想**：
- 多个处理器连成链
- 请求依次传递，每个处理器决定是否处理

**关键代码**：
```java
// 拦截器接口
public interface HandlerInterceptor {
    boolean preHandle(Object request, Object response, Object handler);
    void postHandle(Object request, Object response, Object handler, Object mv);
    void afterCompletion(Object request, Object response, Object handler, Exception ex);
}

// 责任链实现
public class InterceptorChain {
    private final List<HandlerInterceptor> interceptors = new ArrayList<>();
    
    public boolean applyPreHandle(Object request, Object response, Object handler) throws Exception {
        for (int i = 0; i < interceptors.size(); i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            boolean result = interceptor.preHandle(request, response, handler);
            
            if (!result) {
                // 中断请求，反向执行已执行的 afterCompletion
                triggerAfterCompletion(request, response, handler, null, i - 1);
                return false;
            }
        }
        return true;
    }
}

// 拦截器实现示例
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(Object request, Object response, Object handler) {
        String token = getTokenFromRequest(request);
        
        if (!isLoggedIn(token)) {
            sendUnauthorizedResponse(response);
            return false; // 中断请求
        }
        
        if (!hasPermission(token, request)) {
            sendForbiddenResponse(response);
            return false; // 中断请求
        }
        
        return true; // 继续执行
    }
}
```

**测试接口**：`GET /design-pattern/chain-of-responsibility`

---

#### 12. 委派模式（Delegate Pattern）

**核心类**：
- `DispatcherServlet` - 前端控制器（统一入口）
- `HandlerMapping` - 处理器映射器
- `HandlerAdapter` - 处理器适配器
- `ViewResolver` - 视图解析器

**核心思想**：
- 统一入口，集中处理
- 将具体任务委派给专业组件

**关键代码**：
```java
public class DispatcherServlet {
    private final HandlerMapping handlerMapping;
    private final HandlerAdapter handlerAdapter;
    private final ViewResolver viewResolver;
    private final InterceptorChain interceptorChain;
    
    public String handleRequest(String request) {
        // 步骤 1：执行拦截器链
        if (!interceptorChain.applyPreHandle(request, null, null)) {
            return "403 Forbidden";
        }
        
        // 步骤 2：查找处理器（委派给 HandlerMapping）
        Object handler = handlerMapping.getHandler(request);
        if (handler == null) {
            return "404 Not Found";
        }
        
        // 步骤 3：执行处理器（委派给 HandlerAdapter）
        Object modelAndView = handlerAdapter.handle(handler, request);
        
        // 步骤 4：解析视图（委派给 ViewResolver）
        String viewName = viewResolver.resolveView(modelAndView);
        
        // 步骤 5：执行拦截器后处理
        interceptorChain.applyPostHandle(request, null, handler, modelAndView);
        interceptorChain.applyAfterCompletion(request, null, handler, null);
        
        return viewName;
    }
}
```

**测试接口**：`GET /design-pattern/delegation`

---

## 🚀 快速开始

### 1. 编译项目

```bash
mvn -pl interview-agent -am clean compile
```

### 2. 启动应用

```bash
mvn -pl interview-agent/interview-agent-starter -am spring-boot:run
```

### 3. 验证接口

**方法一：使用测试脚本**

Windows:
```bash
scripts\test-design-pattern.bat
```

Linux/Mac:
```bash
./scripts/test-design-pattern.sh
```

**方法二：手动测试**

```bash
# 健康检查
curl http://localhost:9510/design-pattern/health

# 测试单个设计模式
curl http://localhost:9510/design-pattern/factory-singleton
curl http://localhost:9510/design-pattern/prototype
curl http://localhost:9510/design-pattern/builder
curl http://localhost:9510/design-pattern/proxy
curl http://localhost:9510/design-pattern/decorator
curl http://localhost:9510/design-pattern/strategy
curl http://localhost:9510/design-pattern/template-method
curl http://localhost:9510/design-pattern/observer
curl http://localhost:9510/design-pattern/chain-of-responsibility
curl http://localhost:9510/design-pattern/delegation
```

---

## 📊 设计模式对比总结

| 模式名称 | 分类 | 核心作用 | Spring 中的应用 |
|---------|------|---------|----------------|
| 工厂模式 | 创建型 | 封装对象创建逻辑 | BeanFactory 创建 Bean |
| 单例模式 | 创建型 | 保证全局唯一实例 | Bean 默认作用域 |
| 原型模式 | 创建型 | 每次返回新实例 | prototype 作用域 |
| 建造者模式 | 创建型 | 分步构建复杂对象 | ApplicationContextBuilder |
| 代理模式 | 结构型 | 控制访问/添加横切逻辑 | Spring AOP 核心 |
| 装饰器模式 | 结构型 | 动态添加功能 | TransactionAwareDataSource |
| 适配器模式 | 结构型 | 接口转换 | AdvisorAdapter |
| 策略模式 | 行为型 | 灵活切换算法 | PlatformTransactionManager |
| 模板方法模式 | 行为型 | 固定流程，扩展步骤 | JdbcTemplate |
| 观察者模式 | 行为型 | 一对多通知 | Spring 事件机制 |
| 责任链模式 | 行为型 | 链式处理请求 | HandlerInterceptor 链 |
| 委派模式 | 行为型 | 任务分发 | DispatcherServlet |

---

## 💡 学习建议

### 1. 理解顺序
建议按照以下顺序学习：
1. **基础**：工厂 -> 单例 -> 原型
2. **AOP**：代理 -> 装饰器 -> 适配器
3. **扩展**：策略 -> 模板方法 -> 观察者
4. **高级**：责任链 -> 委派 -> 建造者

### 2. 实践方法
- 先运行测试脚本查看输出
- 阅读对应类的源代码和注释
- 修改参数观察不同输出
- 尝试自己实现简化版本

### 3. 面试重点
- **工厂 + 单例**：Spring IoC 容器原理必考
- **代理模式**：AOP 实现原理必考
- **模板方法**：JdbcTemplate 源码分析常考
- **观察者模式**：Spring 事件机制常考
- **策略模式**：事务管理器源码分析

---

## 📝 代码规范说明

### 包结构规范
```
org.doubao.interview.agent.api.dto.designpattern    # DTO
org.doubao.interview.agent.api.service.designpattern # 接口
org.doubao.interview.agent.server.controller        # Controller
org.doubao.interview.agent.server.service.impl      # Service 实现
org.doubao.interview.agent.server.example           # 示例类
```

### 注释规范
- 所有公共类必须有类注释（说明职责、边界、线程安全性）
- 所有公共方法必须有方法注释（输入约束、输出语义、异常场景）
- 关键代码段必须有解释性注释（解释"为什么这样做"）

### 日志规范
- `info`：主流程关键节点
- `warn`：可恢复异常、降级路径
- `error`：不可恢复异常（附带业务主键和上下文）

---

## 🎓 面试满分回答要点

### 1. 工厂模式回答要点
- Spring 通过 BeanFactory 管理 Bean 创建
- 隐藏创建细节，实现创建与使用解耦
- ApplicationContext 是更高级的工厂实现

### 2. 单例模式回答要点
- Bean 默认 singleton 作用域
- 通过 ConcurrentHashMap 实现线程安全
- 使用 DCL（双重检查锁）优化性能

### 3. 代理模式回答要点
- Spring AOP 的核心实现
- JDK 动态代理（有接口）vs CGLIB（无接口）
- 在不修改原代码情况下添加横切逻辑

### 4. 模板方法模式回答要点
- JdbcTemplate 封装固定 JDBC 流程
- 用户只需关注 SQL 和结果映射
- 符合开闭原则，易于扩展

### 5. 观察者模式回答要点
- Spring 事件监听机制
- 实现组件间解耦
- 发布者无需知道有哪些监听器

---

## 🔧 故障排查

### 问题 1：服务启动失败
**原因**：端口 9510 被占用  
**解决**：修改 application.yml 中的 server.port

### 问题 2：curl 命令找不到
**Windows**：需要安装 Git Bash 或使用 PowerShell 的 Invoke-WebRequest  
**Linux**：`sudo apt-get install curl`

### 问题 3：输出乱码
**Windows**：确保控制台编码为 UTF-8（chcp 65001）  
**脚本已配置**：使用 `-s` 参数减少冗余输出

---

## 📖 参考资料

- Spring Framework 官方文档
- 《Spring 源码深度解析》
- 《设计模式之禅》
- 《Head First 设计模式》

---

## ✨ 总结

本实现完全遵循 Spring 框架的设计思想，提供了 12 种常见设计模式的完整可运行示例。每个示例都包含：

✅ 完整的类层次结构  
✅ 详细的中文注释  
✅ 完善的日志记录  
✅ 端到端的测试接口  
✅ 统一的测试脚本  

通过学习这些示例，您可以：
1. 深入理解 Spring 底层原理
2. 掌握设计模式的实际应用场景
3. 提升代码设计和架构能力
4. 轻松应对面试中的源码相关问题

祝您学习顺利！🎉
