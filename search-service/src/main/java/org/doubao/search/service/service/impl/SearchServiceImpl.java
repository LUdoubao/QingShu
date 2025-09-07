package org.doubao.search.service.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.doubao.search.service.mapper.HotSearchMapper;
import org.doubao.search.service.model.dto.SearchRequestDto;
import org.doubao.search.service.model.enums.TimeRangeEnum;
import org.doubao.search.service.model.message.SearchStatisticsMessage;
import org.doubao.search.service.model.vo.HotSearchVO;
import org.doubao.search.service.model.vo.SearchResultVO;
import org.doubao.search.service.repository.elasticsearch.CopywritingEsRepository;
import org.doubao.search.service.service.SearchService;
import org.doubao.search.service.service.remote.UserRemoteService;
import org.doubao.search.service.util.SearchUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.common.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);
    @Resource
    private RestHighLevelClient elasticsearchClient;
    @Resource
    private CopywritingEsRepository copywritingEsRepository;
    @Resource
    private UserRemoteService userRemoteService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private HotSearchMapper hotSearchMapper;

    @Value("${search.elasticsearch.page.max-size}")
    private Integer maxPageSize;

    @Value("${search.cache.result-expire-minutes}")
    private Integer resultExpireMinutes;
    @Value("${search.cache.hot-search-expire-minutes}")
    private Integer hotSearchExpireMinutes;

    @Override
    public SearchResultVO search(SearchRequestDto request, Long userId) {
        // 参数校验与处理
        validateAndProcessRequest(request);
        
        // 生成缓存Key
        String cacheKey = generateSearchCacheKey(request, userId);
        
        // 尝试从缓存获取结果
        SearchResultVO cachedResult = (SearchResultVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }
        
        // 构建搜索请求
        SearchRequest searchRequest = new SearchRequest("copywriting");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        
        // 构建查询条件
        BoolQueryBuilder boolQuery = buildBoolQuery(request, userId);
        
        // 构建功能评分查询（结合相关性和热度）
        FunctionScoreQueryBuilder functionScoreQuery = buildFunctionScoreQuery(boolQuery);
        sourceBuilder.query(functionScoreQuery);
        
        // 设置高亮
        HighlightBuilder highlightBuilder = buildHighlightBuilder();
        sourceBuilder.highlighter(highlightBuilder);
        
        // 设置排序
        setSorting(sourceBuilder, request);
        
        // 设置分页
        int from = (request.getPage() - 1) * request.getPageSize();
        sourceBuilder.from(from);
        sourceBuilder.size(request.getPageSize());
        
        searchRequest.source(sourceBuilder);
        
        try {
            // 执行搜索
            SearchResponse response = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            // 处理搜索结果
            SearchResultVO resultVO = processSearchResponse(response, request);
            
            // 缓存搜索结果
            redisTemplate.opsForValue().set(cacheKey, resultVO, resultExpireMinutes, TimeUnit.MINUTES);
            
            // 异步记录搜索统计
            asyncRecordSearchStatistics(request.getKeyword(), userId, resultVO.getTotal().intValue());
            
            return resultVO;
        } catch (IOException e) {
            log.error("Elasticsearch search error", e);
            throw new RuntimeException("搜索服务异常", e);
        }
    }
    
    @Override
    public List<HotSearchVO> getHotSearches(String period, Integer limit) {
        // 参数处理
        if (StrUtil.isBlank(period)) {
            period = "day";
        }
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        
        // 缓存Key
        String cacheKey = "search:hot:" + period + ":" + limit;
        
        // 尝试从缓存获取
        List<HotSearchVO> cachedHotSearches = (List<HotSearchVO>) redisTemplate.opsForValue().get(cacheKey);
        if (CollUtil.isNotEmpty(cachedHotSearches)) {
            return cachedHotSearches;
        }
        
        // 获取时间范围
        TimeRangeEnum timeRange = TimeRangeEnum.getByCode(period);
        if (timeRange == null) {
            timeRange = TimeRangeEnum.TODAY;
        }
        
        LocalDateTime startTime = timeRange.getStartTime();
        LocalDateTime endTime = timeRange.getEndTime();
        
        // 查询热门搜索
        List<HotSearchVO> hotSearches = hotSearchMapper.selectHotSearchesByTimeRange(startTime, endTime, limit);
        
        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, hotSearches, hotSearchExpireMinutes, TimeUnit.MINUTES);
        
        return hotSearches;
    }
    
    @Override
    @Async
    public void recordSearchStatistics(String keyword, Long userId, Integer resultCount) {
        try {
            // 发送到统计队列
            SearchStatisticsMessage message = new SearchStatisticsMessage();
            message.setKeyword(keyword);
            message.setUserId(userId);
            message.setSearchTime(new Date());
            message.setResultCount(resultCount);
            rabbitTemplate.convertAndSend("search-exchange", "search.statistics", message);
            
            // 直接增加热门搜索计数
            hotSearchMapper.incrementSearchCount(keyword, 1);
        } catch (Exception e) {
            log.error("Failed to record search statistics", e);
        }
    }
    
    /**
     * 验证并处理请求参数
     */
    private void validateAndProcessRequest(SearchRequestDto request) {
        if (StrUtil.isBlank(request.getKeyword())) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        
        // 处理页码
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        
        // 处理页大小
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(20);
        } else if (request.getPageSize() > maxPageSize) {
            request.setPageSize(maxPageSize);
        }
        
        // 处理排序字段
        List<String> validSortFields = Arrays.asList("relevance", "time", "likeCount", 
                                                    "collectCount", "commentCount", "viewCount");
        if (StrUtil.isBlank(request.getSortBy()) || !validSortFields.contains(request.getSortBy())) {
            request.setSortBy("relevance");
        }
        
        // 处理排序方向
        if (StrUtil.isBlank(request.getSortOrder()) || 
            !Arrays.asList("asc", "desc").contains(request.getSortOrder())) {
            request.setSortOrder("desc");
        }
    }
    
    /**
     * 生成搜索缓存Key
     */
    private String generateSearchCacheKey(SearchRequestDto request, Long userId) {
        StringBuilder keyBuilder = new StringBuilder("search:result:");
        keyBuilder.append(request.getKeyword().hashCode()).append(":");
        
        // 添加筛选条件哈希
        if (request.getFilters() != null) {
            keyBuilder.append(SearchUtil.filterToHash(request.getFilters())).append(":");
        }
        
        keyBuilder.append(request.getSortBy()).append(":").append(request.getSortOrder()).append(":");
        keyBuilder.append(request.getPage()).append(":").append(request.getPageSize());
        
        // 如果有用户ID且筛选了关注的作者，需要加入用户ID
        if (userId != null && request.getFilters() != null && Boolean.TRUE.equals(request.getFilters().getFollowing())) {
            keyBuilder.append(":uid:").append(userId);
        }
        
        return keyBuilder.toString();
    }
    
    /**
     * 构建布尔查询
     */
    private BoolQueryBuilder buildBoolQuery(SearchRequestDto request, Long userId) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 关键词查询
        String keyword = request.getKeyword();
        BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("title", keyword).boost(3.0f))
                .should(QueryBuilders.matchQuery("summary", keyword).boost(2.0f))
                .should(QueryBuilders.matchQuery("content", keyword))
                .should(QueryBuilders.matchQuery("tags.text", keyword).boost(2.5f))
                .should(QueryBuilders.matchQuery("authorName", keyword).boost(1.5f));
        boolQuery.must(shouldQuery);
        
        // 排除已删除的内容
        boolQuery.filter(QueryBuilders.termQuery("isDeleted", false));
        
        // 处理筛选条件
        SearchRequestDto.SearchFilter filters = request.getFilters();
        if (filters != null) {
            // 标签筛选
            if (CollUtil.isNotEmpty(Arrays.asList(filters.getTags()))) {
                boolQuery.filter(QueryBuilders.termsQuery("tags", filters.getTags()));
            }
            
            // 作者筛选
            if (CollUtil.isNotEmpty(Arrays.asList(filters.getAuthorIds()))) {
                boolQuery.filter(QueryBuilders.termsQuery("authorId", filters.getAuthorIds()));
            }
            
            // 关注的作者筛选
            if (Boolean.TRUE.equals(filters.getFollowing()) && userId != null) {
                List<Long> followingAuthorIds = userRemoteService.getUserFollowingIds(userId);
                if (CollUtil.isNotEmpty(followingAuthorIds)) {
                    boolQuery.filter(QueryBuilders.termsQuery("authorId", followingAuthorIds));
                } else {
                    // 如果没有关注的作者，返回空结果
                    boolQuery.must(QueryBuilders.matchAllQuery()).mustNot(QueryBuilders.matchAllQuery());
                }
            }
            
            // 认证作者筛选
            if (Boolean.TRUE.equals(filters.getVerified())) {
                boolQuery.filter(QueryBuilders.termQuery("isVerifiedAuthor", true));
            }
            
            // 时间范围筛选
            if (filters.getStartTime() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("publishTime").gte(filters.getStartTime()));
            }
            if (filters.getEndTime() != null) {
                boolQuery.filter(QueryBuilders.rangeQuery("publishTime").lte(filters.getEndTime()));
            }
            
            // 内容类型筛选
            if (CollUtil.isNotEmpty(Arrays.asList(filters.getContentTypes()))) {
                boolQuery.filter(QueryBuilders.termsQuery("contentType", filters.getContentTypes()));
            }
            
            // 点赞数范围筛选
            if (filters.getLikeCountRange() != null && filters.getLikeCountRange().length == 2) {
                boolQuery.filter(QueryBuilders.rangeQuery("likeCount")
                        .gte(filters.getLikeCountRange()[0])
                        .lte(filters.getLikeCountRange()[1]));
            }
            
            // 收藏数范围筛选
            if (filters.getCollectCountRange() != null && filters.getCollectCountRange().length == 2) {
                boolQuery.filter(QueryBuilders.rangeQuery("collectCount")
                        .gte(filters.getCollectCountRange()[0])
                        .lte(filters.getCollectCountRange()[1]));
            }
            
            // 评论数范围筛选
            if (filters.getCommentCountRange() != null && filters.getCommentCountRange().length == 2) {
                boolQuery.filter(QueryBuilders.rangeQuery("commentCount")
                        .gte(filters.getCommentCountRange()[0])
                        .lte(filters.getCommentCountRange()[1]));
            }
            
            // 浏览量范围筛选
            if (filters.getViewCountRange() != null && filters.getViewCountRange().length == 2) {
                boolQuery.filter(QueryBuilders.rangeQuery("viewCount")
                        .gte(filters.getViewCountRange()[0])
                        .lte(filters.getViewCountRange()[1]));
            }
            
            // 审核状态筛选（仅管理员）
            if (StrUtil.isNotBlank(filters.getAuditStatus())) {
                boolQuery.filter(QueryBuilders.termQuery("auditStatus", filters.getAuditStatus()));
            }
        }
        
        return boolQuery;
    }
    
    /**
     * 构建功能评分查询
     */
    private FunctionScoreQueryBuilder buildFunctionScoreQuery(BoolQueryBuilder boolQuery) {
        // 热度因子计算
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = {
            // 点赞数因子
            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                ScoreFunctionBuilders.fieldValueFactorFunction("likeCount")
                    .modifier(FieldValueFactorFunction.Modifier.LOG1P)
                    .factor(0.1f)
            ),
            // 收藏数因子
            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                ScoreFunctionBuilders.fieldValueFactorFunction("collectCount")
                    .modifier(FieldValueFactorFunction.Modifier.LOG1P)
                    .factor(0.15f)
            ),
            // 评论数因子
            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                ScoreFunctionBuilders.fieldValueFactorFunction("commentCount")
                    .modifier(FieldValueFactorFunction.Modifier.LOG1P)
                    .factor(0.2f)
            ),
            // 浏览量因子
            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                ScoreFunctionBuilders.fieldValueFactorFunction("viewCount")
                    .modifier(FieldValueFactorFunction.Modifier.LOG1P)
                    .factor(0.05f)
            ),
            // 时间衰减因子（越新的内容权重越高）
            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                ScoreFunctionBuilders.exponentialDecayFunction(
                    "publishTime", "7d", 0.5f
                )
            )
        };
        
        // 构建功能评分查询，结合相关性得分和功能得分
        return QueryBuilders.functionScoreQuery(
            boolQuery,
            filterFunctionBuilders
        ).boostMode(CombineFunction.MULTIPLY);
    }
    
    /**
     * 构建高亮配置
     */
    private HighlightBuilder buildHighlightBuilder() {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        
        // 标题高亮
        HighlightBuilder.Field titleHighlight = new HighlightBuilder.Field("title");
        titleHighlight.preTags("<em class=\"highlight\">");
        titleHighlight.postTags("</em>");
        highlightBuilder.field(titleHighlight);
        
        // 摘要高亮
        HighlightBuilder.Field summaryHighlight = new HighlightBuilder.Field("summary");
        summaryHighlight.preTags("<em class=\"highlight\">");
        summaryHighlight.postTags("</em>");
        highlightBuilder.field(summaryHighlight);
        
        return highlightBuilder;
    }
    
    /**
     * 设置排序
     */
    private void setSorting(SearchSourceBuilder sourceBuilder, SearchRequestDto request) {
        String sortBy = request.getSortBy();
        SortOrder sortOrder = "asc".equalsIgnoreCase(request.getSortOrder()) ? SortOrder.ASC : SortOrder.DESC;
        
        // 相关性排序不需要额外设置，使用评分排序
        if ("time".equals(sortBy)) {
            sourceBuilder.sort("publishTime", sortOrder);
        } else if ("likeCount".equals(sortBy)) {
            sourceBuilder.sort("likeCount", sortOrder);
        } else if ("collectCount".equals(sortBy)) {
            sourceBuilder.sort("collectCount", sortOrder);
        } else if ("commentCount".equals(sortBy)) {
            sourceBuilder.sort("commentCount", sortOrder);
        } else if ("viewCount".equals(sortBy)) {
            sourceBuilder.sort("viewCount", sortOrder);
        }
    }
    
    /**
     * 处理搜索响应
     */
    private SearchResultVO processSearchResponse(SearchResponse response, SearchRequestDto request) {
        SearchResultVO resultVO = new SearchResultVO();
        resultVO.setPage(request.getPage());
        resultVO.setPageSize(request.getPageSize());
        resultVO.setTotal(Objects.requireNonNull(response.getHits().getTotalHits()).value);
        
        // 处理结果列表
        List<SearchResultVO.CopywritingVO> items = Arrays.stream(response.getHits().getHits())
                .map(this::convertToCopywritingVO)
                .collect(Collectors.toList());
        resultVO.setItems(items);
        
        // 生成相关搜索建议
        resultVO.setRelatedSearches(generateRelatedSearches(request.getKeyword()));
        
        return resultVO;
    }
    
    /**
     * 转换为CopywritingVO
     */
    private SearchResultVO.CopywritingVO convertToCopywritingVO(SearchHit hit) {
        Map<String, Object> sourceMap = hit.getSourceAsMap();
        SearchResultVO.CopywritingVO vo = new SearchResultVO.CopywritingVO();
        
        // 基础信息
        vo.setId(Long.valueOf(sourceMap.get("id").toString()));
        vo.setTitle(sourceMap.get("title").toString());
        vo.setSummary(sourceMap.getOrDefault("summary", "").toString());
        
        // 高亮信息
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        if (highlightFields != null && !highlightFields.isEmpty()) {
            SearchResultVO.HighlightVO highlightVO = new SearchResultVO.HighlightVO();
            if (highlightFields.containsKey("title")) {
                highlightVO.setTitle(Arrays.asList(highlightFields.get("title").getFragments())
                        .stream()
                        .map(Text::string)
                        .collect(Collectors.toList()));
            }
            if (highlightFields.containsKey("summary")) {
                highlightVO.setSummary(Arrays.asList(highlightFields.get("summary").getFragments())
                        .stream()
                        .map(Text::string)
                        .collect(Collectors.toList()));
            }
            vo.setHighlight(highlightVO);
        }
        
        // 作者信息
        SearchResultVO.AuthorVO authorVO = new SearchResultVO.AuthorVO();
        authorVO.setId(Long.valueOf(sourceMap.get("authorId").toString()));
        authorVO.setNickname(sourceMap.get("authorName").toString());
        authorVO.setAvatar(sourceMap.getOrDefault("authorAvatar", "").toString());
        authorVO.setVerified(Boolean.parseBoolean(sourceMap.getOrDefault("isVerifiedAuthor", false).toString()));
        vo.setAuthor(authorVO);
        
        // 标签
        if (sourceMap.containsKey("tags")) {
            List<String> tags = (List<String>) sourceMap.get("tags");
            vo.setTags(tags);
        }
        
        // 统计数据
        SearchResultVO.StatisticsVO statsVO = new SearchResultVO.StatisticsVO();
        statsVO.setLikeCount(Integer.parseInt(sourceMap.getOrDefault("likeCount", 0).toString()));
        statsVO.setCollectCount(Integer.parseInt(sourceMap.getOrDefault("collectCount", 0).toString()));
        statsVO.setCommentCount(Integer.parseInt(sourceMap.getOrDefault("commentCount", 0).toString()));
        statsVO.setViewCount(Integer.parseInt(sourceMap.getOrDefault("viewCount", 0).toString()));
        vo.setStats(statsVO);
        
        // 发布时间
        vo.setPublishTime(LocalDateTime.parse(sourceMap.get("publishTime").toString()));
        
        return vo;
    }
    
    /**
     * 生成相关搜索建议
     */
    private List<String> generateRelatedSearches(String keyword) {
        List<String> relatedSearches = new ArrayList<>();
        
        // 基于关键词生成相关建议
        if (keyword.contains("文案")) {
            relatedSearches.add(keyword.replace("文案", "句子"));
            relatedSearches.add(keyword.replace("文案", "段子"));
        } else {
            relatedSearches.add(keyword + " 文案");
        }
        
        return relatedSearches.stream().limit(5).collect(Collectors.toList());
    }
    
    /**
     * 异步记录搜索统计
     */
    @Async
    private void asyncRecordSearchStatistics(String keyword, Long userId, Integer resultCount) {
        try {
            recordSearchStatistics(keyword, userId, resultCount);
        } catch (Exception e) {
            log.error("Failed to async record search statistics", e);
        }
    }
}
