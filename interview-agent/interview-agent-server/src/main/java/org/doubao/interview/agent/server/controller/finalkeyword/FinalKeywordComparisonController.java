package org.doubao.interview.agent.server.controller.finalkeyword;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.finalkeyword.FinalKeywordComparisonRequest;
import org.doubao.interview.agent.api.dto.finalkeyword.FinalKeywordComparisonResponse;
import org.doubao.interview.agent.api.service.finalkeyword.FinalKeywordComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * final、finally、finalize 区别演示控制器
 * <p>
 * 提供 HTTP 接口，用于演示三个关键字的核心区别
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/finalkeyword")
public class FinalKeywordComparisonController {

    /**
     * final、finally、finalize 对比演示服务
     */
    private final FinalKeywordComparisonService finalKeywordComparisonService;

    /**
     * 构造函数注入服务
     * <p>
     * 使用构造函数注入而非字段注入的优势：
     * 1. 保证依赖不可变
     * 2. 便于单元测试
     * 3. 避免空指针异常
     * </p>
     *
     * @param finalKeywordComparisonService final、finally、finalize 对比演示服务
     */
    @Autowired
    public FinalKeywordComparisonController(FinalKeywordComparisonService finalKeywordComparisonService) {
        this.finalKeywordComparisonService = finalKeywordComparisonService;
    }

    /**
     * 演示 final、finally、finalize 特性接口
     * <p>
     * 根据请求的关键字类型，返回对应的演示结果和详细说明
     * 支持三种类型：FINAL(final)、FINALLY(finally)、FINALIZE(finalize)
     * </p>
     *
     * @param request 请求参数，包含要演示的关键字类型和可选的演示参数
     * @return 演示结果响应，包含特性说明、对比表格等
     *
     * 示例请求：
     * POST /interview-agent/finalkeyword/demonstrate
     * Content-Type: application/json
     * {
     *   "keywordType": "FINAL"
     * }
     */
    @PostMapping("/demonstrate")
    public FinalKeywordComparisonResponse demonstrateFeature(@RequestBody FinalKeywordComparisonRequest request) {
        log.info("收到演示 final/finally/finalize 特性的请求，keywordType={}, demoParameter={}", 
                request.getKeywordType(), request.getDemoParameter());

        long startTime = System.currentTimeMillis();
        
        try {
            // 调用服务进行特性演示
            FinalKeywordComparisonResponse response = finalKeywordComparisonService.demonstrateFeature(request);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("演示完成，keywordType={}, success={}, serviceCostTime={}ms", 
                    request.getKeywordType(), response.getSuccess(), costTime);
            
            return response;
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("演示失败，keywordType={}, costTime={}ms, error={}", 
                    request.getKeywordType(), costTime, e.getMessage(), e);
            
            // 兜底逻辑：返回错误响应
            return FinalKeywordComparisonResponse.builder()
                    .keywordType(request.getKeywordType())
                    .keywordName("未知")
                    .description("演示过程中发生错误")
                    .keyFeatures("")
                    .codeExample("")
                    .comparisonTable("")
                    .usageScenarios("")
                    .precautions("")
                    .success(false)
                    .errorMessage("系统异常：" + e.getMessage())
                    .build();
        }
    }
}
