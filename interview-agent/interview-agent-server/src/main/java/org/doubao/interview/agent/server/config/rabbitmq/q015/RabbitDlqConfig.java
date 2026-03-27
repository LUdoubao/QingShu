package org.doubao.interview.agent.server.config.rabbitmq.q015;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 问题015(RabbitMQ)：DLX + DLQ 配置。
 *
 * 触发死信典型条件：
 * 1. reject/nack 且 requeue=false
 * 2. 消息TTL过期
 * 3. 队列达到长度上限被丢弃
 */
@Configuration
public class RabbitDlqConfig {

    public static final String EXCHANGE = "q015.main.exchange";
    public static final String QUEUE = "q015.main.queue";
    public static final String ROUTING_KEY = "q015.main.key";

    public static final String DLX = "q015.dlx.exchange";
    public static final String DLQ = "q015.dlq.queue";
    public static final String DLQ_ROUTING_KEY = "q015.dlq.key";

    @Bean
    public DirectExchange q015MainExchange() { return new DirectExchange(EXCHANGE, true, false); }

    @Bean
    public DirectExchange q015DlxExchange() { return new DirectExchange(DLX, true, false); }

    @Bean
    public Queue q015MainQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue q015DlqQueue() { return QueueBuilder.durable(DLQ).build(); }

    @Bean
    public Binding q015MainBinding(DirectExchange q015MainExchange, Queue q015MainQueue) {
        return BindingBuilder.bind(q015MainQueue).to(q015MainExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding q015DlqBinding(DirectExchange q015DlxExchange, Queue q015DlqQueue) {
        return BindingBuilder.bind(q015DlqQueue).to(q015DlxExchange).with(DLQ_ROUTING_KEY);
    }
}
