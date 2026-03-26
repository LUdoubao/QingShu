package org.doubao.interview.agent.api.dto.q008;

import java.io.Serializable;

/**
 * 问题008：缓存穿透治理演示请求。
 *
 * 该请求对象只保留两个最小字段，目的是在面试场景中突出“治理链路”本身，
 * 而不是业务字段建模复杂度：
 * 1. dataId：模拟要查询的业务主键。
 * 2. clientId：模拟调用方身份，用于演示限流策略。
 */
public class CachePenetrationCheckRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 待查询的业务ID。
     * 约定由字母、数字、下划线、中划线组成，长度在服务端校验。
     */
    private String dataId;

    /**
     * 客户端标识。
     * 服务端会基于此字段进行“每秒固定阈值”的简单限流。
     */
    private String clientId;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}