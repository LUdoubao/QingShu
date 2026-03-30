package org.doubao.interview.agent.api.dto.question028;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 依赖注入演示请求对象
 * 
 * 【类注释】
 * 职责：封装客户端请求参数，用于演示@Autowired 和@Resource 的区别
 * 边界：仅用于学习和演示场景
 * 线程安全：无状态对象，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Data
public class InjectionDemoRequest {
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;
    
    /**
     * 是否指定具体的实现类名称
     * true: 需要指定实现类，演示多实现场景下的注入方式
     * false: 使用默认注入
     */
    private boolean specifyImpl = false;
    
    /**
     * 指定的实现类名称（当 specifyImpl=true 时有效）
     * 可选值：messageServiceImplA, messageServiceImplB
     */
    private String implName;
}
