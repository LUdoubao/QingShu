package org.doubao.search.service.suggest.impl;

import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchSuggestionDTO;
import org.doubao.search.service.gateway.QuoteSearchGateway;
import org.doubao.search.service.suggest.SuggestAssembler;
import org.doubao.search.service.suggest.SuggestService;
import org.doubao.search.service.suggest.source.CategorySuggestSource;
import org.doubao.search.service.suggest.source.QuoteSuggestSource;
import org.doubao.search.service.suggest.source.TagSuggestSource;
import org.doubao.search.service.suggest.source.UserHistorySuggestSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DefaultSuggestService implements SuggestService {

    @Resource
    private QuoteSearchGateway quoteSearchGateway;

    @Resource
    private QuoteSuggestSource quoteSuggestSource;

    @Resource
    private TagSuggestSource tagSuggestSource;

    @Resource
    private CategorySuggestSource categorySuggestSource;

    @Resource
    private UserHistorySuggestSource userHistorySuggestSource;

    @Resource
    private SuggestAssembler suggestAssembler;

    @Override
    public SearchSuggestionDTO suggest(QueryContext context) {
        Map<String, String> rawSuggestionData = quoteSearchGateway.getSuggestions(context);
        return suggestAssembler.assemble(
                merge(userHistorySuggestSource.load(context, rawSuggestionData),
                        quoteSuggestSource.load(context, rawSuggestionData)),
                tagSuggestSource.load(context, rawSuggestionData),
                categorySuggestSource.load(context, rawSuggestionData)
        );
    }

    private List<String> merge(List<String> first, List<String> second) {
        Set<String> merged = new LinkedHashSet<>();
        merged.addAll(first);
        merged.addAll(second);
        return new java.util.ArrayList<>(merged);
    }
}
