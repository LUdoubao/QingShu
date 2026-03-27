package org.doubao.interview.agent.api.service.q013.rabbitmq;

import org.doubao.interview.agent.api.dto.q013.rabbitmq.RabbitOrderedResponse;
import org.doubao.interview.agent.api.dto.q013.rabbitmq.RabbitOrderedSendRequest;

/** 问题013(RabbitMQ)：消息顺序性服务。 */
public interface RabbitOrderedMessageService {
    RabbitOrderedResponse send(RabbitOrderedSendRequest request);
    RabbitOrderedResponse inspect(String bizKey);
}
