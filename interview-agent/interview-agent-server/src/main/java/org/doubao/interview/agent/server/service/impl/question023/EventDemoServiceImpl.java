package org.doubao.interview.agent.server.service.impl.question023;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question023.UserRegisterRequest;
import org.doubao.interview.agent.api.dto.question023.UserRegisterResponse;
import org.doubao.interview.agent.api.service.question023.EventDemoService;
import org.doubao.interview.agent.server.event.question023.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Spring 事件机制演示服务实现类
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：实现用户注册功能，演示 Spring 事件的发布和监听
 * 边界：仅用于演示，不包含真实业务系统的复杂处理
 * 线程安全：通过 Spring 容器和事件机制保证并发安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@Service
public class EventDemoServiceImpl implements EventDemoService {

    /**
     * Spring 事件发布器
     * 用于发布 ApplicationEvent 类型的事件
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 用户 ID 生成器（模拟）
     */
    private final AtomicLong userIdGenerator = new AtomicLong(1000);

    /**
     * 构造注入 ApplicationEventPublisher
     * Spring 会自动注入事件发布器
     */
    public EventDemoServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 用户注册 - 发布事件并触发监听器
     * 
     * 【关键逻辑说明】
     * 1. 创建用户记录（模拟）
     * 2. 发布 UserRegisteredEvent 事件
     * 3. 所有监听该事件的监听器都会被触发
     * 4. 监听器可以是同步或异步的
     * 
     * 【事件机制的优势】
     * - 解耦：发布者和监听者互不依赖
     * - 扩展：新增监听器无需修改发布者代码
     * - 灵活：支持同步和异步处理
     */
    @Override
    public UserRegisterResponse register(UserRegisterRequest request) {
        log.info("开始处理用户注册，userName={}, email={}", 
                request.getUserName(), request.getEmail());
        
        // 1. 创建用户（模拟）
        Long userId = userIdGenerator.incrementAndGet();
        
        // 2. 发布用户注册事件
        log.info("发布用户注册事件，userId={}", userId);
        UserRegisteredEvent event = new UserRegisteredEvent(
                this, 
                userId, 
                request.getUserName(), 
                request.getEmail()
        );
        eventPublisher.publishEvent(event);
        
        // 3. 构建响应信息
        List<UserRegisterResponse.EventProcessInfo> eventInfos = new ArrayList<>();
        
        // 同步邮件发送监听器
        eventInfos.add(UserRegisterResponse.EventProcessInfo.builder()
                .listenerName("EmailSendListener.handleEmailSend")
                .processType(request.getAsync() ? "异步" : "同步")
                .status("已完成")
                .processTime(100L)
                .description("发送欢迎邮件到用户邮箱")
                .build());
        
        // 如果开启异步，添加异步监听器信息
        if (request.getAsync()) {
            eventInfos.add(UserRegisterResponse.EventProcessInfo.builder()
                    .listenerName("EmailSendListener.handleEmailSendAsync")
                    .processType("异步")
                    .status("异步执行中")
                    .processTime(200L)
                    .description("异步发送欢迎邮件（不阻塞主流程）")
                    .build());
        }
        
        // 统计监听器
        eventInfos.add(UserRegisterResponse.EventProcessInfo.builder()
                .listenerName("StatisticsListener.handleStatistics")
                .processType("同步")
                .status("已完成")
                .processTime(50L)
                .description("更新用户统计数据")
                .build());
        
        UserRegisterResponse response = UserRegisterResponse.builder()
                .success(true)
                .userId(userId)
                .userName(request.getUserName())
                .eventProcessInfos(eventInfos)
                .build();
        
        log.info("用户注册完成，userId={}, userName={}", userId, request.getUserName());
        
        return response;
    }

    /**
     * 获取 Spring 事件机制的详细说明
     */
    @Override
    public String getEventMechanismExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Spring 事件机制详解\n\n");
        
        sb.append("## 一、Spring 事件机制的核心组件\n\n");
        sb.append("Spring 事件机制基于观察者模式，包含三个核心角色：\n\n");
        sb.append("1. **事件（Event）**：继承自 `ApplicationEvent`\n");
        sb.append("   ```java\n");
        sb.append("   public class UserRegisteredEvent extends ApplicationEvent {\n");
        sb.append("       private final Long userId;\n");
        sb.append("       private final String userName;\n");
        sb.append("       // 构造函数和 getter\n");
        sb.append("   }\n");
        sb.append("   ```\n\n");
        
        sb.append("2. **事件发布器（Publisher）**：使用 `ApplicationEventPublisher`\n");
        sb.append("   ```java\n");
        sb.append("   @Service\n");
        sb.append("   public class UserService {\n");
        sb.append("       @Autowired\n");
        sb.append("       private ApplicationEventPublisher publisher;\n");
        sb.append("       \n");
        sb.append("       public void register(User user) {\n");
        sb.append("           // 1. 创建用户\n");
        sb.append("           User savedUser = userRepository.save(user);\n");
        sb.append("           \n");
        sb.append("           // 2. 发布事件\n");
        sb.append("           publisher.publishEvent(new UserRegisteredEvent(this, savedUser));\n");
        sb.append("       }\n");
        sb.append("   }\n");
        sb.append("   ```\n\n");
        
        sb.append("3. **事件监听器（Listener）**：使用 `@EventListener` 注解\n");
        sb.append("   ```java\n");
        sb.append("   @Component\n");
        sb.append("   public class EmailSendListener {\n");
        sb.append("       @EventListener(classes = UserRegisteredEvent.class)\n");
        sb.append("       public void handleEmail(UserRegisteredEvent event) {\n");
        sb.append("           // 发送邮件\n");
        sb.append("           emailService.send(event.getEmail());\n");
        sb.append("       }\n");
        sb.append("   }\n");
        sb.append("   ```\n\n");
        
        sb.append("## 二、事件机制的使用场景\n\n");
        sb.append("### 2.1 模块解耦\n\n");
        sb.append("**场景**：用户注册后需要执行多个操作（发邮件、送积分、统计等）\n\n");
        sb.append("**传统方式（耦合）**：\n");
        sb.append("```java\n");
        sb.append("@Service\n");
        sb.append("public class UserService {\n");
        sb.append("    @Autowired\n");
        sb.append("    private EmailService emailService;\n");
        sb.append("    @Autowired\n");
        sb.append("    private PointService pointService;\n");
        sb.append("    @Autowired\n");
        sb.append("    private StatisticsService statisticsService;\n");
        sb.append("    \n");
        sb.append("    public void register(User user) {\n");
        sb.append("        // 保存用户\n");
        sb.append("        User savedUser = userRepository.save(user);\n");
        sb.append("        \n");
        sb.append("        // 发送邮件（耦合）\n");
        sb.append("        emailService.sendWelcomeEmail(savedUser.getEmail());\n");
        sb.append("        \n");
        sb.append("        // 赠送积分（耦合）\n");
        sb.append("        pointService.awardPoints(savedUser.getId(), 100);\n");
        sb.append("        \n");
        sb.append("        // 更新统计（耦合）\n");
        sb.append("        statisticsService.incrementUserCount();\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("**事件机制（解耦）**：\n");
        sb.append("```java\n");
        sb.append("@Service\n");
        sb.append("public class UserService {\n");
        sb.append("    @Autowired\n");
        sb.append("    private ApplicationEventPublisher publisher;\n");
        sb.append("    \n");
        sb.append("    public void register(User user) {\n");
        sb.append("        // 只负责核心业务\n");
        sb.append("        User savedUser = userRepository.save(user);\n");
        sb.append("        \n");
        sb.append("        // 发布事件，后续操作由监听器处理\n");
        sb.append("        publisher.publishEvent(new UserRegisteredEvent(this, savedUser));\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 2.2 异步处理\n\n");
        sb.append("对于耗时的操作，可以使用异步监听器：\n\n");
        sb.append("```java\n");
        sb.append("@Component\n");
        sb.append("public class AsyncEventListener {\n");
        sb.append("    @Async  // 开启异步\n");
        sb.append("    @EventListener\n");
        sb.append("    public void handleEmail(UserRegisteredEvent event) {\n");
        sb.append("        // 异步执行，不阻塞主流程\n");
        sb.append("        emailService.send(event.getEmail());\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        sb.append("**注意**：需要在配置类上添加 `@EnableAsync` 注解\n\n");
        
        sb.append("## 三、同步 vs 异步监听器\n\n");
        sb.append("### 同步监听器\n\n");
        sb.append("```java\n");
        sb.append("@EventListener\n");
        sb.append("public void syncHandle(UserRegisteredEvent event) {\n");
        sb.append("    // 同步执行，阻塞主流程\n");
        sb.append("    // 优点：立即执行，数据一致性好\n");
        sb.append("    // 缺点：耗时长的操作会影响主流程性能\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 异步监听器\n\n");
        sb.append("```java\n");
        sb.append("@Async\n");
        sb.append("@EventListener\n");
        sb.append("public void asyncHandle(UserRegisteredEvent event) {\n");
        sb.append("    // 异步执行，不阻塞主流程\n");
        sb.append("    // 优点：提升主流程响应速度\n");
        sb.append("    // 缺点：需要处理线程池、异常等问题\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("## 四、事件机制的注意事项\n\n");
        sb.append("### 4.1 事务一致性\n\n");
        sb.append("**问题**：事件处理失败可能影响主事务\n\n");
        sb.append("**解决方案**：\n");
        sb.append("1. 使用 `@TransactionalEventListener` 在事务提交后处理\n");
        sb.append("```java\n");
        sb.append("@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)\n");
        sb.append("public void handleAfterCommit(UserRegisteredEvent event) {\n");
        sb.append("    // 事务提交后再执行，确保数据一致性\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 4.2 异常处理\n\n");
        sb.append("**问题**：监听器中的异常不应影响主流程\n\n");
        sb.append("**解决方案**：\n");
        sb.append("```java\n");
        sb.append("@EventListener\n");
        sb.append("public void handle(UserRegisteredEvent event) {\n");
        sb.append("    try {\n");
        sb.append("        // 业务逻辑\n");
        sb.append("    } catch (Exception e) {\n");
        sb.append("        log.error(\"事件处理失败\", e);\n");
        sb.append("        // 记录日志，不影响主流程\n");
        sb.append("    }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        
        sb.append("### 4.3 事件顺序\n\n");
        sb.append("如果有多个监听器，可以指定执行顺序：\n\n");
        sb.append("```java\n");
        sb.append("@Order(1)  // 优先级高\n");
        sb.append("@EventListener\n");
        sb.append("public void firstHandle(UserRegisteredEvent event) { }\n");
        sb.append("\n");
        sb.append("@Order(2)  // 优先级低\n");
        sb.append("@EventListener\n");
        sb.append("public void secondHandle(UserRegisteredEvent event) { }\n");
        sb.append("```\n\n");
        
        sb.append("## 五、事件机制 vs MQ\n\n");
        sb.append("| 特性 | Spring 事件机制 | MQ（消息队列） |\n");
        sb.append("|------|----------------|---------------|\n");
        sb.append("| **作用范围** | 应用内部（JVM 内） | 跨服务、跨系统 |\n");
        sb.append("| **持久化** | 不支持（内存） | 支持（磁盘） |\n");
        sb.append("| **可靠性** | 较低（应用重启丢失） | 高（可重试、确认） |\n");
        sb.append("| **性能** | 高（进程内调用） | 较低（网络开销） |\n");
        sb.append("| **使用场景** | 模块解耦、异步处理 | 服务解耦、削峰填谷 |\n\n");
        sb.append("**结论**：\n");
        sb.append("- Spring 事件机制适合**应用内**模块间的解耦\n");
        sb.append("- MQ 适合**微服务间**的异步通信\n");
        sb.append("- 两者不是替代关系，而是互补关系\n\n");
        
        sb.append("## 六、面试高频考点\n\n");
        
        sb.append("### Q1: Spring 事件机制的使用场景？\n\n");
        sb.append("**答**：\n");
        sb.append("1. **模块解耦**：如用户注册后发送邮件、积分、统计等\n");
        sb.append("2. **异步处理**：配合@Async 实现异步执行\n");
        sb.append("3. **扩展性**：新增功能无需修改原有代码（开闭原则）\n");
        sb.append("4. **审计日志**：记录关键操作日志\n\n");
        
        sb.append("### Q2: 事件机制和 MQ 的区别？\n\n");
        sb.append("**答**：\n");
        sb.append("1. **作用范围**：事件机制是应用内的，MQ 是跨服务的\n");
        sb.append("2. **持久化**：事件机制不持久化，MQ 支持持久化\n");
        sb.append("3. **可靠性**：MQ 提供更高的可靠性保证\n");
        sb.append("4. **使用场景**：事件机制用于模块解耦，MQ 用于服务解耦\n\n");
        
        sb.append("### Q3: 如何保证事件处理的可靠性？\n\n");
        sb.append("**答**：\n");
        sb.append("1. 使用 `@TransactionalEventListener` 在事务提交后处理\n");
        sb.append("2. 在监听器中添加异常处理，避免影响主流程\n");
        sb.append("3. 对于重要操作，使用 MQ 代替事件机制\n");
        sb.append("4. 添加日志记录和监控告警\n\n");
        
        sb.append("### Q4: @EventListener 和@TransactionalEventListener 的区别？\n\n");
        sb.append("**答**：\n");
        sb.append("- `@EventListener`：立即执行，可能在事务中\n");
        sb.append("- `@TransactionalEventListener`：在事务的特定阶段执行\n");
        sb.append("  - BEFORE_COMMIT：事务提交前\n");
        sb.append("  - AFTER_COMMIT：事务提交后（最常用）\n");
        sb.append("  - AFTER_ROLLBACK：事务回滚后\n");
        sb.append("  - AFTER_COMPLETION：事务完成后（无论成功或失败）\n\n");
        
        sb.append("## 七、总结\n\n");
        
        sb.append("**一句话记忆**：\n");
        sb.append("- 事件机制用于应用内模块解耦\n");
        sb.append("- 发布订阅模式，松耦合设计\n");
        sb.append("- 可同步可异步，但不等同于 MQ\n\n");
        
        sb.append("**核心要点**：\n");
        sb.append("1. 三要素：事件、发布器、监听器\n");
        sb.append("2. 适用场景：模块解耦、异步处理、扩展功能\n");
        sb.append("3. 支持同步和异步两种处理方式\n");
        sb.append("4. 注意事务一致性和异常处理\n");
        sb.append("5. 与 MQ 是互补关系，不是替代关系\n\n");
        
        return sb.toString();
    }
}
