package org.doubao.interview.agent.server.controller.designpattern;

import org.doubao.interview.agent.api.dto.designpattern.BeanConfig;
import org.doubao.interview.agent.api.service.designpattern.OrderService;
import org.doubao.interview.agent.api.service.designpattern.PlatformTransactionManager;
import org.doubao.interview.agent.api.service.designpattern.TransactionDefinition;
import org.doubao.interview.agent.api.service.designpattern.TransactionStatus;
import org.doubao.interview.agent.server.example.designpattern.*;
import org.doubao.interview.agent.server.service.designpattern.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Spring 设计模式演示 Controller
 * 
 * 提供 RESTful 接口，演示 Spring 中常见的设计模式
 * 包括：工厂、单例、原型、建造者、代理、装饰器、适配器、策略、
 *      模板方法、观察者、责任链、委派模式
 * 
 * @author interview-agent
 * @since 2026-03-31
 */
@RestController
@RequestMapping("/design-pattern")
public class DesignPatternDemoController {
    
    private static final Logger log = LoggerFactory.getLogger(DesignPatternDemoController.class);
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("message", "Spring 设计模式演示服务运行正常");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    /**
     * 1. 工厂模式 + 单例模式演示
     * 
     * 演示如何通过 BeanFactory 创建和管理单例 Bean
     */
    @GetMapping("/factory-singleton")
    public Map<String, Object> demoFactoryAndSingleton() {
        log.info("========== 开始演示工厂模式和单例模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建 Bean 工厂
            DefaultBeanFactory beanFactory = new DefaultBeanFactory();
            
            // 2. 注册 UserService（单例作用域）
            BeanConfig userServiceConfig = new BeanConfig();
            userServiceConfig.setBeanName("userService");
            userServiceConfig.setBeanClass(UserService.class.getName());
            userServiceConfig.setScope("singleton"); // 单例
            beanFactory.registerBeanDefinition("userService", userServiceConfig);
            
            // 3. 第一次获取 Bean
            UserService userService1 = (UserService) beanFactory.getBean("userService");
            String info1 = userService1.getUserInfo("user001");
            
            // 4. 第二次获取 Bean
            UserService userService2 = (UserService) beanFactory.getBean("userService");
            String info2 = userService2.getUserInfo("user002");
            
            // 5. 验证单例：两个引用指向同一个对象
            boolean isSameInstance = (userService1 == userService2);
            
            result.put("success", true);
            result.put("firstGet", info1);
            result.put("secondGet", info2);
            result.put("isSingleton", isSameInstance);
            result.put("identityHashCode", "first=" + System.identityHashCode(userService1) + ", second=" + System.identityHashCode(userService2));
            result.put("explanation", "两次获取的 Bean 是同一个实例，证明单例模式生效。两个引用的 identityHashCode 完全相同");
            
            log.info("工厂模式和单例模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 2. 原型模式演示
     * 
     * 演示 prototype 作用域的 Bean，每次获取都是新实例
     */
    @GetMapping("/prototype")
    public Map<String, Object> demoPrototype() {
        log.info("========== 开始演示原型模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            DefaultBeanFactory beanFactory = new DefaultBeanFactory();
            
            // 注册 RequestHandler（原型作用域）
            BeanConfig config = new BeanConfig();
            config.setBeanName("requestHandler");
            config.setBeanClass(RequestHandler.class.getName());
            config.setScope("prototype"); // 原型
            config.setConstructorArgs(new Object[]{"req-001", "test-param"});
            beanFactory.registerBeanDefinition("requestHandler", config);
            
            // 第一次获取
            RequestHandler handler1 = (RequestHandler) beanFactory.getBean("requestHandler");
            
            // 第二次获取
            RequestHandler handler2 = (RequestHandler) beanFactory.getBean("requestHandler");
            
            // 验证原型：两个引用指向不同的对象
            boolean isDifferentInstance = (handler1 != handler2);
            
            result.put("success", true);
            result.put("firstInstance", "hashcode=" + handler1.hashCode() + ", identityHashCode=" + System.identityHashCode(handler1));
            result.put("secondInstance", "hashcode=" + handler2.hashCode() + ", identityHashCode=" + System.identityHashCode(handler2));
            result.put("isPrototype", isDifferentInstance);
            result.put("explanation", "两次获取的 Bean 是不同的实例，证明原型模式生效。注意：虽然业务 hashCode 可能相同（因为字段值相同），但 identityHashCode 一定不同");
            
            log.info("原型模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 3. 建造者模式演示
     * 
     * 演示如何使用 Builder 模式构建复杂的 ApplicationContext
     */
    @GetMapping("/builder")
    public Map<String, Object> demoBuilder() {
        log.info("========== 开始演示建造者模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 使用 Builder 模式构建 ApplicationContext
            MockApplicationContext context = ApplicationContextBuilder.newInstance()
                .withApplicationName("DesignPatternDemoApp")
                .withConfigLocation("classpath:application.yml")
                .withBasePackage("org.doubao.interview.agent")
                .withActiveProfile("dev")
                .withLazyInit()
                .build();
            
            // 初始化上下文
            context.init();
            
            result.put("success", true);
            result.put("applicationName", context.getApplicationName());
            result.put("configLocation", context.getConfigLocation());
            result.put("activeProfile", context.getActiveProfile());
            result.put("lazyInit", context.isLazyInit());
            result.put("explanation", "通过 Builder 模式链式调用，优雅地构建复杂对象");
            
            log.info("建造者模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 4. 代理模式演示（JDK 动态代理）
     * 
     * 演示如何为 OrderService 创建代理，添加事务、日志等横切逻辑
     */
    @GetMapping("/proxy")
    public Map<String, Object> demoProxy() {
        log.info("========== 开始演示代理模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建目标对象（真实业务逻辑）
            OrderService target = new OrderServiceImpl();
            
            // 2. 创建代理对象
            OrderService proxy = JdkProxyFactory.createProxy(target);
            
            // 3. 通过代理调用方法（会触发拦截逻辑）
            String createResult = proxy.createOrder("ORDER-20260331-001", 199.00);
            String getResult = proxy.getOrder("ORDER-20260331-001");
            
            result.put("success", true);
            result.put("createOrderResult", createResult);
            result.put("getOrderResult", getResult);
            result.put("explanation", "通过代理模式，在不修改原代码的情况下添加了事务、日志、权限等横切逻辑");
            
            log.info("代理模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 5. 装饰器模式演示
     * 
     * 演示如何为 DataSource 添加事务管理功能
     */
    @GetMapping("/decorator")
    public Map<String, Object> demoDecorator() {
        log.info("========== 开始演示装饰器模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建基础数据源（被装饰对象）
            SimpleDataSource simpleDataSource = new SimpleDataSource(
                "SimpleDS", 
                "jdbc:mysql://localhost:3306/test"
            );
            
            // 2. 用事务装饰器包装（添加事务功能）
            TransactionAwareDataSource transactionalDS = 
                new TransactionAwareDataSource(simpleDataSource);
            
            // 3. 获取事务感知的连接
            Object connection = transactionalDS.getConnection();
            
            // 4. 提交事务
            transactionalDS.commit();
            
            result.put("success", true);
            result.put("dataSourceName", transactionalDS.getName());
            result.put("connection", connection.getClass().getSimpleName());
            result.put("transactionCommitted", true);
            result.put("explanation", "通过装饰器模式，在基础数据源上动态添加了事务管理功能");
            
            log.info("装饰器模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 6. 策略模式演示（事务管理器）
     * 
     * 演示如何使用不同的事务管理策略
     */
    @GetMapping("/strategy")
    public Map<String, Object> demoStrategy() {
        log.info("========== 开始演示策略模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建事务定义
            TransactionDefinition definition = new TransactionDefinition() {
                @Override
                public int getIsolationLevel() {
                    return ISOLATION_READ_COMMITTED;
                }
                @Override
                public int getPropagationBehavior() {
                    return PROPAGATION_REQUIRED;
                }
                @Override
                public int getTimeout() {
                    return 30;
                }
            };
            
            // 2. 创建事务管理器（策略实现）
            // 注意：这里使用简化的 mock 数据源
            javax.sql.DataSource mockDataSource = new MockDataSource();
            PlatformTransactionManager txManager =
                new DataSourceTransactionManager(mockDataSource);
            
            // 3. 获取事务
            TransactionStatus status = txManager.getTransaction(definition);
            
            // 4. 提交事务
            txManager.commit(status);
            
            result.put("success", true);
            result.put("txManager", txManager.getClass().getSimpleName());
            result.put("isolationLevel", "READ_COMMITTED");
            result.put("propagationBehavior", "REQUIRED");
            result.put("transactionCommitted", true);
            result.put("explanation", "通过策略模式，可以灵活切换不同的事务管理器（如 JDBC/JPA/Hibernate）");
            
            log.info("策略模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 7. 模板方法模式演示（JdbcTemplate）
     * 
     * 演示 JdbcTemplate 如何封装 JDBC 固定流程
     */
    @GetMapping("/template-method")
    public Map<String, Object> demoTemplateMethod() {
        log.info("========== 开始演示模板方法模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建数据源
            javax.sql.DataSource dataSource = new MockDataSource();
            
            // 2. 创建 JdbcTemplate
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // 3. 执行查询（固定流程由模板完成，结果映射由回调完成）
            String sql = "SELECT * FROM users WHERE id = ?";
            Object user = jdbcTemplate.queryForObject(sql, new RowMapper<Object>() {
                @Override
                public Object mapRow(java.sql.ResultSet rs, int rowNum) throws Exception {
                    // 用户自定义的结果集映射逻辑
                    log.info("[回调] 映射第 {} 行数据", rowNum);
                    return "User{id=" + rs.getInt(1) + ", name='张三'}";
                }
            }, 1L);
            
            result.put("success", true);
            result.put("queryResult", user);
            result.put("explanation", "JdbcTemplate 固定了 JDBC 操作流程，用户只需关注 SQL 和结果映射");
            
            log.info("模板方法模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 8. 观察者模式演示（事件驱动）
     * 
     * 演示 Spring 的事件监听机制
     */
    @GetMapping("/observer")
    public Map<String, Object> demoObserver() {
        log.info("========== 开始演示观察者模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建事件发布器
            SimpleApplicationEventMulticaster publisher = 
                new SimpleApplicationEventMulticaster();
            
            // 2. 注册监听器
            publisher.addListener(new CacheWarmupListener());
            
            // 3. 发布事件
            ContextRefreshedEvent event = new ContextRefreshedEvent(this);
            publisher.publishEvent(event);
            
            result.put("success", true);
            result.put("eventType", event.getClass().getSimpleName());
            result.put("eventTimestamp", event.getTimestamp());
            result.put("listenerExecuted", true);
            result.put("explanation", "通过观察者模式，实现组件间的解耦：发布者无需知道有哪些监听器");
            
            log.info("观察者模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 9. 责任链模式演示（拦截器链）
     * 
     * 演示 Spring MVC 拦截器链
     */
    @GetMapping("/chain-of-responsibility")
    public Map<String, Object> demoChainOfResponsibility() {
        log.info("========== 开始演示责任链模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建拦截器链
            InterceptorChain chain = new InterceptorChain();
            
            // 2. 添加拦截器（形成责任链）
            chain.addInterceptor(new LoggingInterceptor());
            chain.addInterceptor(new AuthInterceptor());
            
            // 3. 执行拦截器链
            String request = "/api/users";
            boolean allPassed = chain.applyPreHandle(request, null, null);
            
            if (allPassed) {
                log.info("所有拦截器通过，继续处理业务逻辑");
                chain.applyPostHandle(request, null, null, null);
            }
            
            chain.applyAfterCompletion(request, null, null, null);
            
            result.put("success", true);
            result.put("interceptorCount", chain.getInterceptorCount());
            result.put("allPassed", allPassed);
            result.put("explanation", "多个拦截器形成责任链，每个拦截器决定是否继续或中断请求");
            
            log.info("责任链模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 10. 委派模式演示（DispatcherServlet）
     * 
     * 演示 Spring MVC 前端控制器如何委派任务
     */
    @GetMapping("/delegation")
    public Map<String, Object> demoDelegation() {
        log.info("========== 开始演示委派模式 ==========");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 创建前端控制器（统一入口）
            DispatcherServlet dispatcherServlet = new DispatcherServlet();
            
            // 2. 处理请求（委派给各个组件）
            String response = dispatcherServlet.handleRequest("/api/users");
            
            result.put("success", true);
            result.put("request", "/api/users");
            result.put("response", response);
            result.put("components", new String[]{
                "HandlerMapping（查找处理器）",
                "HandlerAdapter（执行处理器）",
                "ViewResolver（解析视图）",
                "InterceptorChain（拦截器链）"
            });
            result.put("explanation", "DispatcherServlet 作为统一入口，将任务委派给专业组件处理");
            
            log.info("委派模式演示完成");
            
        } catch (Exception e) {
            log.error("演示失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Mock 数据源（用于演示）
     */
    private static class MockDataSource implements javax.sql.DataSource {
        @Override
        public java.sql.Connection getConnection() {
            return new MockConnection();
        }
        
        @Override
        public java.sql.Connection getConnection(String username, String password) {
            return new MockConnection();
        }
        
        // 其他方法简化实现...
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        @Override public java.io.PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(java.io.PrintWriter out) {}
        @Override public void setLoginTimeout(int seconds) {}
        @Override public int getLoginTimeout() { return 0; }
        @Override public java.util.logging.Logger getParentLogger() { return null; }
    }
    
    private static class MockConnection implements java.sql.Connection {
        @Override public java.sql.Statement createStatement() { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql) { return null; }
        @Override public void close() {}
        @Override public boolean isClosed() { return false; }

        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            return null;
        }

        // 其他方法省略...
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        @Override public java.sql.CallableStatement prepareCall(String sql) { return null; }
        @Override public String nativeSQL(String sql) { return null; }
        @Override public void setAutoCommit(boolean autoCommit) {}
        @Override public boolean getAutoCommit() { return false; }
        @Override public void commit() {}
        @Override public void rollback() {}
        @Override public boolean isReadOnly() { return false; }
        @Override public void setReadOnly(boolean readOnly) {}
        @Override public void setCatalog(String catalog) {}
        @Override public String getCatalog() { return null; }
        @Override public void setTransactionIsolation(int level) {}
        @Override public int getTransactionIsolation() { return 0; }
        @Override public java.sql.SQLWarning getWarnings() { return null; }
        @Override public void clearWarnings() {}
        @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) { return null; }
        @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) { return null; }
        @Override public Map<String, Class<?>> getTypeMap() { return null; }
        @Override public void setTypeMap(Map<String, Class<?>> map) {}
        @Override public void setHoldability(int holdability) {}
        @Override public int getHoldability() { return 0; }
        @Override public java.sql.Savepoint setSavepoint() { return null; }
        @Override public java.sql.Savepoint setSavepoint(String name) { return null; }
        @Override public void rollback(java.sql.Savepoint savepoint) {}
        @Override public void releaseSavepoint(java.sql.Savepoint savepoint) {}
        @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { return null; }
        @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) { return null; }

        @Override
        public Clob createClob() throws SQLException {
            return null;
        }

        @Override
        public Blob createBlob() throws SQLException {
            return null;
        }

        @Override
        public NClob createNClob() throws SQLException {
            return null;
        }

        @Override
        public SQLXML createSQLXML() throws SQLException {
            return null;
        }

        @Override public boolean isValid(int timeout) { return true; }

        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {

        }

        @Override
        public void setClientInfo(Properties properties) throws SQLClientInfoException {

        }

        @Override
        public String getClientInfo(String name) throws SQLException {
            return "";
        }

        @Override
        public Properties getClientInfo() throws SQLException {
            return null;
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            return null;
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            return null;
        }

        @Override
        public void setSchema(String schema) throws SQLException {

        }

        @Override
        public String getSchema() throws SQLException {
            return "";
        }

        @Override
        public void abort(Executor executor) throws SQLException {

        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return 0;
        }
    }
}
