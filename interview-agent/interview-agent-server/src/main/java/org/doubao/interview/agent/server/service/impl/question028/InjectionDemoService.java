package org.doubao.interview.agent.server.service.impl.question028;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question028.InjectionDemoRequest;
import org.doubao.interview.agent.api.dto.question028.InjectionDemoResponse;
import org.doubao.interview.agent.api.service.question028.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 依赖注入演示服务
 * 
 * 【类注释】
 * 职责：演示@Autowired 和@Resource 在不同场景下的使用方式
 * 边界：仅用于学习和演示，展示两种注解的核心区别
 * 线程安全：无状态服务，线程安全
 * 是否幂等：所有方法都是幂等的
 * 
 * 【面试知识点 - 问题 028】
 * 这个类通过实际注入两个 MessageService 实现类，展示了：
 * 1. @Resource 默认按名称注入（name="messageServiceImplA"）
 * 2. @Resource 也可以按类型注入（不指定 name 时）
 * 3. @Autowired 默认按类型注入（需要配合@Qualifier 指定具体实现）
 * 4. 多实现场景下必须明确指定要注入的 bean，否则 Spring 无法决定用哪个
 * 
 * 核心区别总结：
 * - @Autowired 是 Spring 提供的注解，按类型注入是首选策略
 * - @Resource 是 JSR-250 规范的注解，按名称注入是首选策略
 * - 当有多个同类型 bean 时，@Resource(name="xxx")更直观
 * - @Autowired + @Qualifier("xxx")也能达到同样效果，但语法稍显复杂
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class InjectionDemoService {
    
    /**
     * 使用@Resource 按名称注入 ServiceA
     * 
     * 【关键代码段注释】
     * 为什么这里使用@Resource(name="messageServiceImplA")？
     * 1. Spring 容器中有两个 MessageService 实现类
     * 2. @Resource 默认先按名称匹配，找到名为"messageServiceImplA"的 bean
     * 3. 如果找不到再按类型匹配，但这里有多个同类型 bean 会报错
     * 4. 所以必须显式指定 name 属性来明确要注入哪个实现
     */
    @Resource(name = "messageServiceImplA")
    private MessageService serviceA;
    
    /**
     * 使用@Resource 按名称注入 ServiceB
     * 
     * 【关键代码段注释】
     * 为什么不写 name 属性也能工作？
     * 1. 字段名是"serviceB"，Spring 会尝试找名为"serviceB"的 bean
     * 2. 但实际上我们的 bean 名是"messageServiceImplB"
     * 3. 如果按名称找不到，@Resource 会回退到按类型注入
     * 4. 但因为有多个 MessageService 实现，按类型注入会失败
     * 5. 所以实际上这里必须指定 name 属性！
     */
    @Resource(name = "messageServiceImplB")
    private MessageService serviceB;
    
    /**
     * 使用@Autowired + @Qualifier 注入 ServiceA
     * 
     * 【关键代码段注释】
     * @Autowired 和@Qualifier 的配合使用：
     * 1. @Autowired 默认按类型注入，但这里有多个 MessageService 实现
     * 2. @Qualifier("messageServiceImplA") 告诉 Spring 要找的具体 bean 名称
     * 3. 这种方式比@Resource 更"Spring 风格"
     * 4. 但语法上稍微繁琐一些（需要两个注解）
     */
    @Autowired
    @Qualifier("messageServiceImplA")
    private MessageService serviceWithAutowiredA;
    
    /**
     * 使用@Autowired + @Qualifier 注入 ServiceB
     */
    @Autowired
    @Qualifier("messageServiceImplB")
    private MessageService serviceWithAutowiredB;
    
    /**
     * 演示使用@Resource 按名称注入
     * 
     * 【方法注释】
     * 输入约束：request 不能为 null，message 不能为空
     * 输出语义：返回使用指定实现类处理后的结果
     * 异常场景：参数校验失败抛出 IllegalArgumentException
     * 性能注意点：无特殊性能考虑，纯内存操作
     * 
     * @param request 请求参数
     * @return 演示结果
     */
    public InjectionDemoResponse demoResourceByName(InjectionDemoRequest request) {
        log.info("[InjectionDemoService] 开始演示@Resource 按名称注入，implName={}", request.getImplName());
        
        // 根据请求选择使用哪个实现类
        MessageService selectedService;
        String explanation;
        
        if ("messageServiceImplB".equals(request.getImplName())) {
            selectedService = serviceB;
            explanation = buildExplanation("@Resource(name=\"messageServiceImplB\")", 
                    "按名称注入", "先查找名为 messageServiceImplB 的 bean，找不到则按类型注入");
        } else {
            // 默认使用 ServiceA
            selectedService = serviceA;
            explanation = buildExplanation("@Resource(name=\"messageServiceImplA\")", 
                    "按名称注入", "先查找名为 messageServiceImplA 的 bean，找不到则按类型注入");
        }
        
        String result = selectedService.processMessage(request.getMessage());
        
        InjectionDemoResponse response = InjectionDemoResponse.builder()
                .injectionType("@Resource(byName)")
                .actualServiceClass(selectedService.getServiceName())
                .resultMessage(result)
                .explanation(explanation)
                .build();
        
        log.info("[InjectionDemoService] @Resource 演示完成，使用实现={}, 结果={}", 
                selectedService.getServiceName(), result);
        
        return response;
    }
    
    /**
     * 演示使用@Autowired + @Qualifier 注入
     * 
     * 【方法注释】
     * 输入约束：request 不能为 null，message 不能为空
     * 输出语义：返回使用指定实现类处理后的结果
     * 异常场景：参数校验失败抛出 IllegalArgumentException
     * 性能注意点：无特殊性能考虑
     * 
     * @param request 请求参数
     * @return 演示结果
     */
    public InjectionDemoResponse demoAutowiredWithQualifier(InjectionDemoRequest request) {
        log.info("[InjectionDemoService] 开始演示@Autowired+@Qualifier，implName={}", request.getImplName());
        
        MessageService selectedService;
        String explanation;
        
        if ("messageServiceImplB".equals(request.getImplName())) {
            selectedService = serviceWithAutowiredB;
            explanation = buildExplanation("@Autowired + @Qualifier(\"messageServiceImplB\")", 
                    "按类型 + 限定符", "先按类型查找，再用@Qualifier 指定具体的 bean 名称");
        } else {
            selectedService = serviceWithAutowiredA;
            explanation = buildExplanation("@Autowired + @Qualifier(\"messageServiceImplA\")", 
                    "按类型 + 限定符", "先按类型查找，再用@Qualifier 指定具体的 bean 名称");
        }
        
        String result = selectedService.processMessage(request.getMessage());
        
        InjectionDemoResponse response = InjectionDemoResponse.builder()
                .injectionType("@Autowired + @Qualifier")
                .actualServiceClass(selectedService.getServiceName())
                .resultMessage(result)
                .explanation(explanation)
                .build();
        
        log.info("[InjectionDemoService] @Autowired+@Qualifier 演示完成，使用实现={}, 结果={}", 
                selectedService.getServiceName(), result);
        
        return response;
    }
    
    /**
     * 获取详细的对比说明
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回@Autowired 和@Resource 的详细对比说明
     * 异常场景：无
     * 性能注意点：无
     * 
     * @return 详细对比说明文本
     */
    public String getDetailedComparison() {
        log.info("[InjectionDemoService] 获取@Autowired 和@Resource 的详细对比");
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== @Autowired vs @Resource 详细对比 ===\n\n");
        
        sb.append("【1. 来源不同】\n");
        sb.append("- @Autowired: Spring 框架提供 (org.springframework.beans.factory.annotation)\n");
        sb.append("- @Resource: JSR-250 规范定义 (javax.annotation 或 jakarta.annotation)\n\n");
        
        sb.append("【2. 注入策略不同】\n");
        sb.append("- @Autowired: \n");
        sb.append("  • 默认按类型注入 (byType)\n");
        sb.append("  • 配合@Qualifier 可按名称注入\n");
        sb.append("  • 可以设置 required=false 允许注入 null\n");
        sb.append("- @Resource: \n");
        sb.append("  • 默认按名称注入 (byName)\n");
        sb.append("  • 可以通过 name 属性显式指定 bean 名称\n");
        sb.append("  • 找不到名称时回退到按类型注入\n\n");
        
        sb.append("【3. 多实现类场景】\n");
        sb.append("当 Spring 容器中存在多个相同类型的 bean 时：\n");
        sb.append("- @Autowired 单独使用会抛出 NoUniqueBeanDefinitionException\n");
        sb.append("- 必须配合@Qualifier(\"beanName\") 使用\n");
        sb.append("- @Resource(name=\"beanName\") 可以直接指定 bean 名称\n\n");
        
        sb.append("【4. 实际使用建议】\n");
        sb.append("- 推荐优先使用@Resource，语义更清晰（按名称注入更符合直觉）\n");
        sb.append("- 如果项目强依赖 Spring，使用@Autowired 也完全没问题\n");
        sb.append("- 多实现场景下，两者都需要显式指定 bean 名称\n");
        sb.append("- 统一团队风格比选择哪个注解更重要\n\n");
        
        sb.append("【5. 本示例中的演示】\n");
        sb.append("- MessageServiceImplA 和 MessageServiceImplB 是两个实现类\n");
        sb.append("- 通过不同的注入方式，可以灵活选择使用哪个实现\n");
        sb.append("- 实际调用时可以观察到使用了不同的服务处理消息\n");
        
        String result = sb.toString();
        log.info("[InjectionDemoService] 返回详细对比说明，长度={} 字符", result.length());
        
        return result;
    }
    
    /**
     * 构建解释说明文本
     * 
     * @param annotation 使用的注解
     * @param strategy 注入策略
     * @param mechanism 工作机制
     * @return 格式化的解释说明
     */
    private String buildExplanation(String annotation, String strategy, String mechanism) {
        return String.format(
            "【注入方式】%s\n" +
            "【注入策略】%s\n" +
            "【工作机制】%s\n" +
            "【适用场景】当存在多个同类型 bean 时，需要显式指定要注入的 bean",
            annotation, strategy, mechanism
        );
    }
}
