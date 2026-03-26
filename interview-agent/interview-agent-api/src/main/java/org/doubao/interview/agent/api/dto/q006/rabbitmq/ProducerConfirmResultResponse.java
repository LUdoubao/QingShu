package org.doubao.interview.agent.api.dto.q006.rabbitmq;

import java.io.Serializable;

/** 问题006：生产者 confirm 结果响应。 */
public class ProducerConfirmResultResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String stage;
    private String message;
    private String correlationId;
    private String confirmStatus;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getConfirmStatus() { return confirmStatus; }
    public void setConfirmStatus(String confirmStatus) { this.confirmStatus = confirmStatus; }
}
