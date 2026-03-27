package org.doubao.interview.agent.server.controller.q009.jvm;

import org.doubao.interview.agent.api.dto.q009.jvm.ReentrantLockDemoRequest;
import org.doubao.interview.agent.api.dto.q009.jvm.ReentrantLockDemoResponse;
import org.doubao.interview.agent.api.service.q009.jvm.ReentrantLockDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 问题009(JVM)：可重入锁控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/jvm/q009-reentrant")
public class ReentrantLockDemoController {

    private final ReentrantLockDemoService reentrantLockDemoService;

    public ReentrantLockDemoController(ReentrantLockDemoService reentrantLockDemoService) {
        this.reentrantLockDemoService = reentrantLockDemoService;
    }

    @PostMapping("/run")
    public ReentrantLockDemoResponse run(@RequestBody ReentrantLockDemoRequest request) {
        return reentrantLockDemoService.run(request);
    }
}
