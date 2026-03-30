package org.doubao.interview.agent.server.controller.oop;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.oop.OopFeatureRequest;
import org.doubao.interview.agent.api.dto.oop.OopFeatureResponse;
import org.doubao.interview.agent.api.service.oop.OopFeatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 面向对象三大特征演示控制器
 * <p>
 * 提供 HTTP 接口，用于演示面向对象的三大基本特征（封装、继承、多态）
 * </p>
 *
 * @author interview-agent
 * @date 2026-03-30
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/oop")
public class OopFeatureController {

    /**
     * 面向对象特征演示服务
     */
    private final OopFeatureService oopFeatureService;

    /**
     * 构造函数注入服务
     * <p>
     * 使用构造函数注入而非字段注入的优势：
     * 1. 保证依赖不可变
     * 2. 便于单元测试
     * 3. 避免空指针异常
     * </p>
     *
     * @param oopFeatureService 面向对象特征演示服务
     */
    @Autowired
    public OopFeatureController(OopFeatureService oopFeatureService) {
        this.oopFeatureService = oopFeatureService;
    }

    /**
     * 演示面向对象特征接口
     * <p>
     * 根据请求的特征类型，返回对应的演示结果和详细说明
     * 支持三种特征：ENCAPSULATION(封装)、INHERITANCE(继承)、POLYMORPHISM(多态)
     * </p>
     *
     * @param request 请求参数，包含要演示的特征类型和可选的演示参数
     * @return 演示结果响应，包含特征名称、描述、核心要点、示例代码等
     *
     * 示例请求：
     * POST /interview-agent/oop/demonstrate
     * Content-Type: application/json
     * {
     *   "featureType": "ENCAPSULATION"
     * }
     */
    @PostMapping("/demonstrate")
    public OopFeatureResponse demonstrateFeature(@RequestBody OopFeatureRequest request) {
        log.info("收到演示面向对象特征的请求，featureType={}, demoParameter={}", 
                request.getFeatureType(), request.getDemoParameter());

        long startTime = System.currentTimeMillis();
        
        try {
            // 调用服务进行特征演示
            OopFeatureResponse response = oopFeatureService.demonstrateFeature(request);
            
            long costTime = System.currentTimeMillis() - startTime;
            log.info("演示完成，featureType={}, success={}, costTime={}ms", 
                    request.getFeatureType(), response.getSuccess(), costTime);
            
            return response;
            
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            log.error("演示失败，featureType={}, costTime={}ms, error={}", 
                    request.getFeatureType(), costTime, e.getMessage(), e);
            
            // 兜底逻辑：返回错误响应
            return OopFeatureResponse.builder()
                    .featureType(request.getFeatureType())
                    .featureName("未知")
                    .description("演示过程中发生错误")
                    .keyPoints("")
                    .codeExample("")
                    .success(false)
                    .errorMessage("系统异常：" + e.getMessage())
                    .build();
        }
    }
}
