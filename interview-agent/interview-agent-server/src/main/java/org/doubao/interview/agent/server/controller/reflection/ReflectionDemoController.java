package org.doubao.interview.agent.server.controller.reflection;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.reflection.ReflectionDemoRequest;
import org.doubao.interview.agent.api.dto.reflection.ReflectionDemoResponse;
import org.doubao.interview.agent.api.service.reflection.ReflectionDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Java 反射机制演示控制器
 * <p>
 * 提供 HTTP 接口，用于演示反射机制的核心功能
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/reflection")
public class ReflectionDemoController {

    /**
     * Java 反射机制演示服务
     */
    private final ReflectionDemoService reflectionDemoService;

    /**
     * 构造函数注入服务
     * <p>
     * 使用构造函数注入而非字段注入的优势：
     * 1. 保证依赖不可变
     * 2. 便于单元测试
     * 3. 避免空指针异常
     * </p>
     *
     * @param reflectionDemoService Java 反射机制演示服务
     */
    @Autowired
    public ReflectionDemoController(ReflectionDemoService reflectionDemoService) {
        this.reflectionDemoService = reflectionDemoService;
    }

    /**
     * 演示反射机制特性接口
     * <p>
     * 根据请求的操作类型，返回对应的演示结果和详细说明
     * 支持五种操作：GET_CLASS、CREATE_INSTANCE、INVOKE_METHOD、ACCESS_FIELD、GET_CONSTRUCTOR
     * </p>
     *
     * @param request 请求参数，包含要演示的操作类型和相关参数
     * @return 演示结果响应，包含特性说明、API 使用示例等
     *
     * 示例请求：
     * POST /interview-agent/reflection/demonstrate
     * Content-Type: application/json
     * {
     *   "operationType": "INVOKE_METHOD",
     *   "className": "java.lang.String"
     * }
     */
    @PostMapping("/demonstrate")
    public ReflectionDemoResponse demonstrateFeature(@RequestBody ReflectionDemoRequest request) {
        log.info("收到演示反射机制特性的请求，operationType={}, className={}", 
                request.getOperationType(), request.getClassName());

        long startTime = System.currentTimeMillis();
        
        try {
            // 调用服务进行特性演示
            ReflectionDemoResponse response = reflectionDemoService.demonstrateFeature(request);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("演示完成，operationType={}, success={}, serviceCostTime={}ms", 
                    request.getOperationType(), response.getSuccess(), costTime);
            
            return response;
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("演示失败，operationType={}, costTime={}ms, error={}", 
                    request.getOperationType(), costTime, e.getMessage(), e);
            
            // 兜底逻辑：返回错误响应
            return ReflectionDemoResponse.builder()
                    .operationType(request.getOperationType())
                    .operationName("未知")
                    .description("演示过程中发生错误")
                    .keyFeatures("")
                    .codeExample("")
                    .reflectionApiUsage("")
                    .precautions("")
                    .actualResult(null)
                    .success(false)
                    .errorMessage("系统异常：" + e.getMessage())
                    .build();
        }
    }
}
