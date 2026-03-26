package org.doubao.interview.agent.api.service.q006.rabbitmq;

import org.doubao.interview.agent.api.dto.q006.rabbitmq.ProducerConfirmResultResponse;
import org.doubao.interview.agent.api.dto.q006.rabbitmq.ProducerConfirmSendRequest;

/** 问题006：RabbitMQ 生产者 confirm 服务接口。 */
public interface ProducerConfirmDemoService {

    ProducerConfirmResultResponse sendOne(ProducerConfirmSendRequest request);

    ProducerConfirmResultResponse sendBatch(int count);
}
