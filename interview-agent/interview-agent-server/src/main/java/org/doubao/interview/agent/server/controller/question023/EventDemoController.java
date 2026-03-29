package org.doubao.interview.agent.server.controller.question023;

import lombok.extern.slf4j.Slf4j;
import org.doubao.interview.agent.api.dto.question023.UserRegisterRequest;
import org.doubao.interview.agent.api.dto.question023.UserRegisterResponse;
import org.doubao.interview.agent.api.service.question023.EventDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Spring 事件机制演示 Controller
 * 
 * 对应面试知识点：问题 023 - Spring 事件机制
 * 
 * 【类注释】
 * 职责：提供 HTTP 接口，演示 Spring 事件的发布和监听
 * 边界：仅用于学习和演示
 * 线程安全：Controller 本身无状态，线程安全
 * 
 * @author interview-agent
 * @date 2026-03-29
 */
@Slf4j
@RestController
@RequestMapping("/interview-agent/question023")
public class EventDemoController {

    @Autowired
    private EventDemoService eventDemoService;

    /**
     * 用户注册 - 触发事件机制
     */
    @PostMapping("/register")
    public UserRegisterResponse register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("收到用户注册请求，userName={}, email={}, async={}", 
                request.getUserName(), request.getEmail(), request.getAsync());
        return eventDemoService.register(request);
    }

    /**
     * 获取事件机制详细说明
     */
    @GetMapping("/explanation")
    public String getEventMechanismExplanation() {
        log.info("获取 Spring 事件机制详细说明");
        return eventDemoService.getEventMechanismExplanation();
    }
}
