package org.doubao.interview.agent.api.service.q015.rabbitmq;

import org.doubao.interview.agent.api.dto.q015.rabbitmq.RabbitDlqResponse;
import org.doubao.interview.agent.api.dto.q015.rabbitmq.RabbitDlqSendRequest;

/** 问题015(RabbitMQ)：死信队列服务。 */
public interface RabbitDlqDemoService {
    RabbitDlqResponse send(RabbitDlqSendRequest request);
    RabbitDlqResponse inspect(String bizId);
    RabbitDlqResponse replay(String bizId);
}
