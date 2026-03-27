package org.doubao.interview.agent.server.config.q008.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 问题008(RabbitMQ)：持久化配置。
 *
 * 三步最小正确集：
 * 1. Exchange durable=true
 * 2. Queue durable=true
 * 3. 发送消息时 deliveryMode=2（持久化消息）
 */
@Configuration
public class RabbitPersistConfig {

    public static final String EXCHANGE = "q008.persist.exchange";
    public static final String QUEUE = "q008.persist.queue";
    public static final String ROUTING_KEY = "q008.persist.key";

    @Bean
    public DirectExchange q008PersistExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue q008PersistQueue() {
        return new Queue(QUEUE, true, false, false);
    }

    @Bean
    public Binding q008PersistBinding(DirectExchange q008PersistExchange, Queue q008PersistQueue) {
        return BindingBuilder.bind(q008PersistQueue).to(q008PersistExchange).with(ROUTING_KEY);
    }
}
