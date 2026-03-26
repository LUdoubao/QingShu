package org.doubao.interview.agent.api.dto.q025;

import java.io.Serializable;

/** 问题025：分布式锁响应。 */
public class RedisDistributedLockResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String lockOwnerToken;
    private boolean watchdogRenewed;
    private boolean unlockedByLua;
    private boolean idempotentHint;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLockOwnerToken() { return lockOwnerToken; }
    public void setLockOwnerToken(String lockOwnerToken) { this.lockOwnerToken = lockOwnerToken; }
    public boolean isWatchdogRenewed() { return watchdogRenewed; }
    public void setWatchdogRenewed(boolean watchdogRenewed) { this.watchdogRenewed = watchdogRenewed; }
    public boolean isUnlockedByLua() { return unlockedByLua; }
    public void setUnlockedByLua(boolean unlockedByLua) { this.unlockedByLua = unlockedByLua; }
    public boolean isIdempotentHint() { return idempotentHint; }
    public void setIdempotentHint(boolean idempotentHint) { this.idempotentHint = idempotentHint; }
}
