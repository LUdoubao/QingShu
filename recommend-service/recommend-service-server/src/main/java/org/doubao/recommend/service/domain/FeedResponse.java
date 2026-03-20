package org.doubao.recommend.service.domain;

import java.util.ArrayList;
import java.util.List;

public class FeedResponse {
    private List<RecommendItem> items = new ArrayList<>();
    private String nextCursor;
    private boolean hasMore;
    private String sessionId;

    public List<RecommendItem> getItems() {
        return items;
    }

    public void setItems(List<RecommendItem> items) {
        this.items = items;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public void setNextCursor(String nextCursor) {
        this.nextCursor = nextCursor;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
