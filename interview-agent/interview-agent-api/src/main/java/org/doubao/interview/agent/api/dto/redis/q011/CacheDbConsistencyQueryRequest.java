package org.doubao.interview.agent.api.dto.redis.q011;

import java.io.Serializable;

/**
 * 问题011：缓存与数据库双写一致性查询请求。
 */
public class CacheDbConsistencyQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataId;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
