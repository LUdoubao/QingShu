package org.doubao.interview.agent.server.controller.q006.jvm;

import org.doubao.interview.agent.api.dto.q006.jvm.VolatileDemoRequest;
import org.doubao.interview.agent.api.dto.q006.jvm.VolatileDemoResponse;
import org.doubao.interview.agent.api.service.q006.jvm.VolatileDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题006(JVM)：volatile 演示控制器。
 */
@RestController
@RequestMapping("/interview-agent/questions/jvm/q006-volatile")
public class VolatileDemoController {

    private final VolatileDemoService volatileDemoService;

    public VolatileDemoController(VolatileDemoService volatileDemoService) {
        this.volatileDemoService = volatileDemoService;
    }

    @PostMapping("/run")
    public VolatileDemoResponse run(@RequestBody VolatileDemoRequest request) {
        return volatileDemoService.run(request);
    }
}
