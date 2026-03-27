package org.doubao.interview.agent.api.dto.q013.rabbitmq;

import java.io.Serializable;

/** 问题013(RabbitMQ)：顺序消息响应。 */
public class RabbitOrderedResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String bizKey;
    private String queueName;
    private long seq;
    private boolean inOrder;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getBizKey() { return bizKey; }
    public void setBizKey(String bizKey) { this.bizKey = bizKey; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
    public boolean isInOrder() { return inOrder; }
    public void setInOrder(boolean inOrder) { this.inOrder = inOrder; }
}
