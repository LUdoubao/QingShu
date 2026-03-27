package org.doubao.interview.agent.api.dto.jvm.q008;

import java.io.Serializable;

/** 问题008(JVM)：锁选择演示响应。 */
public class LockChoiceDemoResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String mode;
    private int expectedCount;
    private int actualCount;
    private boolean fairLock;
    private int tryLockFailedCount;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public int getExpectedCount() { return expectedCount; }
    public void setExpectedCount(int expectedCount) { this.expectedCount = expectedCount; }
    public int getActualCount() { return actualCount; }
    public void setActualCount(int actualCount) { this.actualCount = actualCount; }
    public boolean isFairLock() { return fairLock; }
    public void setFairLock(boolean fairLock) { this.fairLock = fairLock; }
    public int getTryLockFailedCount() { return tryLockFailedCount; }
    public void setTryLockFailedCount(int tryLockFailedCount) { this.tryLockFailedCount = tryLockFailedCount; }
}
