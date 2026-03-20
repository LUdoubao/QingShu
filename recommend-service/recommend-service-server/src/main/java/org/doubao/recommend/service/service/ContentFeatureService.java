package org.doubao.recommend.service.service;

import com.alibaba.fastjson.JSON;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.mall.common.util.UserContext;
import org.doubao.mall.common.vo.TagVo;
import org.doubao.mall.common.vo.UserLoginVo;
import org.doubao.quote.service.service.QuoteService;
import org.doubao.quote.service.vo.QuoteVo;
import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.mapper.ContentFeatureSnapshotMapper;
import org.doubao.recommend.service.mapper.RecommendQuoteMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentFeatureService {

    /**
     * 引用 Mapper
     * 用于查询内容的基本信息
     */
    @Resource
    private RecommendQuoteMapper recommendQuoteMapper;
    
    /**
     * 内容特征快照 Mapper
     * 用于持久化存储内容的特征快照数据
     */
    @Resource
    private ContentFeatureSnapshotMapper snapshotMapper;
    
    /**
     * Redis 模板
     * 用于缓存内容特征，提高查询性能
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private QuoteService quoteService;
    /**
     * 根据内容 ID 获取内容特征
     * 采用缓存优先策略：先查 Redis，未命中则查数据库并回写缓存
     *
     * @param contentId 内容 ID
     * @return 内容特征对象，如果不存在则返回 null
     */
    public ContentFeature getById(Long contentId) {
        // 生成 Redis 缓存键并查询
        String key = RedisKeys.FEATURE_PREFIX + contentId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof ContentFeature) {
            return (ContentFeature) cached;
        }
        
        // 缓存未命中，从数据库查询
        ContentFeature feature = snapshotMapper.selectById(contentId);
        if (feature == null) {
            feature = recommendQuoteMapper.selectFeatureById(contentId);
        }
        
        // 如果查询到数据，进行丰富处理并写入缓存
        if (feature != null) {
            enrich(feature);
            UserLoginVo user = UserContext.getUser();
            List<QuoteVo> quoteVos = quoteService.recommendList(Collections.singletonList(contentId), DoubaoUtils.isNotEmpty(user) ? user.getId() : null);
            if (DoubaoUtils.isNotEmpty(quoteVos)) {
                QuoteVo quoteVo = quoteVos.get(0);
                if (quoteVo != null) {
                    convert(feature, quoteVo);
                }
            }
            redisTemplate.opsForValue().set(key, feature);
        }
        return feature;
    }

    /**
     * 批量获取内容特征列表
     * 根据内容 ID 列表批量查询内容特征，并对每个特征进行丰富处理
     *
     * @param ids 内容 ID 列表
     * @return 内容特征列表
     */
    public List<ContentFeature> getBatchByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量查询并逐个丰富处理
        List<ContentFeature> collect = recommendQuoteMapper.selectFeaturesByIds(ids).stream().peek(this::enrich).collect(Collectors.toList());
        // 批量查询引文信息
        UserLoginVo user = UserContext.getUser();
        List<QuoteVo> quoteVos = quoteService.recommendList(ids, DoubaoUtils.isNotEmpty(user) ? user.getId() : null);
        collect.forEach(feature -> {
            if (DoubaoUtils.isNotEmpty(quoteVos)) {
                QuoteVo quoteVo = quoteVos.stream().filter(quote -> quote.getId().equals(feature.getContentId())).findFirst().orElse(null);
                if (quoteVo != null) {
                    convert(feature, quoteVo);
                }
            }
        });
        return collect;
    }

    private void convert(ContentFeature feature, QuoteVo quoteVo) {
        feature.setContentId(quoteVo.getId());
        feature.setContent(quoteVo.getContent());
        feature.setTitle(quoteVo.getTitle());
        feature.setAuthor(quoteVo.getAuthor());
        feature.setDynasty(quoteVo.getDynasty());
        feature.setSource(quoteVo.getSource());
        feature.setTagVos(JSON.parseArray(JSON.toJSONString(quoteVo.getTags()), TagVo.class));
        feature.setTopicNameVos(Collections.singletonList(quoteVo.getTopic()));
        feature.setCreatedTime(quoteVo.getCreatedTime());
    }
    /**
     * 保存内容特征快照
     * 将内容特征持久化到数据库并更新 Redis 缓存
     *
     * @param feature 内容特征对象
     */
    public void saveSnapshot(ContentFeature feature) {
        if (feature == null || feature.getContentId() == null) {
            return;
        }
        // 更新数据库快照
        snapshotMapper.upsert(feature);
        // 更新 Redis 缓存
        redisTemplate.opsForValue().set(RedisKeys.FEATURE_PREFIX + feature.getContentId(), feature);
    }

    /**
     * 丰富内容特征数据
     * 为内容特征补充热度、质量、新鲜度等评分
     * 如果某些分数为空，则使用默认值或计算得出
     *
     * @param feature 待丰富的内容特征对象
     */
    private void enrich(ContentFeature feature) {
        // 初始化空分数为 0.0
        if (feature.getHotScore() == null) feature.setHotScore(0.0);
        if (feature.getQualityScore() == null) feature.setQualityScore(0.0);
        if (feature.getFreshnessScore() == null) feature.setFreshnessScore(0.0);
        
        // 计算新鲜度分数：基于创建时间，越新分数越高
        if (feature.getCreatedTime() == null) {
            feature.setFreshnessScore(0.0);
        } else {
            // freshness is a runtime score
            long ageDays = java.time.Duration.between(feature.getCreatedTime(), LocalDateTime.now()).toDays();
            feature.setFreshnessScore(1.0 / (1.0 + Math.max(0, ageDays)));
        }
        
        // 计算质量分数：基于内容完整性评估
        if (feature.getQualityScore() == null || feature.getQualityScore() <= 0) {
            double quality = 0.0;
            if (feature.getTitle() != null && !feature.getTitle().isEmpty()) quality += 0.2;  // 标题完整 +0.2
            if (feature.getAuthor() != null && !feature.getAuthor().isEmpty()) quality += 0.15;  // 作者完整 +0.15
            if (DoubaoUtils.isNotEmpty(feature.getTagVos())) quality += 0.25;  // 标签完整 +0.25
            if (DoubaoUtils.isNotEmpty(feature.getTopicNameVos())) quality += 0.2;  // 话题完整 +0.2
            if (feature.getContent() != null && feature.getContent().length() >= 10) quality += 0.2;  // 内容长度足够 +0.2
            if (feature.getDynasty() != null && !feature.getDynasty().isEmpty()) quality += 0.05;  // 朝代完整 +0.05
            feature.setQualityScore(Math.min(1.0, quality));  // 限制最大值为 1.0
        }
    }
}
