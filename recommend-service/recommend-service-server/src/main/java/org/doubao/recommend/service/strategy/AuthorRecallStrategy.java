package org.doubao.recommend.service.strategy;

import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.domain.UserProfile;
import org.doubao.recommend.service.mapper.RecommendQuoteMapper;
import org.doubao.recommend.service.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthorRecallStrategy implements RecallStrategy {

    private static final int DEFAULT_LIMIT = 50;
    private static final int CORE_AUTHOR_COUNT = 2;
    private static final int EXPLORE_AUTHOR_COUNT = 1;

    /**
     * 用户画像服务
     * 用于获取用户的兴趣画像，包含作者偏好权重
     */
    @Autowired
    private UserProfileService userProfileService;
    
    /**
     * 引用 Mapper
     * 用于根据作者查询相关内容
     */
    @Autowired
    private RecommendQuoteMapper recommendQuoteMapper;

    /**
     * 判断是否支持当前召回策略
     * 当请求中包含有效的用户身份标识时，使用作者召回策略
     *
     * @param request 推荐请求参数
     * @return true-支持该策略，false-不支持
     */
    @Override
    public boolean support(RecommendRequest request) {
        return request.getUserIdentity() != null && !request.getUserIdentity().isEmpty();
    }

    /**
     * 执行作者召回策略
     * 基于用户画像中的作者偏好权重，召回相关作者的内容
     * 流程：获取用户画像 -> 提取 Top3 偏好的作者 -> 查询这些作者的最新内容
     *
     * @param request 推荐请求参数
     * @return 召回的候选物品列表
     */
    @Override
    public List<CandidateItem> recall(RecommendRequest request) {
        // 获取用户画像
        UserProfile profile = userProfileService.getProfile(request.getUserIdentity());
        if (profile == null || profile.getAuthorWeights().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 作者召回降低集中度，保留主偏好作者的同时加入弱偏好作者做探索
        List<String> authors = selectRecallAuthors(profile);
        if (authors.isEmpty()) return new ArrayList<>();

        // 根据作者查询内容，每个作者最多查询 50 条
        List<ContentFeature> features = recommendQuoteMapper.selectByAuthors(authors, resolveLimit(request, authors.size()));
        
        // 构建候选物品列表
        List<CandidateItem> list = new ArrayList<>();
        for (ContentFeature feature : features) {
            CandidateItem item = new CandidateItem();
            item.setContentId(feature.getContentId());
            item.setRecallSource("author");  // 标记来源为作者召回
            boolean explore = isExploreAuthor(profile, feature);
            item.setBaseScore(explore ? 0.52 : 0.62);
            item.setReason(explore ? "explore_author" : "preferred_author");
            list.add(item);
        }
        return list;
    }

    private List<String> selectRecallAuthors(UserProfile profile) {
        List<Map.Entry<String, Double>> sorted = UserProfile.topEntries(profile.getAuthorWeights(), profile.getAuthorWeights().size());
        if (sorted.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> selected = new LinkedHashSet<>();
        sorted.stream()
                .limit(CORE_AUTHOR_COUNT)
                .map(Map.Entry::getKey)
                .forEach(selected::add);
        if (sorted.size() > CORE_AUTHOR_COUNT) {
            int exploreIndex = CORE_AUTHOR_COUNT + Math.max(0, (sorted.size() - CORE_AUTHOR_COUNT) / 2);
            sorted.subList(Math.min(exploreIndex, sorted.size() - 1), sorted.size()).stream()
                    .limit(EXPLORE_AUTHOR_COUNT)
                    .map(Map.Entry::getKey)
                    .forEach(selected::add);
        }
        return new ArrayList<>(selected);
    }

    private boolean isExploreAuthor(UserProfile profile, ContentFeature feature) {
        List<String> topAuthors = UserProfile.topEntries(profile.getAuthorWeights(), CORE_AUTHOR_COUNT).stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return feature.getAuthor() != null && !topAuthors.contains(feature.getAuthor());
    }

    private int resolveLimit(RecommendRequest request, int groupCount) {
        int requested = request.getLimit() == null || request.getLimit() <= 0 ? DEFAULT_LIMIT : request.getLimit();
        return Math.max(DEFAULT_LIMIT, requested / Math.max(1, groupCount));
    }
}
