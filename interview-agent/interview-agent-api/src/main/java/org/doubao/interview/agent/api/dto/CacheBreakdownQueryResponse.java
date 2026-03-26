package org.doubao.interview.agent.api.dto;

import java.io.Serializable;

/**
 * 问题009：缓存击穿治理查询响应。
 *
 * 通过 stage 字段标识请求命中的治理阶段，
 * 便于在面试演示时快速说明“当前请求是如何被保护的”。
 */
public class CacheBreakdownQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求是否处理成功。
     */
    private boolean success;

    /**
     * 命中的治理阶段，例如：
     * LOCAL_CACHE_HIT、SINGLE_FLIGHT_REBUILD、LOGICAL_EXPIRE_STALE、RATE_LIMIT。
     */
    private String stage;

    /**
     * 阶段说明，给出可读性强的中文解释。
     */
    private String message;

    /**
     * 业务数据内容。
     * 限流或参数错误等场景通常为 null。
     */
    private String data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}