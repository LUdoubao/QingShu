package org.doubao.search.service.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 索引更新队列
    public static final String COPYWRITING_INDEX_UPDATE_QUEUE = "copywriting-index-update";
    // 搜索统计队列
    public static final String SEARCH_STATISTICS_QUEUE = "search-statistics";
    
    // 交换机
    public static final String SEARCH_EXCHANGE = "search-exchange";
    
    // 路由键
    public static final String INDEX_UPDATE_ROUTING_KEY = "index.update";
    public static final String SEARCH_STATISTICS_ROUTING_KEY = "search.statistics";

    /**
     * 声明交换机
     */
    @Bean
    public DirectExchange searchExchange() {
        return ExchangeBuilder.directExchange(SEARCH_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 声明索引更新队列
     */
    @Bean
    public Queue copywritingIndexUpdateQueue() {
        return QueueBuilder.durable(COPYWRITING_INDEX_UPDATE_QUEUE)
                .withArgument("x-dead-letter-exchange", SEARCH_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", INDEX_UPDATE_ROUTING_KEY + ".dlq")
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    /**
     * 声明搜索统计队列
     */
    @Bean
    public Queue searchStatisticsQueue() {
        return QueueBuilder.durable(SEARCH_STATISTICS_QUEUE)
                .build();
    }

    /**
     * 绑定索引更新队列到交换机
     */
    @Bean
    public Binding bindIndexUpdateQueue(DirectExchange searchExchange, Queue copywritingIndexUpdateQueue) {
        return BindingBuilder.bind(copywritingIndexUpdateQueue)
                .to(searchExchange)
                .with(INDEX_UPDATE_ROUTING_KEY);
    }

    /**
     * 绑定搜索统计队列到交换机
     */
    @Bean
    public Binding bindSearchStatisticsQueue(DirectExchange searchExchange, Queue searchStatisticsQueue) {
        return BindingBuilder.bind(searchStatisticsQueue)
                .to(searchExchange)
                .with(SEARCH_STATISTICS_ROUTING_KEY);
    }
}
