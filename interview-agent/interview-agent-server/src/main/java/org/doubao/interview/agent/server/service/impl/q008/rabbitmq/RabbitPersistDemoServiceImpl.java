package org.doubao.interview.agent.server.service.impl.q008.rabbitmq;

import org.doubao.interview.agent.api.dto.q008.rabbitmq.RabbitPersistDemoRequest;
import org.doubao.interview.agent.api.dto.q008.rabbitmq.RabbitPersistDemoResponse;
import org.doubao.interview.agent.api.service.q008.rabbitmq.RabbitPersistDemoService;
import org.doubao.interview.agent.server.config.q008.rabbitmq.RabbitPersistConfig;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 问题 008(RabbitMQ)：消息持久化示例实现。
 * 
 * <p>RabbitMQ 消息持久化需要同时满足三个条件：</p>
 * <ul>
 *     <li><strong>Exchange 持久化</strong>：声明 Exchange 时设置 durable=true，确保 Exchange 在 RabbitMQ 服务器重启后仍然存在</li>
 *     <li><strong>Queue 持久化</strong>：声明 Queue 时设置 durable=true，确保队列在服务器重启后不会丢失</li>
 *     <li><strong>消息持久化</strong>：发送消息时设置 deliveryMode=2(PERSISTENT)，确保消息内容被写入磁盘</li>
 * </ul>
 * 
 * <p><strong>注意事项：</strong></p>
 * <ul>
 *     <li>即使设置了持久化，RabbitMQ 也不能保证 100% 不丢消息，因为消息从到达交换机到写入磁盘之间存在短暂的时间窗口</li>
 *     <li>如需更高的可靠性，可结合使用 Confirm 机制和事务机制</li>
 *     <li>持久化会降低性能，因为涉及磁盘 I/O 操作，适用于对可靠性要求较高的场景</li>
 * </ul>
 */
@Service
public class RabbitPersistDemoServiceImpl implements RabbitPersistDemoService {

    /**
     * Spring AMQP 的 RabbitTemplate，用于发送和接收 RabbitMQ 消息。
     * 线程安全，可复用，由 Spring 容器注入。
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造器注入 RabbitTemplate。
     * 
     * @param rabbitTemplate Spring 提供的 RabbitMQ 模板，封装了消息发送的核心逻辑
     */
    public RabbitPersistDemoServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送持久化消息到 RabbitMQ。
     * 
     * <p>完整的消息持久化流程包括以下步骤：</p>
     * <ol>
     *     <li><strong>参数校验</strong>：检查请求对象和消息内容是否为空，避免发送无效消息</li>
     *     <li><strong>配置 Exchange 和 Queue</strong>：在 RabbitPersistConfig 中已声明持久化的交换机和队列（durable=true）</li>
     *     <li><strong>消息标记持久化</strong>：通过 MessagePostProcessor 设置消息的 deliveryMode 为 PERSISTENT(值为 2)</li>
     *     <li><strong>发送消息</strong>：使用 convertAndSend 方法将消息发送到指定的 Exchange 和 RoutingKey</li>
     *     <li><strong>返回结果</strong>：构建响应对象，包含持久化配置的详细信息</li>
     * </ol>
     * 
     * <p><strong>关键技术点：</strong></p>
     * <ul>
     *     <li>MessageDeliveryMode.PERSISTENT 的值为 2，表示消息应被持久化到磁盘</li>
     *     <li>MessageDeliveryMode.NON_PERSISTENT 的值为 1，表示消息仅保存在内存中</li>
     *     <li>使用 Lambda 表达式作为 MessagePostProcessor 来定制消息属性，简化代码</li>
     *     <li>在消息末尾添加时间戳（毫秒级），用于演示消息的发送时间和区分不同消息</li>
     * </ul>
     * 
     * @param request 请求对象，包含要发送的消息内容
     * @return 响应对象，包含发送结果、持久化配置信息和阶段标识
     *         - success: true 表示发送成功，false 表示参数校验失败
     *         - stage: 当前处理阶段（PARAM_VALIDATION 或 PERSISTENT_SENT）
     *         - exchangeDurable/queueDurable/deliveryMode: 持久化配置详情
     */
    @Override
    public RabbitPersistDemoResponse sendPersistent(RabbitPersistDemoRequest request) {
        // ========== 第一步：参数校验 ==========
        // 从请求对象中提取消息内容，允许 request 为 null，但 message 不能为空
        String msg = request == null ? null : request.getMessage();
        
        // 严格校验：message 为 null 或仅包含空白字符时拒绝发送
        if (msg == null || msg.trim().isEmpty()) {
            RabbitPersistDemoResponse fail = new RabbitPersistDemoResponse();
            fail.setSuccess(false);
            fail.setStage("PARAM_VALIDATION");  // 标记失败阶段为参数校验
            fail.setMessage("message 不能为空");
            return fail;
        }

        // ========== 第二步：发送持久化消息 ==========
        // 调用链：RabbitPersistConfig（已配置持久的 Exchange + Queue）
        //       + convertAndSend 第三个参数（消息本身）
        //       + 第四个参数 MessagePostProcessor（设置 deliveryMode=2）
        // 
        // 完整持久化三要素：
        // 1. Exchange 持久化：在 RabbitPersistConfig 中声明时设置 durable=true
        // 2. Queue 持久化：在 RabbitPersistConfig 中声明时设置 durable=true
        // 3. 消息持久化：此处通过 setDeliveryMode(MessageDeliveryMode.PERSISTENT) 实现
        rabbitTemplate.convertAndSend(
                RabbitPersistConfig.EXCHANGE,      // 目标交换机名称（已在配置类中声明为持久化）
                RabbitPersistConfig.ROUTING_KEY,   // 路由键，决定消息路由到哪个队列
                msg + " @" + Instant.now().toEpochMilli(),  // 消息体 + 时间戳（毫秒级纪元时间）
                message -> {
                    // MessagePostProcessor 函数式接口：在消息发送前拦截并修改消息属性
                    // 设置消息投递模式为 PERSISTENT(值为 2)，指示 RabbitMQ 将消息写入磁盘
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );

        // ========== 第三步：构建成功响应 ==========
        // 返回持久化配置的完整信息，用于前端展示或调试验证
        RabbitPersistDemoResponse ok = new RabbitPersistDemoResponse();
        ok.setSuccess(true);
        ok.setStage("PERSISTENT_SENT");  // 标记当前阶段为"已发送持久化消息"
        ok.setMessage("已发送持久化消息：exchange durable + queue durable + deliveryMode=2");
        
        // 详细配置信息：三个持久化指标均为 true/2，表示完整的消息持久化已启用
        ok.setExchangeDurable(true);       // Exchange 持久化标志
        ok.setQueueDurable(true);          // Queue 持久化标志
        ok.setDeliveryMode(2);             // 消息投递模式：2=PERSISTENT（持久化），1=NON_PERSISTENT（非持久化）
        
        return ok;
    }
}
