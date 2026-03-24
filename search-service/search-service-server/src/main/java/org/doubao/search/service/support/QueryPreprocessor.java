package org.doubao.search.service.support;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.query.SearchQuery;
import org.doubao.search.service.domain.query.SuggestQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class QueryPreprocessor {

    public QueryContext process(SearchQuery query) {
        String normalizedQuery = normalize(query.getKeyword());
        return new QueryContext(query.getKeyword(), normalizedQuery, query.getType(), query.getPage(),
                query.getSize(), query.getUserId(), splitTerms(normalizedQuery));
    }

    public QueryContext process(SuggestQuery query) {
        String normalizedQuery = normalize(query.getKeyword());
        return new QueryContext(query.getKeyword(), normalizedQuery, null, 1, 10,
                query.getUserId(), splitTerms(normalizedQuery));
    }

    public String normalize(String keyword) {
        if (keyword == null) {
            return "";
        }
        return keyword.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private List<String> splitTerms(String normalizedQuery) {
        List<String> terms = new ArrayList<>();
        if (normalizedQuery == null || normalizedQuery.isEmpty()) {
            return terms;
        }
        terms.add(normalizedQuery);
        for (String part : normalizedQuery.split(" ")) {
            if (!part.isEmpty()) {
                terms.add(part);
            }
        }
        if (!normalizedQuery.contains(" ")) {
            for (int i = 0; i < normalizedQuery.length(); i++) {
                String current = String.valueOf(normalizedQuery.charAt(i));
                if (!terms.contains(current)) {
                    terms.add(current);
                }
            }
        }
        return terms;
    }
}
