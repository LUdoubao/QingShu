package org.doubao.interview.agent.server.service.impl;

import org.doubao.interview.agent.api.dto.AgentGenerateRequest;
import org.doubao.interview.agent.api.dto.AgentGenerateResponse;
import org.doubao.interview.agent.api.service.InterviewAgentCodegenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 代码生成服务默认实现。
 * 当前实现用于打通流程，后续可在此接入真实 LLM/模板引擎。
 */
@Service
public class InterviewAgentCodegenServiceImpl implements InterviewAgentCodegenService {

    private static final Logger log = LoggerFactory.getLogger(InterviewAgentCodegenServiceImpl.class);

    @Override
    public AgentGenerateResponse generate(AgentGenerateRequest request) {
        String title = request == null ? null : request.getTitle();
        String requirement = request == null ? null : request.getRequirement();
        log.info("Interview Agent 开始执行代码生成, title={}, requirementLength={}",
                title, requirement == null ? 0 : requirement.length());

        AgentGenerateResponse response = new AgentGenerateResponse();
        response.setSuccess(true);
        response.setMessage("流程已打通：当前为占位实现，可继续扩展为真实代码生成逻辑。");
        response.setGeneratedCodePreview("// TODO: 在此接入模板引擎或大模型生成代码");

        log.info("Interview Agent 代码生成完成, title={}, success={}", title, response.isSuccess());
        return response;
    }
}
