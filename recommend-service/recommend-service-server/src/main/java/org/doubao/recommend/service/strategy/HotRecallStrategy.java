package org.doubao.recommend.service.strategy;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.CandidateItem;
import org.doubao.recommend.service.domain.RecommendRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class HotRecallStrategy implements RecallStrategy {

    /**
     * Redis String 模板
     * 用于从 Redis 有序集合中获取热门内容 ID 列表
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断是否支持热门召回策略
     * 热门召回对所有请求都生效，作为基础召回策略
     *
     * @param request 推荐请求参数
     * @return true-始终支持该策略
     */
    @Override
    public boolean support(RecommendRequest request) {
        return true;
    }

    /**
     * 执行热门召回策略
     * 从 Redis 的热门内容有序集合中召回 Top200 的热门内容
     * 热门内容基于实时热度分数排序
     *
     * @param request 推荐请求参数
     * @return 召回的候选物品列表
     */
    @Override
    public List<CandidateItem> recall(RecommendRequest request) {
        // 从 Redis 有序集合中按热度倒序获取前 200 个热门内容 ID
        Set<String> ids = stringRedisTemplate.opsForZSet().reverseRange(RedisKeys.HOT_HOME_ZSET, 0, 199);
        List<CandidateItem> list = new ArrayList<>();
        if (ids == null) return list;
        
        // 构建候选物品列表
        for (String id : ids) {
            CandidateItem item = new CandidateItem();
            item.setContentId(Long.valueOf(id));
            item.setRecallSource("hot");  // 标记来源为热门召回
            item.setBaseScore(0.8);
            item.setReason("hot_rank");  // 设置基础分数为 0.8，较高优先级
            list.add(item);
        }
        return list;
    }
}
