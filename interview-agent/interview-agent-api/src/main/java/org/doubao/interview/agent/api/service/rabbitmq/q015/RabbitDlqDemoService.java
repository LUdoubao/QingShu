package org.doubao.interview.agent.api.service.rabbitmq.q015;

import org.doubao.interview.agent.api.dto.rabbitmq.q015.RabbitDlqResponse;
import org.doubao.interview.agent.api.dto.rabbitmq.q015.RabbitDlqSendRequest;

/** 问题015(RabbitMQ)：死信队列服务。 */
public interface RabbitDlqDemoService {
    RabbitDlqResponse send(RabbitDlqSendRequest request);
    RabbitDlqResponse inspect(String bizId);
    RabbitDlqResponse replay(String bizId);
}
