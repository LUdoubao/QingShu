package org.doubao.interview.agent.api.service.rabbitmq.q006;

import org.doubao.interview.agent.api.dto.rabbitmq.q006.ProducerConfirmResultResponse;
import org.doubao.interview.agent.api.dto.rabbitmq.q006.ProducerConfirmSendRequest;

/** 问题006：RabbitMQ 生产者 confirm 服务接口。 */
public interface ProducerConfirmDemoService {

    ProducerConfirmResultResponse sendOne(ProducerConfirmSendRequest request);

    ProducerConfirmResultResponse sendBatch(int count);
}
