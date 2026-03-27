package org.doubao.interview.agent.api.dto.redis.q010;

import java.io.Serializable;

/**
 * 问题010：缓存雪崩治理查询响应。
 */
public class CacheAvalancheQueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求是否成功处理。
     */
    private boolean success;

    /**
     * 治理阶段标识。
     */
    private String stage;

    /**
     * 对本次命中策略的中文说明。
     */
    private String message;

    /**
     * 业务数据。
     */
    private String data;

    /**
     * 是否触发降级。
     */
    private boolean degraded;

    /**
     * 熔断器是否打开。
     */
    private boolean circuitOpen;

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

    public boolean isDegraded() {
        return degraded;
    }

    public void setDegraded(boolean degraded) {
        this.degraded = degraded;
    }

    public boolean isCircuitOpen() {
        return circuitOpen;
    }

    public void setCircuitOpen(boolean circuitOpen) {
        this.circuitOpen = circuitOpen;
    }
}
