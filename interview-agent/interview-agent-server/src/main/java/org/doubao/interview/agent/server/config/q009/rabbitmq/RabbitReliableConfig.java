package org.doubao.interview.agent.server.config.q009.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 问题009(RabbitMQ)：可靠消息配置。
 *
 * 设计说明：
 * 1. 业务队列 durable，消息可持久化存储。
 * 2. 配置死信交换机与死信队列，消费失败时兜底。
 */
@Configuration
public class RabbitReliableConfig {

    public static final String EXCHANGE = "q009.reliable.exchange";
    public static final String QUEUE = "q009.reliable.queue";
    public static final String ROUTING_KEY = "q009.reliable.key";

    public static final String DLX = "q009.reliable.dlx";
    public static final String DLQ = "q009.reliable.dlq";
    public static final String DLQ_ROUTING_KEY = "q009.reliable.dlq.key";

    @Bean
    public DirectExchange q009Exchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange q009Dlx() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue q009Queue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue q009Dlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding q009Binding(DirectExchange q009Exchange, Queue q009Queue) {
        return BindingBuilder.bind(q009Queue).to(q009Exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding q009DlqBinding(DirectExchange q009Dlx, Queue q009Dlq) {
        return BindingBuilder.bind(q009Dlq).to(q009Dlx).with(DLQ_ROUTING_KEY);
    }
}
