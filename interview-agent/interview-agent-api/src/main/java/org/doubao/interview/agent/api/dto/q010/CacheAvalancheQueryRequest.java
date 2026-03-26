package org.doubao.interview.agent.api.dto.q010;

import java.io.Serializable;

/**
 * 问题010：缓存雪崩治理查询请求。
 *
 * 设计说明：
 * 1. bizKey：模拟业务数据主键。
 * 2. clientId：用于限流统计，模拟真实网关/调用方身份。
 * 3. simulateRedisDown：用于演示“缓存层整体不可用”时的治理路径。
 */
public class CacheAvalancheQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务主键。
     */
    private String bizKey;

    /**
     * 调用方标识。
     */
    private String clientId;

    /**
     * 是否模拟 Redis 集群故障。
     */
    private boolean simulateRedisDown;

    public String getBizKey() {
        return bizKey;
    }

    public void setBizKey(String bizKey) {
        this.bizKey = bizKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isSimulateRedisDown() {
        return simulateRedisDown;
    }

    public void setSimulateRedisDown(boolean simulateRedisDown) {
        this.simulateRedisDown = simulateRedisDown;
    }
}
