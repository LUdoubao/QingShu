package org.doubao.interview.agent.api.dto.rabbitmq.q016;

import java.io.Serializable;

/** 问题016(RabbitMQ)：延迟消息响应。 */
public class RabbitDelayResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String bizId;
    private String mode;
    private long delayMillis;
    private String status;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public long getDelayMillis() { return delayMillis; }
    public void setDelayMillis(long delayMillis) { this.delayMillis = delayMillis; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
