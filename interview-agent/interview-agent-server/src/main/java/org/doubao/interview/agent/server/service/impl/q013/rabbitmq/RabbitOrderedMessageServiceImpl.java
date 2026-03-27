package org.doubao.interview.agent.server.service.impl.q013.rabbitmq;

import org.doubao.interview.agent.api.dto.q013.rabbitmq.RabbitOrderedResponse;
import org.doubao.interview.agent.api.dto.q013.rabbitmq.RabbitOrderedSendRequest;
import org.doubao.interview.agent.api.service.q013.rabbitmq.RabbitOrderedMessageService;
import org.doubao.interview.agent.server.config.q013.rabbitmq.RabbitOrderedConfig;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问题 013(RabbitMQ)：消息顺序性实现。
 * 
 * <p>实现 RabbitMQ 消息顺序性的核心方案，通过固定路由和单消费者队列保证局部有序：</p>
 * 
 * <h2>顺序性保障原理：</h2>
 * <ul>
 *     <li><strong>分片队列策略</strong>：
 *         <ul>
 *             <li>创建多个分片队列（如 QUEUE_0、QUEUE_1），每个队列对应一个业务分片</li>
 *             <li>同一 bizKey 的消息总是路由到同一个固定队列，避免分散到不同队列导致乱序</li>
 *             <li>不同 bizKey 可并行处理，提高整体吞吐量</li>
 *         </ul>
 *     </li>
 *     <li><strong>单消费者串行消费</strong>：
 *         <ul>
 *             <li>每个分片队列仅配置一个消费者（concurrency=1）</li>
 *             <li>RabbitMQ 按消息到达顺序依次投递给消费者，保证队列内 FIFO（先进先出）</li>
 *             <li>同一时刻只有一个线程处理该队列的消息，天然串行化</li>
 *         </ul>
 *     </li>
 *     <li><strong>哈希路由算法</strong>：
 *         <ul>
 *             <li>根据 bizKey 的 hashCode 计算槽位（slot），决定路由到哪个队列</li>
 *             <li>相同 bizKey → 相同 slot → 相同队列 → 顺序消费</li>
 *             <li>不同 bizKey → 可能不同队列 → 并行消费</li>
 *         </ul>
 *     </li>
 * </ul>
 * 
 * <h2>乱序检测机制：</h2>
 * <ul>
 *     <li>记录每个 bizKey 最近一次成功消费的序号（lastSeqMap）</li>
 *     <li>新消息序号 ≤ 上次序号时，判定为乱序（disorderMap=true）</li>
 *     <li>乱序原因可能是：生产者重发旧消息、Broker 内部异常、网络重传等</li>
 * </ul>
 * 
 * <p><strong>适用场景：</strong>订单状态流转、支付流水更新、库存变更等需要保序的业务。</p>
 * <p><strong>局限性：</strong>只能保证局部有序（同 bizKey），无法保证全局有序；多队列并发度受限。</p>
 */
@Service
public class RabbitOrderedMessageServiceImpl implements RabbitOrderedMessageService {

    /**
     * Spring AMQP 的 RabbitTemplate，封装了消息发送的核心逻辑。
     * 线程安全，由 Spring 容器注入，单例复用。
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 记录每个 bizKey 最近一次成功消费的序号（线程安全的 ConcurrentHashMap）。
     * Key: 业务唯一标识（bizKey）
     * Value: 已成功消费的最大序号
     * 
     * <p>用途：检测乱序、追踪消费进度、支持 inspect 查询。</p>
     * <p>示例：bizKey="ORDER_001"，lastSeq=5 表示该订单已处理到序号 5 的消息。</p>
     */
    private final Map<String, Long> lastSeqMap = new ConcurrentHashMap<String, Long>();
    /**
     * 记录每个 bizKey 是否检测到乱序（线程安全的 ConcurrentHashMap）。
     * Key: 业务唯一标识（bizKey）
     * Value: true=检测到乱序，false/null=顺序正常
     * 
     * <p>触发条件：当收到的消息序号 ≤ 上次成功消费的序号时，标记为乱序。</p>
     * <p>典型场景：
     *     <ul>
     *         <li>生产者重试机制导致旧消息重发（如 seq=3 的消息发送失败后重试）</li>
     *         <li>网络延迟导致消息延迟到达（如 seq=5 已到，seq=4 才到）</li>
     *         <li>Broker 或消费者重启后，未 ACK 消息重新入队但序号错乱</li>
     *     </ul>
     * </p>
     */
    private final Map<String, Boolean> disorderMap = new ConcurrentHashMap<String, Boolean>();
    /**
     * 记录每个 bizKey 路由到的队列名称（线程安全的 ConcurrentHashMap）。
     * Key: 业务唯一标识（bizKey）
     * Value: 队列名（QUEUE_0 或 QUEUE_1）
     * 
     * <p>用途：追踪消息路由路径，支持前端展示和调试排查。</p>
     * <p>特点：putIfAbsent 仅在首次写入，后续保持不变（即使重启也不会改变路由）。</p>
     */
    private final Map<String, String> routeMap = new ConcurrentHashMap<String, String>();

    /**
     * 构造器注入 RabbitTemplate。
     * 
     * @param rabbitTemplate Spring 提供的 RabbitMQ 模板，封装了消息发送、属性设置等核心功能
     */
    public RabbitOrderedMessageServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送有序消息到 RabbitMQ 分片队列。
     * 
     * <p>完整的发送流程包括以下步骤：</p>
     * <ol>
     *     <li><strong>参数校验</strong>：检查 bizKey、seq、payload 的有效性
     *         <ul>
     *             <li>bizKey：业务唯一标识，不能为空或空白</li>
     *             <li>seq：消息序号，必须 > 0（从 1 开始递增）</li>
     *             <li>payload：消息负载内容，不能为空或空白</li>
     *         </ul>
     *     </li>
     *     <li><strong>计算路由键</strong>：根据 bizKey 的 hashCode 计算槽位，决定路由到 RK_0 还是 RK_1
     *         <ul>
     *             <li>slot = Math.abs(bizKey.hashCode()) % 2</li>
     *             <li>确保相同 bizKey 始终路由到相同队列</li>
     *         </ul>
     *     </li>
     *     <li><strong>记录路由映射</strong>：将 bizKey → queue 的映射关系存入 routeMap（首次写入）
     *     </li>
     *     <li><strong>构建消息体</strong>：格式化为 "bizKey=xxx|seq=N|payload=xxx|ts=时间戳"
     *         <ul>
     *             <li>便于人工查看和调试</li>
     *             <li>时间戳用于追踪发送时刻</li>
     *         </ul>
     *     </li>
     *     <li><strong>设置消息属性</strong>：
     *         <ul>
     *             <li>deliveryMode=PERSISTENT(2)：消息持久化到磁盘</li>
     *             <li>Header.bizKey：业务唯一标识，供消费者提取使用</li>
     *             <li>Header.seq：消息序号，供消费者进行顺序校验</li>
     *         </ul>
     *     </li>
     *     <li><strong>发送消息</strong>：调用 convertAndSend 发送到指定 Exchange 和 RoutingKey</li>
     *     <li><strong>返回状态</strong>：包含路由队列、当前序号、是否顺序正常等信息</li>
     * </ol>
     * 
     * <p><strong>关键技术点：</strong></p>
     * <ul>
     *     <li>哈希路由：Math.abs(hashCode) % N 确保均匀分布且结果稳定</li>
     *     <li>MessagePostProcessor：在发送前拦截并修改消息属性，采用 Lambda 简化代码</li>
     *     <li>局部有序：同 bizKey 的消息进入同一队列 + 单消费者 = 顺序消费</li>
     *     <li>并发安全：所有 Map 均使用 ConcurrentHashMap，支持多线程并发访问</li>
     * </ul>
     * 
     * @param request 请求对象，包含：
     *                - bizKey: 业务唯一标识（如订单号、用户 ID）
     *                - seq: 消息序号（从 1 开始递增，必须连续）
     *                - payload: 消息负载内容（如订单状态、操作类型）
     * @return 响应对象，包含：
     *         - success: 发送是否成功（true=参数校验通过且已发送，false=参数校验失败）
     *         - stage: 当前阶段（PARAM_VALIDATION 或 SENT）
     *         - queueName: 消息路由到的队列名称（QUEUE_0 或 QUEUE_1）
     *         - seq: 当前消息序号
     *         - inOrder: 是否顺序正常（true=未检测到乱序，false=已检测到乱序）
     */
    @Override
    public RabbitOrderedResponse send(RabbitOrderedSendRequest request) {
        // ========== 第一步：参数校验 ==========
        // 从请求对象中提取 bizKey、seq、payload，任一无效则拒绝发送
        String bizKey = request == null ? null : request.getBizKey();
        long seq = request == null ? 0 : request.getSeq();
        String payload = request == null ? null : request.getPayload();
            
        if (bizKey == null || bizKey.trim().isEmpty() || seq <= 0 || payload == null || payload.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizKey/payload 不能为空且 seq>0", bizKey, null, seq, false);
        }
    
        // ========== 第二步：计算路由键 ==========
        // 哈希路由算法：根据 bizKey 的 hashCode 计算槽位（0 或 1）
        // Math.abs 确保结果为正数，% 2 将结果限制在 2 个槽位内
        // 相同 bizKey → 相同 slot → 相同 RoutingKey → 相同队列
        String rk = routeKeyByBizKey(bizKey);
            
        // 根据 RoutingKey 推导队列名称（用于返回给调用方）
        String queue = queueNameByRoutingKey(rk);
            
        // ========== 第三步：记录路由映射 ==========
        // putIfAbsent：仅在 bizKey 首次出现时写入，后续保持不变
        // 用途：追踪消息路由路径，支持 inspect 方法查询
        routeMap.putIfAbsent(bizKey, queue);
    
        // ========== 第四步：构建并发送消息 ==========
        // 消息体结构：bizKey + seq + payload + 时间戳，便于查看和调试
        String body = "bizKey=" + bizKey + "|seq=" + seq + "|payload=" + payload + "|ts=" + Instant.now().toEpochMilli();
            
        rabbitTemplate.convertAndSend(
                RabbitOrderedConfig.EXCHANGE,  // 目标交换机名称
                rk,                            // 路由键（RK_0 或 RK_1，决定进入哪个分片队列）
                body,                          // 消息体内容
                message -> {
                    // MessagePostProcessor：在发送前拦截并设置消息属性
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);  // 消息持久化（deliveryMode=2）
                    message.getMessageProperties().setHeader("bizKey", bizKey);  // 设置业务 ID Header，供消费者提取
                    message.getMessageProperties().setHeader("seq", seq);  // 设置序号 Header，供消费者进行顺序校验
                    return message;
                }
        );
    
        // ========== 第五步：构建响应 ==========
        // 返回当前已知的状态信息（inOrder 基于 disorderMap 判断）
        return build(true, "SENT", "消息已发送到固定分片队列，保证同 bizKey 局部有序", bizKey, queue, seq,
                !Boolean.TRUE.equals(disorderMap.get(bizKey)));  // true=顺序正常，false=已检测到乱序
    }

    /**
     * 查询指定 bizKey 的消息顺序状态。
     * 
     * <p>用于追踪消息的消费顺序，包括：</p>
     * <ul>
     *     <li>最近成功消费的序号（lastSeqMap）</li>
     *     <li>是否检测到乱序（disorderMap）</li>
     *     <li>路由到的队列名称（routeMap）</li>
     * </ul>
     * 
     * <p><strong>使用场景：</strong></p>
     * <ul>
     *     <li>前端轮询查看消息处理进度和顺序状态</li>
     *     <li>运维排查乱序问题（如收到旧序号消息）</li>
     *     <li>调试验证顺序性保障机制</li>
     * </ul>
     * 
     * @param bizKey 业务唯一标识
     * @return 响应对象，包含：
     *         - success: 查询是否成功（true=bizKey 有效，false=bizKey 为空）
     *         - stage: 当前阶段（PARAM_VALIDATION 或 INSPECT）
     *         - queueName: 路由到的队列名称
     *         - seq: 最近成功消费的序号（0=尚未消费）
     *         - inOrder: 是否顺序正常（true=未检测到乱序，false=已检测到乱序）
     */
    @Override
    public RabbitOrderedResponse inspect(String bizKey) {
        if (bizKey == null || bizKey.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizKey 不能为空", bizKey, null, 0, false);
        }
            
        // 获取最近成功消费的序号（null 表示尚未消费过）
        Long seq = lastSeqMap.get(bizKey);
            
        // 判断是否顺序正常：disorderMap 中无记录或值为 false 表示未检测到乱序
        boolean inOrder = !Boolean.TRUE.equals(disorderMap.get(bizKey));
            
        return build(true, "INSPECT", "返回当前顺序状态", bizKey, routeMap.get(bizKey), 
                seq == null ? 0 : seq, inOrder);
    }

    /**
     * 记录消息已被消费并检测乱序（仅供消费者调用）。
     * 
     * <p>消费者成功处理消息后调用此方法，完成以下操作：</p>
     * <ol>
     *     <li><strong>更新消费进度</strong>：将当前序号写入 lastSeqMap，覆盖旧值</li>
     *     <li><strong>检测乱序</strong>：比较当前序号与上次序号
     *         <ul>
     *             <li>首次消费（last=null）：不检测，直接记录</li>
     *             <li>当前序号 > 上次序号：顺序正常，不标记</li>
     *             <li>当前序号 ≤ 上次序号：检测到乱序，标记 disorderMap=true</li>
     *         </ul>
     *     </li>
     * </ol>
     * 
     * <p><strong>乱序场景示例：</strong></p>
     * <ul>
     *     <li>正常顺序：seq=1 → seq=2 → seq=3（last 依次为 null→1→2→3，不标记乱序）</li>
     *     <li>乱序情况：seq=1 → seq=3 → seq=2（收到 seq=2 时，last=3，2≤3 判定为乱序）</li>
     *     <li>重复消息：seq=1 → seq=2 → seq=2（收到第二个 seq=2 时，last=2，2≤2 判定为乱序）</li>
     * </ul>
     * 
     * <p><strong>线程安全性：</strong></p>
     * <p>ConcurrentHashMap 的 put/get 操作是线程安全的，支持多线程并发调用。
     * 即使多个消费者同时处理不同 bizKey 的消息，也不会出现并发问题。</p>
     * 
     * @param bizKey 业务唯一标识
     * @param seq 当前消费的消息序号
     */
    void onConsumed(String bizKey, long seq) {
        // put(key, value): 放入新值并返回旧值（原子操作）
        // last 为 null 表示首次消费该 bizKey 的消息
        Long last = lastSeqMap.put(bizKey, seq);
        
        // 乱序检测逻辑：
        // 1. last != null：排除首次消费的情况
        // 2. seq <= last：当前序号不大于上次序号，说明出现了倒序或重复
        if (last != null && seq <= last) {
            disorderMap.put(bizKey, true);  // 标记为已检测到乱序
        }
    }

    /**
     * 根据 bizKey 计算路由键（RoutingKey）。
     * 
     * <p>哈希路由算法：通过 bizKey 的 hashCode 计算槽位，决定路由到哪个分片队列。</p>
     * 
     * <p><strong>算法原理：</strong></p>
     * <ol>
     *     <li>计算 bizKey 的 hashCode（Java 内置字符串哈希算法）</li>
     *     <li>Math.abs 取绝对值，确保结果为正数（hashCode 可能为负）</li>
     *     <li>% 2 取模，将结果限制在 0 或 1 两个槽位</li>
     *     <li>slot=0 → RK_0，slot=1 → RK_1</li>
     * </ol>
     * 
     * <p><strong>特点：</strong></p>
     * <ul>
     *     <li>确定性：相同 bizKey 永远得到相同 slot，确保路由稳定性</li>
     *     <li>均匀性：hashCode 均匀分布，两个槽位的消息数量大致相等</li>
     *     <li>简单高效：纯计算操作，无需查表或网络 IO</li>
     * </ul>
     * 
     * <p><strong>扩展性：</strong>如需更多分片，只需增加槽位数量（如 % 4 得到 4 个槽位）。</p>
     * 
     * @param bizKey 业务唯一标识
     * @return 路由键（RK_0 或 RK_1）
     */
    private String routeKeyByBizKey(String bizKey) {
        // 计算哈希槽位：Math.abs(hashCode) % 2
        int slot = Math.abs(bizKey.hashCode()) % 2;
        return slot == 0 ? RabbitOrderedConfig.RK_0 : RabbitOrderedConfig.RK_1;
    }

    /**
     * 根据路由键推导队列名称。
     * 
     * <p>RoutingKey 与 Queue 的映射关系：</p>
     * <ul>
     *     <li>RK_0 → QUEUE_0</li>
     *     <li>RK_1 → QUEUE_1</li>
     * </ul>
     * 
     * <p>用途：构建响应对象时返回队列名称，便于追踪消息路由路径。</p>
     * 
     * @param rk 路由键（RK_0 或 RK_1）
     * @return 队列名称（QUEUE_0 或 QUEUE_1）
     */
    private String queueNameByRoutingKey(String rk) {
        return RabbitOrderedConfig.RK_0.equals(rk) ? RabbitOrderedConfig.QUEUE_0 : RabbitOrderedConfig.QUEUE_1;
    }

    /**
     * 构建统一的响应对象。
     * 
     * <p>封装 RabbitOrderedResponse 的创建逻辑，减少代码重复。</p>
     * 
     * @param success 操作是否成功
     * @param stage 当前处理阶段（PARAM_VALIDATION/SENT/INSPECT）
     * @param message 响应消息描述
     * @param bizKey 业务唯一标识
     * @param queue 路由到的队列名称
     * @param seq 消息序号（发送时）或最近消费序号（inspect 时）
     * @param inOrder 是否顺序正常（true=未检测到乱序，false=已检测到乱序）
     * @return 构建好的响应对象
     */
    private RabbitOrderedResponse build(boolean success, String stage, String message,
                                        String bizKey, String queue, long seq, boolean inOrder) {
        RabbitOrderedResponse r = new RabbitOrderedResponse();
        r.setSuccess(success);
        r.setStage(stage);
        r.setMessage(message);
        r.setBizKey(bizKey);
        r.setQueueName(queue);
        r.setSeq(seq);
        r.setInOrder(inOrder);
        return r;
    }
}
