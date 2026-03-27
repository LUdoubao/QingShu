package org.doubao.interview.agent.api.dto.q013.rabbitmq;

import java.io.Serializable;

/** 问题013(RabbitMQ)：顺序消息发送请求。 */
public class RabbitOrderedSendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bizKey;
    private long seq;
    private String payload;

    public String getBizKey() { return bizKey; }
    public void setBizKey(String bizKey) { this.bizKey = bizKey; }
    public long getSeq() { return seq; }
    public void setSeq(long seq) { this.seq = seq; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
