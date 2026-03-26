package org.doubao.interview.agent.server.controller.q008;

import org.doubao.interview.agent.api.dto.q008.CachePenetrationCheckRequest;
import org.doubao.interview.agent.api.dto.q008.CachePenetrationCheckResponse;
import org.doubao.interview.agent.api.service.q008.CachePenetrationGovernanceService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题008：缓存穿透治理演示控制器。
 *
 * 该控制器只负责 HTTP 入参接收与转发，不承载业务判断，
 * 保持 Controller 层“薄职责”，便于单元测试与后续扩展。
 */
@RestController
@RequestMapping("/interview-agent/questions/008")
public class CachePenetrationQuestionController {

    private final CachePenetrationGovernanceService governanceService;

    public CachePenetrationQuestionController(CachePenetrationGovernanceService governanceService) {
        this.governanceService = governanceService;
    }

    /**
     * 演示缓存穿透治理链路。
     *
     * @param request 请求参数（dataId + clientId）
     * @return 分阶段治理结果
     */
    @PostMapping("/check")
    public CachePenetrationCheckResponse check(@RequestBody CachePenetrationCheckRequest request) {
        return governanceService.check(request);
    }
}