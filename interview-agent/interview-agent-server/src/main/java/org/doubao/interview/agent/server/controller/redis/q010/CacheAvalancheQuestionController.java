package org.doubao.interview.agent.server.controller.redis.q010;

import org.doubao.interview.agent.api.dto.redis.q010.CacheAvalancheQueryRequest;
import org.doubao.interview.agent.api.dto.redis.q010.CacheAvalancheQueryResponse;
import org.doubao.interview.agent.api.service.redis.q010.CacheAvalancheGovernanceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题010：缓存雪崩治理控制器。
 */
@RestController
@RequestMapping("/interview-agent/questions/q010-avalanche")
public class CacheAvalancheQuestionController {

    private final CacheAvalancheGovernanceService governanceService;

    public CacheAvalancheQuestionController(CacheAvalancheGovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    /**
     * 雪崩治理演示接口。
     */
    @PostMapping("/query")
    public CacheAvalancheQueryResponse query(@RequestBody CacheAvalancheQueryRequest request) {
        return governanceService.query(request);
    }
}
