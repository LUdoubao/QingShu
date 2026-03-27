package org.doubao.interview.agent.api.dto.q009.jvm;

import java.io.Serializable;

/** 问题009(JVM)：可重入锁演示响应。 */
public class ReentrantLockDemoResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private String mode;
    private int depth;
    private int maxReentrantCount;
    private int finalResult;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
    public int getMaxReentrantCount() { return maxReentrantCount; }
    public void setMaxReentrantCount(int maxReentrantCount) { this.maxReentrantCount = maxReentrantCount; }
    public int getFinalResult() { return finalResult; }
    public void setFinalResult(int finalResult) { this.finalResult = finalResult; }
}
