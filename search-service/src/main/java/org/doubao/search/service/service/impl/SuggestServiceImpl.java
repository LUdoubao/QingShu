package org.doubao.search.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.doubao.search.service.mapper.HotSearchMapper;
import org.doubao.search.service.model.entity.HotSearch;
import org.doubao.search.service.model.vo.SuggestVO;
import org.doubao.search.service.repository.elasticsearch.CopywritingEsRepository;
import org.doubao.search.service.service.SuggestService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.TopHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.common.unit.Fuzziness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SuggestServiceImpl implements SuggestService {

    private static final Logger log = LoggerFactory.getLogger(SuggestServiceImpl.class);
    @Resource
    private RestHighLevelClient elasticsearchClient;
    @Resource
    private CopywritingEsRepository copywritingEsRepository;
    @Resource
    private HotSearchMapper hotSearchMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${search.suggest.min-length}")
    private Integer suggestMinLength;

    @Value("${search.suggest.max-count}")
    private Integer suggestMaxCount;

    @Value("${search.cache.suggest-expire-minutes}")
    private Integer suggestExpireMinutes;

    @Override
    public SuggestVO getSuggestions(String keyword, Long userId) {
        // 参数校验
        if (StrUtil.isBlank(keyword) || keyword.length() < suggestMinLength) {
            return new SuggestVO();
        }
        
        // 生成缓存Key
        String cacheKey = "search:suggest:" + keyword.toLowerCase();
        
        // 尝试从缓存获取
        SuggestVO cachedSuggest = (SuggestVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedSuggest != null) {
            return cachedSuggest;
        }
        
        SuggestVO suggestVO = new SuggestVO();
        
        // 获取热门搜索词联想
        List<SuggestVO.HotKeyword> hotKeywords = getHotKeywordSuggestions(keyword);
        suggestVO.setHotKeywords(hotKeywords);
        
        // 获取标签联想
        List<SuggestVO.Tag> tags = getTagSuggestions(keyword);
        suggestVO.setTags(tags);
        
        // 获取作者联想
        List<SuggestVO.Author> authors = getAuthorSuggestions(keyword);
        suggestVO.setAuthors(authors);
        
        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, suggestVO, suggestExpireMinutes, TimeUnit.MINUTES);
        
        return suggestVO;
    }
    
    /**
     * 获取热门搜索词联想
     */
    private List<SuggestVO.HotKeyword> getHotKeywordSuggestions(String keyword) {
        // 1. 从热门搜索表查询
        List<HotSearch> hotSearches = hotSearchMapper.selectByKeywordLike(keyword);
        
        // 2. 从ES查询热门搜索词
        List<String> esSuggestKeywords = getEsKeywordSuggestions(keyword);
        
        // 3. 合并结果并去重
        Set<String> keywordSet = new LinkedHashSet<>();
        
        // 添加数据库结果
        if (CollUtil.isNotEmpty(hotSearches)) {
            hotSearches.forEach(hotSearch -> keywordSet.add(hotSearch.getKeyword()));
        }
        
        // 添加ES结果
        if (CollUtil.isNotEmpty(esSuggestKeywords)) {
			keywordSet.addAll(esSuggestKeywords);
        }
        
        // 4. 转换为结果对象
        return keywordSet.stream()
                .filter(k -> k.contains(keyword) || keyword.contains(k))
                .limit(suggestMaxCount / 2)
                .map(k -> {
                    SuggestVO.HotKeyword hotKeyword = new SuggestVO.HotKeyword();
                    hotKeyword.setKeyword(k);
                    // 查找热度值
                    HotSearch hotSearch = hotSearches.stream()
                            .filter(hs -> hs.getKeyword().equals(k))
                            .findFirst()
                            .orElse(null);
                    hotKeyword.setHotValue(hotSearch != null ? hotSearch.getHotValue() : 0);
                    return hotKeyword;
                })
                .sorted((a, b) -> Integer.compare(b.getHotValue(), a.getHotValue()))
                .collect(Collectors.toList());
    }
    
    /**
     * 从ES获取关键词联想
     */
    private List<String> getEsKeywordSuggestions(String keyword) {
        SearchRequest searchRequest = new SearchRequest("copywriting");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        // 使用Completion Suggester
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion(
            "keyword_suggest",
            SuggestBuilders.completionSuggestion("title.completion")
                .prefix(keyword, Fuzziness.ONE)
                .size(suggestMaxCount)
        );
        
        sourceBuilder.suggest(suggestBuilder);
        sourceBuilder.size(0); // 不需要返回文档
        searchRequest.source(sourceBuilder);
        
        try {
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            // 处理建议结果
            CompletionSuggestion completionSuggestion = response.getSuggest().getSuggestion("keyword_suggest");
            return completionSuggestion.getOptions().stream()
                    .map(option -> option.getText().string())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("ES keyword suggest error", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取标签联想
     */
    private List<SuggestVO.Tag> getTagSuggestions(String keyword) {
        SearchRequest searchRequest = new SearchRequest("copywriting");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        // 过滤条件：未删除
        sourceBuilder.query(QueryBuilders.termQuery("isDeleted", false));
        
        // 聚合标签
        sourceBuilder.aggregation(
                AggregationBuilders.terms("tags_agg")
                        .field("tags")
                        // 使用IncludeExclude设置包含条件（正则表达式）
                        .includeExclude(new IncludeExclude(
                                keyword + ".*|.*" + keyword + ".*",  // 包含规则：匹配以关键词开头或包含关键词的标签
                                null  // 不设置排除规则
                        ))
                        .size(suggestMaxCount)
        );
        
        sourceBuilder.size(0); // 不需要返回文档
        searchRequest.source(sourceBuilder);
        
        try {
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            // 处理聚合结果
            Terms tagsAgg = response.getAggregations().get("tags_agg");
            return tagsAgg.getBuckets().stream()
                    .map(bucket -> {
                        SuggestVO.Tag tag = new SuggestVO.Tag();
                        tag.setName(bucket.getKeyAsString());
                        tag.setCount(bucket.getDocCount());
                        return tag;
                    })
                    .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("ES tag suggest error", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取作者联想
     */
    private List<SuggestVO.Author> getAuthorSuggestions(String keyword) {
        SearchRequest searchRequest = new SearchRequest("copywriting");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        // 查询条件：作者名包含关键词，且内容未删除
        sourceBuilder.query(
            QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("authorName", keyword))
                .filter(QueryBuilders.termQuery("isDeleted", false))
        );
        
        // 聚合作者
        sourceBuilder.aggregation(
            AggregationBuilders.terms("authors_agg")
                .field("authorId")
                .size(suggestMaxCount)
                .subAggregation(
                    AggregationBuilders.topHits("author_info")
                        .size(1)
                        .fetchSource(new String[]{"authorId", "authorName", "authorAvatar", "isVerifiedAuthor"}, null)
                )
        );
        
        sourceBuilder.size(0); // 不需要返回文档
        searchRequest.source(sourceBuilder);
        
        try {
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            // 处理聚合结果
            Terms authorsAgg = response.getAggregations().get("authors_agg");
            return authorsAgg.getBuckets().stream()
                    .map(bucket -> {
                        // 获取作者信息
                        TopHits topHits = bucket.getAggregations().get("author_info");
                        SearchHit hit = topHits.getHits().getAt(0);
                        Map<String, Object> sourceMap = hit.getSourceAsMap();
                        
                        SuggestVO.Author author = new SuggestVO.Author();
                        author.setId(Long.valueOf(sourceMap.get("authorId").toString()));
                        author.setName(sourceMap.get("authorName").toString());
                        author.setAvatar(sourceMap.getOrDefault("authorAvatar", "").toString());
                        author.setVerified(Boolean.parseBoolean(sourceMap.getOrDefault("isVerifiedAuthor", false).toString()));
                        author.setCopyCount(bucket.getDocCount());
                        
                        return author;
                    })
                    .sorted((a, b) -> Long.compare(b.getCopyCount(), a.getCopyCount()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("ES author suggest error", e);
            return Collections.emptyList();
        }
    }
}
