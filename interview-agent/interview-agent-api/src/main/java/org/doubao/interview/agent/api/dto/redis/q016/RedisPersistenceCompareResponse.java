package org.doubao.interview.agent.api.dto.redis.q016;

import java.io.Serializable;

/**
 * 问题016：RDB/AOF 对比演示响应。
 */
public class RedisPersistenceCompareResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;

    private String stage;

    private String summary;

    private PersistenceProfile rdbProfile;

    private PersistenceProfile aofProfile;

    private PersistenceProfile hybridProfile;

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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public PersistenceProfile getRdbProfile() {
        return rdbProfile;
    }

    public void setRdbProfile(PersistenceProfile rdbProfile) {
        this.rdbProfile = rdbProfile;
    }

    public PersistenceProfile getAofProfile() {
        return aofProfile;
    }

    public void setAofProfile(PersistenceProfile aofProfile) {
        this.aofProfile = aofProfile;
    }

    public PersistenceProfile getHybridProfile() {
        return hybridProfile;
    }

    public void setHybridProfile(PersistenceProfile hybridProfile) {
        this.hybridProfile = hybridProfile;
    }
}
