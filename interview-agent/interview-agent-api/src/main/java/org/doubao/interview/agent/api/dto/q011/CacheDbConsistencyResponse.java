package org.doubao.interview.agent.api.dto.q011;

import java.io.Serializable;

/**
 * 问题011：双写一致性统一响应。
 */
public class CacheDbConsistencyResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;

    private String stage;

    private String message;

    private String dataId;

    private String dbValue;

    private String cacheValue;

    private boolean eventuallyConsistent;

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

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getDbValue() {
        return dbValue;
    }

    public void setDbValue(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getCacheValue() {
        return cacheValue;
    }

    public void setCacheValue(String cacheValue) {
        this.cacheValue = cacheValue;
    }

    public boolean isEventuallyConsistent() {
        return eventuallyConsistent;
    }

    public void setEventuallyConsistent(boolean eventuallyConsistent) {
        this.eventuallyConsistent = eventuallyConsistent;
    }
}
