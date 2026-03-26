package org.doubao.interview.agent.server.controller.q029;

import org.doubao.interview.agent.api.dto.q029.RedisWatchCasRequest;
import org.doubao.interview.agent.api.dto.q029.RedisWatchCasResponse;
import org.doubao.interview.agent.api.service.q029.RedisWatchCasService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 问题029：WATCH 乐观锁控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/q029-watch")
public class RedisWatchCasController {

    private final RedisWatchCasService watchCasService;

    public RedisWatchCasController(RedisWatchCasService watchCasService) {
        this.watchCasService = watchCasService;
    }

    /** 执行 WATCH -> GET -> MULTI -> SET -> EXEC 重试流程。 */
    @PostMapping("/increment")
    public RedisWatchCasResponse increment(@RequestBody RedisWatchCasRequest request) {
        return watchCasService.incrementByWatch(request);
    }
}
