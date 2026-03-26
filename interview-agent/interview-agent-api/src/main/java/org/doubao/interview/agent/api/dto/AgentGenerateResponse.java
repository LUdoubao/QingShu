package org.doubao.interview.agent.api.dto;

import java.io.Serializable;

/**
 * Agent 代码生成响应。
 */
public class AgentGenerateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否生成成功。
     */
    private boolean success;

    /**
     * 结果消息，记录当前流程说明。
     */
    private String message;

    /**
     * 生成结果示例，后续可替换为完整结构化对象。
     */
    private String generatedCodePreview;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGeneratedCodePreview() {
        return generatedCodePreview;
    }

    public void setGeneratedCodePreview(String generatedCodePreview) {
        this.generatedCodePreview = generatedCodePreview;
    }
}
