package org.doubao.recommend.service.strategy;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.domain.UserProfile;
import org.doubao.recommend.service.service.UserProfileService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SimilarRecallStrategy implements RecallStrategy {

    /**
     * 用户画像服务
     * 用于获取用户的兴趣画像，包含最近浏览的内容 ID 等信息
     */
    @Resource
    private UserProfileService userProfileService;
    
    /**
     * Redis 模板
     * 用于从缓存中获取相似内容列表
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断是否支持相似召回策略
     * 当请求中包含有效的用户身份标识时，使用相似召回策略
     *
     * @param request 推荐请求参数
     * @return true-支持该策略，false-不支持
     */
    @Override
    public boolean support(RecommendRequest request) {
        return request.getUserIdentity() != null && !request.getUserIdentity().isEmpty();
    }

    /**
     * 执行相似召回策略
     * 基于种子内容 ID（当前内容 + 用户最近浏览的 Top3 内容），从 Redis 缓存中召回相似内容
     * 流程：收集种子 ID -> 从 Redis 获取相似内容列表 -> 构建候选物品
     *
     * @param request 推荐请求参数
     * @return 召回的候选物品列表
     */
    @Override
    public List<CandidateItem> recall(RecommendRequest request) {
        // 收集种子内容 ID 列表
        List<Long> seeds = new ArrayList<>();
        if (request.getContentId() != null) {
            seeds.add(request.getContentId());
        }

        // 从用户画像中获取最近浏览的 3 个内容 ID
        UserProfile profile = userProfileService.getProfile(request.getUserIdentity());
        if (profile != null && profile.getRecentContentIds() != null) {
            for (Long id : profile.getRecentContentIds().stream().limit(3).collect(Collectors.toList())) {
                if (!seeds.contains(id)) {
                    seeds.add(id);
                }
            }
        }
        if (seeds.isEmpty()) {
            return new ArrayList<>();
        }

        // 从 Redis 缓存中获取每个种子内容的相似内容
        List<CandidateItem> result = new ArrayList<>();
        for (Long seed : seeds) {
            // 从 Redis 获取预计算的相似内容列表
            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet().reverseRangeWithScores(RedisKeys.SIMILAR_PREFIX + seed, 0, 19);
            if (tuples != null) {
                for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<String> tuple : tuples) {
                    if (tuple == null || tuple.getValue() == null) {
                        continue;
                    }
                    CandidateItem item = new CandidateItem();
                    item.setContentId(Long.valueOf(tuple.getValue()));
                    item.setRecallSource("similar:" + seed);
                    item.setBaseScore(tuple.getScore() == null ? 0.9 : tuple.getScore());
                    item.setReason("similar_content_seed_" + seed);
                    result.add(item);
                }
            }
        }
        return result;
    }
}
