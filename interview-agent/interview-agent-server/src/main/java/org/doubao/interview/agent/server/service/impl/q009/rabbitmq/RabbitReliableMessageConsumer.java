package org.doubao.interview.agent.server.service.impl.q009.rabbitmq;

import com.rabbitmq.client.Channel;
import org.doubao.interview.agent.server.config.q009.rabbitmq.RabbitReliableConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 问题 009(RabbitMQ)：可靠消息消费者示例。
 * 
 * <p>实现可靠消息消费的核心策略，确保消息不丢失、不重复消费：</p>
 * 
 * <h2>三大核心机制：</h2>
 * <ul>
 *     <li><strong>手动 ACK 确认</strong>：
 *         <ul>
 *             <li>关闭 Spring AMQP 的自动确认模式（acknowledge-mode: manual）</li>
 *             <li>业务逻辑处理成功后，手动调用 channel.basicAck() 发送确认</li>
 *             <li>只有收到 ACK 后，RabbitMQ 才会从队列中删除该消息</li>
 *         </ul>
 *     </li>
 *     <li><strong>失败 Reject 进入死信队列</strong>：
 *         <ul>
 *             <li>业务处理失败时，调用 channel.basicReject(tag, false)</li>
 *             <li>requeue=false 表示不再重新入队，避免无限循环重试</li>
 *             <li>配合 DLX（Dead Letter Exchange）配置，消息自动路由到死信队列</li>
 *             <li>死信队列中的消息可人工排查或定时重试</li>
 *         </ul>
 *     </li>
 *     <li><strong>幂等性去重</strong>：
 *         <ul>
 *             <li>通过 bizId（业务唯一标识）判断是否为重复消息</li>
 *             <li>首次消费：执行业务逻辑并标记为已处理</li>
 *             <li>重复消息：直接 ACK 跳过，避免重复扣款、重复下单等问题</li>
 *             <li>使用 ConcurrentHashMap 的 putIfAbsent 保证线程安全</li>
 *         </ul>
 *     </li>
 * </ul>
 * 
 * <p><strong>异常处理原则：</strong></p>
 * <ul>
 *     <li>参数校验失败 → basicReject(requeue=false) → 进入 DLQ</li>
 *     <li>业务逻辑失败 → basicReject(requeue=false) → 进入 DLQ</li>
 *     <li>系统异常（如数据库超时）→ basicReject(requeue=false) → 进入 DLQ + 记录日志</li>
 *     <li>重复消息 → 直接 ACK → 跳过处理</li>
 * </ul>
 * 
 * <p><strong>为什么不用 requeue=true？</strong></p>
 * <p>如果 requeue=true，失败消息会重新回到队列并被再次消费，可能导致：</p>
 * <ul>
 *     <li>无限循环重试（特别是永久性错误）</li>
 *     <li>阻塞后续正常消息的处理</li>
 *     <li>造成队列拥堵</li>
 * </ul>
 * <p>正确做法：requeue=false 让消息进入 DLQ，人工介入或定时任务处理。</p>
 */
@Component
public class RabbitReliableMessageConsumer {

    /**
     * 日志记录器，用于记录消息接收、业务处理、异常情况等关键信息。
     */
    private static final Logger log = LoggerFactory.getLogger(RabbitReliableMessageConsumer.class);

    /**
     * 可靠消息服务实现类，提供幂等校验和状态标记功能。
     * 由 Spring 容器注入，单例复用。
     */
    private final RabbitReliableMessageServiceImpl reliableService;

    /**
     * 构造器注入 RabbitReliableMessageServiceImpl。
     * 
     * @param reliableService 提供幂等检查、状态标记等能力的服务实例
     */
    public RabbitReliableMessageConsumer(RabbitReliableMessageServiceImpl reliableService) {
        this.reliableService = reliableService;
    }

    /**
     * 监听并消费 RabbitMQ 队列中的消息。
     * 
     * <p>完整的消息消费流程包括以下步骤：</p>
     * <ol>
     *     <li><strong>获取消息元数据</strong>：
     *         <ul>
     *             <li>deliveryTag：消息在 Channel 中的唯一标识，用于 ACK/Reject</li>
     *             <li>bizId：从 Header 中提取的业务唯一标识，用于幂等校验</li>
     *             <li>body：消息体内容（UTF-8 解码）</li>
     *         </ul>
     *     </li>
     *     <li><strong>参数校验</strong>：检查 bizId 是否为空，无效消息直接 Reject 进入 DLQ</li>
     *     <li><strong>幂等去重</strong>：调用 markIfFirstConsume 检查是否为重复消息
     *         <ul>
     *             <li>首次消费：继续执行后续业务逻辑</li>
     *             <li>重复消息：标记为 DUPLICATED_SKIPPED 并 ACK，跳过处理</li>
     *         </ul>
     *     </li>
     *     <li><strong>业务失败模拟</strong>：检测消息体是否包含 "FAIL" 关键字
     *         <ul>
     *             <li>包含 FAIL：标记为 FAILED_TO_DLQ 并 Reject 进入死信队列</li>
     *             <li>不包含 FAIL：继续执行正常业务逻辑</li>
     *         </ul>
     *     </li>
     *     <li><strong>成功确认</strong>：调用 basicAck 告知 RabbitMQ 消息已成功处理</li>
     *     <li><strong>异常捕获</strong>：catch 块捕获所有未预期异常，标记失败并 Reject 进入 DLQ</li>
     * </ol>
     * 
     * <p><strong>关键技术点：</strong></p>
     * <ul>
     *     <li>@RabbitListener(queues = RabbitReliableConfig.QUEUE)：监听指定队列，自动反序列化消息</li>
     *     <li>Channel 参数：Spring AMQP 自动注入当前通信频道，用于手动 ACK/Reject</li>
     *     <li>Message 对象：包含消息体、Header、属性等完整信息</li>
     *     <li>StandardCharsets.UTF_8：统一字符编码，避免乱码</li>
     *     <li>basicAck(tag, false)：单个确认（false=不批量确认）</li>
     *     <li>basicReject(tag, false)：拒绝单个消息（false=不重新入队）</li>
     * </ul>
     * 
     * @param message Spring AMQP 封装的消息对象，包含消息体、Header、属性等
     * @param channel RabbitMQ 通信频道，提供 ACK、Reject 等底层操作
     * @throws IOException 网络异常、Channel 关闭等 IO 相关错误
     */
    @RabbitListener(queues = RabbitReliableConfig.QUEUE)
    public void onMessage(Message message, Channel channel) throws IOException {
        // ========== 第一步：提取消息元数据 ==========
        // deliveryTag: 消息在当前 Channel 中的唯一递增标识，从 1 开始
        // 注意：deliveryTag 是 Channel 级别的，不同 Channel 之间不共享
        long tag = message.getMessageProperties().getDeliveryTag();
        
        // 从 Header 中提取 bizId（生产者在发送时已设置）
        // Header 方式比解析消息体更高效，且不受消息体格式影响
        Object header = message.getMessageProperties().getHeaders().get("bizId");
        String bizId = header == null ? null : String.valueOf(header);
        
        // 解码消息体：使用 UTF-8 编码，避免中文乱码
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        // ========== 第二步：try-catch 包裹业务逻辑 ==========
        // 所有业务异常都应捕获并 Reject，防止消息丢失
        try {
            // ========== 参数校验 ==========
            // bizId 为空属于无效消息，无法进行幂等校验，直接 Reject 进入 DLQ
            if (bizId == null || bizId.trim().isEmpty()) {
                // basicReject(deliveryTag, requeue):
                // - deliveryTag: 要拒绝的消息标识
                // - requeue: false=不重新入队（进入死信队列），true=重新入队（可能无限循环）
                channel.basicReject(tag, false);
                return;
            }

            // ========== 幂等去重 ==========
            // 核心逻辑：检查该 bizId 是否已被处理过
            // markIfFirstConsume 返回 true 表示首次消费，返回 false 表示重复消息
            boolean first = reliableService.markIfFirstConsume(bizId);
            if (!first) {
                // 重复消息处理策略：
                // 1. 标记状态为 DUPLICATED_SKIPPED（便于 inspect 查询）
                // 2. 直接 ACK 确认，不再执行业务逻辑（避免重复扣款、重复下单）
                reliableService.markConsumed(bizId, true);  // duplicated=true
                channel.basicAck(tag, false);  // basicAck(deliveryTag, multiple): multiple=false 表示仅确认当前消息
                return;
            }

            // ========== 业务失败模拟 ==========
            // 演示用途：当消息体包含 "FAIL" 关键字时，模拟业务处理失败
            // 真实场景：这里是实际的业务逻辑，如订单创建、库存扣减、支付处理等
            if (body.contains("FAIL")) {
                // 业务失败处理策略：
                // 1. 标记状态为 FAILED_TO_DLQ（Failed To Dead Letter Queue）
                // 2. Reject 消息且不重新入队，让其进入死信队列等待人工处理
                reliableService.markConsumeFailed(bizId);
                channel.basicReject(tag, false);
                return;
            }

            // ========== 成功处理 ==========
            // 正常业务逻辑执行完成，标记为已消费并 ACK
            reliableService.markConsumed(bizId, false);  // duplicated=false 表示正常消费
            channel.basicAck(tag, false);  // 发送 ACK 确认，RabbitMQ 从队列删除该消息
            
        } catch (Exception ex) {
            // ========== 异常兜底处理 ==========
            // 捕获所有未预期的异常（如数据库超时、网络抖动、空指针等）
            // 处理策略：
            // 1. 标记为消费失败（FAILED_TO_DLQ）
            // 2. Reject 消息进入死信队列
            // 3. 记录详细错误日志（包含 bizId 和堆栈信息），便于排查问题
            reliableService.markConsumeFailed(bizId);
            channel.basicReject(tag, false);
            log.error("q009 consume error, bizId={}", bizId, ex);  // {} 占位符 + 异常对象，自动打印堆栈
        }
    }
}
