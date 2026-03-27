package org.doubao.interview.agent.api.dto.q009.rabbitmq;

import java.io.Serializable;

/** 问题009(RabbitMQ)：防丢链路响应。 */
public class RabbitReliableResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String bizId;
    private String producerStatus;
    private String consumerStatus;
    private boolean idempotentApplied;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public String getProducerStatus() { return producerStatus; }
    public void setProducerStatus(String producerStatus) { this.producerStatus = producerStatus; }
    public String getConsumerStatus() { return consumerStatus; }
    public void setConsumerStatus(String consumerStatus) { this.consumerStatus = consumerStatus; }
    public boolean isIdempotentApplied() { return idempotentApplied; }
    public void setIdempotentApplied(boolean idempotentApplied) { this.idempotentApplied = idempotentApplied; }
}
