package org.doubao.interview.agent.api.service.q008.rabbitmq;

import org.doubao.interview.agent.api.dto.q008.rabbitmq.RabbitPersistDemoRequest;
import org.doubao.interview.agent.api.dto.q008.rabbitmq.RabbitPersistDemoResponse;

/** 问题008(RabbitMQ)：消息持久化服务。 */
public interface RabbitPersistDemoService {
    RabbitPersistDemoResponse sendPersistent(RabbitPersistDemoRequest request);
}
