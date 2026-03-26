package org.doubao.interview.agent.server.controller.q011;

import org.doubao.interview.agent.api.dto.q011.CacheDbConsistencyQueryRequest;
import org.doubao.interview.agent.api.dto.q011.CacheDbConsistencyResponse;
import org.doubao.interview.agent.api.dto.q011.CacheDbConsistencyUpdateRequest;
import org.doubao.interview.agent.api.service.q011.CacheDbConsistencyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题011：双写一致性控制器。
 *
 * 分层说明：
 * 1. Controller 只负责HTTP参数接收和响应返回。
 * 2. 核心一致性策略由 Service 实现，保持职责单一。
 */
@RestController
@RequestMapping("/interview-agent/questions/q011-consistency")
public class CacheDbConsistencyController {

    private final CacheDbConsistencyService consistencyService;

    public CacheDbConsistencyController(CacheDbConsistencyService consistencyService) {
        this.consistencyService = consistencyService;
    }

    /**
     * 更新接口：演示写库后删缓存的主流程。
     */
    @PostMapping("/update")
    public CacheDbConsistencyResponse update(@RequestBody CacheDbConsistencyUpdateRequest request) {
        return consistencyService.update(request);
    }

    /**
     * 查询接口：观察缓存命中/失效后的回源与重建行为。
     */
    @PostMapping("/query")
    public CacheDbConsistencyResponse query(@RequestBody CacheDbConsistencyQueryRequest request) {
        return consistencyService.query(request);
    }
}
