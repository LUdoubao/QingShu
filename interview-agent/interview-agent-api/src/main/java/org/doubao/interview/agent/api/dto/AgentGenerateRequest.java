package org.doubao.interview.agent.api.dto;

import java.io.Serializable;

/**
 * Agent 代码生成请求。
 * 后续扩展字段时，请保持字段语义稳定，避免破坏历史调用。
 */
public class AgentGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 需求标题，用于日志与任务追踪。
     */
    private String title;

    /**
     * 需求描述，建议写清楚输入、输出与约束。
     */
    private String requirement;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }
}
