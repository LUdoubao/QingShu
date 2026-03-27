package org.doubao.interview.agent.server.service.impl.rabbitmq.q009;

import org.doubao.interview.agent.api.dto.rabbitmq.q009.RabbitReliableResponse;
import org.doubao.interview.agent.api.dto.rabbitmq.q009.RabbitReliableSendRequest;
import org.doubao.interview.agent.api.service.rabbitmq.q009.RabbitReliableMessageService;
import org.doubao.interview.agent.server.config.rabbitmq.q009.RabbitReliableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问题 009(RabbitMQ)：可靠消息传递示例实现。
 * 
 * <p>实现消息可靠传递的完整链路，涵盖生产者、Broker、消费者三个环节：</p>
 * 
 * <h2>生产者可靠性保障（生产侧）：</h2>
 * <ul>
 *     <li><strong>Confirm 确认机制</strong>：RabbitMQ 收到消息后发送 ACK 确认，若失败则返回 NACK 和原因</li>
 *     <li><strong>Return 回退机制</strong>：当消息无法路由到任何队列时，触发 Return 回调将消息返回给生产者</li>
 *     <li><strong>CorrelationData 关联数据</strong>：为每条消息生成唯一 ID，用于追踪 Confirm 状态</li>
 *     <li><strong>重试/补偿预留</strong>：当 Confirm 失败时，可实现重试逻辑或将消息写入补偿表</li>
 * </ul>
 * 
 * <h2>Broker 可靠性保障（中间件侧）：</h2>
 * <ul>
 *     <li><strong>队列持久化</strong>：在 RabbitReliableConfig 中声明队列时设置 durable=true</li>
 *     <li><strong>消息持久化</strong>：发送消息时设置 deliveryMode=2(PERSISTENT)，确保消息写入磁盘</li>
 *     <li><strong>死信队列（DLQ）</strong>：消费失败的消息进入死信队列，便于后续排查和重试</li>
 *     <li><strong>Exchange 持久化</strong>：交换机也需持久化，防止重启后丢失</li>
 * </ul>
 * 
 * <h2>消费者可靠性保障（消费侧）：</h2>
 * <ul>
 *     <li><strong>手动 ACK</strong>：关闭自动确认，业务处理成功后再手动发送 ACK</li>
 *     <li><strong>失败 Reject</strong>：业务处理失败时调用 basicReject(requeue=false)，让消息进入 DLQ</li>
 *     <li><strong>幂等去重</strong>：通过 bizId 标识业务唯一性，重复消息直接 ACK 跳过，避免重复消费</li>
 * </ul>
 * 
 * <p><strong>核心设计思想：</strong>端到端确认 + 故障隔离 + 幂等性保障</p>
 */
@Service
public class RabbitReliableMessageServiceImpl implements RabbitReliableMessageService {

    /**
     * 日志记录器，用于记录消息发送、Confirm 状态、消费异常等关键信息。
     */
    private static final Logger log = LoggerFactory.getLogger(RabbitReliableMessageServiceImpl.class);

    /**
     * Spring AMQP 的 RabbitTemplate，封装了消息发送的核心逻辑。
     * 线程安全，由 Spring 容器注入，可复用。
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 生产者 Confirm/Return 状态存储（线程安全的 ConcurrentHashMap）。
     * Key: CorrelationData ID（消息唯一标识）
     * Value: 状态值（"ACK"-已确认、"NACK"-确认失败、"RETURNED"-被回退、"PENDING"-待确认）
     * 
     * <p>用途：异步跟踪每条消息的发送结果，供前端展示或调试使用。</p>
     */
    private final Map<String, String> producerStatus = new ConcurrentHashMap<String, String>();
    
    /**
     * 消费者处理状态存储（线程安全的 ConcurrentHashMap）。
     * Key: 业务 ID（bizId）
     * Value: 消费状态（"CONSUMED_ACKED"-已消费确认、"FAILED_TO_DLQ"-失败进入死信队列、"DUPLICATED_SKIPPED"-重复消息跳过）
     * 
     * <p>用途：追踪消息的消费结果，支持 inspect 方法查询完整链路状态。</p>
     */
    private final Map<String, String> consumerStatus = new ConcurrentHashMap<String, String>();
    
    /**
     * 幂等性记录集合（线程安全的 ConcurrentHashMap）。
     * Key: 业务 ID（bizId）
     * Value: Boolean.TRUE（仅作为 Set 使用，Value 无实际意义）
     * 
     * <p>用途：记录已处理过的 bizId，防止同一条消息被重复消费造成业务逻辑错误。</p>
     * <p>原理：putIfAbsent 原子操作，首次放入返回 null，后续重复放入返回已存在的值。</p>
     */
    private final Map<String, Boolean> idempotentSet = new ConcurrentHashMap<String, Boolean>();

    /**
     * 构造器注入 RabbitTemplate。
     * 
     * @param rabbitTemplate Spring 提供的 RabbitMQ 模板，封装了消息发送、回调设置等核心功能
     */
    public RabbitReliableMessageServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 初始化 Producer Confirm 和 Return 回调监听器。
     * 
     * <p>在 Spring Bean 初始化完成后（@PostConstruct）执行，注册异步回调函数：</p>
     * <ol>
     *     <li><strong>ConfirmCallback</strong>：监听 RabbitMQ 对消息的确认结果（ACK/NACK）</li>
     *     <li><strong>ReturnsCallback</strong>：监听消息无法路由时的回退事件</li>
     * </ol>
     * 
     * <p><strong>触发时机：</strong></p>
     * <ul>
     *     <li>Confirm：消息到达 Exchange 并成功路由到 Queue 后触发</li>
     *     <li>Return：消息无法路由到任何 Queue（如 RoutingKey 不匹配）时触发</li>
     * </ul>
     * 
     * <p><strong>注意事项：</strong></p>
     * <ul>
     *     <li>回调是异步执行的，不能依赖其返回值决定后续业务逻辑</li>
     *     <li>NACK 或 Return 发生时，应记录日志并考虑重试或补偿</li>
     *     <li>CorrelationData 用于关联原始消息和确认结果，必须设置</li>
     * </ul>
     */
    @PostConstruct
    public void initCallbacks() {
        // ========== 设置 Confirm 确认回调 ==========
        // 当消息到达 Exchange 并成功路由到 Queue 后，RabbitMQ 会发送 Confirm 确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // correlationData: 发送消息时传入的关联数据，用于匹配原始消息
            // ack: true 表示 RabbitMQ 已接收（ACK），false 表示拒绝接收（NACK）
            // cause: NACK 时的原因描述（如 broker 宕机、队列不存在等）
            String cid = correlationData == null ? "unknown" : correlationData.getId();
            producerStatus.put(cid, ack ? "ACK" : "NACK");
            
            if (!ack) {
                // NACK 场景：Broker 内部错误、队列未就绪、资源不足等
                // 处理策略：记录警告日志，可结合重试机制或写入补偿表
                log.warn("q009 confirm nack, cid={}, cause={}", cid, cause);
            }
            // ACK 场景：消息已成功到达 Broker，无需额外处理
        });

        // ========== 设置 Return 回退回调 ==========
        // 当消息无法路由到任何 Queue 时（如 RoutingKey 不匹配），触发 Return 回调
        rabbitTemplate.setReturnsCallback(returned -> {
            // returned: 包含被回退的消息及其属性（Exchange、RoutingKey、回复码等）
            // 常见回复码：312(AMQP_NO_ROUTE)、313(AMQP_NO_CONSUMERS)
            String cid = returned.getMessage().getMessageProperties().getCorrelationId();
            producerStatus.put(cid == null ? "unknown" : cid, "RETURNED");
            // RETURNED 场景：消息已到达 Exchange，但找不到匹配的 Queue
            // 处理策略：记录日志，检查 RoutingKey 配置，或重新发送修正后的消息
        });
    }

    /**
     * 发送可靠消息到 RabbitMQ，并返回当前链路状态。
     * 
     * <p>完整的发送流程包括以下步骤：</p>
     * <ol>
     *     <li><strong>参数校验</strong>：检查 bizId 和 payload 是否为空，避免发送无效消息</li>
     *     <li><strong>生成 CorrelationData</strong>：使用 UUID 生成消息唯一标识，用于追踪 Confirm 状态</li>
     *     <li><strong>构建消息体</strong>：payload + bizId + 时间戳，便于追踪和幂等校验</li>
     *     <li><strong>设置消息属性</strong>：
     *         <ul>
     *             <li>deliveryMode=PERSISTENT(2)：消息持久化到磁盘</li>
     *             <li>correlationId：关联数据 ID，匹配 Confirm 回调</li>
     *             <li>bizId Header：业务唯一标识，供消费者进行幂等校验</li>
     *         </ul>
     *     </li>
     *     <li><strong>发送消息</strong>：调用 convertAndSend 发送到指定 Exchange 和 RoutingKey</li>
     *     <li><strong>返回状态</strong>：返回 Producer Confirm 状态、Consumer 状态、幂等标记</li>
     * </ol>
     * 
     * <p><strong>关键技术点：</strong></p>
     * <ul>
     *     <li>CorrelationData 必须在发送时传入，才能在 Confirm 回调中获取对应关系</li>
     *     <li>MessagePostProcessor 用于在发送前拦截并修改消息属性，采用 Lambda 简化代码</li>
     *     <li>bizId 同时存在于消息体（便于查看）和 Header（便于消费者提取），双重冗余</li>
     *     <li>时间戳使用 Instant.now().toEpochMilli() 生成毫秒级纪元时间，精确追踪发送时刻</li>
     * </ul>
     * 
     * @param request 请求对象，包含业务 ID（bizId）和消息负载（payload）
     * @return 响应对象，包含：
     *         - success: 发送是否成功（true=参数校验通过且已发送，false=参数校验失败）
     *         - stage: 当前阶段（PARAM_VALIDATION 或 PRODUCER_SENT）
     *         - producerStatus: Producer Confirm 状态（PENDING/ACK/NACK/RETURNED）
     *         - consumerStatus: Consumer 处理状态（PENDING/CONSUMED_ACKED/FAILED_TO_DLQ/DUPLICATED_SKIPPED）
     *         - idempotentApplied: 是否命中幂等（true=该 bizId 已被处理过）
     */
    @Override
    public RabbitReliableResponse send(RabbitReliableSendRequest request) {
        // ========== 第一步：参数校验 ==========
        // 从请求对象中提取 bizId 和 payload，任一为空或空白则拒绝发送
        String bizId = request == null ? null : request.getBizId();
        String payload = request == null ? null : request.getPayload();

        if (bizId == null || bizId.trim().isEmpty() || payload == null || payload.trim().isEmpty()) {
            // 参数校验失败，直接返回错误响应，不发送消息
            return build(false, "PARAM_VALIDATION", "bizId/payload 不能为空", bizId,
                    "UNKNOWN", "UNKNOWN", false);
        }

        // ========== 第二步：生成 CorrelationData ==========
        // 使用 UUID 生成全局唯一的消息标识符，用于关联发送消息和 Confirm 回调
        // UUID 格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx（36 字符）
        String cid = UUID.randomUUID().toString();
        CorrelationData cd = new CorrelationData(cid);

        // ========== 第三步：构建并发送消息 ==========
        // 消息体结构：payload + "|bizId=" + bizId + "|ts=" + 时间戳
        // 好处：既方便人工查看消息内容，又能在消费者端快速提取 bizId 进行幂等校验
        rabbitTemplate.convertAndSend(
                RabbitReliableConfig.EXCHANGE,           // 目标交换机名称（已在配置类中声明为持久化）
                RabbitReliableConfig.ROUTING_KEY,        // 路由键，决定消息路由到哪个队列
                payload + "|bizId=" + bizId + "|ts=" + Instant.now().toEpochMilli(),  // 消息体
                message -> {
                    // MessagePostProcessor：在消息发送前拦截并设置属性
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);  // 消息持久化（deliveryMode=2）
                    message.getMessageProperties().setCorrelationId(cid);  // 设置关联 ID，用于 Confirm 回调匹配
                    message.getMessageProperties().setHeader("bizId", bizId);  // 设置业务 ID Header，供消费者幂等校验使用
                    return message;
                },
                cd  // 传入 CorrelationData，建立消息与 Confirm 回调的关联关系
        );

        // ========== 第四步：构建响应 ==========
        // 返回当前已知的状态信息（Confirm 和消费状态是异步更新的，此处可能还是 PENDING）
        return build(true, "PRODUCER_SENT", "消息已发送，confirm/消费状态将异步更新", bizId,
                producerStatus.getOrDefault(cid, "PENDING"),          // Producer Confirm 状态
                consumerStatus.getOrDefault(bizId, "PENDING"),        // Consumer 处理状态
                idempotentSet.containsKey(bizId));                    // 是否已存在幂等记录
    }

    /**
     * 查询指定 bizId 的消息链路状态。
     * 
     * <p>用于追踪消息的完整生命周期，包括：</p>
     * <ul>
     *     <li>Producer Confirm 状态（通过 CorrelationData ID 查询）</li>
     *     <li>Consumer 处理状态（通过 bizId 查询）</li>
     *     <li>幂等性标记（是否已被处理过）</li>
     * </ul>
     * 
     * <p><strong>使用场景：</strong></p>
     * <ul>
     *     <li>前端轮询查看消息处理进度</li>
     *     <li>运维排查消息丢失问题</li>
     *     <li>调试验证可靠消息传递机制</li>
     * </ul>
     * 
     * @param bizId 业务唯一标识
     * @return 响应对象，包含当前链路状态信息
     */
    @Override
    public RabbitReliableResponse inspect(String bizId) {
        if (bizId == null || bizId.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizId 不能为空", bizId,
                    "UNKNOWN", "UNKNOWN", false);
        }
        
        // 获取消费者处理状态（Producer Confirm 状态需通过 CorrelationData ID 查询，此处不展示）
        String consumer = consumerStatus.getOrDefault(bizId, "PENDING");
        
        return build(true, "INSPECT", "返回当前链路状态", bizId,
                "SEE_LOG_OR_CORRELATION",  // Producer Confirm 状态需查看日志或通过 CorrelationData 关联
                consumer,                   // Consumer 处理状态
                idempotentSet.containsKey(bizId));  // 是否已应用幂等性
    }

    /**
     * 标记消息已被消费（供消费者调用）。
     * 
     * <p>消费者处理成功后调用此方法，记录消费状态到 consumerStatus Map。</p>
     * 
     * @param bizId 业务唯一标识
     * @param duplicated 是否为重复消息（true=幂等跳过，false=正常消费）
     */
    void markConsumed(String bizId, boolean duplicated) {
        // duplicated=true：幂等去重，跳过业务处理但仍 ACK
        // duplicated=false：正常消费，执行完业务逻辑后 ACK
        consumerStatus.put(bizId, duplicated ? "DUPLICATED_SKIPPED" : "CONSUMED_ACKED");
    }

    /**
     * 标记消息消费失败（供消费者调用）。
     * 
     * <p>消费者业务处理失败时调用此方法，记录失败状态，消息将被 Reject 并进入死信队列。</p>
     * 
     * @param bizId 业务唯一标识
     */
    void markConsumeFailed(String bizId) {
        // FAILED_TO_DLQ：Failed To Dead Letter Queue，失败进入死信队列
        consumerStatus.put(bizId, "FAILED_TO_DLQ");
    }

    /**
     * 幂等性检查：判断是否为首次消费该消息。
     * 
     * <p>使用 ConcurrentHashMap 的 putIfAbsent 原子操作实现线程安全的幂等校验：</p>
     * <ul>
     *     <li>首次调用：putIfAbsent 返回 null，表示之前未处理过，返回 true 允许执行</li>
     *     <li>重复调用：putIfAbsent 返回已存在的 Boolean.TRUE，表示已处理过，返回 false 跳过</li>
     * </ul>
     * 
     * <p><strong>线程安全性：</strong></p>
     * <p>ConcurrentHashMap 的 putIfAbsent 是 CAS 原子操作，保证多线程并发消费同一消息时，
     * 只有一个线程能成功放入，其他线程都会检测到已存在，从而实现幂等去重。</p>
     * 
     * @param bizId 业务唯一标识
     * @return true=首次消费，执行业务逻辑；false=重复消息，跳过处理
     */
    boolean markIfFirstConsume(String bizId) {
        // putIfAbsent(key, value): 若 key 不存在则放入并返回 null；若 key 已存在则返回原值
        return idempotentSet.putIfAbsent(bizId, Boolean.TRUE) == null;
    }

    /**
     * 构建统一的响应对象。
     * 
     * <p>封装 RabbitReliableResponse 的创建逻辑，减少代码重复。</p>
     * 
     * @param success 操作是否成功
     * @param stage 当前处理阶段（PARAM_VALIDATION/PRODUCER_SENT/INSPECT）
     * @param message 响应消息描述
     * @param bizId 业务 ID
     * @param pStatus Producer Confirm 状态
     * @param cStatus Consumer 处理状态
     * @param idempotentApplied 是否命中幂等
     * @return 构建好的响应对象
     */
    private RabbitReliableResponse build(boolean success, String stage, String message, String bizId,
                                         String pStatus, String cStatus, boolean idempotentApplied) {
        RabbitReliableResponse r = new RabbitReliableResponse();
        r.setSuccess(success);
        r.setStage(stage);
        r.setMessage(message);
        r.setBizId(bizId);
        r.setProducerStatus(pStatus);
        r.setConsumerStatus(cStatus);
        r.setIdempotentApplied(idempotentApplied);
        return r;
    }
}
