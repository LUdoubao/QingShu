package org.doubao.search.service.domain.query;

public class SearchQuery {

    private final String keyword;
    private final int page;
    private final int size;
    private final String type;
    private final Long userId;

    public SearchQuery(String keyword, int page, int size, String type, Long userId) {
        this.keyword = keyword;
        this.page = page;
        this.size = size;
        this.type = type;
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public Long getUserId() {
        return userId;
    }
}
