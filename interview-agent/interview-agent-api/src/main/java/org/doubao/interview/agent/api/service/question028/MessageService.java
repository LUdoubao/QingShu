package org.doubao.interview.agent.api.service.question028;

/**
 * 消息服务接口
 * 
 * 【类注释】
 * 职责：定义消息处理服务的标准接口，用于演示依赖注入的不同方式
 * 边界：这是演示面试题的示例接口，实际业务中应根据具体需求设计
 * 线程安全：接口本身不涉及线程安全问题，实现类需自行保证
 * 
 * 【面试知识点 - 问题 028】
 * 这个接口有多个实现类，正好演示了为什么需要区分@Autowired 和@Resource：
 * - 当 Spring 容器中存在多个 MessageService 实现类时
 * - @Autowired 默认按类型注入会报错（找不到唯一的 bean）
 * - @Resource 默认按名称注入可以选择指定的实现类
 * - @Autowired + @Qualifier 也可以指定具体的实现类
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
public interface MessageService {
    
    /**
     * 处理消息
     * 
     * 【方法注释】
     * 输入约束：message 不能为 null 或空
     * 输出语义：返回处理后的消息内容
     * 异常场景：当 message 为空时抛出 IllegalArgumentException
     * 性能注意点：无特殊性能考虑
     * 
     * @param message 待处理的消息
     * @return 处理后的消息
     */
    String processMessage(String message);
    
    /**
     * 获取服务实现类的标识信息
     * 
     * 【方法注释】
     * 输入约束：无
     * 输出语义：返回当前实现类的名称或标识
     * 异常场景：无
     * 性能注意点：无
     * 
     * @return 服务实现类标识
     */
    String getServiceName();
}
