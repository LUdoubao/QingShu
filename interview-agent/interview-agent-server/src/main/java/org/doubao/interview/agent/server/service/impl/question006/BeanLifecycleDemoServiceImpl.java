package org.doubao.interview.agent.server.service.impl.question006;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question006.BeanLifecycleRequest;
import org.doubao.interview.agent.api.dto.question006.BeanLifecycleResponse;
import org.doubao.interview.agent.api.service.question006.BeanLifecycleDemoService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Bean 生命周期演示服务实现类
 * 
 * 对应面试知识点：问题 006 - Spring Bean 的生命周期
 * 
 * 【类注释】
 * 职责：实现 Bean 生命周期的完整演示流程
 * 边界：仅用于演示，不包含真实业务系统的复杂处理
 * 线程安全：通过 Spring 容器的线程安全保证
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Service
public class BeanLifecycleDemoServiceImpl implements BeanLifecycleDemoService {

    /**
     * 演示 Bean 生命周期
     * 
     * 【关键逻辑说明】
     * Spring Bean 的完整生命周期流程：
     * 1. 实例化（Instantiation）- 创建 Bean 实例
     * 2. 属性注入（Populate）- 填充属性值
     * 3. Aware 接口回调 - BeanNameAware、BeanFactoryAware 等
     * 4. BeanPostProcessor 前置处理 - postProcessBeforeInitialization
     * 5. 初始化（Initialization）- @PostConstruct、init-method、InitializingBean
     * 6. BeanPostProcessor 后置处理 - postProcessAfterInitialization
     * 7. Bean 可用 - 执行业务逻辑
     * 8. 销毁前回调 - @PreDestroy、DisposableBean、destroy-method
     * 9. 销毁 - Bean 从容器中移除
     */
    @Override
    public BeanLifecycleResponse demoLifecycle(BeanLifecycleRequest request) {
        log.info("开始演示 Bean 生命周期，beanName={}", request.getBeanName());
        
        String beanName = request.getBeanName() != null ? request.getBeanName() : "demoBean";
        
        // 构建完整的生命周期阶段列表
        List<BeanLifecycleResponse.LifecyclePhase> phases = buildLifecyclePhases(request);
        
        // 构建回调方法信息
        BeanLifecycleResponse.CallbackInfo callbackInfo = buildCallbackInfo();
        
        BeanLifecycleResponse response = BeanLifecycleResponse.builder()
                .success(true)
                .beanName(beanName)
                .lifecyclePhases(phases)
                .callbackInfo(callbackInfo)
                .build();
        
        log.info("Bean 生命周期演示完成，beanName={}, phaseCount={}", 
                beanName, phases.size());
        
        return response;
    }

    /**
     * 构建生命周期阶段列表
     */
    private List<BeanLifecycleResponse.LifecyclePhase> buildLifecyclePhases(BeanLifecycleRequest request) {
        return Arrays.asList(
                // 阶段 1: 实例化
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(1)
                        .phaseName("实例化 (Instantiation)")
                        .description("Spring 容器创建 Bean 的实例对象，通过构造函数或工厂方法")
                        .annotationOrInterface("构造函数 / 静态工厂方法")
                        .executed(true)
                        .executionTime(1L)
                        .build(),
                
                // 阶段 2: 属性注入
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(2)
                        .phaseName("属性注入 (Populate Properties)")
                        .description("为 Bean 的属性赋值，包括依赖注入（@Autowired）和普通属性值")
                        .annotationOrInterface("@Autowired / @Value")
                        .executed(true)
                        .executionTime(2L)
                        .build(),
                
                // 阶段 3: Aware 接口回调
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(3)
                        .phaseName("Aware 接口回调")
                        .description("调用各种 Aware 接口的方法，让 Bean 感知容器相关信息")
                        .annotationOrInterface("BeanNameAware, BeanFactoryAware, ApplicationContextAware")
                        .executed(true)
                        .executionTime(1L)
                        .build(),
                
                // 阶段 4: BeanPostProcessor 前置处理
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(4)
                        .phaseName("BeanPostProcessor 前置处理")
                        .description("执行所有 BeanPostProcessor 的 postProcessBeforeInitialization 方法")
                        .annotationOrInterface("BeanPostProcessor.postProcessBeforeInitialization")
                        .executed(request.getIncludePostProcessor())
                        .executionTime(1L)
                        .build(),
                
                // 阶段 5: 初始化 - @PostConstruct
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(5)
                        .phaseName("初始化 - @PostConstruct")
                        .description("执行标注了@PostConstruct 注解的方法（JSR-250 标准）")
                        .annotationOrInterface("@PostConstruct")
                        .executed(true)
                        .executionTime(1L)
                        .build(),
                
                // 阶段 6: 初始化 - InitializingBean
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(6)
                        .phaseName("初始化 - InitializingBean")
                        .description("执行 InitializingBean 接口的 afterPropertiesSet 方法")
                        .annotationOrInterface("InitializingBean.afterPropertiesSet")
                        .executed(true)
                        .executionTime(1L)
                        .build(),
                
                // 阶段 7: 初始化 - init-method
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(7)
                        .phaseName("初始化 - init-method")
                        .description("执行自定义的初始化方法（XML 配置或注解指定）")
                        .annotationOrInterface("init-method / @Bean(initMethod)")
                        .executed(true)
                        .executionTime(1L)
                        .build(),
                
                // 阶段 8: BeanPostProcessor 后置处理
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(8)
                        .phaseName("BeanPostProcessor 后置处理")
                        .description("执行所有 BeanPostProcessor 的 postProcessAfterInitialization 方法（AOP 代理在此阶段创建）")
                        .annotationOrInterface("BeanPostProcessor.postProcessAfterInitialization")
                        .executed(request.getIncludePostProcessor())
                        .executionTime(2L)
                        .build(),
                
                // 阶段 9: Bean 就绪可用
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(9)
                        .phaseName("Bean 就绪可用 (Ready to Use)")
                        .description("Bean 已经完全初始化，可以执行业务逻辑")
                        .annotationOrInterface("-")
                        .executed(true)
                        .executionTime(0L)
                        .build(),
                
                // 阶段 10: 销毁前回调
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(10)
                        .phaseName("销毁前回调 (Destruction Callback)")
                        .description("容器关闭时，执行销毁前的回调方法")
                        .annotationOrInterface("@PreDestroy / DisposableBean / destroy-method")
                        .executed(request.getFullLifecycle())
                        .executionTime(1L)
                        .build(),
                
                // 阶段 11: Bean 销毁
                BeanLifecycleResponse.LifecyclePhase.builder()
                        .phaseOrder(11)
                        .phaseName("Bean 销毁 (Destroyed)")
                        .description("Bean 从容器中移除，释放资源")
                        .annotationOrInterface("-")
                        .executed(request.getFullLifecycle())
                        .executionTime(1L)
                        .build()
        );
    }

    /**
     * 构建回调方法信息
     */
    private BeanLifecycleResponse.CallbackInfo buildCallbackInfo() {
        return BeanLifecycleResponse.CallbackInfo.builder()
                .initMethod("@PostConstruct -> InitializingBean.afterPropertiesSet -> init-method")
                .destroyMethod("@PreDestroy -> DisposableBean.destroy -> destroy-method")
                .postProcessorBefore("BeanPostProcessor.postProcessBeforeInitialization（可修改 Bean 实例）")
                .postProcessorAfter("BeanPostProcessor.postProcessAfterInitialization（通常返回代理对象）")
                .awareCallbacks(Arrays.asList(
                        "BeanNameAware.setBeanName(String beanName)",
                        "BeanFactoryAware.setBeanFactory(BeanFactory factory)",
                        "ApplicationContextAware.setApplicationContext(ApplicationContext context)"
                ))
                .build();
    }

    /**
     * 获取 Bean 生命周期的详细说明
     */
    @Override
    public String getDetailedExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Spring Bean 的生命周期详解\n\n");
        
        sb.append("## 一、完整生命周期流程图\n\n");
        sb.append("```\n");
        sb.append("实例化 → 属性注入 → Aware 回调 → BPP 前置 → 初始化 → BPP 后置 → 可用 → 销毁回调 → 销毁\n");
        sb.append("```\n\n");
        
        sb.append("## 二、详细阶段说明\n\n");
        
        sb.append("### 2.1 实例化（Instantiation）\n\n");
        sb.append("**作用**：创建 Bean 的实例对象\n\n");
        sb.append("**实现方式**：\n");
        sb.append("- 构造函数实例化（最常用）\n");
        sb.append("- 静态工厂方法\n");
        sb.append("- 实例工厂方法\n\n");
        sb.append("```java\n");
        sb.append("// 构造函数方式\n");
        sb.append("@Component\n");
        sb.append("public class UserService {\n");
        sb.append("    public UserService() {\n");
        sb.append("        System.out.println(\"1. 实例化 - 构造函数执行\");\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 2.2 属性注入（Populate Properties）\n\n");
        sb.append("**作用**：为 Bean 的属性赋值，包括依赖注入\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class UserService {\n");
        sb.append("    @Autowired\n");
        sb.append("    private UserRepository userRepository;\n");
        sb.append("    \n");
        sb.append("    @Value(\"${app.name}\")\n");
        sb.append("    private String appName;\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 2.3 Aware 接口回调\n\n");
        sb.append("**作用**：让 Bean 感知容器相关信息\n\n");
        sb.append("**常见 Aware 接口**：\n");
        sb.append("- `BeanNameAware`：获取 Bean 的名称\n");
        sb.append("- `BeanFactoryAware`：获取 BeanFactory\n");
        sb.append("- `ApplicationContextAware`：获取 ApplicationContext\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class MyBean implements BeanNameAware, ApplicationContextAware {\n");
        sb.append("    private String beanName;\n");
        sb.append("    private ApplicationContext context;\n");
        sb.append("    \n");
        sb.append("    @Override\n");
        sb.append("    public void setBeanName(String name) {\n");
        sb.append("        this.beanName = name;\n");
        sb.append("        System.out.println(\"3. Aware 回调 - BeanName: \" + name);\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    @Override\n");
        sb.append("    public void setApplicationContext(ApplicationContext ctx) {\n");
        sb.append("        this.context = ctx;\n");
        sb.append("        System.out.println(\"3. Aware 回调 - ApplicationContext\");\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 2.4 BeanPostProcessor 前置处理\n\n");
        sb.append("**作用**：在初始化之前对 Bean 进行增强处理\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class MyBeanPostProcessor implements BeanPostProcessor {\n");
        sb.append("    @Override\n");
        sb.append("    public Object postProcessBeforeInitialization(Object bean, String beanName) {\n");
        sb.append("        System.out.println(\"4. BPP 前置处理 - \" + beanName);\n");
        sb.append("        // 可以在这里修改 Bean 的属性\n");
        sb.append("        return bean;\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 2.5 初始化阶段（三种方式）\n\n");
        sb.append("**执行顺序**：`@PostConstruct` → `InitializingBean` → `init-method`\n\n");
        
        sb.append("#### 方式 1：@PostConstruct 注解（JSR-250 标准）\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class UserService {\n");
        sb.append("    @PostConstruct\n");
        sb.append("    public void init() {\n");
        sb.append("        System.out.println(\"5. @PostConstruct 执行\");\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("#### 方式 2：InitializingBean 接口\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class UserService implements InitializingBean {\n");
        sb.append("    @Override\n");
        sb.append("    public void afterPropertiesSet() {\n");
        sb.append("        System.out.println(\"6. InitializingBean.afterPropertiesSet 执行\");\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("#### 方式 3：自定义 init-method\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class UserService {\n");
        sb.append("    public void customInit() {\n");
        sb.append("        System.out.println(\"7. 自定义 init-method 执行\");\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("\n");
        sb.append("// XML 配置\n");
        sb.append("<bean id=\"userService\" class=\"...UserService\" init-method=\"customInit\"/>\n");
        sb.append("\n");
        sb.append("// 或使用@Bean 注解\n");
        sb.append("@Bean(initMethod = \"customInit\")\n");
        sb.append("public UserService userService() {\n");
        sb.append("    return new UserService();\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 2.6 BeanPostProcessor 后置处理\n\n");
        sb.append("**作用**：在初始化之后对 Bean 进行增强处理（**AOP 代理在此阶段创建**）\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class MyBeanPostProcessor implements BeanPostProcessor {\n");
        sb.append("    @Override\n");
        sb.append("    public Object postProcessAfterInitialization(Object bean, String beanName) {\n");
        sb.append("        System.out.println(\"8. BPP 后置处理 - \" + beanName);\n");
        sb.append("        // AOP 代理通常在这里创建\n");
        sb.append("        if (bean instanceof UserService) {\n");
        sb.append("            return createProxy(bean);\n");
        sb.append("        }\n");
        sb.append("        return bean;\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 2.7 销毁阶段（三种方式）\n\n");
        sb.append("**执行顺序**：`@PreDestroy` → `DisposableBean` → `destroy-method`\n\n");
        
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class UserService implements DisposableBean {\n");
        sb.append("    \n");
        sb.append("    @PreDestroy\n");
        sb.append("    public void preDestroy() {\n");
        sb.append("        System.out.println(\"10. @PreDestroy 执行\");\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    @Override\n");
        sb.append("    public void destroy() throws Exception {\n");
        sb.append("        System.out.println(\"10. DisposableBean.destroy 执行\");\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    public void customDestroy() {\n");
        sb.append("        System.out.println(\"10. 自定义 destroy-method 执行\");\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("## 三、面试高频考点\n\n");
        
        sb.append("### Q1: 请描述 Spring Bean 的生命周期？\n\n");
        sb.append("**答**：\n");
        sb.append("1. **实例化**：创建 Bean 实例\n");
        sb.append("2. **属性注入**：填充属性和依赖注入\n");
        sb.append("3. **Aware 回调**：调用 BeanNameAware、BeanFactoryAware 等\n");
        sb.append("4. **BPP 前置**：执行 BeanPostProcessor.postProcessBeforeInitialization\n");
        sb.append("5. **初始化**：执行@PostConstruct、afterPropertiesSet、init-method\n");
        sb.append("6. **BPP 后置**：执行 BeanPostProcessor.postProcessAfterInitialization（AOP 在此）\n");
        sb.append("7. **Bean 可用**：执行业务逻辑\n");
        sb.append("8. **销毁回调**：执行@PreDestroy、destroy 方法\n");
        sb.append("9. **Bean 销毁**：从容器中移除\n\n");
        
        sb.append("### Q2: 初始化的三种方式及执行顺序？\n\n");
        sb.append("**答**：\n");
        sb.append("1. **@PostConstruct**（JSR-250 标准，优先级最高）\n");
        sb.append("2. **InitializingBean.afterPropertiesSet**（Spring 接口）\n");
        sb.append("3. **init-method**（自定义方法，优先级最低）\n\n");
        
        sb.append("### Q3: BeanPostProcessor 的作用？\n\n");
        sb.append("**答**：\n");
        sb.append("- 在 Bean 初始化前后进行增强处理\n");
        sb.append("- 前置处理：postProcessBeforeInitialization\n");
        sb.append("- 后置处理：postProcessAfterInitialization（**AOP 代理在此创建**）\n");
        sb.append("- 可以修改或替换 Bean 实例\n\n");
        
        sb.append("### Q4: AOP 在哪个阶段实现的？\n\n");
        sb.append("**答**：在 BeanPostProcessor 的**后置处理**阶段（postProcessAfterInitialization），\n");
        sb.append("Spring AOP 会在这个方法中为 Bean 创建代理对象。\n\n");
        
        sb.append("## 四、总结\n\n");
        
        sb.append("**一句话记忆**：\n");
        sb.append("实→属→Aw→BPP 前→初（@Post→Inter→init）→BPP 后→用→销→毁\n\n");
        
        sb.append("**核心要点**：\n");
        sb.append("1. 初始化顺序：@PostConstruct → InitializingBean → init-method\n");
        sb.append("2. 销毁顺序：@PreDestroy → DisposableBean → destroy-method\n");
        sb.append("3. AOP 代理在 BPP 后置处理阶段创建\n");
        sb.append("4. BeanPostProcessor 是 Spring 扩展的核心机制\n\n");
        
        return sb.toString();
    }
}
