package org.doubao.interview.agent.api.service.q009.rabbitmq;

import org.doubao.interview.agent.api.dto.q009.rabbitmq.RabbitReliableResponse;
import org.doubao.interview.agent.api.dto.q009.rabbitmq.RabbitReliableSendRequest;

/** 问题009(RabbitMQ)：消息不丢服务。 */
public interface RabbitReliableMessageService {
    RabbitReliableResponse send(RabbitReliableSendRequest request);
    RabbitReliableResponse inspect(String bizId);
}
