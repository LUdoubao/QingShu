package org.doubao.interview.agent.api.dto.q006.jvm;

import java.io.Serializable;

/**
 * 问题006(JVM)：volatile 演示响应。
 */
public class VolatileDemoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String stage;
    private String message;

    /** volatile 标志位可见性演示是否成功结束。 */
    private boolean visibilityWorked;

    /** 理论值：threadCount * incrementPerThread。 */
    private int expectedCount;

    /** 实际值：volatile count++ 并发结果。 */
    private int actualCount;

    /** 是否出现了原子性丢失。 */
    private boolean atomicityLost;

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

    public boolean isVisibilityWorked() {
        return visibilityWorked;
    }

    public void setVisibilityWorked(boolean visibilityWorked) {
        this.visibilityWorked = visibilityWorked;
    }

    public int getExpectedCount() {
        return expectedCount;
    }

    public void setExpectedCount(int expectedCount) {
        this.expectedCount = expectedCount;
    }

    public int getActualCount() {
        return actualCount;
    }

    public void setActualCount(int actualCount) {
        this.actualCount = actualCount;
    }

    public boolean isAtomicityLost() {
        return atomicityLost;
    }

    public void setAtomicityLost(boolean atomicityLost) {
        this.atomicityLost = atomicityLost;
    }
}
