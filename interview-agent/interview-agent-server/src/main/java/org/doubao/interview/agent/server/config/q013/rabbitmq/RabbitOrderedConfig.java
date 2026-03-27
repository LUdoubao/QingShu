package org.doubao.interview.agent.server.config.q013.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 问题013(RabbitMQ)：局部有序队列拓扑。
 *
 * 核心策略：
 * 1. 同一 bizKey 固定路由到同一分片队列（局部有序）。
 * 2. 每个分片队列由单消费者串行处理。
 */
@Configuration
public class RabbitOrderedConfig {

    public static final String EXCHANGE = "q013.ordered.exchange";
    public static final String QUEUE_0 = "q013.ordered.queue.0";
    public static final String QUEUE_1 = "q013.ordered.queue.1";
    public static final String RK_0 = "q013.ordered.0";
    public static final String RK_1 = "q013.ordered.1";

    @Bean
    public DirectExchange q013Exchange() { return new DirectExchange(EXCHANGE, true, false); }

    @Bean
    public Queue q013Queue0() { return QueueBuilder.durable(QUEUE_0).build(); }

    @Bean
    public Queue q013Queue1() { return QueueBuilder.durable(QUEUE_1).build(); }

    @Bean
    public Binding q013Binding0(DirectExchange q013Exchange, Queue q013Queue0) {
        return BindingBuilder.bind(q013Queue0).to(q013Exchange).with(RK_0);
    }

    @Bean
    public Binding q013Binding1(DirectExchange q013Exchange, Queue q013Queue1) {
        return BindingBuilder.bind(q013Queue1).to(q013Exchange).with(RK_1);
    }
}
