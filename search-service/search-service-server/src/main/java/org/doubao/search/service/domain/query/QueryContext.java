package org.doubao.search.service.domain.query;

import java.util.Collections;
import java.util.List;

public class QueryContext {

    private final String rawQuery;
    private final String normalizedQuery;
    private final String type;
    private final int page;
    private final int size;
    private final Long userId;
    private final List<String> terms;

    public QueryContext(String rawQuery, String normalizedQuery, String type, int page, int size, Long userId,
                        List<String> terms) {
        this.rawQuery = rawQuery;
        this.normalizedQuery = normalizedQuery;
        this.type = type;
        this.page = page;
        this.size = size;
        this.userId = userId;
        this.terms = terms == null ? Collections.emptyList() : Collections.unmodifiableList(terms);
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public String getNormalizedQuery() {
        return normalizedQuery;
    }

    public String getType() {
        return type;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public Long getUserId() {
        return userId;
    }

    public List<String> getTerms() {
        return terms;
    }
}
