package org.doubao.interview.agent.api.service.q016.rabbitmq;

import org.doubao.interview.agent.api.dto.q016.rabbitmq.RabbitDelayResponse;
import org.doubao.interview.agent.api.dto.q016.rabbitmq.RabbitDelaySendRequest;

/** 问题016(RabbitMQ)：延迟消息服务。 */
public interface RabbitDelayDemoService {
    RabbitDelayResponse send(RabbitDelaySendRequest request);
    RabbitDelayResponse inspect(String bizId);
}
