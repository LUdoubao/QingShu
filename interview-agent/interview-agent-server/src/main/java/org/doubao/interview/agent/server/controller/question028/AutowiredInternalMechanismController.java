package org.doubao.interview.agent.server.controller.question028;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * @Autowired 底层原理详解 Controller
 * 
 * 【类注释】
 * 职责：通过实际代码演示@Autowired 的底层工作原理，包括源码级解析
 * 边界：仅用于学习和演示，简化了部分 Spring 内部逻辑以便理解
 * 线程安全：Controller 本身无状态，线程安全
 * 
 * 【面试知识点 - 问题 028 扩展】
 * 这个类深入解析@Autowired 的底层工作机制：
 * 1. Spring 如何扫描和处理@Autowired 注解
 * 2. AutowiredAnnotationBeanPostProcessor 的作用
 * 3. doResolveDependency 方法的核心流程
 * 4. 按类型注入的具体实现逻辑
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question028/internal")
public class AutowiredInternalMechanismController {
    
    /**
     * 演示用服务类
     */
    @Autowired
    private DemoService demoService;
    
    /**
     * 获取@Autowired 底层工作原理详解
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回源码级的@Autowired 工作机制说明
     * 异常场景：无
     * 性能注意点：无
     * 
     * @return 详细的原理说明
     */
    @GetMapping("/principle")
    public String getAutowiredPrinciple() {
        log.info("[AutowiredInternalMechanismController] 获取@Autowired 底层原理说明");
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("=== @Autowired 底层工作原理（源码级解析）===\n\n");
        
        sb.append("【一、整体流程概览】\n");
        sb.append("1. Spring 容器启动时，AutowiredAnnotationBeanPostProcessor 被注册\n");
        sb.append("2. Bean 实例化后，调用 postProcessProperties 方法\n");
        sb.append("3. 扫描所有标注了@Autowired 的字段和方法\n");
        sb.append("4. 调用 doResolveDependency 完成依赖注入\n\n");
        
        sb.append("【二、核心类与接口】\n");
        sb.append("1. AutowiredAnnotationBeanPostProcessor\n");
        sb.append("   - 位置：org.springframework.beans.factory.annotation\n");
        sb.append("   - 职责：处理@Autowired 注解的后处理器\n");
        sb.append("   - 实现了 MergedBeanDefinitionPostProcessor 接口\n\n");
        
        sb.append("2. DependencyDescriptor\n");
        sb.append("   - 封装了注入点的元数据（字段/方法的反射信息）\n");
        sb.append("   - 包含 required 属性、注解信息等\n\n");
        
        sb.append("3. DefaultListableBeanFactory\n");
        sb.append("   - Spring 默认 Bean 工厂实现\n");
        sb.append("   - 提供 doResolveDependency 方法执行实际的依赖查找\n\n");
        
        sb.append("【三、源码级执行流程】\n\n");
        
        sb.append("步骤 1: 后处理器初始化\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AnnotationConfigUtils.registerAnnotationConfigProcessors()\n");
        sb.append("public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(\n");
        sb.append("        BeanDefinitionRegistry registry, Object source) {\n");
        sb.append("    \n");
        sb.append("    // 注册 AutowiredAnnotationBeanPostProcessor\n");
        sb.append("    if (!registry.containsBeanDefinition(AUTO_WIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {\n");
        sb.append("        RootBeanDefinition def = new RootBeanDefinition(\n");
        sb.append("            AutowiredAnnotationBeanPostProcessor.class);\n");
        sb.append("        registry.registerBeanDefinition(\n");
        sb.append("            AUTO_WIRED_ANNOTATION_PROCESSOR_BEAN_NAME, def);\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("步骤 2: Bean 实例化后的处理\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AutowiredAnnotationBeanPostProcessor.postProcessProperties()\n");
        sb.append("@Override\n");
        sb.append("public PropertyValues postProcessProperties(PropertyValues pvs, \n");
        sb.append("        Object bean, String beanName) {\n");
        sb.append("    \n");
        sb.append("    // 查找需要注入的元数据\n");
        sb.append("    InjectionMetadata metadata = findAutowiringMetadata(beanName, \n");
        sb.append("        bean.getClass(), pvs);\n");
        sb.append("    \n");
        sb.append("    try {\n");
        sb.append("        // 执行注入逻辑\n");
        sb.append("        metadata.inject(bean, beanName, pvs);\n");
        sb.append("    } catch (Throwable ex) {\n");
        sb.append("        throw new BeanCreationException(..., ex);\n");
        sb.append("    }\n");
        sb.append("    return pvs;\n");
        sb.append("}\n\n");
        
        sb.append("步骤 3: 扫描@Autowired 注解字段和方法\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AutowiredAnnotationBeanPostProcessor.findAutowiringMetadata()\n");
        sb.append("private InjectionMetadata findAutowiringMetadata(\n");
        sb.append("        String beanName, Class<?> clazz, PropertyValues pvs) {\n");
        sb.append("    \n");
        sb.append("    // 快速路径：从缓存中获取\n");
        sb.append("    String cacheKey = (StringUtils.hasLength(beanName) ? \n");
        sb.append("        beanName : clazz.getName());\n");
        sb.append("    InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);\n");
        sb.append("    \n");
        sb.append("    if (needsRefresh(metadata)) {\n");
        sb.append("        synchronized (this.injectionMetadataCache) {\n");
        sb.append("            // 双重检查锁定，重新构建元数据\n");
        sb.append("            metadata = buildAutowiringMetadata(clazz);\n");
        sb.append("            this.injectionMetadataCache.put(cacheKey, metadata);\n");
        sb.append("        }\n");
        sb.append("    }\n");
        sb.append("    return metadata;\n");
        sb.append("}\n\n");
        
        sb.append("步骤 4: 构建注入元数据（关键！）\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AutowiredAnnotationBeanPostProcessor.buildAutowiringMetadata()\n");
        sb.append("private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {\n");
        sb.append("    LinkedList<InjectionMetadata.InjectedElement> elements = \n");
        sb.append("        new LinkedList<>();\n");
        sb.append("    \n");
        sb.append("    // 遍历类及其父类的所有字段\n");
        sb.append("    ReflectionUtils.doWithLocalFields(targetClass, field -> {\n");
        sb.append("        // 查找字段上的@Autowired 注解\n");
        sb.append("        AnnotationAttributes ann = findAutowiredAnnotation(field);\n");
        sb.append("        if (ann != null) {\n");
        sb.append("            // 标记该字段不需要访问检查（即使 private 也能注入）\n");
        sb.append("            if (Modifier.isStatic(field.getModifiers())) {\n");
        sb.append("                return; // 跳过静态字段\n");
        sb.append("            }\n");
        sb.append("            boolean required = determineRequiredStatus(ann);\n");
        sb.append("            elements.add(new AutowiredFieldElement(field, required));\n");
        sb.append("        }\n");
        sb.append("    });\n");
        sb.append("    \n");
        sb.append("    // 同样处理标注了@Autowired 的方法\n");
        sb.append("    ReflectionUtils.doWithMethods(targetClass, method -> {\n");
        sb.append("        AnnotationAttributes ann = findAutowiredAnnotation(method);\n");
        sb.append("        if (ann != null) {\n");
        sb.append("            elements.add(new AutowiredMethodElement(method, required));\n");
        sb.append("        }\n");
        sb.append("    });\n");
        sb.append("    \n");
        sb.append("    return new InjectionMetadata(elements, this.defaultRequired);\n");
        sb.append("}\n\n");
        
        sb.append("步骤 5: 执行字段注入\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AutowiredFieldElement.inject() 内部类方法\n");
        sb.append("@Override\n");
        sb.append("protected void inject(Object bean, String beanName, \n");
        sb.append("        PropertyValues pvs) throws Throwable {\n");
        sb.append("    \n");
        sb.append("    Field field = (Field) this.member;\n");
        sb.append("    \n");
        sb.append("    // 创建依赖描述符\n");
        sb.append("    DependencyDescriptor desc = new DependencyDescriptor(\n");
        sb.append("        field, this.required);\n");
        sb.append("    desc.setContainingClass(getClass());\n");
        sb.append("    \n");
        sb.append("    // 设置回调，用于处理泛型等情况\n");
        sb.append("    Set<String> autowiredBeanNames = new LinkedHashSet<>(1);\n");
        sb.append("    Assert.state(beanFactory != null, ...);\n");
        sb.append("    TypeConverter typeConverter = beanFactory.getTypeConverter();\n");
        sb.append("    \n");
        sb.append("    // 【核心】调用 BeanFactory 解析依赖\n");
        sb.append("    Object value = resolveDependency(desc, beanName, \n");
        sb.append("        autowiredBeanNames, typeConverter);\n");
        sb.append("    \n");
        sb.append("    if (value != null) {\n");
        sb.append("        // 使用反射设置字段值\n");
        sb.append("        ReflectionUtils.makeAccessible(field);\n");
        sb.append("        field.set(bean, value);\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("步骤 6: 依赖解析的核心逻辑\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// DefaultListableBeanFactory.doResolveDependency()\n");
        sb.append("@Override\n");
        sb.append("@Nullable\n");
        sb.append("public Object doResolveDependency(DependencyDescriptor descriptor,\n");
        sb.append("        @Nullable String requestingBeanName,\n");
        sb.append("        @Nullable Set<String> autowiredBeanNames,\n");
        sb.append("        @Nullable TypeConverter typeConverter) {\n");
        sb.append("    \n");
        sb.append("    // 1. 获取要注入的类型\n");
        sb.append("    Class<?> type = descriptor.getDependencyType();\n");
        sb.append("    \n");
        sb.append("    // 2. 处理@Value 注解（如果有）\n");
        sb.append("    Object value = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(\n");
        sb.append("        descriptor, requestingBeanName);\n");
        sb.append("    if (value == null) {\n");
        sb.append("        // 3. 进入真正的依赖解析\n");
        sb.append("        value = getAutowireCandidateResolver().resolveCandidate(\n");
        sb.append("            descriptor, requestingBeanName, this);\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    return value;\n");
        sb.append("}\n\n");
        
        sb.append("步骤 7: 按类型查找 Bean（最核心！）\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// DependencyDescriptor.resolveCandidate()\n");
        sb.append("public Object resolveCandidate(\n");
        sb.append("        DependencyDescriptor descriptor, \n");
        sb.append("        String requestingBeanName,\n");
        sb.append("        BeanFactory beanFactory) {\n");
        sb.append("    \n");
        sb.append("    // 获取要注入的类型\n");
        sb.append("    Class<?> dependencyType = descriptor.getDependencyType();\n");
        sb.append("    \n");
        sb.append("    // 【关键】从 BeanFactory 中获取 Bean\n");
        sb.append("    // 这里会触发 Bean 的创建（如果还没创建）\n");
        sb.append("    return beanFactory.getBean(dependencyType);\n");
        sb.append("}\n\n");
        
        sb.append("步骤 8: getBean 的内部调用链\n");
        sb.append("─────────────────────────────────────\n");
        sb.append("// AbstractApplicationContext.getBean(Class<T>)\n");
        sb.append("@Override\n");
        sb.append("public <T> T getBean(Class<T> requiredType) \n");
        sb.append("        throws BeansException {\n");
        sb.append("    \n");
        sb.append("    // 委托给 BeanFactory\n");
        sb.append("    return beanFactory.getBean(requiredType);\n");
        sb.append("}\n\n");
        
        sb.append("// AbstractBeanFactory.getBean(Class<T>)\n");
        sb.append("@Override\n");
        sb.append("public <T> T getBean(Class<T> requiredType) \n");
        sb.append("        throws BeansException {\n");
        sb.append("    \n");
        sb.append("    // 转换为 BeanName（对于接口会有特殊处理）\n");
        sb.append("    do {\n");
        sb.append("        // 尝试从缓存获取已创建的 Bean\n");
        sb.append("        Object sharedInstance = getSingleton(beanName);\n");
        sb.append("        \n");
        sb.append("        if (sharedInstance != null) {\n");
        sb.append("            // 返回最终的 Bean（处理 FactoryBean 等）\n");
        sb.append("            return (T) getObjectForBeanInstance(...);\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        // 如果不存在，创建 Bean\n");
        sb.append("        return (T) doGetBean(name, requiredType, ...);\n");
        sb.append("    } while (...);\n");
        sb.append("}\n\n");
        
        sb.append("【四、关键点总结】\n");
        sb.append("1. AutowiredAnnotationBeanPostProcessor 是核心处理器\n");
        sb.append("2. 使用反射扫描字段和方法上的@Autowired 注解\n");
        sb.append("3. 通过 DependencyDescriptor 封装注入点信息\n");
        sb.append("4. DefaultListableBeanFactory.doResolveDependency() 执行实际解析\n");
        sb.append("5. 最终调用 getBean() 从容器中获取或创建 Bean\n");
        sb.append("6. 使用反射的 Field.set() 完成注入\n\n");
        
        sb.append("【五、为什么多实现会报错？】\n");
        sb.append("当调用 beanFactory.getBean(MessageService.class) 时：\n");
        sb.append("1. Spring 会查找所有 MessageService 类型的 Bean\n");
        sb.append("2. 如果找到多个（messageServiceImplA 和 messageServiceImplB）\n");
        sb.append("3. NoUniqueBeanDefinitionCreator 会被触发\n");
        sb.append("4. 抛出 NoUniqueBeanDefinitionException:\n");
        sb.append("   'expected single matching bean but found 2: \n");
        sb.append("   messageServiceImplA,messageServiceImplB'\n\n");
        
        sb.append("【六、@Qualifier 如何工作？】\n");
        sb.append("1. @Qualifier 提供了 qualifier 元数据\n");
        sb.append("2. AutowiredAnnotationBeanPostProcessor 会提取 qualifier\n");
        sb.append("3. 在 doResolveDependency 时，不仅匹配类型，还匹配名称\n");
        sb.append("4. 最终调用 getBean(\"messageServiceImplA\", MessageService.class)\n");
        sb.append("5. 先按名称精确查找，再验证类型是否匹配\n\n");
        
        String result = sb.toString();
        log.info("[AutowiredInternalMechanismController] 返回源码级原理说明，长度={} 字符", result.length());
        
        return result;
    }
    
    /**
     * 简化的流程图
     */
    @GetMapping("/flow-chart")
    public String getFlowChart() {
        log.info("[AutowiredInternalMechanismController] 获取流程图");
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== @Autowired 工作流程图 ===\n\n");
        sb.append("Spring 容器启动\n");
        sb.append("    ↓\n");
        sb.append("注册 AutowiredAnnotationBeanPostProcessor\n");
        sb.append("    ↓\n");
        sb.append("创建业务 Bean（如 YourController）\n");
        sb.append("    ↓\n");
        sb.append("实例化 Bean（调用构造函数）\n");
        sb.append("    ↓\n");
        sb.append("postProcessProperties() 被调用\n");
        sb.append("    ↓\n");
        sb.append("findAutowiringMetadata() 扫描@Autowired\n");
        sb.append("    ↓\n");
        sb.append("buildAutowiringMetadata() 构建元数据\n");
        sb.append("    ├→ 扫描所有字段（doWithLocalFields）\n");
        sb.append("    ├→ 发现@Autowired 注解\n");
        sb.append("    ├→ 创建 AutowiredFieldElement\n");
        sb.append("    └→ 加入待注入列表\n");
        sb.append("    ↓\n");
        sb.append("metadata.inject() 执行注入\n");
        sb.append("    ↓\n");
        sb.append("为每个字段创建 DependencyDescriptor\n");
        sb.append("    ↓\n");
        sb.append("调用 resolveDependency()\n");
        sb.append("    ↓\n");
        sb.append("DefaultListableBeanFactory.doResolveDependency()\n");
        sb.append("    ├→ 获取依赖类型（descriptor.getDependencyType()）\n");
        sb.append("    ├→ 查找匹配的 Bean（getBean(dependencyType)）\n");
        sb.append("    ├→ 如果找到多个 → 抛异常\n");
        sb.append("    ├→ 如果找到一个 → 返回该 Bean\n");
        sb.append("    └→ 如果没找到 → 根据 required 决定是否报错\n");
        sb.append("    ↓\n");
        sb.append("使用反射设置字段值\n");
        sb.append("    ├→ ReflectionUtils.makeAccessible(field)\n");
        sb.append("    └→ field.set(bean, resolvedBean)\n");
        sb.append("    ↓\n");
        sb.append("注入完成，Bean 可用\n");
        
        return sb.toString();
    }
    
    /**
     * 演示用服务接口
     */
    public interface DemoService {
        String getName();
    }
    
    /**
     * 演示用服务实现
     */
    @Component
    public static class DemoServiceImpl implements DemoService {
        @Override
        public String getName() {
            return "DemoServiceImpl";
        }
    }
}
