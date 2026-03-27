package org.doubao.interview.agent.server.controller.rabbitmq.q008;

import org.doubao.interview.agent.api.dto.rabbitmq.q008.RabbitPersistDemoRequest;
import org.doubao.interview.agent.api.dto.rabbitmq.q008.RabbitPersistDemoResponse;
import org.doubao.interview.agent.api.service.rabbitmq.q008.RabbitPersistDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 问题008(RabbitMQ)：消息持久化控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/q008-rabbitmq-persist")
public class RabbitPersistDemoController {

    private final RabbitPersistDemoService persistDemoService;

    public RabbitPersistDemoController(RabbitPersistDemoService persistDemoService) {
        this.persistDemoService = persistDemoService;
    }

    @PostMapping("/send")
    public RabbitPersistDemoResponse send(@RequestBody RabbitPersistDemoRequest request) {
        return persistDemoService.sendPersistent(request);
    }
}
