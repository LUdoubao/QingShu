package org.doubao.interview.agent.server.controller.equals;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.equals.EqualsComparisonRequest;
import org.doubao.interview.agent.api.dto.equals.EqualsComparisonResponse;
import org.doubao.interview.agent.api.service.equals.EqualsComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ==和 equals() 区别演示控制器
 * <p>
 * 提供 HTTP 接口，用于演示==和 equals() 方法的比较区别
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/equals")
public class EqualsComparisonController {

    /**
     * ==和 equals() 比较演示服务
     */
    private final EqualsComparisonService equalsComparisonService;

    /**
     * 构造函数注入服务
     * <p>
     * 使用构造函数注入而非字段注入的优势：
     * 1. 保证依赖不可变
     * 2. 便于单元测试
     * 3. 避免空指针异常
     * </p>
     *
     * @param equalsComparisonService ==和 equals() 比较演示服务
     */
    @Autowired
    public EqualsComparisonController(EqualsComparisonService equalsComparisonService) {
        this.equalsComparisonService = equalsComparisonService;
    }

    /**
     * 演示比较操作接口
     * <p>
     * 根据请求的比较类型，返回对应的演示结果和详细说明
     * 支持四种类型：BASIC(基本类型)、STRING(字符串)、WRAPPER(包装类)、CUSTOM(自定义对象)
     * </p>
     *
     * @param request 请求参数，包含要演示的比较类型和可选的演示参数
     * @return 演示结果响应，包含比较说明、示例代码、对比表格等
     *
     * 示例请求：
     * POST /interview-agent/equals/demonstrate
     * Content-Type: application/json
     * {
     *   "comparisonType": "STRING"
     * }
     */
    @PostMapping("/demonstrate")
    public EqualsComparisonResponse demonstrateComparison(@RequestBody EqualsComparisonRequest request) {
        log.info("收到演示比较操作的请求，comparisonType={}, demoParameter={}", 
                request.getComparisonType(), request.getDemoParameter());

        long startTime = System.currentTimeMillis();
        
        try {
            // 调用服务进行比较演示
            EqualsComparisonResponse response = equalsComparisonService.demonstrateComparison(request);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("演示完成，comparisonType={}, success={}, costTime={}ms", 
                    request.getComparisonType(), response.getSuccess(), costTime);
            
            return response;
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("演示失败，comparisonType={}, costTime={}ms, error={}", 
                    request.getComparisonType(), costTime, e.getMessage(), e);
            
            // 兜底逻辑：返回错误响应
            return EqualsComparisonResponse.builder()
                    .comparisonType(request.getComparisonType())
                    .typeName("未知")
                    .description("演示过程中发生错误")
                    .keyPoints("")
                    .codeExample("")
                    .comparisonTable("")
                    .success(false)
                    .errorMessage("系统异常：" + e.getMessage())
                    .build();
        }
    }
}
