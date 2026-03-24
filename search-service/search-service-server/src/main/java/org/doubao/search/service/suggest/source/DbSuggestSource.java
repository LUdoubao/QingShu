package org.doubao.search.service.suggest.source;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.entity.SearchSuggestTermDO;
import org.doubao.search.service.mapper.SearchSuggestTermMapper;
import org.doubao.search.service.suggest.SuggestSource;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class DbSuggestSource implements SuggestSource {

    private static final int LIMIT = 10;

    @Resource
    private SearchSuggestTermMapper searchSuggestTermMapper;

    @Override
    public List<String> load(QueryContext context, Map<String, String> rawSuggestionData) {
        if (context.getNormalizedQuery().isEmpty()) {
            return Collections.emptyList();
        }
        List<SearchSuggestTermDO> records = searchSuggestTermMapper.selectByPrefixAndType(
                context.getNormalizedQuery(), getTermType(), LIMIT);
        List<String> results = new ArrayList<>();
        for (SearchSuggestTermDO record : records) {
            results.add(record.getTermText());
        }
        if (!results.isEmpty()) {
            return results;
        }
        return fallback(rawSuggestionData);
    }

    protected abstract String getTermType();

    protected abstract List<String> fallback(Map<String, String> rawSuggestionData);
}
