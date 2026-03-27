package org.doubao.interview.agent.server.config.rabbitmq.q016;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 问题016(RabbitMQ)：延迟消息拓扑。
 *
 * TTL+DLX 方案：
 * 1. 延迟队列（消息带TTL）
 * 2. 到期后转发到业务交换机/业务队列
 */
@Configuration
public class RabbitDelayConfig {

    public static final String DELAY_EXCHANGE = "q016.delay.exchange";
    public static final String DELAY_QUEUE = "q016.delay.queue";
    public static final String DELAY_ROUTING_KEY = "q016.delay.key";

    public static final String BIZ_EXCHANGE = "q016.biz.exchange";
    public static final String BIZ_QUEUE = "q016.biz.queue";
    public static final String BIZ_ROUTING_KEY = "q016.biz.key";

    @Bean
    public DirectExchange q016DelayExchange() { return new DirectExchange(DELAY_EXCHANGE, true, false); }

    @Bean
    public DirectExchange q016BizExchange() { return new DirectExchange(BIZ_EXCHANGE, true, false); }

    @Bean
    public Queue q016DelayQueue() {
        return QueueBuilder.durable(DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", BIZ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", BIZ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue q016BizQueue() { return QueueBuilder.durable(BIZ_QUEUE).build(); }

    @Bean
    public Binding q016DelayBinding(DirectExchange q016DelayExchange, Queue q016DelayQueue) {
        return BindingBuilder.bind(q016DelayQueue).to(q016DelayExchange).with(DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding q016BizBinding(DirectExchange q016BizExchange, Queue q016BizQueue) {
        return BindingBuilder.bind(q016BizQueue).to(q016BizExchange).with(BIZ_ROUTING_KEY);
    }
}
