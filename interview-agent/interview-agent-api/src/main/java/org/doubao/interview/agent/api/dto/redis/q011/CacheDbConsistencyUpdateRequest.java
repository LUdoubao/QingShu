package org.doubao.interview.agent.api.dto.redis.q011;

import java.io.Serializable;

/**
 * 问题011：缓存与数据库双写一致性更新请求。
 *
 * 该请求专门用于演示“写库后删缓存”的一致性策略：
 * 1. dataId：业务主键。
 * 2. newValue：要写入数据库的新值。
 * 3. simulateDeleteFail：是否模拟首次删缓存失败，验证重试补偿链路。
 */
public class CacheDbConsistencyUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dataId;

    private String newValue;

    private boolean simulateDeleteFail;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public boolean isSimulateDeleteFail() {
        return simulateDeleteFail;
    }

    public void setSimulateDeleteFail(boolean simulateDeleteFail) {
        this.simulateDeleteFail = simulateDeleteFail;
    }
}
