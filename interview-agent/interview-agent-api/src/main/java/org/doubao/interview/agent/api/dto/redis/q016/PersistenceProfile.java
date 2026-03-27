package org.doubao.interview.agent.api.dto.redis.q016;

import java.io.Serializable;

/**
 * 持久化策略画像。
 *
 * 用统一结构表达 RDB/AOF/混合模式在面试常考维度上的表现，
 * 避免仅停留在文字描述。
 */
public class PersistenceProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 策略名。 */
    private String mode;

    /** 预计恢复耗时（毫秒，模拟值）。 */
    private long recoverCostMs;

    /** 预计文件大小（KB，模拟值）。 */
    private long fileSizeKb;

    /** 可能丢失窗口（秒，越小越安全）。 */
    private double dataLossWindowSeconds;

    /** 详细解释。 */
    private String explanation;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public long getRecoverCostMs() {
        return recoverCostMs;
    }

    public void setRecoverCostMs(long recoverCostMs) {
        this.recoverCostMs = recoverCostMs;
    }

    public long getFileSizeKb() {
        return fileSizeKb;
    }

    public void setFileSizeKb(long fileSizeKb) {
        this.fileSizeKb = fileSizeKb;
    }

    public double getDataLossWindowSeconds() {
        return dataLossWindowSeconds;
    }

    public void setDataLossWindowSeconds(double dataLossWindowSeconds) {
        this.dataLossWindowSeconds = dataLossWindowSeconds;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
