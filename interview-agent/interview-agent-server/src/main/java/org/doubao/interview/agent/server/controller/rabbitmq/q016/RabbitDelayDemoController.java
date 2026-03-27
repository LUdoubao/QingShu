package org.doubao.interview.agent.server.controller.rabbitmq.q016;

import org.doubao.interview.agent.api.dto.rabbitmq.q016.RabbitDelayResponse;
import org.doubao.interview.agent.api.dto.rabbitmq.q016.RabbitDelaySendRequest;
import org.doubao.interview.agent.api.service.rabbitmq.q016.RabbitDelayDemoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 问题016(RabbitMQ)：延迟消息控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/rabbitmq/q016-delay")
public class RabbitDelayDemoController {

    private final RabbitDelayDemoService delayDemoService;

    public RabbitDelayDemoController(RabbitDelayDemoService delayDemoService) {
        this.delayDemoService = delayDemoService;
    }

    @PostMapping("/send")
    public RabbitDelayResponse send(@RequestBody RabbitDelaySendRequest request) {
        return delayDemoService.send(request);
    }

    @GetMapping("/inspect")
    public RabbitDelayResponse inspect(@RequestParam("bizId") String bizId) {
        return delayDemoService.inspect(bizId);
    }
}
