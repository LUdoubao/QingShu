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
public class TagRecallStrategy implements RecallStrategy {

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
        
        // 从用户画像中提取权重最高的前 3 个标签
        List<String> tags = UserProfile.topEntries(profile.getTagWeights(), 3).stream()
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());
        if (tags.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据标签查询内容，每个标签最多查询 50 条
        List<ContentFeature> features = recommendQuoteMapper.selectByTags(tags, 50);
        
        // 构建候选物品列表
        List<CandidateItem> list = new ArrayList<>();
        for (ContentFeature feature : features) {
            CandidateItem item = new CandidateItem();
            item.setContentId(feature.getContentId());
            item.setRecallSource("tag");  // 标记来源为标签召回
            item.setBaseScore(0.7);  // 设置基础分数为 0.7
            list.add(item);
        }
        return list;
    }
}
