package org.doubao.interview.agent.server.controller.jvm.q008;

import org.doubao.interview.agent.api.dto.jvm.q008.LockChoiceDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q008.LockChoiceDemoResponse;
import org.doubao.interview.agent.api.service.jvm.q008.LockChoiceDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 问题008(JVM)：锁选择控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/jvm/q008-lock-choice")
public class LockChoiceDemoController {

    private final LockChoiceDemoService lockChoiceDemoService;

    public LockChoiceDemoController(LockChoiceDemoService lockChoiceDemoService) {
        this.lockChoiceDemoService = lockChoiceDemoService;
    }

    @PostMapping("/run")
    public LockChoiceDemoResponse run(@RequestBody LockChoiceDemoRequest request) {
        return lockChoiceDemoService.run(request);
    }
}
