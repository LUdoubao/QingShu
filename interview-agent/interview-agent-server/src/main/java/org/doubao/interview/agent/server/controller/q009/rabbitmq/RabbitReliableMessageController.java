package org.doubao.interview.agent.server.controller.q009.rabbitmq;

import org.doubao.interview.agent.api.dto.q009.rabbitmq.RabbitReliableResponse;
import org.doubao.interview.agent.api.dto.q009.rabbitmq.RabbitReliableSendRequest;
import org.doubao.interview.agent.api.service.q009.rabbitmq.RabbitReliableMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 问题009(RabbitMQ)：消息不丢控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/q009-rabbitmq-reliable")
public class RabbitReliableMessageController {

    private final RabbitReliableMessageService reliableMessageService;

    public RabbitReliableMessageController(RabbitReliableMessageService reliableMessageService) {
        this.reliableMessageService = reliableMessageService;
    }

    @PostMapping("/send")
    public RabbitReliableResponse send(@RequestBody RabbitReliableSendRequest request) {
        return reliableMessageService.send(request);
    }

    @GetMapping("/inspect")
    public RabbitReliableResponse inspect(@RequestParam("bizId") String bizId) {
        return reliableMessageService.inspect(bizId);
    }
}
