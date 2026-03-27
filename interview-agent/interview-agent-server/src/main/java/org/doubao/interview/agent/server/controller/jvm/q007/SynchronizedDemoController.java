package org.doubao.interview.agent.server.controller.jvm.q007;

import org.doubao.interview.agent.api.dto.jvm.q007.SynchronizedDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q007.SynchronizedDemoResponse;
import org.doubao.interview.agent.api.service.jvm.q007.SynchronizedDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 问题007(JVM)：synchronized 控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/jvm/q007-synchronized")
public class SynchronizedDemoController {

    private final SynchronizedDemoService synchronizedDemoService;

    public SynchronizedDemoController(SynchronizedDemoService synchronizedDemoService) {
        this.synchronizedDemoService = synchronizedDemoService;
    }

    @PostMapping("/run")
    public SynchronizedDemoResponse run(@RequestBody SynchronizedDemoRequest request) {
        return synchronizedDemoService.run(request);
    }
}
