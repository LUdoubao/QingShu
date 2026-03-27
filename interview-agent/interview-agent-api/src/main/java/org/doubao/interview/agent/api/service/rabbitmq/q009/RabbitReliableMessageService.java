package org.doubao.interview.agent.api.service.rabbitmq.q009;

import org.doubao.interview.agent.api.dto.rabbitmq.q009.RabbitReliableResponse;
import org.doubao.interview.agent.api.dto.rabbitmq.q009.RabbitReliableSendRequest;

/** 问题009(RabbitMQ)：消息不丢服务。 */
public interface RabbitReliableMessageService {
    RabbitReliableResponse send(RabbitReliableSendRequest request);
    RabbitReliableResponse inspect(String bizId);
}
