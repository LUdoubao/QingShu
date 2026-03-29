package org.doubao.interview.agent.server.service.impl.question005;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question005.ContainerDemoRequest;
import org.doubao.interview.agent.api.dto.question005.ContainerDemoResponse;
import org.doubao.interview.agent.api.service.question005.SpringContainerDemoService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring 容器演示服务实现类
 * 
 * 对应面试知识点：问题 005 - BeanFactory 和 ApplicationContext 区别
 * 
 * 【类注释】
 * 职责：实现 Spring 容器功能演示，对比 BeanFactory 和 ApplicationContext 的差异
 * 边界：仅用于演示，不包含真实业务系统的复杂处理
 * 线程安全：通过 Spring 容器的线程安全保证
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Service
public class SpringContainerDemoServiceImpl implements SpringContainerDemoService {

    /**
     * 演示 Spring 容器功能
     * 
     * 【关键逻辑说明】
     * 1. BeanFactory 是 Spring 容器的基础接口，提供最核心的 Bean 管理
     * 2. ApplicationContext 是 BeanFactory 的子接口，提供更完整的企业级功能
     * 3. 主要区别体现在：初始化时机、生命周期管理、国际化、事件机制、AOP 支持等
     */
    @Override
    public ContainerDemoResponse demoContainer(ContainerDemoRequest request) {
        log.info("开始演示 Spring 容器，containerType={}", request.getContainerType());
        
        // 构建容器功能对比
        ContainerDemoResponse.ContainerComparison comparison = buildContainerComparison();
        
        // 获取 Bean 定义列表（模拟）
        List<ContainerDemoResponse.BeanInfo> beanList = getBeanDefinitions();
        
        // 额外功能演示（仅 ApplicationContext 支持）
        Map<String, Object> additionalFeatures = new HashMap<>();
        if ("ApplicationContext".equals(request.getContainerType())) {
            additionalFeatures.put("messageSource", "支持国际化消息");
            additionalFeatures.put("applicationEvent", "支持事件发布/订阅");
            additionalFeatures.put("resourceLoader", "支持资源访问抽象");
            additionalFeatures.put("parentContext", "支持层次化容器结构");
        } else {
            additionalFeatures.put("note", "BeanFactory 不支持这些高级功能");
        }
        
        ContainerDemoResponse response = ContainerDemoResponse.builder()
                .success(true)
                .containerType(request.getContainerType())
                .comparison(comparison)
                .beanList(beanList)
                .additionalFeatures(additionalFeatures)
                .build();
        
        log.info("容器演示完成，containerType={}, beanCount={}", 
                request.getContainerType(), beanList.size());
        
        return response;
    }

    /**
     * 构建容器功能对比
     */
    private ContainerDemoResponse.ContainerComparison buildContainerComparison() {
        return ContainerDemoResponse.ContainerComparison.builder()
                .initializationTiming("BeanFactory: 延迟加载 | ApplicationContext: 启动预加载")
                .lifecycleManagement("BeanFactory: 不完整 | ApplicationContext: 完整自动管理")
                .i18nSupport(true) // ApplicationContext 支持
                .eventMechanism(true) // ApplicationContext 支持
                .aopSupport(true) // ApplicationContext 更好支持
                .resourceAccess(true) // ApplicationContext 支持 ResourceLoader
                .parentContainerSupport(true) // ApplicationContext 支持层次化
                .recommendedUsage("企业开发推荐使用 ApplicationContext，几乎不使用 BeanFactory")
                .build();
    }

    /**
     * 获取 Bean 定义列表（模拟数据）
     */
    private List<ContainerDemoResponse.BeanInfo> getBeanDefinitions() {
        // 模拟一些 Bean 定义
        return Arrays.asList(
                ContainerDemoResponse.BeanInfo.builder()
                        .beanName("userService")
                        .beanType("org.example.UserService")
                        .scope("singleton")
                        .singleton(true)
                        .initMethod("init")
                        .destroyMethod("destroy")
                        .build(),
                ContainerDemoResponse.BeanInfo.builder()
                        .beanName("orderService")
                        .beanType("org.example.OrderService")
                        .scope("singleton")
                        .singleton(true)
                        .initMethod("init")
                        .destroyMethod("destroy")
                        .build(),
                ContainerDemoResponse.BeanInfo.builder()
                        .beanName("dataSource")
                        .beanType("com.zaxxer.hikari.HikariDataSource")
                        .scope("singleton")
                        .singleton(true)
                        .initMethod("init")
                        .destroyMethod("close")
                        .build()
        );
    }

    /**
     * 获取两种容器的详细对比说明
     */
    @Override
    public String getDetailedComparison() {
        StringBuilder sb = new StringBuilder();
        sb.append("# BeanFactory 和 ApplicationContext 的区别详解\n\n");
        
        sb.append("## 一、核心关系\n\n");
        sb.append("```\n");
        sb.append("BeanFactory (基础接口)\n");
        sb.append("    ↑\n");
        sb.append("    |\n");
        sb.append("ApplicationContext (高级接口，继承自 BeanFactory)\n");
        sb.append("```\n\n");
        
        sb.append("**本质**：ApplicationContext 是 BeanFactory 的超集，提供了所有 BeanFactory 的功能，并在此基础上扩展了更多企业级特性。\n\n");
        
        sb.append("## 二、主要区别对比表\n\n");
        sb.append("| 特性 | BeanFactory | ApplicationContext |\n");
        sb.append("|------|-------------|--------------------|\n");
        sb.append("| **Bean 实例化时机** | 延迟加载（第一次使用时创建） | 启动时预加载所有 singleton Bean |\n");
        sb.append("| **生命周期管理** | 不完整（需手动调用 close） | 完整（自动管理初始化和销毁） |\n");
        sb.append("| **国际化支持** | ❌ 不支持 | ✅ 支持 MessageSource |\n");
        sb.append("| **事件机制** | ❌ 不支持 | ✅ 支持 ApplicationEvent |\n");
        sb.append("| **AOP 集成** | ⚠️ 基础支持 | ✅ 完整支持 |\n");
        sb.append("| **资源访问** | ❌ 不支持 | ✅ 支持 ResourceLoader |\n");
        sb.append("| **父容器支持** | ❌ 不支持 | ✅ 支持层次化容器 |\n");
        sb.append("| **注解支持** | ⚠️ 有限支持 | ✅ 完整支持 (@Autowired 等) |\n");
        sb.append("| **使用场景** | 资源受限环境 | 企业级应用（推荐） |\n\n");
        
        sb.append("## 三、详细功能说明\n\n");
        
        sb.append("### 3.1 Bean 实例化时机\n\n");
        sb.append("**BeanFactory**：\n");
        sb.append("```java\n");
        sb.append("BeanFactory factory = new XmlBeanFactory(\"applicationContext.xml\");\n");
        sb.append("// 此时 Bean 并未创建\n");
        sb.append("MyBean bean = factory.getBean(\"myBean\"); \n");
        sb.append("// 第一次调用 getBean 时才创建 Bean\n");
        sb.append("```\n\n");
        
        sb.append("**ApplicationContext**：\n");
        sb.append("```java\n");
        sb.append("ApplicationContext context = new ClassPathXmlApplicationContext(\"applicationContext.xml\");\n");
        sb.append("// 容器启动时就创建所有 singleton Bean\n");
        sb.append("MyBean bean = context.getBean(\"myBean\"); \n");
        sb.append("// 直接从缓存中获取，无需创建\n");
        sb.append("```\n\n");
        
        sb.append("**优缺点对比**：\n");
        sb.append("- BeanFactory 延迟加载：节省启动内存，但首次访问慢\n");
        sb.append("- ApplicationContext 预加载：启动慢，但运行时响应快\n\n");
        
        sb.append("### 3.2 Bean 生命周期管理\n\n");
        sb.append("**BeanFactory**：\n");
        sb.append("- 需要手动调用 `close()` 或 `destroy()` 方法\n");
        sb.append("- 否则销毁方法不会被执行\n");
        sb.append("- 示例：`((BeanFactory)context).close()`\n\n");
        
        sb.append("**ApplicationContext**：\n");
        sb.append("- 自动管理 Bean 的生命周期\n");
        sb.append("- JVM 关闭时自动调用销毁方法\n");
        sb.append("- 支持 @PostConstruct 和 @PreDestroy 注解\n\n");
        
        sb.append("### 3.3 国际化支持（MessageSource）\n\n");
        sb.append("**ApplicationContext** 独有功能：\n");
        sb.append("```java\n");
        sb.append("// 定义消息配置文件 messages.properties\n");
        sb.append("welcome.message=欢迎 {0}!\n");
        sb.append("\n");
        sb.append("// 代码中使用\n");
        sb.append("String message = context.getMessage(\n");
        sb.append("    \"welcome.message\", \n");
        sb.append("    new Object[]{\"张三\"}, \n");
        sb.append("    Locale.CHINA\n");
        sb.append(");\n");
        sb.append("// 输出：欢迎 张三！\n");
        sb.append("```\n\n");
        
        sb.append("### 3.4 事件机制（ApplicationEvent）\n\n");
        sb.append("**ApplicationContext** 独有功能：\n");
        sb.append("```java\n");
        sb.append("// 定义事件\n");
        sb.append("public class MyEvent extends ApplicationEvent {\n");
        sb.append("    public MyEvent(Object source) {\n");
        sb.append("        super(source);\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("\n");
        sb.append("// 发布事件\n");
        sb.append("context.publishEvent(new MyEvent(this));\n");
        sb.append("\n");
        sb.append("// 监听事件（使用@EventListener）\n");
        sb.append("@EventListener\n");
        sb.append("public void handleMyEvent(MyEvent event) {\n");
        sb.append("    // 处理事件\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 3.5 资源访问抽象（ResourceLoader）\n\n");
        sb.append("**ApplicationContext** 实现 ResourceLoader 接口：\n");
        sb.append("```java\n");
        sb.append("// 加载 classpath 下的资源\n");
        sb.append("Resource resource = context.getResource(\"classpath:config.properties\");\n");
        sb.append("\n");
        sb.append("// 加载文件系统资源\n");
        sb.append("Resource fileResource = context.getResource(\"file:/path/to/file.txt\");\n");
        sb.append("\n");
        sb.append("// 加载 URL 资源\n");
        sb.append("Resource urlResource = context.getResource(\"http://example.com/data.json\");\n");
        sb.append("```\n\n");
        
        sb.append("## 四、实际使用建议\n\n");
        
        sb.append("### 4.1 企业开发标准\n\n");
        sb.append("**永远优先使用 ApplicationContext**，原因：\n");
        sb.append("1. 提供完整的企业级功能\n");
        sb.append("2. 更好的 Spring 生态集成\n");
        sb.append("3. 自动管理 Bean 生命周期\n");
        sb.append("4. 支持现代 Spring 特性（注解、AOP 等）\n\n");
        
        sb.append("### 4.2 BeanFactory 的使用场景\n\n");
        sb.append("仅在以下特殊场景考虑使用 BeanFactory：\n");
        sb.append("1. **资源极度受限**：如嵌入式设备、移动端\n");
        sb.append("2. **对启动内存要求苛刻**：需要极致优化启动性能\n");
        sb.append("3. **简单应用**：不需要国际化、事件等高级功能\n\n");
        
        sb.append("### 4.3 常见实现类\n\n");
        sb.append("**BeanFactory 实现**：\n");
        sb.append("- `XmlBeanFactory`（已废弃）\n");
        sb.append("- `DefaultListableBeanFactory`\n\n");
        
        sb.append("**ApplicationContext 实现**：\n");
        sb.append("- `ClassPathXmlApplicationContext`：从 classpath 加载配置\n");
        sb.append("- `FileSystemXmlApplicationContext`：从文件系统加载配置\n");
        sb.append("- `AnnotationConfigApplicationContext`：基于注解配置\n");
        sb.append("- `WebApplicationContext`：Web 应用专用\n\n");
        
        sb.append("## 五、面试高频考点\n\n");
        
        sb.append("### Q1: BeanFactory 和 ApplicationContext 的主要区别？\n\n");
        sb.append("**答**：\n");
        sb.append("1. **层级关系**：ApplicationContext 继承自 BeanFactory，是其超集\n");
        sb.append("2. **实例化时机**：BeanFactory 延迟加载，ApplicationContext 启动预加载\n");
        sb.append("3. **功能完整性**：ApplicationContext 提供国际化、事件机制、资源访问等企业级功能\n");
        sb.append("4. **生命周期管理**：ApplicationContext 自动管理，BeanFactory 需手动关闭\n");
        sb.append("5. **使用场景**：企业开发都用 ApplicationContext\n\n");
        
        sb.append("### Q2: 为什么企业开发都使用 ApplicationContext？\n\n");
        sb.append("**答**：\n");
        sb.append("1. 提供完整的企业级功能（国际化、事件、AOP 等）\n");
        sb.append("2. 更好的 Spring 生态集成\n");
        sb.append("3. 自动管理 Bean 生命周期，减少出错可能\n");
        sb.append("4. 支持现代 Spring 特性（注解、扫描等）\n");
        sb.append("5. 资源开销的差异在现代硬件环境下可忽略\n\n");
        
        sb.append("### Q3: ApplicationContext 的预加载有什么优缺点？\n\n");
        sb.append("**答**：\n");
        sb.append("**优点**：\n");
        sb.append("- 启动时发现配置问题（快速失败）\n");
        sb.append("- 运行时响应更快（无需创建 Bean）\n");
        sb.append("- 便于提前发现问题\n\n");
        sb.append("**缺点**：\n");
        sb.append("- 启动时间较长\n");
        sb.append("- 启动占用内存较多\n\n");
        
        sb.append("### Q4: 如何实现延迟加载？\n");
        sb.append("**答**：在 ApplicationContext 中可通过以下方式：\n");
        sb.append("1. 在 Bean 上添加 `@Lazy` 注解\n");
        sb.append("2. 在 XML 配置中设置 `lazy-init=\"true\"`\n");
        sb.append("3. 全局配置 `default-lazy-init=\"true\"`\n\n");
        
        sb.append("## 六、总结\n\n");
        
        sb.append("**一句话记忆**：\n");
        sb.append("- BeanFactory 是基础版，ApplicationContext 是完整版\n");
        sb.append("- ApplicationContext = BeanFactory + 企业级功能\n");
        sb.append("- 企业开发只用 ApplicationContext\n\n");
        
        sb.append("**核心要点**：\n");
        sb.append("1. ApplicationContext 继承自 BeanFactory，提供更完整的功能\n");
        sb.append("2. 主要区别：初始化时机、生命周期管理、国际化、事件机制、资源访问\n");
        sb.append("3. 企业开发标准是使用 ApplicationContext\n");
        sb.append("4. BeanFactory 仅用于资源极度受限的特殊场景\n\n");
        
        return sb.toString();
    }
}
