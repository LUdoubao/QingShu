package org.doubao.interview.agent.api.dto.rabbitmq.q015;

import java.io.Serializable;

/** 问题015(RabbitMQ)：DLQ发送请求。 */
public class RabbitDlqSendRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bizId;
    private String payload;
    private boolean forceFail;

    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public boolean isForceFail() { return forceFail; }
    public void setForceFail(boolean forceFail) { this.forceFail = forceFail; }
}
