package org.doubao.interview.agent.server.controller.redis.q017;

import org.doubao.interview.agent.api.dto.redis.q017.AofRewriteRequest;
import org.doubao.interview.agent.api.dto.redis.q017.AofRewriteResponse;
import org.doubao.interview.agent.api.service.redis.q017.AofRewriteDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 问题017：AOF rewrite 控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/q017-aof-rewrite")
public class AofRewriteDemoController {

    private final AofRewriteDemoService demoService;

    public AofRewriteDemoController(AofRewriteDemoService demoService) {
        this.demoService = demoService;
    }

    /** 触发 AOF rewrite 模拟。 */
    @PostMapping("/run")
    public AofRewriteResponse run(@RequestBody AofRewriteRequest request) {
        return demoService.rewrite(request);
    }
}
