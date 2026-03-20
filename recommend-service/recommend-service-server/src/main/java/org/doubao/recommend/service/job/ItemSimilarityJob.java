package org.doubao.recommend.service.job;

import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.ContentFeature;
import org.doubao.recommend.service.mapper.RecommendQuoteMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class ItemSimilarityJob {

    /**
     * 引用 Mapper
     * 用于查询内容特征数据，获取候选物品列表
     */
    @Resource
    private RecommendQuoteMapper recommendQuoteMapper;

    /**
     * Redis 模板
     * 用于将计算好的相似内容列表写入 Redis 缓存
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 重建相似内容缓存定时任务
     * 每小时第 30 分钟执行一次（cron: 0 30 * * * ?）
     * 基于内容的多维度特征（作者、朝代、分类、标签、话题等）计算相似度
     * 并将结果缓存到 Redis，设置 6 小时过期时间
     */
    @Scheduled(cron = "0 30 * * * ?")
    public void rebuildSimilarCache() {
    
        // 1. 取候选（最近 300 条）
        List<ContentFeature> candidates = recommendQuoteMapper.selectRecentPublished(null, 300);
    
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
    
        // 2. 计算每个内容的相似内容
        for (ContentFeature seed : candidates) {
            List<ScoredId> similarList = findSimilar(seed, candidates, 20);
    
            // 3. 写入 Redis ZSET（member=contentId, score=similarity）
            String key = RedisKeys.SIMILAR_PREFIX + seed.getContentId();
            stringRedisTemplate.delete(key);
            for (ScoredId s : similarList) {
                stringRedisTemplate.opsForZSet().add(key, String.valueOf(s.getId()), s.getScore());
            }
            stringRedisTemplate.expire(key, 6, TimeUnit.HOURS);
        }
    }

    /**
     * 查找与种子内容最相似的内容列表
     * 基于多维度特征进行相似度计算：
     * - 作者相同：+0.35 分
     * - 朝代相同：+0.15 分
     * - 分类相同：+0.15 分
     * - 标签 Jaccard 相似度：* 0.20 分
     * - 话题 Jaccard 相似度：* 0.15 分
     * - 热度加权：* 0.05 分
     *
     * @param seed 种子内容特征
     * @param all 所有候选内容列表
     * @param limit 返回的最大数量
     * @return 相似度最高的内容 ID 及分数列表
     */
    private List<ScoredId> findSimilar(ContentFeature seed,
                                       List<ContentFeature> all,
                                       int limit) {

        List<ScoredId> result = new ArrayList<>();

        // 预先提取种子内容的标签和话题集合，便于后续比较
        Set<String> seedTags = new HashSet<>(seed.tagList());
        Set<Long> seedTopics = new HashSet<>(seed.topicIdList());

        for (ContentFeature other : all) {

            // 跳过自身
            if (seed.getContentId().equals(other.getContentId())) {
                continue;
            }

            double score = 0.0;

            // ===== 1. 作者匹配 =====
            if (Objects.equals(seed.getAuthor(), other.getAuthor())) {
                score += 0.35;
            }

            // ===== 2. 朝代匹配 =====
            if (Objects.equals(seed.getDynasty(), other.getDynasty())) {
                score += 0.15;
            }

            // ===== 3. 分类匹配 =====
            if (Objects.equals(seed.getPoetryCategory(), other.getPoetryCategory())) {
                score += 0.15;
            }

            // ===== 4. 标签相似度（Jaccard）=====
            Set<String> otherTags = new HashSet<>(other.tagList());
            double tagScore = jaccard(seedTags, otherTags);
            score += tagScore * 0.20;

            // ===== 5. 话题相似度（Jaccard）=====
            Set<Long> otherTopics = new HashSet<>(other.topicIdList());
            double topicScore = jaccard(seedTopics, otherTopics);
            score += topicScore * 0.15;

            // ===== 6. 轻微热度加权 =====
            score += other.getHotScore() * 0.05;

            if (score > 0) {
                result.add(new ScoredId(other.getContentId(), score));
            }
        }

        // 按相似度分数降序排序
        result.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 截断到指定数量
        return result.size() > limit ? result.subList(0, limit) : result;
    }

    /**
     * 计算两个集合的 Jaccard 相似度
     * Jaccard 相似度 = 交集大小 / 并集大小
     * 值域 [0, 1]，越接近 1 表示越相似
     *
     * @param a 第一个集合
     * @param b 第二个集合
     * @param <T> 集合元素类型
     * @return Jaccard 相似度分数
     */
    private <T> double jaccard(Set<T> a, Set<T> b) {
        // 如果任一集合为空，返回 0
        if (a.isEmpty() || b.isEmpty()) return 0.0;

        // 计算交集
        Set<T> inter = new HashSet<>(a);
        inter.retainAll(b);

        // 计算并集
        Set<T> union = new HashSet<>(a);
        union.addAll(b);

        // 返回交集与并集的比值
        return (double) inter.size() / union.size();
    }

    /**
     * 带分数的 ID 对象
     * 用于存储内容 ID 及其对应的相似度分数
     * JDK8 兼容写法（替代 record）
     */
    private static class ScoredId {
        /**
         * 内容 ID
         */
        private Long id;
        
        /**
         * 相似度分数
         */
        private double score;

        /**
         * 构造函数
         * @param id 内容 ID
         * @param score 相似度分数
         */
        public ScoredId(Long id, double score) {
            this.id = id;
            this.score = score;
        }

        /**
         * 获取内容 ID
         * @return 内容 ID
         */
        public Long getId() {
            return id;
        }

        /**
         * 获取相似度分数
         * @return 相似度分数
         */
        public double getScore() {
            return score;
        }
    }
}