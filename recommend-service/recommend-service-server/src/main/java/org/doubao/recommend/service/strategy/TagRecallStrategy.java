package org.doubao.recommend.service.strategy;

import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.domain.UserProfile;
import org.doubao.recommend.service.mapper.RecommendQuoteMapper;
import org.doubao.recommend.service.service.UserProfileService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TagRecallStrategy implements RecallStrategy {

    private static final int DEFAULT_LIMIT = 50;
    private static final int CORE_TAG_COUNT = 3;
    private static final int EXPLORE_TAG_COUNT = 2;

    /**
     * 用户画像服务
     * 用于获取用户的兴趣画像，包含标签偏好权重
     */
    @Resource
    private UserProfileService userProfileService;
    
    /**
     * 引用 Mapper
     * 用于根据标签查询相关内容
     */
    @Resource
    private RecommendQuoteMapper recommendQuoteMapper;

    /**
     * 判断是否支持标签召回策略
     * 当请求中包含有效的用户身份标识时，使用标签召回策略
     *
     * @param request 推荐请求参数
     * @return true-支持该策略，false-不支持
     */
    @Override
    public boolean support(RecommendRequest request) {
        return request.getUserIdentity() != null && !request.getUserIdentity().isEmpty();
    }

    /**
     * 执行标签召回策略
     * 基于用户画像中的标签偏好权重，召回相关标签的内容
     * 流程：获取用户画像 -> 提取 Top3 偏好的标签 -> 查询这些标签关联的内容
     *
     * @param request 推荐请求参数
     * @return 召回的候选物品列表
     */
    @Override
    public List<CandidateItem> recall(RecommendRequest request) {
        // 获取用户画像
        UserProfile profile = userProfileService.getProfile(request.getUserIdentity());
        if (profile == null || profile.getTagWeights().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 采用“主偏好 + 探索池”召回，避免结果持续收敛到少数熟悉标签
        List<String> tags = selectRecallTags(profile);
        if (tags.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据标签查询内容，每个标签最多查询 50 条
        List<ContentFeature> features = recommendQuoteMapper.selectByTags(tags, resolveLimit(request, tags.size()));
        
        // 构建候选物品列表
        List<CandidateItem> list = new ArrayList<>();
        for (ContentFeature feature : features) {
            CandidateItem item = new CandidateItem();
            item.setContentId(feature.getContentId());
            item.setRecallSource("tag");  // 标记来源为标签召回
            boolean explore = isExploreTag(profile, feature);
            item.setBaseScore(explore ? 0.64 : 0.78);
            item.setReason(explore ? "explore_tag" : "preferred_tag");
            list.add(item);
        }
        return list;
    }

    private List<String> selectRecallTags(UserProfile profile) {
        List<Map.Entry<String, Double>> sorted = UserProfile.topEntries(profile.getTagWeights(), profile.getTagWeights().size());
        if (sorted.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> selected = new LinkedHashSet<>();
        sorted.stream()
                .limit(CORE_TAG_COUNT)
                .map(Map.Entry::getKey)
                .forEach(selected::add);
        int tailStart = Math.min(CORE_TAG_COUNT, sorted.size());
        int tailEnd = sorted.size();
        int exploreStart = tailStart + Math.max(0, (tailEnd - tailStart) / 2 - 1);
        sorted.subList(Math.min(exploreStart, tailEnd), tailEnd).stream()
                .limit(EXPLORE_TAG_COUNT)
                .map(Map.Entry::getKey)
                .forEach(selected::add);
        return new ArrayList<>(selected);
    }

    private boolean isExploreTag(UserProfile profile, ContentFeature feature) {
        List<String> topTags = UserProfile.topEntries(profile.getTagWeights(), CORE_TAG_COUNT).stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return feature.tagList().stream().noneMatch(topTags::contains);
    }

    private int resolveLimit(RecommendRequest request, int groupCount) {
        int requested = request.getLimit() == null || request.getLimit() <= 0 ? DEFAULT_LIMIT : request.getLimit();
        return Math.max(DEFAULT_LIMIT, requested / Math.max(1, groupCount));
    }
}
