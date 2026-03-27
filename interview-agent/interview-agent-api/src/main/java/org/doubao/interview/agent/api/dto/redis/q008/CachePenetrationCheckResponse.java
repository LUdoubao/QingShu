package org.doubao.interview.agent.api.dto.redis.q008;

import java.io.Serializable;

/**
 * 问题008：缓存穿透治理演示响应。
 *
 * 通过 stage + message 的组合，明确返回“当前请求在治理链路中经过了哪个阶段”，
 * 便于调试、联调和面试讲解。
 */
public class CachePenetrationCheckResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 请求是否被允许继续处理。
     * false 一般表示参数校验失败或触发限流。
     */
    private boolean allowed;

    /**
     * 当前阶段标识，例如：
     * PARAM_VALIDATION、RATE_LIMIT、CACHE_HIT、BLOOM_FILTER、DB_HIT。
     */
    private String stage;

    /**
     * 阶段说明，用中文给出可读性更强的结果解释。
     */
    private String message;

    /**
     * 查询到的数据内容。
     * 对于“空值缓存/布隆拦截/限流”等场景通常为 null。
     */
    private String data;

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
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