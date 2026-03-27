package org.doubao.interview.agent.api.dto.redis.q033;

import java.io.Serializable;

/** 问题033：Bloom Filter 检查请求。 */
public class BloomFilterCheckRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bizKey;

    public String getBizKey() { return bizKey; }
    public void setBizKey(String bizKey) { this.bizKey = bizKey; }
}
