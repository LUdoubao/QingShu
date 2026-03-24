package org.doubao.search.service.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.cache.SearchCacheKeyBuilder;
import org.doubao.search.service.domain.query.QueryContext;
import org.doubao.search.service.domain.query.SearchQuery;
import org.doubao.search.service.domain.query.SuggestQuery;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.dto.SearchSuggestionDTO;
import org.doubao.search.service.provider.SearchProvider;
import org.doubao.search.service.service.SearchService;
import org.doubao.search.service.support.QueryPreprocessor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class SearchServiceImpl implements SearchService {

    private static final long CACHE_EXPIRE_MINUTES = 5;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private QueryPreprocessor queryPreprocessor;

    @Resource
    private SearchCacheKeyBuilder searchCacheKeyBuilder;

    @Resource
    private SearchProvider searchProvider;

    @Override
    public SearchSuggestionDTO getSearchSuggestions(String keyword) {
        QueryContext context = queryPreprocessor.process(new SuggestQuery(keyword, null));
        String cacheKey = searchCacheKeyBuilder.buildSuggestionKey(context);
        SearchSuggestionDTO cachedSuggestion = (SearchSuggestionDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedSuggestion != null) {
            return cachedSuggestion;
        }

        SearchSuggestionDTO suggestionDTO = searchProvider.suggest(context);
        redisTemplate.opsForValue().set(cacheKey, suggestionDTO, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return suggestionDTO;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Page<SearchResultDTO> search(String keyword, int page, int size, String type, Long currentUserId) {
        QueryContext context = queryPreprocessor.process(new SearchQuery(keyword, page, size, type, currentUserId));
        String cacheKey = searchCacheKeyBuilder.buildResultKey(context);
        Page<SearchResultDTO> cachedResult = (Page<SearchResultDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        Page<SearchResultDTO> result = searchProvider.search(context);
        redisTemplate.opsForValue().set(cacheKey, result, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return result;
    }
}
