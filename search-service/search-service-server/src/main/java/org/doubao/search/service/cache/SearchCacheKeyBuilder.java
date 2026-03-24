package org.doubao.search.service.cache;

import org.doubao.search.service.domain.query.QueryContext;
import org.springframework.stereotype.Component;

@Component
public class SearchCacheKeyBuilder {

    public String buildSuggestionKey(QueryContext context) {
        return "search:suggestion:" + context.getNormalizedQuery();
    }

    public String buildResultKey(QueryContext context) {
        return "search:result:" + context.getType() + ":" + context.getNormalizedQuery()
                + ":" + context.getPage() + ":" + context.getSize();
    }
}
