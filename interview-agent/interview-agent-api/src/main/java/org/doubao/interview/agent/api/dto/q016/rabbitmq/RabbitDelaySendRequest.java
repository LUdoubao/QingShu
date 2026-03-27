package org.doubao.interview.agent.api.dto.q016.rabbitmq;

import java.io.Serializable;

/** 问题016(RabbitMQ)：延迟消息请求。 */
public class RabbitDelaySendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bizId;
    private String payload;
    private long delayMillis;
    private String mode;

    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public long getDelayMillis() { return delayMillis; }
    public void setDelayMillis(long delayMillis) { this.delayMillis = delayMillis; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
