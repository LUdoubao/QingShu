package org.doubao.interview.agent.server.service.impl.q006.rabbitmq;

import org.doubao.interview.agent.api.dto.q006.rabbitmq.ProducerConfirmResultResponse;
import org.doubao.interview.agent.api.dto.q006.rabbitmq.ProducerConfirmSendRequest;
import org.doubao.interview.agent.api.service.q006.rabbitmq.ProducerConfirmDemoService;
import org.doubao.interview.agent.server.config.q006.rabbitmq.ProducerConfirmRabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 问题 006:生产者 confirm 示例实现（RabbitMQ）。
 *
 * 【核心原理】
 * RabbitMQ 的 Producer Confirm（生产者确认）机制用于确保消息可靠地发送到 Broker。
 * 
 * 【重点说明】
 * 1. confirm 只确认"消息是否到达 Exchange"，不代表一定进入 Queue：
 *    - ack：Broker 已接收消息到 Exchange
 *    - nack：Broker 未接收，需要重试或补偿
 *    - 注意：即使收到 ack，消息也可能因为路由失败而进不了 Queue
 *
 * 2. return 回调的作用：
 *    - 处理"到达 Exchange 但未路由到 Queue"的场景
 *    - 例如：routingKey 不匹配、Queue 不存在等
 *    - 需要开启 mandatory 标志才能触发 return 回调
 *
 * 3. confirm 的工作模式：
 *    - 同步 confirm：发送后阻塞等待确认（性能低，少用）
 *    - 异步 confirm：注册回调函数，非阻塞接收确认结果（推荐）
 *    - 批量 confirm：一次性发送多条消息，统一等待确认（高吞吐场景）
 *
 * 4. 生产环境最佳实践：
 *    - 高吞吐场景通常采用"异步 + 批量 confirm"
 *    - 配合消息持久化、手动 ACK 等机制保证可靠性
 *    - 需要处理 nack 和 return 的补偿逻辑（重试/记录日志/告警）
 *
 * 【应用场景】
 * - 订单创建后发送通知消息（必须确保到达 MQ）
 * - 支付成功后发送积分变更消息（不能丢失）
 * - 重要业务数据的异步处理流程
 */
@Service
public class ProducerConfirmDemoServiceImpl implements ProducerConfirmDemoService {

    private static final Logger log = LoggerFactory.getLogger(ProducerConfirmDemoServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;

    /**
     * 记录每条消息的 confirm 异步结果。
     * key=correlationId, value=ACK/NACK/RETURNED/UNKNOWN
     */
    private final Map<String, String> confirmState = new ConcurrentHashMap<String, String>();

    public ProducerConfirmDemoServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 初始化 RabbitMQ 的 confirm 和 return 回调。
     *
     * 【@PostConstruct 作用】
     * 在 Spring Bean 初始化完成后自动执行，确保回调在发送消息前已注册。
     *
     * 【两个关键回调】
     * 1. ConfirmCallback（确认回调）：
     *    - 触发时机：消息到达 Exchange 后
     *    - ack=true：Broker 成功接收
     *    - ack=false：Broker 拒绝接收（cause 包含原因）
     *    - 处理：记录状态到 confirmState，供后续查询或补偿
     *
     * 2. ReturnsCallback（返回回调）：
     *    - 触发时机：消息到达 Exchange 但无法路由到 Queue
     *    - 常见原因：routingKey 不匹配、Queue 不存在、Queue 已满等
     *    - 处理：标记为 RETURNED，记录详细错误信息
     *
     * 【注意事项】
     * - confirm 回调和 return 回调是独立的，可能同时触发
     * - 需要在发送前就注册好回调，否则可能错过确认
     * - correlationData 用于关联发送记录和确认结果
     */
    public void initCallbacks() {
        // ========== 注册 Confirm 回调（消息到达 Exchange 后的确认）==========
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // 获取关联 ID（用于追踪消息）
            String cid = correlationData == null ? "unknown" : correlationData.getId();
            
            if (ack) {
                // 情况 1: Broker 成功接收消息
                confirmState.put(cid, "ACK");
                log.info("q006 producer confirm ack, correlationId={}", cid);
            } else {
                // 情况 2: Broker 拒绝接收（可能是网络故障、Broker 宕机等）
                confirmState.put(cid, "NACK");
                log.warn("q006 producer confirm nack, correlationId={}, cause={}", cid, cause);
            }
        });

        // ========== 注册 Return 回调（消息无法路由到 Queue 时的返回）==========
        rabbitTemplate.setReturnsCallback(returned -> {
            // 从返回的消息中提取 correlationId
            String cid = returned.getMessage().getMessageProperties().getCorrelationId();
            if (cid == null) {
                cid = "unknown";
            }
            
            // 标记为 RETURNED，表示消息未被正确路由
            confirmState.put(cid, "RETURNED");
            
            // 记录详细的返回信息，便于排查问题
            log.warn("q006 producer return, correlationId={}, replyCode={}, replyText={}, exchange={}, routingKey={}",
                    cid,
                    returned.getReplyCode(),
                    returned.getReplyText(),
                    returned.getExchange(),
                    returned.getRoutingKey());
        });
    }

    /**
     * 发送单条消息并获取 confirm 结果。
     *
     * 【执行流程】
     * 1. 参数校验：检查 message 是否合法
     * 2. 准备发送参数：routingKey（使用默认值或传入值）
     * 3. 生成唯一标识：UUID 作为 correlationId，用于追踪消息
     * 4. 发送消息：调用 convertAndSend，并设置 correlationId
     * 5. 根据 waitSync 决定等待策略：
     *    - false：立即返回，confirm 结果通过异步回调获取
     *    - true：阻塞等待最多 1.5 秒，同步获取 confirm 结果
     * 6. 返回响应：包含 correlationId 和 confirm 状态
     *
     * @param request 请求参数（包含 message、routingKey、waitSync）
     * @return 响应结果（包含 correlationId、confirm 状态、是否成功等）
     */
    @Override
    public ProducerConfirmResultResponse sendOne(ProducerConfirmSendRequest request) {
        // ========== Step 1: 解析并校验参数 ==========
        String msg = request == null ? null : request.getMessage();
        String routingKey = request == null ? null : request.getRoutingKey();
        boolean waitSync = request != null && request.isWaitSync();

        if (msg == null || msg.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "message 不能为空", null, "UNKNOWN");
        }

        // ========== Step 2: 准备发送参数 ==========
        // 如果未指定 routingKey，使用配置中的默认值
        String realRoutingKey = (routingKey == null || routingKey.trim().isEmpty())
                ? ProducerConfirmRabbitConfig.ROUTING_KEY
                : routingKey;

        // ========== Step 3: 生成唯一标识 ==========
        // UUID 作为 correlationId，用于关联发送记录和 confirm 回调结果
        String cid = UUID.randomUUID().toString();
        CorrelationData cd = new CorrelationData(cid);

        // ========== Step 4: 发送消息 ==========
        // convertAndSend 会自动将对象序列化为字节数组
        // 通过 MessagePostProcessor 设置 correlationId
        // 最后一个参数 CorrelationData 用于 confirm 回调
        rabbitTemplate.convertAndSend(ProducerConfirmRabbitConfig.EXCHANGE, realRoutingKey,
                msg + " @" + Instant.now().toEpochMilli(), message -> {
                    message.getMessageProperties().setCorrelationId(cid);
                    return message;
                }, cd);

        // ========== Step 5: 根据 waitSync 决定等待策略 ==========
        if (!waitSync) {
            // ========== 情况 A: 异步模式 ==========
            // 立即返回，confirm 结果通过后台回调更新到 confirmState
            // 适用于高吞吐场景，不阻塞主流程
            return build(true, "ASYNC_SENT", "消息已异步发送，结果请根据回调日志或稍后查询状态", cid,
                    confirmState.getOrDefault(cid, "PENDING"));
        }

        // ========== 情况 B: 同步模式 ==========
        // 阻塞等待 confirm 回调，最多等待 1.5 秒
        // 适用于对可靠性要求高、可以接受少量延迟的场景
        String status = waitConfirm(cid, 1500L);
        boolean ok = "ACK".equals(status);  // 只有 ACK 才认为成功
        return build(ok, "SYNC_CONFIRM", "同步等待 confirm 完成", cid, status);
    }

    /**
     * 批量发送消息（演示批量 confirm 场景）。
     *
     * 【执行流程】
     * 1. 参数校验：count 必须大于 0
     * 2. 循环发送 count 条消息：
     *    - 每条消息生成独立的 correlationId
     *    - 独立发送，依赖异步 confirm 回调统计结果
     * 3. 立即返回：不等待 confirm 结果
     *
     * 【批量 confirm 的优势】
     * - 高吞吐：不需要逐条等待确认，充分利用网络带宽
     * - 低延迟：发送方不会被 confirm 阻塞
     * - 适合场景：日志收集、数据统计等非关键业务
     *
     * 【注意事项】
     * - 需要配合 confirm 回调使用，否则无法知道发送结果
     * - 大批量发送时要注意控制速率，避免压垮 Broker
     * - 对于关键业务，建议分批发送 + 逐批确认
     *
     * @param count 要发送的消息数量
     * @return 响应结果（包含发送数量）
     */
    @Override
    public ProducerConfirmResultResponse sendBatch(int count) {
        if (count <= 0) {
            return build(false, "PARAM_VALIDATION", "count 必须大于 0", null, "UNKNOWN");
        }

        // ========== 批量 confirm 演示：快速连续发送 ==========
        // 依赖异步回调统计结果，不阻塞等待
        int sent = 0;
        for (int i = 0; i < count; i++) {
            // 每条消息独立生成 correlationId
            String cid = UUID.randomUUID().toString();
            CorrelationData cd = new CorrelationData(cid);
            
            // 发送消息，设置 correlationId
            rabbitTemplate.convertAndSend(ProducerConfirmRabbitConfig.EXCHANGE,
                    ProducerConfirmRabbitConfig.ROUTING_KEY,
                    "batch-message-" + i,
                    message -> {
                        message.getMessageProperties().setCorrelationId(cid);
                        return message;
                    }, cd);
            sent++;
        }

        // 立即返回，confirm 结果将通过异步回调更新
        return build(true, "BATCH_ASYNC_SENT", "批量异步消息已发送，confirm 将异步回调", null,
                "SENT=" + sent);
    }

    /**
     * 同步等待 confirm 回调结果。
     *
     * 【工作原理】
     * 1. 计算超时时间点：当前时间 + timeoutMillis
     * 2. 轮询检查 confirmState：
     *    - 每 20ms 检查一次状态
     *    - 如果发现状态变化（ACK/NACK/RETURNED），立即返回
     *      *    - 如果超时仍未变化，返回 TIMEOUT
     * 3. 被中断时立即退出
     *
     * 【为什么用轮询而不是 wait/notify？】
     * - 简单直观，易于理解和调试
     * - confirm 回调线程和业务线程不同，轮询更可靠
     * - 20ms 的间隔在实时性和 CPU 消耗间取得平衡
     *
     * 【超时时间选择】
     * - 太短：可能错过 confirm（网络延迟时）
     * - 太长：影响用户体验
     * - 经验值：1-3 秒（根据网络环境和业务容忍度调整）
     *
     * @param cid correlationId
     * @param timeoutMillis 超时时间（毫秒）
     * @return confirm 状态（ACK/NACK/RETURNED/TIMEOUT/PENDING）
     */
    private String waitConfirm(String cid, long timeoutMillis) {
        // 计算超时时间点
        long end = System.currentTimeMillis() + timeoutMillis;
        
        // 轮询检查 confirm 状态
        while (System.currentTimeMillis() < end) {
            String status = confirmState.get(cid);
            if (status != null) {
                // confirm 回调已触发，返回最终状态
                return status;
            }
            
            // 短暂休眠，降低 CPU 占用
            try {
                TimeUnit.MILLISECONDS.sleep(20L);
            } catch (InterruptedException ex) {
                // 线程被中断，立即退出
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // 超时仍未收到 confirm，返回 TIMEOUT
        return confirmState.getOrDefault(cid, "TIMEOUT");
    }

    private ProducerConfirmResultResponse build(boolean success, String stage, String message,
                                                String cid, String status) {
        ProducerConfirmResultResponse response = new ProducerConfirmResultResponse();
        response.setSuccess(success);
        response.setStage(stage);
        response.setMessage(message);
        response.setCorrelationId(cid);
        response.setConfirmStatus(status);
        return response;
    }
}
