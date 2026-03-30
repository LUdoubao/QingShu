package org.doubao.interview.agent.api.dto.question028;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 依赖注入演示响应对象
 * 
 * 【类注释】
 * 职责：返回依赖注入的演示结果，包含使用的注入方式和实际调用的实现类
 * 边界：仅用于学习和演示场景
 * 线程安全：不可变对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InjectionDemoResponse {
    
    /**
     * 使用的注入方式
     * 可选值：@Autowired, @Resource(byName), @Resource(byType), @Autowired+@Qualifier
     */
    private String injectionType;
    
    /**
     * 实际调用的服务实现类名称
     */
    private String actualServiceClass;
    
    /**
     * 处理结果消息
     */
    private String resultMessage;
    
    /**
     * 详细说明
     */
    private String explanation;
}
