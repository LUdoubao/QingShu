package org.doubao.interview.agent.api.dto.q009;

import java.io.Serializable;

/**
 * 问题009：缓存击穿治理查询请求。
 *
 * 该请求用于模拟“同一个热点 key 在高并发下被访问”的场景，
 * 字段保持最小集合，便于将注意力集中在击穿治理链路本身。
 */
public class CacheBreakdownQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 热点 key。
     * 服务端会基于该字段选择治理策略（互斥重建 / 逻辑过期 / 不过期主动更新）。
     */
    private String hotKey;

    /**
     * 调用方标识。
     * 服务端用它做固定窗口限流，模拟真实系统的入口防护。
     */
    private String clientId;

    public String getHotKey() {
        return hotKey;
    }

    public void setHotKey(String hotKey) {
        this.hotKey = hotKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}