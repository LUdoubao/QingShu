package org.doubao.search.service.provider.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.dto.SearchSuggestionDTO;
import org.doubao.search.service.provider.SearchProvider;
import org.doubao.search.service.search.SearchExecutionService;
import org.doubao.search.service.suggest.SuggestService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class QuoteClientSearchProvider implements SearchProvider {

    @Resource
    private SuggestService suggestService;

    @Resource
    private SearchExecutionService searchExecutionService;

    @Override
    public SearchSuggestionDTO suggest(QueryContext context) {
        return suggestService.suggest(context);
    }

    @Override
    public Page<SearchResultDTO> search(QueryContext context) {
        return searchExecutionService.search(context);
    }
}
