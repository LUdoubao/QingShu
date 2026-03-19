package org.doubao.recommend.service.service;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.domain.RecommendItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.strategy.RecallStrategy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {

    /**
     * 召回策略列表
     * 包含多种召回算法实现，如协同过滤召回、热门召回、基于内容召回等
     */
    @Resource
    private List<RecallStrategy> recallStrategies;
    
    /**
     * 排序服务
     * 负责对候选物品进行去重、过滤已读和最终排序
     */
    @Resource
    private RankService rankService;
    
    /**
     * 内容特征服务
     * 用于获取内容的特征信息，支持排序阶段的特征计算
     */
    @Resource
    private ContentFeatureService contentFeatureService;
    
    /**
     * Redis 模板
     * 用于缓存推荐结果，提高查询性能
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 执行推荐操作
     * 完整的推荐流程：缓存检查 -> 多路召回 -> 去重 -> 过滤已读 -> 排序 -> 缓存结果
     *
     * @param request 推荐请求参数
     * @return 推荐结果列表
     */
    public List<RecommendItem> recommend(RecommendRequest request) {
        String cacheKey = cacheKey(request);
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List && !((List) cached).isEmpty()) {
            @SuppressWarnings("unchecked")
            List<RecommendItem> items = (List<RecommendItem>) cached;
            return paginate(items, request.getPage(), request.getPageSize());
        }

        // 多路召回：遍历所有支持的召回策略，收集候选物品
        Set<CandidateItem> candidates = new LinkedHashSet<>();
        for (RecallStrategy strategy : recallStrategies) {
            if (strategy.support(request)) {
                candidates.addAll(strategy.recall(request));
            }
        }
        
        // 去重：移除重复的候选物品
        candidates = rankService.deduplicate(candidates);
        
        // 过滤已读：移除用户已经看过的内容
        candidates = rankService.filterSeen(request, candidates);

        // 获取候选物品的内容特征，用于后续排序
        List<Long> ids = candidates.stream().map(CandidateItem::getContentId).limit(200).collect(Collectors.toList());
        Map<Long, ContentFeature> featureMap = contentFeatureService.getBatchByIds(ids).stream()
                .collect(java.util.stream.Collectors.toMap(ContentFeature::getContentId, f -> f));

        // 对候选物品进行排序
        List<RecommendItem> ranked = rankService.rank(request, candidates, featureMap);

        // 将排序后的结果缓存到 Redis
        redisTemplate.opsForValue().set(cacheKey, ranked);
        
        // 分页返回结果
        return paginate(ranked, request.getPage(), request.getPageSize());
    }

    /**
     * 获取相关内容推荐
     * 用于详情页的"相关推荐"场景，自动设置场景为 detail 并传入内容 ID
     *
     * @param contentId 当前内容 ID
     * @param request 推荐请求参数
     * @return 相关内容推荐列表
     */
    public List<RecommendItem> related(Long contentId, RecommendRequest request) {
        request.setScene("detail");
        request.setContentId(contentId);
        return recommend(request);
    }

    /**
     * 生成缓存键
     * 根据用户身份和场景生成唯一的缓存键
     *
     * @param request 推荐请求参数
     * @return 缓存键字符串
     */
    private String cacheKey(RecommendRequest request) {
        String identity = request.getUserIdentity() == null ? "guest" : request.getUserIdentity();
        String scene = request.getScene() == null ? "home" : request.getScene();
        return RedisKeys.RESULT_CACHE_PREFIX + scene + ":" + identity;
    }

    /**
     * 分页处理
     * 对推荐结果列表进行分页，返回指定页码和大小的子列表
     *
     * @param items 完整的推荐结果列表
     * @param page 页码，为空或小于等于 0 时默认为 1
     * @param pageSize 每页大小，为空或小于等于 0 时默认为 20，最大不超过 50
     * @return 分页后的推荐结果子列表
     */
    private List<RecommendItem> paginate(List<RecommendItem> items, Integer page, Integer pageSize) {
        int p = page == null || page <= 0 ? 1 : page;
        int ps = pageSize == null || pageSize <= 0 ? 20 : Math.min(pageSize, 50);
        int from = Math.max(0, (p - 1) * ps);
        int to = Math.min(items.size(), from + ps);
        if (from >= to) {
            return Collections.emptyList();
        }
        return items.subList(from, to);
    }
}
