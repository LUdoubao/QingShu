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
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TopicRecallStrategy implements RecallStrategy {

    /**
     * 用户画像服务
     * 用于获取用户的兴趣画像，包含话题偏好权重
     */
    @Resource
    private UserProfileService userProfileService;
    
    /**
     * 引用 Mapper
     * 用于根据话题 ID 查询相关内容
     */
    @Resource
    private RecommendQuoteMapper recommendQuoteMapper;

    /**
     * 判断是否支持话题召回策略
     * 当请求中包含有效的用户身份标识时，使用话题召回策略
     *
     * @param request 推荐请求参数
     * @return true-支持该策略，false-不支持
     */
    @Override
    public boolean support(RecommendRequest request) {
        return request.getUserIdentity() != null && !request.getUserIdentity().isEmpty();
    }

    /**
     * 执行话题召回策略
     * 基于用户画像中的话题偏好权重，召回相关话题的内容
     * 流程：获取用户画像 -> 提取 Top3 偏好的话题 ID -> 查询这些话题关联的内容
     *
     * @param request 推荐请求参数
     * @return 召回的候选物品列表
     */
    @Override
    public List<CandidateItem> recall(RecommendRequest request) {
        // 获取用户画像
        UserProfile profile = userProfileService.getProfile(request.getUserIdentity());
        if (profile == null || profile.getTopicWeights().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 从用户画像中提取权重最高的前 3 个话题 ID
        List<Long> topicIds = UserProfile.topEntries(profile.getTopicWeights(), 3).stream()
                .map(e -> Long.valueOf(e.getKey()))
                .collect(Collectors.toList());
        if (topicIds.isEmpty()) return new ArrayList<>();

        // 根据话题 ID 查询内容，每个话题最多查询 50 条
        List<ContentFeature> features = recommendQuoteMapper.selectByTopicIds(topicIds, 50);
        
        // 构建候选物品列表
        List<CandidateItem> list = new ArrayList<>();
        for (ContentFeature feature : features) {
            CandidateItem item = new CandidateItem();
            item.setContentId(feature.getContentId());
            item.setRecallSource("topic");  // 标记来源为话题召回
            item.setBaseScore(0.75);
            item.setReason("preferred_topic");  // 设置基础分数为 0.75
            list.add(item);
        }
        return list;
    }
}
