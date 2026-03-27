package org.doubao.interview.agent.server.service.impl.q016.rabbitmq;

import com.rabbitmq.client.Channel;
import org.doubao.interview.agent.api.dto.q016.rabbitmq.RabbitDelayResponse;
import org.doubao.interview.agent.api.dto.q016.rabbitmq.RabbitDelaySendRequest;
import org.doubao.interview.agent.api.service.q016.rabbitmq.RabbitDelayDemoService;
import org.doubao.interview.agent.server.config.q016.rabbitmq.RabbitDelayConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问题 016(RabbitMQ)：延迟消息实现服务类。
 * <p>
 * 【核心功能】
 * 本服务提供 RabbitMQ 延迟消息的两种实现模式演示：
 * 1. ttl-dlx（TTL+ 死信交换器）：可运行的主方案。消息先进入延迟队列，设置 TTL 过期时间，
 *    到期后消息被死信交换器 (DLX) 自动转发到业务队列供消费者处理。
 * 2. plugin（x-delayed-message 插件）：RabbitMQ 官方延迟消息插件模式。
 *    当前示例环境默认不启用该插件，仅返回提示信息。
 * <p>
 * 【技术要点与注意事项】
 * - TTL+DLX 方案的局限性：在单队列场景下可能受到"队头阻塞"问题的影响，导致延迟精度下降。
 *   具体表现为：当一条长 TTL 消息位于队列头部时，即使后续消息的 TTL 已到期，也必须等待
 *   队头消息被消费或移除后才能被处理，这会影响后续短 TTL 消息的触发时机。
 * - 消息持久化：本示例中消息设置为持久化模式，确保 Broker 重启后消息不丢失。
 * - 手动 ACK：消费者采用手动确认模式，确保消息被可靠消费。
 */
@Service
public class RabbitDelayDemoServiceImpl implements RabbitDelayDemoService {

    /**
     * Spring AMQP 模板工具类，用于发送消息到 RabbitMQ 交换器。
     * 通过构造函数注入，确保线程安全。
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 内存状态追踪器：记录每条消息从发送到最终消费的生命周期状态。
     * 使用 ConcurrentHashMap 保证多线程环境下的线程安全性。
     * Key: 业务 ID (bizId)
     * Value: 消息当前状态字符串
     * <p>
     * 【状态流转说明】
     * - DELAY_ENQUEUED: 消息已成功写入延迟队列
     * - DELAY_DELIVERED_TO_BIZ_QUEUE: 消息已从延迟队列转发到业务队列（但尚未被消费）
     * - CONSUMED: 消息已被业务消费者成功处理
     * - UNKNOWN: 未找到对应业务 ID 的状态记录
     */
    private final Map<String, String> statusMap = new ConcurrentHashMap<>();

    /**
     * 构造函数注入 RabbitTemplate。
     * 
     * @param rabbitTemplate Spring AMQP 消息模板，由 Spring 容器自动注入
     */
    public RabbitDelayDemoServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送延迟消息的核心方法。
     * <p>
     * 【处理流程】
     * 1. 参数校验：检查业务 ID、消息内容和延迟时间是否有效
     * 2. 模式判断：当前仅支持 ttl-dlx 模式，plugin 模式暂不启用
     * 3. 消息构建：组装消息体，包含业务 ID、负载内容和发送时间戳
     * 4. 发送消息：通过 RabbitTemplate 将消息发送到延迟交换器
     * 5. 状态记录：在内存中记录消息状态为"已进入延迟队列"
     * <p>
     * 【TTL+DLX 模式的关键配置】
     * - x-expires 或 expiration: 设置消息的过期时间（毫秒），这是延迟效果的核心
     * - 消息持久化：防止 Broker 重启导致消息丢失
     * - bizId 头信息：用于后续追踪消息状态和业务识别
     * 
     * @param request 请求对象，包含以下字段：
     *                - bizId: 业务唯一标识符（必填，不能为空）
     *                - payload: 消息负载内容（必填，不能为空）
     *                - delayMillis: 延迟时间，单位为毫秒（必须大于 0）
     *                - mode: 延迟模式，可选值为 "ttl-dlx" 或 "plugin"，默认为 "ttl-dlx"
     * @return 响应对象，包含发送结果、当前阶段、状态信息等
     */
    @Override
    public RabbitDelayResponse send(RabbitDelaySendRequest request) {
        // ========== 步骤 1: 提取并校验请求参数 ==========
        // 空值保护：防止请求对象或字段为 null 导致 NPE
        String bizId = request == null ? null : request.getBizId();
        String payload = request == null ? null : request.getPayload();
        long delay = request == null ? 0 : request.getDelayMillis();
        // 模式参数默认值为 "ttl-dlx"，即 TTL+ 死信交换器方案
        String mode = request == null || request.getMode() == null ? "ttl-dlx" : request.getMode();
        
        // 严格参数校验：bizId 和 payload 不能为空字符串（允许空白字符），delay 必须为正数
        if (bizId == null || bizId.trim().isEmpty() || payload == null || payload.trim().isEmpty() || delay <= 0) {
            return build(false, "PARAM_VALIDATION", "bizId/payload 不能为空且 delayMillis>0", bizId, mode, delay, "INVALID");
        }

        // ========== 步骤 2: 模式检查 ==========
        if ("plugin".equalsIgnoreCase(mode)) {
            // 插件方案依赖：需要 RabbitMQ Broker 安装 rabbitmq_delayed_message_exchange 插件
            // 当前示例环境未启用该插件，因此返回提示信息
            return build(false, "PLUGIN_NOT_ENABLED", "当前示例未启用 x-delayed-message 插件，请改用 ttl-dlx 模式", bizId,
                    mode, delay, "PLUGIN_UNAVAILABLE");
        }

        // ========== 步骤 3: 构建消息体 ==========
        // 消息格式：key=value 形式拼接业务 ID、负载内容和发送时间戳（Unix 毫秒时间）
        // 发送时间戳用于后续计算实际延迟时间，验证延迟精度
        String body = "bizId=" + bizId + "|payload=" + payload + "|sendTs=" + Instant.now().toEpochMilli();
        
        // ========== 步骤 4: 发送消息到延迟队列 ==========
        // 使用 Lambda 表达式自定义消息属性
        rabbitTemplate.convertAndSend(RabbitDelayConfig.DELAY_EXCHANGE, RabbitDelayConfig.DELAY_ROUTING_KEY, body, message -> {
            // 设置消息持久化：Broker 重启后消息不会丢失，但会增加磁盘 IO 开销
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            // 添加业务 ID 到头信息：便于消费者识别和追踪消息
            message.getMessageProperties().setHeader("bizId", bizId);
            // 【核心配置】设置消息的 TTL（Time To Live），单位为毫秒
            // 这是 TTL+DLX 方案实现延迟效果的关键：消息在延迟队列中等待指定时间后才会过期
            // 过期消息会被死信交换器 (DLX) 捕获并转发到绑定的业务队列
            message.getMessageProperties().setExpiration(String.valueOf(delay));
            return message;
        });
        
        // ========== 步骤 5: 记录消息状态 ==========
        // 将消息状态标记为"已进入延迟队列"，供后续 inspect 方法查询
        statusMap.put(bizId, "DELAY_ENQUEUED");

        // 返回成功响应：消息已成功进入延迟队列，等待 TTL 到期后自动转发到业务队列
        return build(true, "TTL_DLX_SENT", "消息已进入延迟队列，过期后将转发到业务队列", bizId,
                mode, delay, "DELAY_ENQUEUED");
    }

    /**
     * 查询指定业务 ID 的消息状态。
     * 
     * 【用途】
     * 用于调试和监控，实时查看消息当前所处的处理阶段。
     * 
     * 【状态说明】
     * - DELAY_ENQUEUED: 消息在延迟队列中等待 TTL 到期
     * - DELAY_DELIVERED_TO_BIZ_QUEUE: 消息已到达业务队列，等待消费者处理
     * - UNKNOWN: 未找到对应的业务 ID 记录（可能是 bizId 错误或状态已被清理）
     * 
     * @param bizId 业务唯一标识符（必填，不能为空）
     * @return 响应对象，包含查询结果和当前状态
     */
    @Override
    public RabbitDelayResponse inspect(String bizId) {
        // 参数校验：bizId 不能为空
        if (bizId == null || bizId.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizId 不能为空", bizId, null, 0, "INVALID");
        }
        // 从内存状态表中查询，若不存在则返回 "UNKNOWN"
        String st = statusMap.getOrDefault(bizId, "UNKNOWN");
        return build(true, "INSPECT", "返回当前状态", bizId, "ttl-dlx", 0, st);
    }

    /**
     * 业务消息消费者：监听业务队列，处理从延迟队列转发过来的消息。
     * 
     * 【工作流程】
     * 1. 监听配置：通过 @RabbitListener 注解绑定到业务队列（RabbitDelayConfig.BIZ_QUEUE）
     * 2. 提取头信息：从消息头中获取 bizId，用于状态追踪
     * 3. 参数校验：若 bizId 为空，则拒绝该消息且不重新入队
     * 4. 更新状态：将消息状态标记为"已送达业务队列"
     * 5. 手动 ACK：确认消息已成功处理，从队列中移除
     * 
     * 【关键技术点】
     * - Channel 基本操作：
     *   - basicAck(tag, false): 确认单条消息，第二个参数 false 表示不批量确认
     *   - basicReject(tag, false): 拒绝单条消息，第二个参数 false 表示不重新入队
     * - 手动 ACK 模式：确保消息可靠性，防止消费者宕机导致消息丢失
     * - 异常处理：方法抛出 IOException，交由 Spring 的重试机制处理
     * 
     * @param message Spring AMQP 消息对象，包含消息体、头信息和属性
     * @param channel RabbitMQ 信道对象，用于执行 ACK/REJECT 等操作
     * @throws IOException 当网络通信或协议错误时抛出
     */
    @RabbitListener(queues = RabbitDelayConfig.BIZ_QUEUE)
    public void consumeBiz(Message message, Channel channel) throws IOException {
        // ========== 步骤 1: 获取消息递送标签 ==========
        // deliveryTag 是消息在当前信道中的唯一标识，用于后续的 ACK/REJECT 操作
        // 注意：deliveryTag 不是全局唯一的，仅在单个信道内有效
        long tag = message.getMessageProperties().getDeliveryTag();
        
        // ========== 步骤 2: 提取业务 ID ==========
        // 从消息头信息中获取 bizId，这是发送时自定义的头信息
        Object bizIdHeader = message.getMessageProperties().getHeaders().get("bizId");
        String bizId = bizIdHeader == null ? null : String.valueOf(bizIdHeader);
        
        // ========== 步骤 3: 校验业务 ID ==========
        // 如果 bizId 为空，说明消息格式异常，直接拒绝且不重新入队
        if (bizId == null || bizId.trim().isEmpty()) {
            // basicReject(tag, false): 拒绝消息，false 表示不重新入队（避免无限循环）
            channel.basicReject(tag, false);
            return;
        }
        
        // ========== 步骤 4: 更新消息状态 ==========
        // 标记消息已从延迟队列转发到业务队列（注意：此时还未完成业务处理）
        statusMap.put(bizId, "DELAY_DELIVERED_TO_BIZ_QUEUE");
        
        // ========== 步骤 5: 手动确认消息 ==========
        // basicAck(tag, false): 确认单条消息已成功消费，从队列中永久删除
        // 第二个参数 false 表示不使用批量确认模式
        channel.basicAck(tag, false);
    }

    /**
     * 统一响应构建器方法。
     * 
     * 【设计目的】
     * 封装 RabbitDelayResponse 对象的创建逻辑，确保响应格式一致性，
     * 同时减少代码重复。
     * 
     * @param success 操作是否成功（true=成功，false=失败）
     * @param stage 当前处理阶段或错误类型标识，例如：
     *              - PARAM_VALIDATION: 参数校验失败
     *              - PLUGIN_NOT_ENABLED: 插件模式未启用
     *              - TTL_DLX_SENT: 消息已成功发送到延迟队列
     *              - INSPECT: 状态查询操作
     * @param message 人类可读的描述信息，用于说明结果或错误原因
     * @param bizId 业务唯一标识符
     * @param mode 使用的延迟模式（"ttl-dlx" 或 "plugin"）
     * @param delay 延迟时间（毫秒）
     * @param status 消息当前状态，例如：
     *               - DELAY_ENQUEUED: 已进入延迟队列
     *               - DELAY_DELIVERED_TO_BIZ_QUEUE: 已送达业务队列
     *               - UNKNOWN: 未知状态
     *               - INVALID: 参数无效
     * @return 封装好的响应对象
     */
    private RabbitDelayResponse build(boolean success, String stage, String message, String bizId,
                                      String mode, long delay, String status) {
        // 创建响应对象并填充所有字段
        RabbitDelayResponse r = new RabbitDelayResponse();
        r.setSuccess(success);          // 设置操作成功标志
        r.setStage(stage);              // 设置当前阶段标识
        r.setMessage(message);          // 设置描述信息
        r.setBizId(bizId);              // 设置业务 ID
        r.setMode(mode);                // 设置延迟模式
        r.setDelayMillis(delay);        // 设置延迟时间（毫秒）
        r.setStatus(status);            // 设置消息状态
        return r;
    }
}
