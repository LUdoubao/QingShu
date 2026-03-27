package org.doubao.interview.agent.server.service.impl.rabbitmq.q013;

import com.rabbitmq.client.Channel;
import org.doubao.interview.agent.server.config.rabbitmq.q013.RabbitOrderedConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 问题 013(RabbitMQ)：单消费者串行消费示例。
 * 
 * <p>实现 RabbitMQ 消息顺序性的消费者侧核心逻辑，通过分片队列 + 单消费者保证局部有序：</p>
 * 
 * <h2>顺序性保障架构：</h2>
 * <ul>
 *     <li><strong>分片队列（Sharding Queue）</strong>：
 *         <ul>
 *             <li>创建两个分片队列：QUEUE_0 和 QUEUE_1</li>
 *             <li>每个队列绑定独立的 RoutingKey（RK_0 或 RK_1）</li>
 *             <li>相同 bizKey 的消息总是进入同一个固定队列</li>
 *         </ul>
 *     </li>
 *     <li><strong>单消费者配置（Single Consumer）</strong>：
 *         <ul>
 *             <li>每个队列仅配置一个消费者实例（concurrency=1）</li>
 *             <li>RabbitMQ 按 FIFO 顺序依次投递消息给消费者</li>
 *             <li>同一时刻只有一个线程处理该队列的消息，天然串行化</li>
 *         </ul>
 *     </li>
 *     <li><strong>局部有序 vs 全局有序</strong>：
 *         <ul>
 *             <li>局部有序：同 bizKey 的消息在单个队列内顺序消费（本方案实现）</li>
 *             <li>全局有序：所有消息严格按发送顺序消费（需单队列单消费者，性能极低）</li>
 *             <li>实际业务通常只需局部有序（如订单状态流转、支付流水更新）</li>
 *         </ul>
 *     </li>
 * </ul>
 * 
 * <h2>消费者设计原则：</h2>
 * <ul>
 *     <li><strong>手动 ACK 确认</strong>：业务处理成功后再发送 ACK，确保消息不丢失</li>
 *     <li><strong>失败 Reject 策略</strong>：参数校验失败时 Reject 且不重新入队（requeue=false）</li>
 *     <li><strong>无幂等检测</strong>：顺序性场景下，序号由生产者保证连续，消费者只负责串行处理</li>
 *     <li><strong>异常兜底处理</strong>：catch 块捕获所有未预期异常，防止消费者崩溃</li>
 * </ul>
 * 
 * <p><strong>为什么能保证顺序？</strong></p>
 * <ol>
 *     <li>生产者侧：同 bizKey → 同 RoutingKey → 同队列（固定路由）</li>
 *     <li>Broker 侧：RabbitMQ 队列是 FIFO 数据结构，先到的消息先被消费</li>
 *     <li>消费者侧：单消费者串行处理，不存在并发乱序问题</li>
 * </ol>
 * 
 * <p><strong>适用场景：</strong>订单状态机、支付流水更新、库存变更、评论回复链等需要保序的业务。</p>
 */
@Component
public class RabbitOrderedConsumer {

    /**
     * 可靠消息服务实现类，提供顺序记录、乱序检测等功能。
     * 由 Spring 容器注入，单例复用。
     */
    private final RabbitOrderedMessageServiceImpl orderedService;

    /**
     * 构造器注入 RabbitOrderedMessageServiceImpl。
     * 
     * @param orderedService 提供顺序记录、乱序检测等能力的服务实例
     */
    public RabbitOrderedConsumer(RabbitOrderedMessageServiceImpl orderedService) {
        this.orderedService = orderedService;
    }

    /**
     * 监听并消费分片队列 0（QUEUE_0）的消息。
     * 
     * <p>@RabbitListener 配置说明：</p>
     * <ul>
     *     <li>queues = RabbitOrderedConfig.QUEUE_0：监听指定的分片队列</li>
     *     <li>concurrency = "1"：单消费者配置，确保串行化处理</li>
     *     <li>Spring AMQP 自动反序列化消息并注入 Message 和 Channel 参数</li>
     * </ul>
     * 
     * <p><strong>为什么设置 concurrency=1？</strong></p>
     * <ul>
     *     <li>保证同一时刻只有一个线程处理该队列的消息</li>
     *     <li>避免多线程并发消费导致顺序错乱</li>
     *     <li>即使消息处理较慢，也能保证严格按序执行</li>
     * </ul>
     * 
     * @param message Spring AMQP 封装的消息对象，包含消息体、Header、属性等
     * @param channel RabbitMQ 通信频道，提供 ACK、Reject 等底层操作
     * @throws IOException 网络异常、Channel 关闭等 IO 相关错误
     */
    @RabbitListener(queues = RabbitOrderedConfig.QUEUE_0, concurrency = "1")
    public void consume0(Message message, Channel channel) throws IOException {
        // 委托给统一的 consume 方法处理，减少代码重复
        consume(message, channel);
    }

    /**
     * 监听并消费分片队列 1（QUEUE_1）的消息。
     * 
     * <p>与 consume0 对称，监听另一个分片队列，同样采用单消费者配置。</p>
     * <p>两个队列并行工作，不同 bizKey 可并行处理，提高整体吞吐量。</p>
     * 
     * @param message Spring AMQP 封装的消息对象，包含消息体、Header、属性等
     * @param channel RabbitMQ 通信频道，提供 ACK、Reject 等底层操作
     * @throws IOException 网络异常、Channel 关闭等 IO 相关错误
     */
    @RabbitListener(queues = RabbitOrderedConfig.QUEUE_1, concurrency = "1")
    public void consume1(Message message, Channel channel) throws IOException {
        // 委托给统一的 consume 方法处理，减少代码重复
        consume(message, channel);
    }

    /**
     * 统一的消息消费处理逻辑。
     * 
     * <p>完整的消费流程包括以下步骤：</p>
     * <ol>
     *     <li><strong>提取消息元数据</strong>：
     *         <ul>
     *             <li>deliveryTag：消息在 Channel 中的唯一标识，用于 ACK/Reject</li>
     *             <li>bizKey：从 Header 中提取的业务唯一标识，决定消息属于哪个业务分片</li>
     *             <li>seq：从 Header 中提取的消息序号，用于顺序检测和乱序识别</li>
     *         </ul>
     *     </li>
     *     <li><strong>参数校验</strong>：检查 bizKey 和 seq 是否存在
     *         <ul>
     *             <li>任一为 null：无效消息，直接 Reject 且不重新入队</li>
     *             <li>校验通过：继续后续处理</li>
     *         </ul>
     *     </li>
     *     <li><strong>类型转换</strong>：将 Header 值转换为对应的数据类型
     *         <ul>
     *             <li>bizKey：Object → String</li>
     *             <li>seq：Object → String → long（Long.parseLong）</li>
     *         </ul>
     *     </li>
     *     <li><strong>调用服务层</strong>：通知 orderedService 记录消费并检测乱序
     *         <ul>
     *             <li>onConsumed(bizKey, seq)：更新 lastSeqMap 和 disorderMap</li>
     *             <li>检测到乱序时标记 disorderMap=true，但不影响当前消息处理</li>
     *         </ul>
     *     </li>
     *     <li><strong>成功确认</strong>：调用 basicAck 告知 RabbitMQ 消息已成功处理</li>
     *     <li><strong>异常兜底</strong>：catch 块捕获所有未预期异常，Reject 消息防止阻塞</li>
     * </ol>
     * 
     * <p><strong>关键技术点：</strong></p>
     * <ul>
     *     <li>Header 提取：从 message.getMessageProperties().getHeaders() 获取生产者设置的元数据</li>
     *     <li>手动 ACK：关闭自动确认，业务处理成功后再发送 ACK，确保消息不丢失</li>
     *     <li>basicAck(tag, false)：单个确认（false=不批量确认），精确控制每条消息</li>
     *     <li>basicReject(tag, false)：拒绝消息且不重新入队（requeue=false），进入死信队列</li>
     *     <li>委托模式：consume0 和 consume1 都委托给 consume 方法，避免代码重复</li>
     * </ul>
     * 
     * @param message Spring AMQP 封装的消息对象，包含：
     *                - body: 消息体（bizKey=xxx|seq=N|payload=xxx|ts=时间戳）
     *                - headers: 消息头（bizKey、seq 等业务元数据）
     *                - properties: 消息属性（deliveryMode、correlationId 等）
     * @param channel RabbitMQ 通信频道，提供底层 AMQP 操作（ACK、Reject 等）
     * @throws IOException 网络异常、Channel 关闭、AMQP 协议错误等 IO 相关错误
     */
    private void consume(Message message, Channel channel) throws IOException {
        // ========== 第一步：提取消息元数据 ==========
        // deliveryTag: 消息在当前 Channel 中的唯一递增标识，从 1 开始
        // 注意：deliveryTag 是 Channel 级别的，不同 Channel 之间不共享
        long tag = message.getMessageProperties().getDeliveryTag();
        
        // 从 Header 中提取 bizKey（生产者在发送时已设置）
        // Header 方式比解析消息体更高效，且不受消息体格式影响
        Object bizKeyHeader = message.getMessageProperties().getHeaders().get("bizKey");
        
        // 从 Header 中提取 seq（消息序号，用于顺序检测）
        Object seqHeader = message.getMessageProperties().getHeaders().get("seq");
        
        // ========== 第二步：try-catch 包裹业务逻辑 ==========
        // 所有业务异常都应捕获并 Reject，防止消息丢失或消费者崩溃
        try {
            // ========== 参数校验 ==========
            // bizKey 或 seq 为空属于无效消息，无法进行顺序处理，直接 Reject 进入 DLQ
            if (bizKeyHeader == null || seqHeader == null) {
                // basicReject(deliveryTag, requeue):
                // - deliveryTag: 要拒绝的消息标识
                // - requeue: false=不重新入队（进入死信队列），true=重新入队（可能无限循环）
                channel.basicReject(tag, false);
                return;
            }
            
            // ========== 类型转换 ==========
            // 将 Header 中的 Object 值转换为实际数据类型
            String bizKey = String.valueOf(bizKeyHeader);  // Object → String
            long seq = Long.parseLong(String.valueOf(seqHeader));  // Object → String → long
            
            // ========== 调用服务层 ==========
            // 通知 orderedService 记录消费进度并检测是否乱序
            // onConsumed 内部逻辑：
            // 1. 更新 lastSeqMap：put(bizKey, seq)，记录最新消费序号
            // 2. 检测乱序：如果 seq <= lastSeq，标记 disorderMap=true
            orderedService.onConsumed(bizKey, seq);
            
            // ========== 成功确认 ==========
            // 业务处理完成，发送 ACK 确认
            // basicAck(deliveryTag, multiple):
            // - deliveryTag: 要确认的消息标识
            // - multiple: false=仅确认当前消息，true=批量确认当前及之前的所有消息
            channel.basicAck(tag, false);
            
        } catch (Exception ex) {
            // ========== 异常兜底处理 ==========
            // 捕获所有未预期的异常（如 NumberFormatException、空指针、业务异常等）
            // 处理策略：
            // 1. Reject 消息且不重新入队，让其进入死信队列等待人工处理
            // 2. 不打印日志（由全局异常处理器或上层统一记录）
            channel.basicReject(tag, false);
        }
    }
}
