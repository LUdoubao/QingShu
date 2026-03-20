package org.doubao.recommend.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.doubao.recommend.service.common.RedisKeys;
import org.doubao.recommend.service.domain.HotStat;
import org.doubao.recommend.service.mapper.UserBehaviorMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 热度排行定时任务类
 * 负责定期计算和更新热门内容的排行榜数据
 * 使用 Spring 的@Scheduled 注解实现定时调度
 */
@Component
public class HotRankJob {

    private static final Logger logger = LoggerFactory.getLogger(HotRankJob.class);

    /**
     * 用户行为 Mapper
     * 用于查询用户行为统计数据，计算内容热度分数
     */
    @Resource
    private UserBehaviorMapper behaviorMapper;
    
    /**
     * Redis String 模板
     * 用于将计算好的热度排行榜写入 Redis 有序集合
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 重建首页热门内容排行榜
     * 每小时的第 10 分钟执行一次（cron: 0 10 * * * ?）
     * 从数据库中统计最近 7 天内热度最高的 500 个内容，并写入 Redis 有序集合
     */
    @Scheduled(cron = "0 10 * * * ?")
    public void rebuildHotHome() {
        long startTime = System.currentTimeMillis();
        logger.info("开始执行首页热门内容排行榜定时任务...");
        
        try {
            // 查询最近 7 天内的热门内容统计，最多返回 500 条
            LocalDateTime sinceTime = LocalDateTime.now().minusDays(7);
            logger.debug("查询时间范围：{} 至今", sinceTime);
            
            List<HotStat> stats = behaviorMapper.selectHotStats(sinceTime, 500);
            logger.info("查询到 {} 条热门内容数据", stats.size());
            
            if (stats.isEmpty()) {
                logger.warn("未查询到任何热门内容数据，请检查数据库是否有足够的用户行为数据");
                return;
            }
            
            // 删除旧的排行榜数据
            stringRedisTemplate.delete(RedisKeys.HOT_HOME_ZSET);
            logger.debug("已删除旧的排行榜数据");
            
            // 将新的热度数据逐个添加到 Redis 有序集合中
            int successCount = 0;
            for (HotStat stat : stats) {
                logger.info("添加内容 {} 到排行榜，分数为 {}", stat.getContentId(), stat.getScore());
                stringRedisTemplate.opsForZSet().add(
                    RedisKeys.HOT_HOME_ZSET, 
                    String.valueOf(stat.getContentId()), 
                    stat.getScore()
                );
                successCount++;
            }
            
            long endTime = System.currentTimeMillis();
            logger.info("首页热门内容排行榜更新完成，成功添加 {} 条记录，耗时 {}ms", 
                       successCount, (endTime - startTime));
            
        } catch (Exception e) {
            logger.error("执行首页热门内容排行榜定时任务时发生错误", e);
            throw e; // 重新抛出异常，便于 Spring 重试机制处理
        }
    }
}
