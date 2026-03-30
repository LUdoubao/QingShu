package org.doubao.interview.agent.server.service.impl.question028;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.service.question028.MessageService;
import org.springframework.stereotype.Service;

/**
 * 消息服务实现类 B
 * 
 * 【类注释】
 * 职责：MessageService 的第二个实现，与 ServiceImplA 形成对比，演示多实现场景
 * 边界：仅用于学习和演示，实际业务中应有具体的业务逻辑
 * 线程安全：无状态服务，线程安全
 * 是否幂等：processMessage 方法是幂等的
 * 
 * 【面试知识点 - 问题 028】
 * 这个类的存在就是为了演示当 Spring 容器中有多个 MessageService 实现时会发生什么：
 * - 如果只使用@Autowired，Spring 不知道该注入哪个实现，会报错
 * - 必须使用@Qualifier 或@Resource(name=...) 来明确指定
 * 
 * Bean 的名称默认为首字母小写的类名：messageServiceImplB
 * 
 * @author interview-agent
 * @date 2026-03-30
 */
@Slf4j
@Service
public class MessageServiceImplB implements MessageService {
    
    @Override
    public String processMessage(String message) {
        log.info("[MessageServiceImplB] 处理消息：{}", message);
        
        // 【关键代码段注释】
        // 为什么两个实现类都要做空值校验？
        // 1. 每个实现类都是独立的组件，需要保证自身的健壮性
        // 2. 即使接口定义中有说明，实现层也应该做参数验证
        // 3. 不同的实现可能有不同的处理逻辑，都需要保护
        if (message == null || message.trim().isEmpty()) {
            log.warn("[MessageServiceImplB] 消息为空，拒绝处理");
            throw new IllegalArgumentException("消息不能为空");
        }
        
        // 模拟业务处理：在消息前添加实现类 B 的标识
        // 这里故意使用和 ServiceImplA 不同的处理逻辑，便于观察调用了哪个实现
        String result = "[ServiceB] >> " + message;
        log.info("[MessageServiceImplB] 消息处理完成：{}", result);
        return result;
    }
    
    @Override
    public String getServiceName() {
        return "MessageServiceImplB";
    }
}
