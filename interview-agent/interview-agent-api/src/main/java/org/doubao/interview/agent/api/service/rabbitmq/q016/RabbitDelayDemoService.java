package org.doubao.interview.agent.api.service.rabbitmq.q016;

import org.doubao.interview.agent.api.dto.rabbitmq.q016.RabbitDelayResponse;
import org.doubao.interview.agent.api.dto.rabbitmq.q016.RabbitDelaySendRequest;

/** 问题016(RabbitMQ)：延迟消息服务。 */
public interface RabbitDelayDemoService {
    RabbitDelayResponse send(RabbitDelaySendRequest request);
    RabbitDelayResponse inspect(String bizId);
}
