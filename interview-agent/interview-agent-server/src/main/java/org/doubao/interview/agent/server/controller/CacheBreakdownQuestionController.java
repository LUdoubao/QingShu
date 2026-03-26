package org.doubao.interview.agent.server.controller;

import org.doubao.interview.agent.api.dto.CacheBreakdownQueryRequest;
import org.doubao.interview.agent.api.dto.CacheBreakdownQueryResponse;
import org.doubao.interview.agent.api.service.CacheBreakdownGovernanceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题009：缓存击穿治理演示控制器。
 *
 * 控制器职责保持轻量：只处理 HTTP 入参和结果返回，
 * 核心治理逻辑全部下沉到 Service，便于测试与复用。
 */
@RestController
@RequestMapping("/interview-agent/questions/009")
public class CacheBreakdownQuestionController {

    private final CacheBreakdownGovernanceService governanceService;

    public CacheBreakdownQuestionController(CacheBreakdownGovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    /**
     * 热点 key 查询入口。
     *
     * @param request 包含 hotKey 和 clientId
     * @return 分阶段治理结果
     */
    @PostMapping("/query")
    public CacheBreakdownQueryResponse query(@RequestBody CacheBreakdownQueryRequest request) {
        return governanceService.query(request);
    }
}