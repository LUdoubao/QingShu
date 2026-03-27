package org.doubao.interview.agent.server.controller.jvm.q011;

import org.doubao.interview.agent.api.dto.jvm.q011.PessimisticOptimisticLockRequest;
import org.doubao.interview.agent.api.dto.jvm.q011.PessimisticOptimisticLockResponse;
import org.doubao.interview.agent.api.service.jvm.q011.PessimisticOptimisticLockService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interview-agent/questions/jvm/q011-lock-mode")
public class PessimisticOptimisticLockController {

    private final PessimisticOptimisticLockService lockService;

    public PessimisticOptimisticLockController(PessimisticOptimisticLockService lockService) {
        this.lockService = lockService;
    }

    @PostMapping("/run")
    public PessimisticOptimisticLockResponse run(@RequestBody PessimisticOptimisticLockRequest request) {
        return lockService.run(request);
    }
}
