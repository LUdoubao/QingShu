package org.doubao.interview.agent.server.controller.q013.rabbitmq;

import org.doubao.interview.agent.api.dto.q013.rabbitmq.RabbitOrderedResponse;
import org.doubao.interview.agent.api.dto.q013.rabbitmq.RabbitOrderedSendRequest;
import org.doubao.interview.agent.api.service.q013.rabbitmq.RabbitOrderedMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 问题013(RabbitMQ)：顺序性控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/rabbitmq/q013-order")
public class RabbitOrderedController {

    private final RabbitOrderedMessageService orderedMessageService;

    public RabbitOrderedController(RabbitOrderedMessageService orderedMessageService) {
        this.orderedMessageService = orderedMessageService;
    }

    @PostMapping("/send")
    public RabbitOrderedResponse send(@RequestBody RabbitOrderedSendRequest request) {
        return orderedMessageService.send(request);
    }

    @GetMapping("/inspect")
    public RabbitOrderedResponse inspect(@RequestParam("bizKey") String bizKey) {
        return orderedMessageService.inspect(bizKey);
    }
}
