package org.doubao.recommend.service.service;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.*;
import org.doubao.recommend.service.util.ScoreUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RankService {

    /**
     * 用户画像服务
     * 用于获取用户兴趣画像，计算用户对内容的兴趣分数
     */
    @Resource
    private UserProfileService userProfileService;
    
    /**
     * Redis String 模板
     * 用于查询用户已读内容集合
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 对候选物品进行去重处理
     * 当同一个内容 ID 被多个召回策略召回时，合并为一个候选物品
     * 保留最高的基础分数，并合并所有召回来源
     *
     * @param candidates 待去重的候选物品集合
     * @return 去重后的候选物品集合
     */
    public Set<CandidateItem> deduplicate(Collection<CandidateItem> candidates) {
        // 使用 Map 存储，key 为内容 ID，value 为候选物品
        Map<Long, CandidateItem> merged = new HashMap<>();
        for (CandidateItem c : candidates) {
            if (c == null || c.getContentId() == null) continue;
            CandidateItem old = merged.get(c.getContentId());
            if (old == null) {
                merged.put(c.getContentId(), c);
            } else {
                // 如果已存在，取较高的基础分数
                old.setBaseScore(Math.max(old.getBaseScore(), c.getBaseScore()));
                // 合并召回来源
                old.setRecallSource(old.getRecallSource() + "," + c.getRecallSource());
            }
        }
        return new LinkedHashSet<>(merged.values());
    }

    /**
     * 过滤用户已读内容
     * 从候选物品中移除用户已经看过的内容
     * 对于未登录用户，直接返回所有候选物品
     *
     * @param request 推荐请求参数
     * @param candidates 待过滤的候选物品集合
     * @return 过滤后的候选物品集合
     */
    public Set<CandidateItem> filterSeen(RecommendRequest request, Set<CandidateItem> candidates) {
        // 未登录用户不过滤
        if (request.getUserIdentity() == null || request.getUserIdentity().isEmpty()) {
            return candidates;
        }
        
        // 从 Redis 获取用户已读内容 ID 集合
        Set<String> seen = stringRedisTemplate.opsForSet().members(RedisKeys.SEEN_PREFIX + request.getUserIdentity());
        if (seen == null || seen.isEmpty()) {
            return candidates;
        }
        
        // 过滤掉已读内容
        return candidates.stream()
                .filter(c -> !seen.contains(String.valueOf(c.getContentId())))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 对候选物品进行排序
     * 基于多维度特征（兴趣度、质量、热度、新鲜度）计算综合分数，并按分数降序排列
     * 评分权重：兴趣度 35% + 质量 25% + 热度 25% + 新鲜度 10% + 基础分 5%
     * 额外加分：内容长度奖励（log1p 函数）
     *
     * @param request 推荐请求参数
     * @param candidates 候选物品集合
     * @param featureMap 内容特征映射表
     * @return 排序后的推荐物品列表
     */
    public List<RecommendItem> rank(RecommendRequest request, Set<CandidateItem> candidates, Map<Long, ContentFeature> featureMap) {
        // 获取用户画像
        UserProfile profile = userProfileService.getProfile(request.getUserIdentity());

        List<RecommendItem> result = new ArrayList<>();
        for (CandidateItem candidate : candidates) {
            // 获取内容特征
            ContentFeature feature = featureMap.get(candidate.getContentId());
            if (feature == null) continue;

            // 计算各维度分数
            double interest = userProfileService.interestScore(profile, feature);  // 兴趣度分数
            double hot = feature.getHotScore() == null ? 0.0 : feature.getHotScore();  // 热度分数
            double quality = feature.getQualityScore() == null ? 0.0 : feature.getQualityScore();  // 质量分数
            double freshness = feature.getFreshnessScore() == null ? 0.0 : feature.getFreshnessScore();  // 新鲜度分数

            // 计算综合分数：加权求和
            double finalScore = 0.35 * interest
                    + 0.25 * quality
                    + 0.25 * hot
                    + 0.10 * freshness
                    + 0.05 * candidate.getBaseScore();

            // 内容长度奖励：使用 log1p 函数避免过长内容的过度奖励
            finalScore = finalScore + 0.02 * ScoreUtils.safeLog1p(feature.getContent() == null ? 0 : feature.getContent().length());

            // 构建推荐物品对象
            RecommendItem item = new RecommendItem();
            item.setContentId(feature.getContentId());
            item.setTitle(feature.getTitle());
            item.setContent(feature.getContent());
            item.setAuthor(feature.getAuthor());
            item.setSource(feature.getSource());
            item.setDynasty(feature.getDynasty());
            item.setPoetryCategory(feature.getPoetryCategory());
            item.setAuthorId(feature.getAuthorId());
            item.setTagVos(feature.getTagVos());
            item.setTopicNameVos(feature.getTopicNameVos());
            item.setScore(finalScore);  // 综合分数
            item.setHotScore(hot);  // 热度分数
            item.setQualityScore(quality);  // 质量分数
            item.setFreshnessScore(freshness);  // 新鲜度分数
            item.setRecallSource(candidate.getRecallSource());  // 召回来源
            item.setCreatedTime(feature.getCreatedTime());
            item.setOriginal(feature.getOriginal());
            item.setUserInfo(feature.getUserInfo());
            item.setFollow(feature.isFollow());
            result.add(item);
        }

        // 按综合分数降序排序
        result.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return result;
    }
}
