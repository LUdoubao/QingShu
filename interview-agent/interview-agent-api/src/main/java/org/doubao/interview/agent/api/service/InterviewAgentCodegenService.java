package org.doubao.interview.agent.api.service;

import org.doubao.interview.agent.api.dto.AgentGenerateRequest;
import org.doubao.interview.agent.api.dto.AgentGenerateResponse;

/**
 * 代码生成服务接口。
 */
public interface InterviewAgentCodegenService {

    /**
     * 根据请求执行代码生成。
     *
     * @param request 生成请求
     * @return 生成响应
     */
    AgentGenerateResponse generate(AgentGenerateRequest request);
}
