package org.doubao.interview.agent.server.service.impl.q015.rabbitmq;

import com.rabbitmq.client.Channel;
import org.doubao.interview.agent.api.dto.q015.rabbitmq.RabbitDlqResponse;
import org.doubao.interview.agent.api.dto.q015.rabbitmq.RabbitDlqSendRequest;
import org.doubao.interview.agent.api.service.q015.rabbitmq.RabbitDlqDemoService;
import org.doubao.interview.agent.server.config.q015.rabbitmq.RabbitDlqConfig;
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
 * 问题 015(RabbitMQ)：死信队列示例实现。
 *
 * 教学要点：
 * 1. DLQ(Dead Letter Queue) 用于承接"无法正常处理"的消息，避免主队列阻塞。
 *    - 消息被拒绝 (basicReject/basicNack 且 requeue=false)
 *    - 消息过期 (TTL 超时)
 *    - 队列长度限制超出
 * 2. 死信不是终点：需要配套告警、排查、重放工具。
 *    - 监控死信队列数量，及时告警
 *    - 分析死信原因 (业务异常/数据问题/系统故障)
 *    - 提供重放机制进行补偿处理
 * 3. 本示例提供 inspect/replay，模拟排查与补偿闭环。
 *    - inspect: 查询消息当前状态
 *    - replay: 将死信消息重新投递到主队列
 */
@Service
public class RabbitDlqDemoServiceImpl implements RabbitDlqDemoService {

    /**
     * RabbitTemplate 是 Spring AMQP 提供的核心模板类
     * 用于发送和接收 RabbitMQ 消息，封装了底层的 Channel 操作
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 内存状态映射表，用于记录消息处理状态，便于演示 inspect 功能
     * Key: bizId (业务 ID)
     * Value: 状态字符串 (SENT, MAIN_CONSUMED_ACKED, MAIN_REJECT_TO_DLQ, IN_DLQ, REPLAYED 等)
     * 
     * 注意：实际生产环境应使用持久化存储 (如数据库/Redis)，而非内存 Map
     * 使用 ConcurrentHashMap 保证多线程环境下的线程安全性
     */
    private final Map<String, String> statusMap = new ConcurrentHashMap<String, String>();

    public RabbitDlqDemoServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public RabbitDlqResponse send(RabbitDlqSendRequest request) {
        // 从请求对象中提取业务参数
        String bizId = request == null ? null : request.getBizId();
        String payload = request == null ? null : request.getPayload();
        // forceFail 标志位：true 时强制让消息失败，触发死信流程
        boolean forceFail = request != null && request.isForceFail();

        // 参数校验：bizId 和 payload 均不能为空或空白字符串
        if (bizId == null || bizId.trim().isEmpty() || payload == null || payload.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizId/payload 不能为空", bizId, "INVALID");
        }

        // 构建消息体：包含业务 ID、失败标志、负载内容和时间戳
        // 时间戳使用毫秒级 Unix 时间戳 (Instant.now().toEpochMilli())
        String body = "bizId=" + bizId + "|forceFail=" + forceFail + "|payload=" + payload + "|ts=" + Instant.now().toEpochMilli();
        
        // 发送消息到 RabbitMQ
        // EXCHANGE: 交换机名称，负责接收消息并路由到对应队列
        // ROUTING_KEY: 路由键，决定消息从交换机路由到哪个队列
        rabbitTemplate.convertAndSend(RabbitDlqConfig.EXCHANGE, RabbitDlqConfig.ROUTING_KEY, body, message -> {
            // 设置消息投递模式为 PERSISTENT(持久化)
            // 持久化消息会在 RabbitMQ 服务器重启后仍然存在，防止数据丢失
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            // 设置自定义消息头 bizId，便于后续消费者识别和业务追踪
            message.getMessageProperties().setHeader("bizId", bizId);
            // 设置自定义消息头 forceFail，用于控制消费者是否强制失败
            message.getMessageProperties().setHeader("forceFail", forceFail);
            return message;
        });
        
        // 更新内存状态为 SENT，表示消息已发送到主队列
        statusMap.put(bizId, "SENT");
        // 构建成功响应返回
        return build(true, "SENT", "消息已发送到主队列", bizId, "SENT");
    }

    @Override
    public RabbitDlqResponse inspect(String bizId) {
        // 参数校验：bizId 不能为空或空白字符串
        if (bizId == null || bizId.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizId 不能为空", bizId, "INVALID");
        }
        // 从内存状态表中查询该业务 ID 的当前状态
        // getOrDefault: 如果不存在则返回"UNKNOWN"未知状态
        String st = statusMap.getOrDefault(bizId, "UNKNOWN");
        // 返回查询结果，stage=INSPECT 表示这是检查操作
        return build(true, "INSPECT", "返回当前状态", bizId, st);
    }

    @Override
    public RabbitDlqResponse replay(String bizId) {
        // 参数校验：bizId 不能为空或空白字符串
        if (bizId == null || bizId.trim().isEmpty()) {
            return build(false, "PARAM_VALIDATION", "bizId 不能为空", bizId, "INVALID");
        }
        // 简化示例：将 replay 消息重新投递到主交换机，不再强制失败
        // 实际生产环境中，这里应该从死信队列获取原始消息并原样重放
        // 或者根据业务需求进行数据修复后再发送
        String body = "bizId=" + bizId + "|forceFail=false|payload=replay|ts=" + Instant.now().toEpochMilli();
        
        // 重新发送消息到主队列，forceFail 设置为 false 确保这次能成功消费
        rabbitTemplate.convertAndSend(RabbitDlqConfig.EXCHANGE, RabbitDlqConfig.ROUTING_KEY, body, message -> {
            // 同样设置为持久化消息，保证可靠性
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            message.getMessageProperties().setHeader("bizId", bizId);
            // 关键：将 forceFail 设置为 false，避免再次进入死信队列
            message.getMessageProperties().setHeader("forceFail", false);
            return message;
        });
        
        // 更新状态为 REPLAYED，表示已重放到主队列等待再次消费
        statusMap.put(bizId, "REPLAYED");
        return build(true, "REPLAYED", "死信消息已重放到主队列", bizId, "REPLAYED");
    }

    /**
     * 主队列消费者：监听并处理来自主队列的消息
     * 
     * @RabbitListener(queues = RabbitDlqConfig.QUEUE) 注解指定监听的队列名称
     * 该方法会被 Spring AMQP 自动调用，当有新消息到达时触发
     * 
     * @param message 接收到的消息对象，包含消息体、消息头、属性等完整信息
     * @param channel RabbitMQ 的 Channel 通道，用于执行 ACK/NACK 等操作
     * @throws IOException 可能发生 IO 异常
     */
    @RabbitListener(queues = RabbitDlqConfig.QUEUE)
    public void consumeMain(Message message, Channel channel) throws IOException {
        // 获取消息的投递标签 (Delivery Tag)
        // Delivery Tag 是消息在 Channel 中的唯一标识符，用于后续的 ACK/NACK 确认
        long tag = message.getMessageProperties().getDeliveryTag();
        
        // 从消息头中提取 bizId，用于业务追踪和状态记录
        // 注意：getHeaders() 返回 Object，需要强制转换为 String
        String bizId = String.valueOf(message.getMessageProperties().getHeaders().get("bizId"));
        
        // 提取 forceFail 标志位，判断是否需要强制失败
        Object failHeader = message.getMessageProperties().getHeaders().get("forceFail");
        boolean forceFail = failHeader != null && Boolean.parseBoolean(String.valueOf(failHeader));

        try {
            if (forceFail) {
                // 【关键逻辑】当 forceFail=true 时，模拟业务处理失败场景
                // 更新状态为"MAIN_REJECT_TO_DLQ"，表示在主队列被拒绝并将进入死信队列
                statusMap.put(bizId, "MAIN_REJECT_TO_DLQ");
                
                // basicReject(tag, requeue) 拒绝消息
                // tag: 要拒绝的消息的投递标签
                // requeue=false: 拒绝后不重新入队 (不回主队列)
                // 
                // 【requeue 参数详解】
                // requeue=true:  拒绝后消息重新回到原队列头部，会立即被再次投递
                //                如果消费者持续返回 true，会导致消息无限循环重试，造成"毒丸"问题
                // requeue=false: 拒绝后消息不会回到原队列，而是:
                //                1. 如果队列配置了死信交换机 (DLX)，消息会被路由到死信队列 (DLQ)
                //                   - 消息头会添加特殊字段：x-death 记录死亡次数、原因、原始队列等信息
                //                   - 死信队列可以有独立的消费者进行处理、告警、归档
                //                2. 如果没有配置 DLX，消息会被直接丢弃
                // 
                // 【死信触发机制】当消息被拒绝且 requeue=false 时：
                // 1. RabbitMQ 会将消息标记为"dead-letter"
                // 2. 检查队列是否配置了 x-dead-letter-exchange 参数
                // 3. 如果有 DLX，使用新的 routing key(可通过 x-dead-letter-routing-key 指定) 重新路由
                // 4. 消息进入死信队列，等待后续处理 (人工介入/自动重放/归档)
                // 
                // 【最佳实践】
                // - 对于业务验证失败、数据格式错误等不可自动恢复的场景，使用 requeue=false
                // - 对于临时性故障 (如网络抖动、数据库锁)，可以使用 requeue=true 并配合重试次数限制
                // - 必须配置 DLQ 来承接无法处理的消息，避免消息丢失
                channel.basicReject(tag, false);
                return;
            }
            
            // 正常业务处理成功场景
            // 更新状态为"MAIN_CONSUMED_ACKED"，表示已在主队列成功消费并确认
            statusMap.put(bizId, "MAIN_CONSUMED_ACKED");
            
            // basicAck(tag, multiple) 确认消息已成功处理
            // tag: 要确认的消息的投递标签
            // multiple=false: 只确认这一条消息 (不批量确认)
            channel.basicAck(tag, false);
        } catch (Exception ex) {
            // 【异常捕获】当 consumeMain 方法内部抛出任何异常时
            // 更新状态为"MAIN_EXCEPTION_TO_DLQ"，表示因异常进入死信队列
            statusMap.put(bizId, "MAIN_EXCEPTION_TO_DLQ");
            
            // 同样使用 basicReject 拒绝消息且不回队，触发死信转发
            // requeue=false: 确保消息不会无限循环重试，而是进入死信队列等待分析
            // 这样可以保证即使代码抛异常，消息也不会丢失，而是进入死信队列等待处理
            channel.basicReject(tag, false);
        }
    }

    /**
     * 死信队列消费者：监听并处理来自死信队列的消息
     * 
     * @RabbitListener(queues = RabbitDlqConfig.DLQ) 注解指定监听死信队列
     * 当主队列的消息成为死信后，会自动路由到这个队列被处理
     * 
     * 死信队列的典型处理策略：
     * 1. 记录日志并告警，通知开发人员介入
     * 2. 分析失败原因，分类存储 (数据问题/系统问题/业务逻辑问题)
     * 3. 提供手动/自动重放机制
     * 4. 对于无法恢复的消息，进行归档或丢弃处理
     * 
     * @param message 接收到的死信消息对象
     * @param channel RabbitMQ 的 Channel 通道
     * @throws IOException 可能发生 IO 异常
     */
    @RabbitListener(queues = RabbitDlqConfig.DLQ)
    public void consumeDlq(Message message, Channel channel) throws IOException {
        // 获取消息的投递标签，用于后续确认
        long tag = message.getMessageProperties().getDeliveryTag();
        
        // 从消息头中提取 bizId，用于状态追踪
        String bizId = String.valueOf(message.getMessageProperties().getHeaders().get("bizId"));
        
        // 更新状态为"IN_DLQ"，表示该消息已进入死信队列
        // 此时通常需要人工介入分析原因，或通过监控系统触发告警
        statusMap.put(bizId, "IN_DLQ");
        
        // 对死信消息进行 ACK 确认
        // 注意：这里直接 ACK 是因为我们已经在 statusMap 中记录了状态
        // 实际生产环境可能需要：
        // - 记录到数据库以便后续追踪
        // - 发送告警通知
        // - 转入专门的死信存储系统
        channel.basicAck(tag, false);
    }

    /**
     * 统一的响应构建方法
     * 用于创建标准化的 RabbitDlqResponse 响应对象
     * 
     * @param success 操作是否成功 (true/false)
     * @param stage 当前所处的阶段或操作类型 (如：SENT, INSPECT, REPLAYED, PARAM_VALIDATION 等)
     * @param message 人类可读的描述信息，用于前端展示或日志记录
     * @param bizId 业务 ID，用于追踪具体哪条消息
     * @param status 消息当前的状态值 (如：SENT, MAIN_CONSUMED_ACKED, IN_DLQ, REPLAYED 等)
     * @return 封装好的响应对象
     */
    private RabbitDlqResponse build(boolean success, String stage, String message, String bizId, String status) {
        RabbitDlqResponse r = new RabbitDlqResponse();
        r.setSuccess(success);
        r.setStage(stage);
        r.setMessage(message);
        r.setBizId(bizId);
        r.setStatus(status);
        return r;
    }
}
