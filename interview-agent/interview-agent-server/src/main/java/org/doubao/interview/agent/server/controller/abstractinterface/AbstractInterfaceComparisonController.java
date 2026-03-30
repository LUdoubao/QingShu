package org.doubao.interview.agent.server.controller.abstractinterface;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.abstractinterface.AbstractInterfaceComparisonRequest;
import org.doubao.interview.agent.api.dto.abstractinterface.AbstractInterfaceComparisonResponse;
import org.doubao.interview.agent.api.service.abstractinterface.AbstractInterfaceComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口和抽象类区别演示控制器
 * <p>
 * 提供 HTTP 接口，用于演示接口和抽象类的核心区别
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/abstractinterface")
public class AbstractInterfaceComparisonController {

    /**
     * 接口和抽象类对比演示服务
     */
    private final AbstractInterfaceComparisonService abstractInterfaceComparisonService;

    /**
     * 构造函数注入服务
     * <p>
     * 使用构造函数注入而非字段注入的优势：
     * 1. 保证依赖不可变
     * 2. 便于单元测试
     * 3. 避免空指针异常
     * </p>
     *
     * @param abstractInterfaceComparisonService 接口和抽象类对比演示服务
     */
    @Autowired
    public AbstractInterfaceComparisonController(AbstractInterfaceComparisonService abstractInterfaceComparisonService) {
        this.abstractInterfaceComparisonService = abstractInterfaceComparisonService;
    }

    /**
     * 演示接口或抽象类特性接口
     * <p>
     * 根据请求的类型，返回对应的演示结果和详细说明
     * 支持两种类型：ABSTRACT_CLASS(抽象类)、INTERFACE(接口)
     * </p>
     *
     * @param request 请求参数，包含要演示的类型和可选的演示参数
     * @return 演示结果响应，包含特性说明、对比表格等
     *
     * 示例请求：
     * POST /interview-agent/abstractinterface/demonstrate
     * Content-Type: application/json
     * {
     *   "targetType": "ABSTRACT_CLASS"
     * }
     */
    @PostMapping("/demonstrate")
    public AbstractInterfaceComparisonResponse demonstrateFeature(@RequestBody AbstractInterfaceComparisonRequest request) {
        log.info("收到演示接口或抽象类特性的请求，targetType={}, demoParameter={}", 
                request.getTargetType(), request.getDemoParameter());

        long startTime = System.currentTimeMillis();
        
        try {
            // 调用服务进行特性演示
            AbstractInterfaceComparisonResponse response = abstractInterfaceComparisonService.demonstrateFeature(request);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("演示完成，targetType={}, success={}, serviceCostTime={}ms", 
                    request.getTargetType(), response.getSuccess(), costTime);
            
            return response;
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("演示失败，targetType={}, costTime={}ms, error={}", 
                    request.getTargetType(), costTime, e.getMessage(), e);
            
            // 兜底逻辑：返回错误响应
            return AbstractInterfaceComparisonResponse.builder()
                    .targetType(request.getTargetType())
                    .typeName("未知")
                    .description("演示过程中发生错误")
                    .keyFeatures("")
                    .codeExample("")
                    .comparisonTable("")
                    .usageScenarios("")
                    .success(false)
                    .errorMessage("系统异常：" + e.getMessage())
                    .build();
        }
    }
}
