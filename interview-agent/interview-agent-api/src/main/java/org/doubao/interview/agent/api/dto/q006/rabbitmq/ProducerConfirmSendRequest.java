package org.doubao.interview.agent.api.dto.q006.rabbitmq;

import java.io.Serializable;

/** 问题006：生产者 confirm 发送请求。 */
public class ProducerConfirmSendRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String message;
    private String routingKey;
    private boolean waitSync;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRoutingKey() { return routingKey; }
    public void setRoutingKey(String routingKey) { this.routingKey = routingKey; }
    public boolean isWaitSync() { return waitSync; }
    public void setWaitSync(boolean waitSync) { this.waitSync = waitSync; }
}
