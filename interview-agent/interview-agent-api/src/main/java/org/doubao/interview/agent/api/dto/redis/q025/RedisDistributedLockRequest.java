package org.doubao.interview.agent.api.dto.redis.q025;

import java.io.Serializable;

/** 问题025：分布式锁请求。 */
public class RedisDistributedLockRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String lockKey;
    private String clientId;
    private long lockTtlMillis;
    private long bizWorkMillis;
    private boolean enableWatchdog;

    public String getLockKey() { return lockKey; }
    public void setLockKey(String lockKey) { this.lockKey = lockKey; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public long getLockTtlMillis() { return lockTtlMillis; }
    public void setLockTtlMillis(long lockTtlMillis) { this.lockTtlMillis = lockTtlMillis; }
    public long getBizWorkMillis() { return bizWorkMillis; }
    public void setBizWorkMillis(long bizWorkMillis) { this.bizWorkMillis = bizWorkMillis; }
    public boolean isEnableWatchdog() { return enableWatchdog; }
    public void setEnableWatchdog(boolean enableWatchdog) { this.enableWatchdog = enableWatchdog; }
}
