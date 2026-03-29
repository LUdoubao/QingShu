package org.doubao.interview.agent.server.controller.question005;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question005.ContainerDemoRequest;
import org.doubao.interview.agent.api.dto.question005.ContainerDemoResponse;
import org.doubao.interview.agent.api.service.question005.SpringContainerDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Spring 容器演示 Controller
 * 
 * 对应面试知识点：问题 005 - BeanFactory 和 ApplicationContext 区别
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示 BeanFactory 和 ApplicationContext 的差异
 * 边界：仅用于学习和演示
 * 线程安全：Controller 本身无状态，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question005")
public class SpringContainerDemoController {

    @Autowired
    private SpringContainerDemoService springContainerDemoService;

    /**
     * 演示 Spring 容器功能
     */
    @PostMapping("/demo")
    public ContainerDemoResponse demoContainer(@RequestBody ContainerDemoRequest request) {
        log.info("收到容器演示请求，containerType={}", request.getContainerType());
        return springContainerDemoService.demoContainer(request);
    }

    /**
     * 获取详细对比说明
     */
    @GetMapping("/comparison")
    public String getDetailedComparison() {
        log.info("获取详细对比说明");
        return springContainerDemoService.getDetailedComparison();
    }
}
