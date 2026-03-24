package org.doubao.search.service.domain.query;

public class SuggestQuery {

    private final String keyword;
    private final Long userId;

    public SuggestQuery(String keyword, Long userId) {
        this.keyword = keyword;
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public Long getUserId() {
        return userId;
    }
}
