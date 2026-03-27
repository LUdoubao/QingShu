package org.doubao.interview.agent.api.dto.redis.q017;

import java.io.Serializable;
import java.util.List;

/** 问题017：AOF rewrite 演示响应。 */
public class AofRewriteResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String stage;
    private String message;
    private int beforeCommandCount;
    private int afterCommandCount;
    private int finalValue;
    private List<String> sampleBefore;
    private List<String> sampleAfter;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getBeforeCommandCount() { return beforeCommandCount; }
    public void setBeforeCommandCount(int beforeCommandCount) { this.beforeCommandCount = beforeCommandCount; }
    public int getAfterCommandCount() { return afterCommandCount; }
    public void setAfterCommandCount(int afterCommandCount) { this.afterCommandCount = afterCommandCount; }
    public int getFinalValue() { return finalValue; }
    public void setFinalValue(int finalValue) { this.finalValue = finalValue; }
    public List<String> getSampleBefore() { return sampleBefore; }
    public void setSampleBefore(List<String> sampleBefore) { this.sampleBefore = sampleBefore; }
    public List<String> getSampleAfter() { return sampleAfter; }
    public void setSampleAfter(List<String> sampleAfter) { this.sampleAfter = sampleAfter; }
}
