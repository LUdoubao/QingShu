package org.doubao.recommend.service.domain;

import java.time.LocalDateTime;

public class FeedSession {
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime lastRequestTime;
    private Integer returnedCount;
    private String lastCursor;
    private Long fallbackAnchorId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getLastRequestTime() {
        return lastRequestTime;
    }

    public void setLastRequestTime(LocalDateTime lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }

    public Integer getReturnedCount() {
        return returnedCount;
    }

    public void setReturnedCount(Integer returnedCount) {
        this.returnedCount = returnedCount;
    }

    public String getLastCursor() {
        return lastCursor;
    }

    public void setLastCursor(String lastCursor) {
        this.lastCursor = lastCursor;
    }

    public Long getFallbackAnchorId() {
        return fallbackAnchorId;
    }

    public void setFallbackAnchorId(Long fallbackAnchorId) {
        this.fallbackAnchorId = fallbackAnchorId;
    }
}
