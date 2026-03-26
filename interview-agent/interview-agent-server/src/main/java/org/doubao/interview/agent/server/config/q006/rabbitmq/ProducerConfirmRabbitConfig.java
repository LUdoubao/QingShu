package org.doubao.interview.agent.server.config.q006.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 问题006：RabbitMQ 示例基础配置。
 *
 * 说明：
 * 1. 使用 direct exchange，便于演示 routingKey 命中与不命中。
 * 2. 开启 publisher-confirm/publisher-returns 后，可分别观察 confirm 与 return 回调。
 */
@Configuration
public class ProducerConfirmRabbitConfig {

    public static final String EXCHANGE = "q006.confirm.exchange";
    public static final String QUEUE = "q006.confirm.queue";
    public static final String ROUTING_KEY = "q006.confirm.key";

    @Bean
    public DirectExchange q006Exchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue q006Queue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding q006Binding(DirectExchange q006Exchange, Queue q006Queue) {
        return BindingBuilder.bind(q006Queue).to(q006Exchange).with(ROUTING_KEY);
    }
}
