package org.doubao.interview.agent.server.controller;

import org.doubao.interview.agent.api.dto.AgentGenerateRequest;
import org.doubao.interview.agent.api.dto.AgentGenerateResponse;
import org.doubao.interview.agent.api.service.InterviewAgentCodegenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Interview Agent 示例控制器。
 * 提供健康检查与最小代码生成接口，用于后续扩展。
 */
@RestController
@RequestMapping("/interview-agent")
public class InterviewAgentController {

    private static final Logger log = LoggerFactory.getLogger(InterviewAgentController.class);

    private final InterviewAgentCodegenService codegenService;

    public InterviewAgentController(InterviewAgentCodegenService codegenService) {
        this.codegenService = codegenService;
    }

    /**
     * 健康检查接口。
     *
     * @return 服务可用标识
     */
    @GetMapping("/health")
    public String health() {
        log.info("收到健康检查请求");
        return "interview-agent-ok";
    }

    /**
     * 最小代码生成入口。
     *
     * @param request 生成请求
     * @return 生成响应
     */
    @PostMapping("/generate")
    public AgentGenerateResponse generate(@RequestBody AgentGenerateRequest request) {
        log.info("收到代码生成请求, title={}", request == null ? null : request.getTitle());
        return codegenService.generate(request);
    }
}
