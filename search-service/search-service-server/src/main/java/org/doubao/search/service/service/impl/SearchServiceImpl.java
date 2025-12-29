package org.doubao.search.service.service.impl;

import com.alibaba.fastjson.JSON;
import org.doubao.mall.common.enums.ErrorCode;
import org.doubao.mall.common.exception.BusinessException;
import org.doubao.search.service.feign.QuoteClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.doubao.search.service.dto.SearchSuggestionDTO;
import org.doubao.search.service.dto.SearchResultDTO;
import org.doubao.search.service.service.SearchService;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchServiceImpl.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private QuoteClient quoteClient;
    // 每种类型的建议数量限制
    private static final int SUGGESTION_LIMIT_PER_TYPE = 3;
    // Redis缓存时间(分钟)
    private static final long CACHE_EXPIRE_MINUTES = 5;

    @Override
    public SearchSuggestionDTO getSearchSuggestions(String keyword) {
        // 先尝试从Redis获取缓存
        String cacheKey = "search:suggestion:" + keyword;
        SearchSuggestionDTO cachedSuggestion = (SearchSuggestionDTO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedSuggestion != null) {
            return cachedSuggestion;
        }
        
        SearchSuggestionDTO suggestionDTO = new SearchSuggestionDTO();
        
        // 搜索用户建议

        // 搜索引文建议, 搜索标签建议, 搜索分类建议
        Map<String, String> data = quoteClient.getSearchSuggestions(keyword).getData();
        String tags = data.getOrDefault("tags", "");
        if (!tags.isEmpty()) {
            suggestionDTO.setTags(Arrays.asList(tags.split(",")));
        }
        String categories = data.getOrDefault("categories", "");
        if (!categories.isEmpty()) {
            suggestionDTO.setCategories(Arrays.asList(categories.split(",")));
        }
        String quotes = data.getOrDefault("quotes", "");
        if (!quotes.isEmpty()) {
            suggestionDTO.setQuotes(Arrays.asList(quotes.split(",")));
        }
        // 存入Redis缓存
        redisTemplate.opsForValue().set(cacheKey, suggestionDTO, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        
        return suggestionDTO;
    }

    @Override
    public Page<SearchResultDTO> search(String keyword, int page, int size, String type, Long currentUserId) {
        switch(type) {
            case "quote":
            case "tag":
                Map<String, Object>  data = quoteClient.searchQuotes(keyword, page, size, currentUserId, type).getData();
                if (data == null) {
                    return new Page<>();
                }
                // 封装结果
                Page<SearchResultDTO> result = new Page<>(page, size);
                result.setTotal(Long.parseLong(data.get("total").toString()));
                Object object = data.get("records");
                List<SearchResultDTO> searchResultDTOS = JSON.parseArray(JSON.toJSONString(object), SearchResultDTO.class);
                result.setRecords(searchResultDTOS);
                return result;
            default:
                throw new BusinessException(ErrorCode.SEARCH_TYPE_NOT_SUPPORT);
        }
    }
}