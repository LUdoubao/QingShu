package org.doubao.interview.agent.server.controller.method;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.method.MethodFeatureRequest;
import org.doubao.interview.agent.api.dto.method.MethodFeatureResponse;
import org.doubao.interview.agent.api.service.method.MethodFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 方法重载和重写演示控制器
 * <p>
 * 提供 HTTP 接口，用于演示方法重载（Overload）和方法重写（Override）的区别
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/method")
public class MethodFeatureController {

    /**
     * 方法特性演示服务
     */
    private final MethodFeatureService methodFeatureService;

    /**
     * 构造函数注入服务
     * <p>
     * 使用构造函数注入而非字段注入的优势：
     * 1. 保证依赖不可变
     * 2. 便于单元测试
     * 3. 避免空指针异常
     * </p>
     *
     * @param methodFeatureService 方法特性演示服务
     */
    @Autowired
    public MethodFeatureController(MethodFeatureService methodFeatureService) {
        this.methodFeatureService = methodFeatureService;
    }

    /**
     * 演示方法特性接口
     * <p>
     * 根据请求的特性类型，返回对应的演示结果和详细说明
     * 支持两种特性：OVERLOAD(重载)、OVERRIDE(重写)
     * </p>
     *
     * @param request 请求参数，包含要演示的特性类型和可选的演示参数
     * @return 演示结果响应，包含特性名称、描述、核心要点、示例代码、对比表格等
     *
     * 示例请求：
     * POST /interview-agent/method/demonstrate
     * Content-Type: application/json
     * {
     *   "featureType": "OVERLOAD"
     * }
     */
    @PostMapping("/demonstrate")
    public MethodFeatureResponse demonstrateFeature(@RequestBody MethodFeatureRequest request) {
        log.info("收到演示方法特性的请求，featureType={}, demoParameter={}", 
                request.getFeatureType(), request.getDemoParameter());

        long startTime = System.currentTimeMillis();
        
        try {
            // 调用服务进行特性演示
            MethodFeatureResponse response = methodFeatureService.demonstrateFeature(request);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("演示完成，featureType={}, success={}, costTime={}ms", 
                    request.getFeatureType(), response.getSuccess(), costTime);
            
            return response;
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("演示失败，featureType={}, costTime={}ms, error={}", 
                    request.getFeatureType(), costTime, e.getMessage(), e);
            
            // 兜底逻辑：返回错误响应
            return MethodFeatureResponse.builder()
                    .featureType(request.getFeatureType())
                    .featureName("未知")
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
