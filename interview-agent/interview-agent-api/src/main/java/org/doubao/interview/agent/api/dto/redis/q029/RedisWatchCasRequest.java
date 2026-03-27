package org.doubao.interview.agent.api.dto.redis.q029;

import java.io.Serializable;

/** 问题029：WATCH 乐观锁请求。 */
public class RedisWatchCasRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String key;
    private int delta;
    private int maxRetry;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public int getDelta() { return delta; }
    public void setDelta(int delta) { this.delta = delta; }
    public int getMaxRetry() { return maxRetry; }
    public void setMaxRetry(int maxRetry) { this.maxRetry = maxRetry; }
}
