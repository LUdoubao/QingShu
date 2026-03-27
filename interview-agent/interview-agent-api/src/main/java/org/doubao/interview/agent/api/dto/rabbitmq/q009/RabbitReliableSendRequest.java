package org.doubao.interview.agent.api.dto.rabbitmq.q009;

import java.io.Serializable;

/** 问题009(RabbitMQ)：防丢消息发送请求。 */
public class RabbitReliableSendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bizId;
    private String payload;

    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
