package org.doubao.interview.agent.server.controller.question006;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question006.BeanLifecycleRequest;
import org.doubao.interview.agent.api.dto.question006.BeanLifecycleResponse;
import org.doubao.interview.agent.api.service.question006.BeanLifecycleDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Spring Bean 生命周期演示 Controller
 * 
 * 对应面试知识点：问题 006 - Spring Bean 的生命周期
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示 Bean 的完整生命周期流程
 * 边界：仅用于学习和演示
 * 线程安全：Controller 本身无状态，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question006")
public class BeanLifecycleDemoController {

    @Autowired
    private BeanLifecycleDemoService beanLifecycleDemoService;

    /**
     * 演示 Bean 生命周期
     */
    @PostMapping("/lifecycle")
    public BeanLifecycleResponse demoLifecycle(@RequestBody BeanLifecycleRequest request) {
        log.info("收到 Bean 生命周期演示请求，beanName={}, fullLifecycle={}", 
                request.getBeanName(), request.getFullLifecycle());
        return beanLifecycleDemoService.demoLifecycle(request);
    }

    /**
     * 获取详细说明
     */
    @GetMapping("/explanation")
    public String getDetailedExplanation() {
        log.info("获取 Bean 生命周期详细说明");
        return beanLifecycleDemoService.getDetailedExplanation();
    }
}
