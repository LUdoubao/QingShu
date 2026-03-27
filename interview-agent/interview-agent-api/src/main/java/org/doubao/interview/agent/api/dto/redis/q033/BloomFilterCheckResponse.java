package org.doubao.interview.agent.api.dto.redis.q033;

import java.io.Serializable;

/** 问题033：Bloom Filter 检查响应。 */
public class BloomFilterCheckResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String bizKey;
    private boolean definitelyNotExist;
    private boolean maybeExist;
    private boolean passedToDb;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getBizKey() { return bizKey; }
    public void setBizKey(String bizKey) { this.bizKey = bizKey; }
    public boolean isDefinitelyNotExist() { return definitelyNotExist; }
    public void setDefinitelyNotExist(boolean definitelyNotExist) { this.definitelyNotExist = definitelyNotExist; }
    public boolean isMaybeExist() { return maybeExist; }
    public void setMaybeExist(boolean maybeExist) { this.maybeExist = maybeExist; }
    public boolean isPassedToDb() { return passedToDb; }
    public void setPassedToDb(boolean passedToDb) { this.passedToDb = passedToDb; }
}
