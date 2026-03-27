package org.doubao.interview.agent.server.controller.q015.rabbitmq;

import org.doubao.interview.agent.api.dto.q015.rabbitmq.RabbitDlqResponse;
import org.doubao.interview.agent.api.dto.q015.rabbitmq.RabbitDlqSendRequest;
import org.doubao.interview.agent.api.service.q015.rabbitmq.RabbitDlqDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 问题015(RabbitMQ)：死信队列控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/rabbitmq/q015-dlq")
public class RabbitDlqDemoController {

    private final RabbitDlqDemoService dlqDemoService;

    public RabbitDlqDemoController(RabbitDlqDemoService dlqDemoService) {
        this.dlqDemoService = dlqDemoService;
    }

    @PostMapping("/send")
    public RabbitDlqResponse send(@RequestBody RabbitDlqSendRequest request) {
        return dlqDemoService.send(request);
    }

    @GetMapping("/inspect")
    public RabbitDlqResponse inspect(@RequestParam("bizId") String bizId) {
        return dlqDemoService.inspect(bizId);
    }

    @PostMapping("/replay")
    public RabbitDlqResponse replay(@RequestParam("bizId") String bizId) {
        return dlqDemoService.replay(bizId);
    }
}
