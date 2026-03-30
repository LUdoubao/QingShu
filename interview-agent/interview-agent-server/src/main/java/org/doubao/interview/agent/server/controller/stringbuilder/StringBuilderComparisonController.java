package org.doubao.interview.agent.server.controller.stringbuilder;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.stringbuilder.StringBuilderComparisonRequest;
import org.doubao.interview.agent.api.dto.stringbuilder.StringBuilderComparisonResponse;
import org.doubao.interview.agent.api.service.stringbuilder.StringBuilderComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * String、StringBuffer、StringBuilder 区别演示控制器
 * <p>
 * 提供 HTTP 接口，用于演示三种字符串类的核心区别
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/stringbuilder")
public class StringBuilderComparisonController {

    /**
     * 字符串类对比演示服务
     */
    private final StringBuilderComparisonService stringBuilderComparisonService;

    /**
     * 构造函数注入服务
     * <p>
     * 使用构造函数注入而非字段注入的优势：
     * 1. 保证依赖不可变
     * 2. 便于单元测试
     * 3. 避免空指针异常
     * </p>
     *
     * @param stringBuilderComparisonService 字符串类对比演示服务
     */
    @Autowired
    public StringBuilderComparisonController(StringBuilderComparisonService stringBuilderComparisonService) {
        this.stringBuilderComparisonService = stringBuilderComparisonService;
    }

    /**
     * 演示字符串类特性接口
     * <p>
     * 根据请求的字符串类类型，返回对应的演示结果和详细说明
     * 支持三种类型：STRING(String)、STRINGBUFFER(StringBuffer)、STRINGBUILDER(StringBuilder)
     * </p>
     *
     * @param request 请求参数，包含要演示的字符串类类型和可选的循环次数
     * @return 演示结果响应，包含特性说明、性能测试、对比表格等
     *
     * 示例请求：
     * POST /interview-agent/stringbuilder/demonstrate
     * Content-Type: application/json
     * {
     *   "stringType": "STRINGBUILDER"
     * }
     */
    @PostMapping("/demonstrate")
    public StringBuilderComparisonResponse demonstrateFeature(@RequestBody StringBuilderComparisonRequest request) {
        log.info("收到演示字符串类特性的请求，stringType={}, loopCount={}", 
                request.getStringType(), request.getLoopCount());

        long startTime = System.currentTimeMillis();
        
        try {
            // 调用服务进行特性演示
            StringBuilderComparisonResponse response = stringBuilderComparisonService.demonstrateFeature(request);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("演示完成，stringType={}, success={}, serviceCostTime={}ms", 
                    request.getStringType(), response.getSuccess(), costTime);
            
            return response;
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("演示失败，stringType={}, costTime={}ms, error={}", 
                    request.getStringType(), costTime, e.getMessage(), e);
            
            // 兜底逻辑：返回错误响应
            return StringBuilderComparisonResponse.builder()
                    .stringType(request.getStringType())
                    .typeName("未知")
                    .description("演示过程中发生错误")
                    .keyFeatures("")
                    .performanceTime(0L)
                    .codeExample("")
                    .comparisonTable("")
                    .usageScenarios("")
                    .success(false)
                    .errorMessage("系统异常：" + e.getMessage())
                    .build();
        }
    }
}
