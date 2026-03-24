package org.doubao.search.service.config;

import org.doubao.mall.common.constant.Constants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchRabbitMQConfig {

    public static final String SEARCH_SYNC_QUEUE = "search.sync.queue";

    @Bean
    public TopicExchange searchFanoutEventExchange() {
        return ExchangeBuilder.topicExchange(Constants.FANOUT_EVENT_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue searchSyncQueue() {
        return QueueBuilder.durable(SEARCH_SYNC_QUEUE).build();
    }

    @Bean
    public Binding searchSyncBinding() {
        return BindingBuilder.bind(searchSyncQueue())
                .to(searchFanoutEventExchange())
                .with(Constants.USER_QUOTE_ROUTING_KEY);
    }
}
