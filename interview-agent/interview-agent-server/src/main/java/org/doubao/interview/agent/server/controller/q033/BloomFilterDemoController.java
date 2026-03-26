package org.doubao.interview.agent.server.controller.q033;

import org.doubao.interview.agent.api.dto.q033.BloomFilterCheckRequest;
import org.doubao.interview.agent.api.dto.q033.BloomFilterCheckResponse;
import org.doubao.interview.agent.api.service.q033.BloomFilterDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 问题033：Bloom Filter 防穿透控制器。 */
@RestController
@RequestMapping("/interview-agent/questions/q033-bloom")
public class BloomFilterDemoController {

    private final BloomFilterDemoService bloomFilterDemoService;

    public BloomFilterDemoController(BloomFilterDemoService bloomFilterDemoService) {
        this.bloomFilterDemoService = bloomFilterDemoService;
    }

    /** 检查 key 是否通过布隆预过滤。 */
    @PostMapping("/check")
    public BloomFilterCheckResponse check(@RequestBody BloomFilterCheckRequest request) {
        return bloomFilterDemoService.check(request);
    }

    /** 重建布隆过滤器（模拟更新场景）。 */
    @PostMapping("/rebuild")
    public BloomFilterCheckResponse rebuild() {
        return bloomFilterDemoService.rebuild();
    }
}
