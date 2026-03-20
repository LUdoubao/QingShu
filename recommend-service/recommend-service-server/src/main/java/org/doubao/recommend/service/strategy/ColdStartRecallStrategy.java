package org.doubao.recommend.service.strategy;

import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.doubao.recommend.service.mapper.RecommendQuoteMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class ColdStartRecallStrategy implements RecallStrategy {

    /**
     * 引用 Mapper
     * 用于查询最新发布的热门内容
     */
    @Resource
    private RecommendQuoteMapper recommendQuoteMapper;

    /**
     * 判断是否支持冷启动召回策略
     * 适用于以下场景：
     * 1. 用户未登录（userIdentity 为空或 null）
     * 2. 用户在"猜你喜欢"场景下
     *
     * @param request 推荐请求参数
     * @return true-支持该策略，false-不支持
     */
    @Override
    public boolean support(RecommendRequest request) {
        return request.getUserIdentity() == null || request.getUserIdentity().trim().isEmpty() || "guess".equalsIgnoreCase(request.getScene());
    }

    /**
     * 执行冷启动召回策略
     * 针对新用户或无历史偏好的用户，召回最近发布的热门内容
     * 默认返回 50 条最新发布的内容
     *
     * @param request 推荐请求参数
     * @return 召回的候选物品列表
     */
    @Override
    public List<CandidateItem> recall(RecommendRequest request) {
        List<CandidateItem> list = new ArrayList<>();
        // 查询最近发布的 50 条内容
        recommendQuoteMapper.selectRecentPublished(null, 50).forEach(feature -> {
            CandidateItem item = new CandidateItem();
            item.setContentId(feature.getContentId());
            item.setRecallSource("cold_start,new");  // 标记来源为新品召回
            item.setBaseScore(0.5);
            item.setReason("cold_start_fallback");  // 设置基础分数为 0.5
            list.add(item);
        });
        return list;
    }
}
