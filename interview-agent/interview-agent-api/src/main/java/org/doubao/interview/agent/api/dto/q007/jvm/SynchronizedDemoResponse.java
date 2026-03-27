package org.doubao.interview.agent.api.dto.q007.jvm;

import java.io.Serializable;

/** 问题007(JVM)：synchronized 演示响应。 */
public class SynchronizedDemoResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private int expectedCount;
    private int actualCount;
    private boolean mutexWorked;
    private boolean visibilityWorked;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getExpectedCount() { return expectedCount; }
    public void setExpectedCount(int expectedCount) { this.expectedCount = expectedCount; }
    public int getActualCount() { return actualCount; }
    public void setActualCount(int actualCount) { this.actualCount = actualCount; }
    public boolean isMutexWorked() { return mutexWorked; }
    public void setMutexWorked(boolean mutexWorked) { this.mutexWorked = mutexWorked; }
    public boolean isVisibilityWorked() { return visibilityWorked; }
    public void setVisibilityWorked(boolean visibilityWorked) { this.visibilityWorked = visibilityWorked; }
}
