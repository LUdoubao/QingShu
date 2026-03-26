package org.doubao.interview.agent.api.dto.q029;

import java.io.Serializable;

/** 问题029：WATCH 乐观锁响应。 */
public class RedisWatchCasResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String key;
    private Integer oldValue;
    private Integer newValue;
    private int retries;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public Integer getOldValue() { return oldValue; }
    public void setOldValue(Integer oldValue) { this.oldValue = oldValue; }
    public Integer getNewValue() { return newValue; }
    public void setNewValue(Integer newValue) { this.newValue = newValue; }
    public int getRetries() { return retries; }
    public void setRetries(int retries) { this.retries = retries; }
}
