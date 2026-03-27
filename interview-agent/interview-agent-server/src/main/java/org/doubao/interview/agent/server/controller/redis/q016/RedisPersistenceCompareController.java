package org.doubao.interview.agent.server.controller.redis.q016;

import org.doubao.interview.agent.api.dto.redis.q016.RedisPersistenceCompareRequest;
import org.doubao.interview.agent.api.dto.redis.q016.RedisPersistenceCompareResponse;
import org.doubao.interview.agent.api.service.redis.q016.RedisPersistenceCompareService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题016：RDB/AOF 对比控制器。
 */
@RestController
@RequestMapping("/interview-agent/questions/q016-persistence")
public class RedisPersistenceCompareController {

    private final RedisPersistenceCompareService compareService;

    public RedisPersistenceCompareController(RedisPersistenceCompareService compareService) {
        this.compareService = compareService;
    }

    /**
     * 对比 RDB、AOF、混合模式的恢复速度、文件体积和丢失窗口。
     */
    @PostMapping("/compare")
    public RedisPersistenceCompareResponse compare(@RequestBody RedisPersistenceCompareRequest request) {
        return compareService.compare(request);
    }
}
