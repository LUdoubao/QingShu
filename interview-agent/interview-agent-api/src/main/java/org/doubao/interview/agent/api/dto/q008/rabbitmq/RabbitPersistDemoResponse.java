package org.doubao.interview.agent.api.dto.q008.rabbitmq;

import java.io.Serializable;

/** 问题008(RabbitMQ)：消息持久化响应。 */
public class RabbitPersistDemoResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private boolean exchangeDurable;
    private boolean queueDurable;
    private int deliveryMode;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isExchangeDurable() { return exchangeDurable; }
    public void setExchangeDurable(boolean exchangeDurable) { this.exchangeDurable = exchangeDurable; }
    public boolean isQueueDurable() { return queueDurable; }
    public void setQueueDurable(boolean queueDurable) { this.queueDurable = queueDurable; }
    public int getDeliveryMode() { return deliveryMode; }
    public void setDeliveryMode(int deliveryMode) { this.deliveryMode = deliveryMode; }
}
