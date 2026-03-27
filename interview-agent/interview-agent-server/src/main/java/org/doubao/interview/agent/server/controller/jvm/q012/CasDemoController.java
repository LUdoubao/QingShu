package org.doubao.interview.agent.server.controller.jvm.q012;

import org.doubao.interview.agent.api.dto.jvm.q012.CasDemoRequest;
import org.doubao.interview.agent.api.dto.jvm.q012.CasDemoResponse;
import org.doubao.interview.agent.api.service.jvm.q012.CasDemoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 问题 012（JVM）：CAS 演示控制器。
 * <p>
 * 对外暴露一个最小可运行 HTTP 接口，让“CAS 是什么，会有什么问题”
 * 这道面试题可以直接通过接口触发并观察结果。
 */
@RestController
@RequestMapping("/interview-agent/questions/jvm/q012-cas")
public class CasDemoController {

    private final CasDemoService casDemoService;

    public CasDemoController(CasDemoService casDemoService) {
        this.casDemoService = casDemoService;
    }

    @PostMapping("/run")
    public CasDemoResponse run(@RequestBody CasDemoRequest request) {
        return casDemoService.run(request);
    }
}
