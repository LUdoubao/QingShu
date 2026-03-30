package org.doubao.interview.agent.server.service.impl.question028;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.service.question028.MessageService;
import org.springframework.stereotype.Service;

/**
 * 消息服务实现类 A
 * 
 * 【类注释】
 * 职责：MessageService 的第一个实现，演示多实现场景下的依赖注入
 * 边界：仅用于学习和演示，实际业务中应有具体的业务逻辑
 * 线程安全：无状态服务，线程安全
 * 是否幂等：processMessage 方法是幂等的
 * 
 * 【面试知识点 - 问题 028】
 * 这个类和 MessageServiceImplB 一起演示了当存在多个实现类时：
 * - @Autowired 默认按类型注入会抛出 NoUniqueBeanDefinitionException
 * - 需要使用@Qualifier("messageServiceImplA") 来指定具体的实现
 * - 或使用@Resource(name="messageServiceImplA") 按名称注入
 * 
 * Bean 的名称默认为首字母小写的类名：messageServiceImplA
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class MessageServiceImplA implements MessageService {
    
    @Override
    public String processMessage(String message) {
        log.info("[MessageServiceImplA] 处理消息：{}", message);
        
        // 【关键代码段注释】
        // 为什么需要校验空值？
        // 1. 防御性编程：防止后续逻辑出现 NPE
        // 2. 快速失败原则：在方法入口处检查参数，便于定位问题
        // 3. 接口契约：明确告知调用者参数的合法范围
        if (message == null || message.trim().isEmpty()) {
            log.warn("[MessageServiceImplA] 消息为空，拒绝处理");
            throw new IllegalArgumentException("消息不能为空");
        }
        
        // 模拟业务处理：在消息前添加实现类 A 的标识
        String result = "[ServiceA] " + message;
        log.info("[MessageServiceImplA] 消息处理完成：{}", result);
        return result;
    }
    
    @Override
    public String getServiceName() {
        return "MessageServiceImplA";
    }
}
