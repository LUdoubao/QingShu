package org.doubao.interview.agent.server.controller.q007.jvm;

import org.doubao.interview.agent.api.dto.q007.jvm.SynchronizedDemoRequest;
import org.doubao.interview.agent.api.dto.q007.jvm.SynchronizedDemoResponse;
import org.doubao.interview.agent.api.service.q007.jvm.SynchronizedDemoService;
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
