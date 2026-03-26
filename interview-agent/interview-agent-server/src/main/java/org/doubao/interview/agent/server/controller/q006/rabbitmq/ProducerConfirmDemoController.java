package org.doubao.interview.agent.server.controller.q006.rabbitmq;

import org.doubao.interview.agent.api.dto.q006.rabbitmq.ProducerConfirmResultResponse;
import org.doubao.interview.agent.api.dto.q006.rabbitmq.ProducerConfirmSendRequest;
import org.doubao.interview.agent.api.service.q006.rabbitmq.ProducerConfirmDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 问题006：生产者 confirm 控制器（RabbitMQ 分包版本）。 */
@RestController
@RequestMapping("/interview-agent/questions/q006-rabbitmq-confirm")
public class ProducerConfirmDemoController {

    private final ProducerConfirmDemoService confirmDemoService;

    public ProducerConfirmDemoController(ProducerConfirmDemoService confirmDemoService) {
        this.confirmDemoService = confirmDemoService;
    }

    @PostMapping("/send-one")
    public ProducerConfirmResultResponse sendOne(@RequestBody ProducerConfirmSendRequest request) {
        return confirmDemoService.sendOne(request);
    }

    @PostMapping("/send-batch")
    public ProducerConfirmResultResponse sendBatch(@RequestParam("count") int count) {
        return confirmDemoService.sendBatch(count);
    }
}
