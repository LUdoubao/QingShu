package org.doubao.interview.agent.api.service.rabbitmq.q013;

import org.doubao.interview.agent.api.dto.rabbitmq.q013.RabbitOrderedResponse;
import org.doubao.interview.agent.api.dto.rabbitmq.q013.RabbitOrderedSendRequest;

/** 问题013(RabbitMQ)：消息顺序性服务。 */
public interface RabbitOrderedMessageService {
    RabbitOrderedResponse send(RabbitOrderedSendRequest request);
    RabbitOrderedResponse inspect(String bizKey);
}
