package org.doubao.interview.agent.api.dto.q008.rabbitmq;

import java.io.Serializable;

/** 问题008(RabbitMQ)：消息持久化请求。 */
public class RabbitPersistDemoRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
